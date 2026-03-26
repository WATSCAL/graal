package com.oracle.truffle.espresso.analysis.typehints;

import com.oracle.truffle.espresso.analysis.BlockIterator;
import com.oracle.truffle.espresso.analysis.GraphBuilder;
import com.oracle.truffle.espresso.analysis.graph.Graph;
import com.oracle.truffle.espresso.analysis.graph.LinkedBlock;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.runtime.EspressoContext;
import com.oracle.truffle.espresso.classfile.bytecode.Bytecodes;
import com.oracle.truffle.espresso.impl.Field;
import com.oracle.truffle.espresso.classfile.JavaKind;
import com.oracle.truffle.espresso.classfile.bytecode.BytecodeStream;


public class TypeHintAnalysis {
    public static TypeAnalysisResult[] analyze(Method.MethodVersion methodVersion) {
        Method method = methodVersion.getMethod();
        if (!mayNeedAnalysis(method)) {
            return null;
        }
        Graph<? extends LinkedBlock> graph = GraphBuilder.build(method);
        EspressoContext context = method.getContext();
        int codeLength = method.getOriginalCode().length;
        int maxLocals = methodVersion.getMaxLocals();
        int maxStack = methodVersion.getMaxStackSize();
        int totalBlocks = graph.totalBlocks();
        TypePropagationClosure closure = new TypePropagationClosure(context, codeLength, methodVersion, maxLocals, maxStack, totalBlocks);
        //System.out.println("Analyze " + method.getDeclaringKlass().getNameAsString() + " " + method.getNameAsString());
        BlockIterator.analyze(method, graph, closure);
        return closure.getRes();
    }

    private static boolean mayNeedAnalysis(Method method) {
        if ((method.getMethodParameterTypeAttribute() != null) || (method.getInvokeReturnTypeAttribute() != null) || (method.getExtraBoxUnboxAttribute() != null)) {
            return true;
        }
        BytecodeStream bs = new BytecodeStream(method.getOriginalCode());
        for (int i = 0; i < bs.endBCI(); i = bs.nextBCI(i)) {
            int opcode = bs.currentBC(i);
            if (opcode == Bytecodes.GETFIELD || opcode == Bytecodes.PUTFIELD) {
                int cpi = bs.readCPI(i);
                Field field = method.getRuntimeConstantPool().resolvedFieldAt(method.getDeclaringKlass(), cpi);
                if (field.getKind() == JavaKind.Object && field.genericTypeParamIdx >= 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
