package com.oracle.truffle.espresso.classfile.attributes.reified;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;

public class ClassTypeParamListAttribute extends Attribute {
    public static final Symbol<Name> NAME = ParserNames.ClassTypeParamList;

    private final int[] typeParamFieldRefCpis;

    public int[] getTypeParamFieldRefCpis() {
        return typeParamFieldRefCpis;
    }

    public ClassTypeParamListAttribute(Symbol<Name> name, int[] typeParamFieldRefCpis) {
        assert name == NAME;
        this.typeParamFieldRefCpis = typeParamFieldRefCpis;
    }

    @Override
    public Symbol<Name> getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return "ClassTypeParameterCountAttribute{" +
                        "..." +
                        '}';
    }
    
}
