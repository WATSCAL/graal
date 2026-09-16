package com.oracle.truffle.espresso.classfile.attributes.reified;

import java.util.Arrays;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;

public class TraitTypeParamListAttribute extends Attribute {
    public static final Symbol<Name> NAME = ParserNames.TraitTypeParamList;

    private final int[] typeParamAccessorMethodRefCpis;

    public int[] getTypeParamAccessorMethodRefCpis() {
        return typeParamAccessorMethodRefCpis;
    }

    public TraitTypeParamListAttribute(Symbol<Name> name, int[] typeParamAccessorMethodRefCpis) {
        assert name == NAME;
        this.typeParamAccessorMethodRefCpis = typeParamAccessorMethodRefCpis;
    }

    @Override
    public Symbol<Name> getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return "TraitTypeParamListAttribute{" +
                        "typeParamAccessorMethodRefCpis=" + Arrays.toString(typeParamAccessorMethodRefCpis) +
                        '}';
    }
}
