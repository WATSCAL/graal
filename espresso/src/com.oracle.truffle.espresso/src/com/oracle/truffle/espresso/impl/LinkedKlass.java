/*
 * Copyright (c) 2019, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.espresso.impl;

import static com.oracle.truffle.espresso.classfile.Constants.ACC_FINALIZER;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.espresso.meta.EspressoError;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.staticobject.StaticShape;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.espresso.EspressoLanguage;
import com.oracle.truffle.espresso.classfile.ImmutableConstantPool;
import com.oracle.truffle.espresso.classfile.ParserKlass;
import com.oracle.truffle.espresso.classfile.ParserMethod;
import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.attributes.reified.ClassTypeParamListAttribute;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;
import com.oracle.truffle.espresso.classfile.descriptors.Type;
import com.oracle.truffle.espresso.descriptors.EspressoSymbols.Types;
import com.oracle.truffle.espresso.runtime.staticobject.StaticObject.StaticObjectFactory;

// Structural shareable klass (superklass in superinterfaces resolved and linked)
// contains shape, field locations.
// Klass shape, vtable and field locations can be computed at the structural level.
public final class LinkedKlass {

    public static final LinkedKlass[] EMPTY_ARRAY = new LinkedKlass[0];
    private final ParserKlass parserKlass;

    // Linked structural references.
    private final LinkedKlass superKlass;

    public final int curLevelTypeParamNum;
    public final int allTypeParamNum;

    @CompilationFinal(dimensions = 1) //
    private final LinkedKlass[] interfaces;

    @CompilationFinal(dimensions = 1) //
    private final LinkedMethod[] methods;

    private final boolean hasFinalizer;

    private final StaticShape<StaticObjectFactory> instanceShape;
    private final StaticShape<StaticObjectFactory> staticShape;

    // instance fields declared in the corresponding LinkedKlass (includes hidden fields)
    @CompilationFinal(dimensions = 1) //
    final LinkedField[] instanceFields;
    // static fields declared in the corresponding LinkedKlass (no hidden fields)
    @CompilationFinal(dimensions = 1) //
    final LinkedField[] staticFields;

    @CompilationFinal(dimensions = 2)
    private byte[][] specializedKeys;
    @CompilationFinal(dimensions = 1)
    private Object[] specializedShapes;
    @CompilationFinal(dimensions = 2)
    private LinkedField[][] specializedInstanceFields;

    private static final byte[][] EMPTY_BYTE_KEY = new byte[0][];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final LinkedField[][] EMPTY_LINKED_FIELD_ARRAY = new LinkedField[0][];

    final int fieldTableLength;

    private LinkedKlass(ParserKlass parserKlass, LinkedKlass superKlass, LinkedKlass[] interfaces, StaticShape<StaticObjectFactory> instanceShape,
                    StaticShape<StaticObjectFactory> staticShape, LinkedField[] instanceFields, LinkedField[] staticFields, int fieldTableLength) {
        this.parserKlass = parserKlass;
        this.superKlass = superKlass;
        this.interfaces = interfaces;
        this.instanceShape = instanceShape;
        this.staticShape = staticShape;
        this.instanceFields = instanceFields;
        this.staticFields = staticFields;
        this.fieldTableLength = fieldTableLength;

        // Streams are forbidden in Espresso.
        // assert Arrays.stream(interfaces).allMatch(i -> Modifier.isInterface(i.getFlags()));
        assert superKlass == null || !Modifier.isInterface(superKlass.getFlags());

        // Super interfaces are not checked for finalizers; a default .finalize method will be
        // resolved to Object.finalize, making the finalizer not observable.
        this.hasFinalizer = ((parserKlass.getFlags() & ACC_FINALIZER) != 0) || (superKlass != null && (superKlass.getFlags() & ACC_FINALIZER) != 0);
        assert !this.hasFinalizer || !Types.java_lang_Object.equals(parserKlass.getType()) : "java.lang.Object cannot be marked as finalizable";

        final int methodCount = parserKlass.getMethods().length;
        LinkedMethod[] linkedMethods = new LinkedMethod[methodCount];

        for (int i = 0; i < methodCount; ++i) {
            ParserMethod parserMethod = parserKlass.getMethods()[i];
            // TODO(peterssen): Methods with custom constant pool should spawned here, but not
            // supported.
            linkedMethods[i] = new LinkedMethod(parserMethod);
        }
        this.methods = linkedMethods;

        ClassTypeParamListAttribute typeParamList = (ClassTypeParamListAttribute) this.parserKlass.getAttribute(ClassTypeParamListAttribute.NAME);
        this.curLevelTypeParamNum = typeParamList != null ? typeParamList.getTypeParams().length : 0;
        this.allTypeParamNum = superKlass != null ? superKlass.allTypeParamNum + this.curLevelTypeParamNum : this.curLevelTypeParamNum;

        this.specializedKeys = EMPTY_BYTE_KEY;
        this.specializedShapes = EMPTY_OBJECT_ARRAY;
        this.specializedInstanceFields = EMPTY_LINKED_FIELD_ARRAY;
    }

    public static LinkedKlass create(EspressoLanguage language, ParserKlass parserKlass, LinkedKlass superKlass, LinkedKlass[] interfaces) {
        LinkedKlassFieldLayout fieldLayout = new LinkedKlassFieldLayout(language, parserKlass, superKlass);
        return new LinkedKlass(
                        parserKlass,
                        superKlass,
                        interfaces,
                        fieldLayout.instanceShape,
                        fieldLayout.staticShape,
                        fieldLayout.instanceFields,
                        fieldLayout.staticFields,
                        fieldLayout.fieldTableLength);
    }

    public static LinkedKlass redefine(ParserKlass parserKlass, LinkedKlass superKlass, LinkedKlass[] interfaces, LinkedKlass redefinedKlass) {
        // On class redefinition we need to re-use the old shape.
        // If we don't do it, shape checks on field accesses fail because `Field` instances in
        // `ObjectKlass.fieldTable` hold references to the old shape, which does not match the shape
        // of the new object instances.
        // We work around this by means of an extension mechanism where all shapes contain
        // one extra element
        return new LinkedKlass(
                        parserKlass,
                        superKlass,
                        interfaces,
                        redefinedKlass.instanceShape,
                        redefinedKlass.staticShape,
                        redefinedKlass.instanceFields,
                        redefinedKlass.staticFields,
                        redefinedKlass.fieldTableLength);
    }

    int getFlags() {
        int flags = parserKlass.getFlags();
        if (hasFinalizer) {
            flags |= ACC_FINALIZER;
        }
        return flags;
    }

    ImmutableConstantPool getConstantPool() {
        return parserKlass.getConstantPool();
    }

    Attribute getAttribute(Symbol<Name> name) {
        return parserKlass.getAttribute(name);
    }

    Symbol<Type> getType() {
        return parserKlass.getType();
    }

    Symbol<Name> getName() {
        return parserKlass.getName();
    }

    public ParserKlass getParserKlass() {
        return parserKlass;
    }

    LinkedKlass getSuperKlass() {
        return superKlass;
    }

    LinkedKlass[] getInterfaces() {
        return interfaces;
    }

    int getMajorVersion() {
        return getConstantPool().getMajorVersion();
    }

    int getMinorVersion() {
        return getConstantPool().getMinorVersion();
    }

    LinkedMethod[] getLinkedMethods() {
        return methods;
    }

    LinkedField[] getInstanceFields() {
        return instanceFields;
    }

    LinkedField[] getStaticFields() {
        return staticFields;
    }

    int getFieldTableLength() {
        return fieldTableLength;
    }

    public StaticShape<StaticObjectFactory> getShape(boolean isStatic) {
        return isStatic ? staticShape : instanceShape;
    }

    @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.FULL_UNROLL_UNTIL_RETURN)
    private boolean matchSpecializationKey(int specializationIndex, byte[] classTypeArgs) {
        for (int i = 0; i < this.allTypeParamNum; ++i) {
            if (this.specializedKeys[specializationIndex][i] != classTypeArgs[i]) {
                return false;
            }
        }
        return true;
    }

    @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.FULL_UNROLL_UNTIL_RETURN)
    public int getSpecializationIndexReadOnly(byte[] classTypeArgs) {
        for (int idx = 0; idx < this.specializedKeys.length; ++idx) {
            if (matchSpecializationKey(idx, classTypeArgs)) {
                return idx;
            }
        }

        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw EspressoError.shouldNotReachHere();
    }

    @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.FULL_UNROLL_UNTIL_RETURN)
    public int getSpecializationIndex(EspressoLanguage language, byte[] classTypeArgs) {
        for (int idx = 0; idx < this.specializedKeys.length; ++idx) {
            if (matchSpecializationKey(idx, classTypeArgs)) {
                return idx;
            }
        }

        CompilerDirectives.transferToInterpreterAndInvalidate();

        LinkedKlassFieldLayout.SpecializedLayout newLayout = new LinkedKlassFieldLayout.SpecializedLayout(language, this.parserKlass, this.superKlass, classTypeArgs);

        int curLen = this.specializedKeys.length;

        this.specializedKeys = Arrays.copyOf(this.specializedKeys, curLen + 1);
        this.specializedKeys[curLen] = Arrays.copyOf(classTypeArgs, this.allTypeParamNum);
        this.specializedShapes = Arrays.copyOf(this.specializedShapes, curLen + 1);
        this.specializedShapes[curLen] = newLayout.instanceShape;
        this.specializedInstanceFields = Arrays.copyOf(this.specializedInstanceFields, curLen + 1);
        this.specializedInstanceFields[curLen] = newLayout.instanceFields;

        //System.out.println(parserKlass.getName().toString() + " Inserted a new specialization at " + curLen);
        return curLen;
    }

    @SuppressWarnings("unchecked")
    public StaticShape<StaticObjectFactory> getSpecializedShapeAt(int idx) {
        return (StaticShape<StaticObjectFactory>) this.specializedShapes[idx];
    }

    public byte[] getSpecializedKeyAt(int idx) {
        //System.out.println(parserKlass.getName().toString() + " getSpecializedKeyAt " + idx);
        return this.specializedKeys[idx];
    }

    public LinkedField[] getSpecializedInstanceFieldsAt(int idx) {
        return this.specializedInstanceFields[idx];
    }

    @SuppressWarnings("unchecked")
    public StaticShape<StaticObjectFactory> getSpecializedShape(EspressoLanguage language, byte[] classTypeArgs) {
        if (this.allTypeParamNum == 0) {
            return this.instanceShape;
        }

        //CompilerDirectives.transferToInterpreterAndInvalidate();
        int idx = this.getSpecializationIndex(language, classTypeArgs);
        return (StaticShape<StaticObjectFactory>) this.specializedShapes[idx];
    }


    /*
    public StaticShape<StaticObjectFactory> getReifiedShape(boolean isStatic, byte[] reifiedTypeValues) {
        assert reifiedTypeValues != null : "reifiedTypeValues must not be null";
        ByteArrayKey key = new ByteArrayKey(reifiedTypeValues);
        LinkedKlassFieldLayout fieldLayout = reifiedShapes.get(key);
        if (fieldLayout != null) return isStatic ? fieldLayout.staticShape : fieldLayout.instanceShape;
        fieldLayout = new LinkedKlassFieldLayout(language, parserKlass, superKlass, reifiedTypeValues);
        reifiedShapes.put(key, fieldLayout);
        return isStatic ? fieldLayout.staticShape : fieldLayout.instanceShape;
    }
    */

    @Override
    public String toString() {
        return "LinkedKlass<" + getType() + ">";
    }
}
