package com.oracle.truffle.espresso.classfile.attributes.reified;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;


public class FieldTypeAttribute extends Attribute{
    public static final Symbol<Name> NAME = ParserNames.FieldType;

    public final TypeHints.TypeB hint;

    public FieldTypeAttribute(Symbol<Name> name, TypeHints.TypeB hint) {
        super(name, null);
        this.hint = hint;
    }

    @Override
    public String toString() {
        return "FieldTypeAttribute{}";
    }
}