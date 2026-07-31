package com.oracle.truffle.espresso.nodes.quick.invoke;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.nodes.EspressoFrame;
import com.oracle.truffle.espresso.runtime.staticobject.StaticObject;
import com.oracle.truffle.espresso.vm.InterpreterToVM;
import com.oracle.truffle.espresso.meta.Meta;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;
import static com.oracle.truffle.espresso.nodes.EspressoFrame.popDouble;
import static com.oracle.truffle.espresso.nodes.EspressoFrame.popFloat;
import static com.oracle.truffle.espresso.nodes.EspressoFrame.popInt;
import static com.oracle.truffle.espresso.nodes.EspressoFrame.popLong;
import static com.oracle.truffle.espresso.nodes.EspressoFrame.popObject;
import static com.oracle.truffle.espresso.nodes.EspressoFrame.putObject;

public final class InvokeUpcastNode extends InvokeScalaNode {

    @CompilerDirectives.CompilationFinal private final byte inputType;

    public InvokeUpcastNode(Method method, int top, int callerBCI, byte inputType) {
        super(method, top, callerBCI);
        assert resultAt == top - 2;
        assert stackEffect == -1;
        assert !method.isStatic();
        this.inputType = inputType;
    }

    @Override
    public int execute(VirtualFrame frame, boolean isContinuationResume) {
        Meta meta = getMeta();
        switch (inputType) {
            case TypeHints.BYTE:
                putObject(frame, resultAt, meta.boxByte((byte) popInt(frame, top - 1)));
                break;
            case TypeHints.CHAR:
                putObject(frame, resultAt, meta.boxCharacter((char) popInt(frame, top - 1)));
                break;
            case TypeHints.DOUBLE:
                putObject(frame, resultAt, meta.boxDouble(popDouble(frame, top - 1)));
                break;
            case TypeHints.FLOAT:
                putObject(frame, resultAt, meta.boxFloat(popFloat(frame, top - 1)));
                break;
            case TypeHints.INT:
                putObject(frame, resultAt, meta.boxInteger(popInt(frame, top - 1)));
                break;
            case TypeHints.LONG:
                putObject(frame, resultAt, meta.boxLong(popLong(frame, top - 1)));
                break;
            case TypeHints.SHORT:
                putObject(frame, resultAt, meta.boxShort((short) popInt(frame, top - 1)));
                break;
            case TypeHints.BOOLEAN:
                putObject(frame, resultAt, meta.boxBoolean(popInt(frame, top - 1) != 0));
                break;
            default:
                putObject(frame, resultAt, popObject(frame, top - 1));
        }

        return stackEffect;
    }
  
}
