package com.oracle.truffle.espresso.nodes.quick.invoke;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.nodes.EspressoFrame;
import com.oracle.truffle.espresso.runtime.staticobject.StaticObject;
import com.oracle.truffle.espresso.vm.InterpreterToVM;

public final class InvokeArrayLengthNode extends InvokeScalaNode {

    public InvokeArrayLengthNode(Method method, int top, int callerBCI) {
        super(method, top, callerBCI);
        assert !method.isStatic();
    }

    @Override
    public int execute(VirtualFrame frame, boolean isContinuationResume) {
        StaticObject array = nullCheck(EspressoFrame.popObject(frame, top - 1));
        int arrayLength = InterpreterToVM.arrayLength(array, getLanguage());
        EspressoFrame.putInt(frame, resultAt, arrayLength);
        return stackEffect;
    }
  
}
