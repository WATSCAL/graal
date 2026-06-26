package com.oracle.truffle.espresso.classfile.attributes.reified;

import java.util.Arrays;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.constantpool.InterfaceMethodRefConstant;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;

public class TraitTypeParamListAttribute extends Attribute {
    public static final Symbol<Name> NAME = ParserNames.TraitTypeParamList;

    private final int[] typeParamAccessorMethodRefCpis;
    private final InterfaceMethodRefConstant.Indexes[] typeParamAccessorMethodRefs;

    public int[] getTypeParamAccessorMethodRefCpis() {
        return typeParamAccessorMethodRefCpis;
    }

    public InterfaceMethodRefConstant.Indexes[] getTypeParamAccessorMethodRefs() {
        return typeParamAccessorMethodRefs;
    }

    public TraitTypeParamListAttribute(Symbol<Name> name, int[] typeParamAccessorMethodRefCpis, InterfaceMethodRefConstant.Indexes[] typeParamAccessorMethodRefs) {
        super(name, null);
        this.typeParamAccessorMethodRefCpis = typeParamAccessorMethodRefCpis;
        this.typeParamAccessorMethodRefs = typeParamAccessorMethodRefs;
    }

    @Override
    public String toString() {
        return "TraitTypeParamListAttribute{" +
                        "typeParamAccessorMethodRefCpis=" + Arrays.toString(typeParamAccessorMethodRefCpis) +
                        '}';
    }
}
