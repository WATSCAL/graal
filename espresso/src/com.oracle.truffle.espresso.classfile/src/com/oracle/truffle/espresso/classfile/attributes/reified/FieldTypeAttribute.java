package com.oracle.truffle.espresso.classfile.attributes.reified;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;


public class FieldTypeAttribute extends Attribute{
    public static final Symbol<Name> NAME = ParserNames.FieldType;

    public final int classTypeParamIndex;

    public FieldTypeAttribute(Symbol<Name> name, int classTypeParamIndex) {
        super(name, null);
        this.classTypeParamIndex = classTypeParamIndex;
    }

    @Override
    public String toString() {
        return "FieldTypeAttribute{" +
                        "classTypeParamIndex=" + classTypeParamIndex +
                        '}';
    }
}