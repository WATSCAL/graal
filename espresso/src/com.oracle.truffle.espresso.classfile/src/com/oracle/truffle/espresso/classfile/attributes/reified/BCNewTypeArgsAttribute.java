package com.oracle.truffle.espresso.classfile.attributes.reified;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;


public class BCNewTypeArgsAttribute extends Attribute{
    public static final Symbol<Name> NAME = ParserNames.BCNewTypeArgs;

    public record Entry(int bcOffset, int[] localSlotIndices) {}

    private final Entry[] entries;

    public BCNewTypeArgsAttribute(Symbol<Name> name, Entry[] entries) {
        super(name, null);
        this.entries = entries;
    }

    public Entry[] getEntires() {
        return entries;
    }

    @Override
    public String toString() {
        return "BCNewTypeArgsAttribute{" +
                        "..." +
                        '}';
    }
}
