/*
 * Copyright (c) 2022, 2022, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.espresso.nodes;

import java.util.Arrays;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.StandardTags.RootBodyTag;
import com.oracle.truffle.api.instrumentation.StandardTags.RootTag;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.interop.NodeLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.ExplodeLoop.LoopExplosionKind;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;
import com.oracle.truffle.espresso.analysis.typehints.TypeAnalysisResult;
import com.oracle.truffle.espresso.analysis.typehints.TypeHintAnalysis;
import com.oracle.truffle.espresso.classfile.attributes.reified.MethodTypeParameterCountAttribute;
import com.oracle.truffle.espresso.classfile.attributes.reified.MethodParameterTypeAttribute;
import com.oracle.truffle.espresso.classfile.attributes.reified.TraitTypeParamListAttribute;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.impl.ObjectKlass;
import com.oracle.truffle.espresso.impl.SuppressFBWarnings;
import com.oracle.truffle.espresso.runtime.staticobject.StaticObject;
import com.oracle.truffle.espresso.meta.EspressoError;

/**
 * {@link RootTag} node that separates the Java method prolog e.g. copying arguments to the frame,
 * initializes {@code bci=0}, from the execution of the {@link BytecodeNode bytecodes/body}.
 * 
 * This class exists to conform to the Truffle instrumentation APIs, namely {@link RootTag} and
 * {@link RootBodyTag} in order to support proper unwind and re-enter.
 */
@ExportLibrary(NodeLibrary.class)
final class MethodWithBytecodeNode extends EspressoInstrumentableRootNodeImpl {
    static private final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    @Child AbstractInstrumentableBytecodeNode bytecodeNode;
    private final Method.MethodVersion methodVersion;
    private final FrameDescriptor frameDescriptor;
    private final boolean trivialBytecode;
    private final boolean hasReceiver;
    private final boolean hasStaticForwarderReceiver;

    @Children BytecodeNode[] specializations = null;
    @CompilerDirectives.CompilationFinal(dimensions=2) private byte[][] cacheKeys = null;
    
    private final int methodTypeParamCount;
    private final int classTypeParamCount;
    @CompilerDirectives.CompilationFinal(dimensions = 1) private final Method[] traitTypeParamAccessors;
    private TypeAnalysisResult[] analysis = null;

    MethodWithBytecodeNode(BytecodeNode bytecodeNode) {
        super(bytecodeNode.getMethodVersion());
        this.bytecodeNode = bytecodeNode;
        this.methodVersion = bytecodeNode.getMethodVersion();
        this.frameDescriptor = bytecodeNode.getFrameDescriptor();
        this.trivialBytecode = BytecodeNode.isTrivialBytecodes(methodVersion);
        this.hasStaticForwarderReceiver = false;
        this.hasReceiver = methodVersion.getMethod().hasReceiver();
        this.methodTypeParamCount = 0;
        this.classTypeParamCount = 0;
        this.traitTypeParamAccessors = null;
    }

    MethodWithBytecodeNode(Method.MethodVersion methodVersion) {
        super(methodVersion);
        Method method = methodVersion.getMethod();
        this.methodVersion = methodVersion;
        this.trivialBytecode = BytecodeNode.isTrivialBytecodes(methodVersion);
        CompilerAsserts.neverPartOfCompilation();

        MethodParameterTypeAttribute methodParameterTypeAttribute = method.getMethodParameterTypeAttribute();
        this.hasStaticForwarderReceiver = method.isStatic() && methodParameterTypeAttribute != null && methodParameterTypeAttribute.getParameterTypes().length > 0 && methodParameterTypeAttribute.getParameterTypes()[0].getKind() == TypeHints.RECEIVER;
        this.hasReceiver = method.hasReceiver() || hasStaticForwarderReceiver;

        MethodTypeParameterCountAttribute attr = methodVersion.getMethod().getMethodTypeParameterCountAttribute();
        this.methodTypeParamCount = attr != null ? attr.getCount() : 0;

        this.analysis = TypeHintAnalysis.analyze(methodVersion);
        this.traitTypeParamAccessors = this.hasReceiver && methodVersion.getDeclaringKlass().isInterface()
                        ? resolveTraitTypeParamAccessors(methodVersion.getDeclaringKlass())
                        : null;
        int classTypeParamCountForMethod = this.hasReceiver ? methodVersion.getDeclaringKlass().getLinkedKlass().allTypeParamNum : 0;

        if (classTypeParamCountForMethod > 0 || this.analysis != null) {
            this.bytecodeNode = null;
            this.frameDescriptor = BytecodeNode.calcFrameDescriptor(methodVersion);
            this.classTypeParamCount = classTypeParamCountForMethod;
            this.specializations = new BytecodeNode[0];
            this.cacheKeys = new byte[0][];
        } else {
            BytecodeNode t = new BytecodeNode(methodVersion, null, TypeHints.EMPTY_TYPE_ARGS, TypeHints.EMPTY_TYPE_ARGS, hasStaticForwarderReceiver ? 1 : 0);
            this.bytecodeNode = t;
            this.frameDescriptor = t.getFrameDescriptor();
            this.classTypeParamCount = 0;
        }
    }

    private static Method[] resolveTraitTypeParamAccessors(ObjectKlass declaringKlass) {
        TraitTypeParamListAttribute traitTypeParamListAttribute = declaringKlass.getTraitTypeParamListAttribute();
        if (traitTypeParamListAttribute == null) {
            return null;
        }
        int[] methodRefCpis = traitTypeParamListAttribute.getTypeParamAccessorMethodRefCpis();
        Method[] accessors = new Method[methodRefCpis.length];
        for (int i = 0; i < methodRefCpis.length; ++i) {
            accessors[i] = declaringKlass.getConstantPool().resolvedMethodAt(declaringKlass, methodRefCpis[i]);
        }
        return accessors;
    }

    public FrameDescriptor getFrameDescriptor() {
        return frameDescriptor;
    }

    @Override
    public int getBci(Frame frame) {
        return EspressoFrame.getBCI(frame);
    }

    @Override
    Object execute(VirtualFrame frame) {
        return executeSpecialization(frame.getArguments(), frame);
    }

    @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.FULL_UNROLL_UNTIL_RETURN)
    private Object executeSpecialization(Object[] args, VirtualFrame frame){
        if (this.bytecodeNode != null) {
            this.bytecodeNode.initializeFrame(frame);
            return this.bytecodeNode.execute(frame);
        }
        byte[] methodTypeParams = collectMethodTypeParams(args);
        byte[] classTypeParams = this.classTypeParamCount > 0 ? collectClassTypeParams((StaticObject) args[0]) : EMPTY_BYTE_ARRAY;
        
        for (int i = 0; i < cacheKeys.length; i++){
            if (matchKey(i, methodTypeParams, classTypeParams)) {
                specializations[i].initializeFrame(frame);
                return specializations[i].execute(frame);
            }
        }

        CompilerDirectives.transferToInterpreterAndInvalidate();

        BytecodeNode newNode = insertSpecialization(methodTypeParams, classTypeParams);
        newNode.initializeFrame(frame);
        return newNode.execute(frame);
    }

    private byte[] collectMethodTypeParams(Object[] args) {
        if (methodTypeParamCount == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] key = new byte[methodTypeParamCount];
        int start = this.hasReceiver ? 1 : 0;
        for (int i = 0; i < methodTypeParamCount; i++) {
            key[i] = (byte) args[start + i];
        }
        return key;
    }

    private byte[] collectClassTypeParams(StaticObject receiver) {
        if (traitTypeParamAccessors != null) {
            return collectTraitTypeParams(receiver);
        }
        return receiver.classTypeParams;
    }

    private byte[] collectTraitTypeParams(StaticObject receiver) {
        byte[] key = new byte[traitTypeParamAccessors.length];
        for (int i = 0; i < traitTypeParamAccessors.length; ++i) {
            key[i] = (byte) traitTypeParamAccessors[i].invokeDirectInterface(receiver);
        }
        return key;
    }

    @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.FULL_UNROLL_UNTIL_RETURN)
    private boolean matchKey(int idx, byte[] methodTypeParams, byte[] classTypeParams) {
        for (int i = 0; i < methodTypeParamCount; ++i) {
            if (cacheKeys[idx][i] != methodTypeParams[i]) {
                return false;
            }
        }
        for (int i = 0; i < classTypeParamCount; ++i) {
            if (cacheKeys[idx][methodTypeParamCount + i] != classTypeParams[i]) {
                return false;
            }
        }
        return true;
    }

    private BytecodeNode insertSpecialization(byte[] methodTypeParams, byte[] classTypeParams) {
        CompilerAsserts.neverPartOfCompilation();

        /*
        System.err.println("insert specialization for " + methodVersion.getDeclaringKlass().getName().toString() + "." + methodVersion.getName().toString());
        System.err.println("method type params:");
        for (byte b : methodTypeParams) {
            System.err.print(b);
            System.err.print(' ');
        }
        System.err.print('\n');
        System.err.println("class type params:");
        for (byte b : classTypeParams) {
            System.err.print(b);
            System.err.print(' ');
        }
        System.err.print('\n');
        */


        byte[] classTypeParamsUntilCurLevel = new byte[classTypeParamCount];
        for (int i = 0; i < classTypeParamCount; ++i) {
            classTypeParamsUntilCurLevel[i] = classTypeParams[i];
        }
        BytecodeNode node = new BytecodeNode(methodVersion, analysis, methodTypeParams, classTypeParamsUntilCurLevel, hasStaticForwarderReceiver ? 1 : 0);
        node = this.insert(node);

        int len = specializations.length;
        this.specializations = Arrays.copyOf(this.specializations, len + 1);
        this.specializations[len] = node;
        this.cacheKeys = Arrays.copyOf(this.cacheKeys, len + 1);
        this.cacheKeys[len] = new byte[methodTypeParamCount + classTypeParamCount];
        for (int i = 0; i < methodTypeParamCount; ++i) {
            this.cacheKeys[len][i] = methodTypeParams[i];
        }
        for (int i = 0; i < classTypeParamCount; ++i) {
            this.cacheKeys[len][methodTypeParamCount + i] = classTypeParams[i];
        }
        notifyInserted(node);

        return node;
    }

    @Override
    @SuppressFBWarnings(value = "BC_IMPOSSIBLE_INSTANCEOF", justification = "bytecodeNode may be replaced by instrumentation with a wrapper node")
    boolean isTrivial() {
        // Instrumented nodes are not trivial.
        if (bytecodeNode != null) {
            return !(bytecodeNode instanceof WrapperNode) && bytecodeNode.isTrivial();
        } else {
            return this.trivialBytecode;
        }
    }

    @Override
    public boolean hasTag(Class<? extends Tag> tag) {
        if (tag == StandardTags.RootTag.class) {
            return true;
        }
        return false;
    }

    @ExportMessage
    @SuppressWarnings("static-method")
    public boolean hasScope(@SuppressWarnings("unused") Frame frame) {
        return true;
    }

    @ExportMessage
    public Object getScope(Frame frame, boolean nodeEnter) {
        return getScopeSlowPath(frame != null ? frame.materialize() : null, nodeEnter);
    }

    @TruffleBoundary
    private Object getScopeSlowPath(MaterializedFrame frame, boolean nodeEnter) {
        return bytecodeNode.getScope(frame, nodeEnter);
    }
}
