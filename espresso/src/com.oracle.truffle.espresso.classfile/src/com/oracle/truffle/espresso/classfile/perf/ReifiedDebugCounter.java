package com.oracle.truffle.espresso.classfile.perf;

import com.oracle.truffle.api.TruffleLogger;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;

public class ReifiedDebugCounter {
    private static String PARAM_REF_TO_PRIMITIVE = "Reference typed parameter to primitive type";
    private static String ALOAD_TO_PRIMITIVE = "aload to primitive type";
    private static String ASTORE_TO_PRIMITIVE = "astore to primitive type";
    private static String ARETURN_TO_PRIMITIVE = "areturn to primitive type";
    private static String GETFIELD_TO_PRIMITIVE = "getfield to primitive type";
    private static String PUTFIELD_TO_PRIMITIVE = "putfield to primitive type";
    private static String ARRAY_APPLY_TO_PRIMITIVE = "array_apply to primitive type";
    private static String ARRAY_UPDATE_TO_PRIMITIVE = "array_update to primitive type";
    private static String EXTRA_BOX_UNBOX_IGNORED = "Extra boxing/unboxing ignored";
    private static String INTERFACE_ACCESSOR_CALL = "Interface accessor call";

    private static DebugCounter PARAM_REF_TO_PRIMITIVE_COUNTER = DebugCounter.create(PARAM_REF_TO_PRIMITIVE);
    private static DebugCounter ALOAD_TO_PRIMITIVE_COUNTER = DebugCounter.create(ALOAD_TO_PRIMITIVE);
    private static DebugCounter ASTORE_TO_PRIMITIVE_COUNTER = DebugCounter.create(ASTORE_TO_PRIMITIVE);
    private static DebugCounter ARETURN_TO_PRIMITIVE_COUNTER = DebugCounter.create(ARETURN_TO_PRIMITIVE);
    private static DebugCounter GETFIELD_TO_PRIMITIVE_COUNTER = DebugCounter.create(GETFIELD_TO_PRIMITIVE);
    private static DebugCounter PUTFIELD_TO_PRIMITIVE_COUNTER = DebugCounter.create(PUTFIELD_TO_PRIMITIVE);
    private static DebugCounter ARRAY_APPLY_TO_PRIMITIVE_COUNTER = DebugCounter.create(ARRAY_APPLY_TO_PRIMITIVE);
    private static DebugCounter ARRAY_UPDATE_TO_PRIMITIVE_COUNTER = DebugCounter.create(ARRAY_UPDATE_TO_PRIMITIVE);
    private static DebugCounter EXTRA_BOX_UNBOX_IGNORED_COUNTER = DebugCounter.create(EXTRA_BOX_UNBOX_IGNORED);
    private static DebugCounter INTERFACE_ACCESSOR_CALL_COUNTER = DebugCounter.create(INTERFACE_ACCESSOR_CALL);

    ReifiedDebugCounter() {
    }

    public static boolean isPrimitiveTypeHint(byte hint){
        switch (hint) {
            case TypeHints.BYTE:
            case TypeHints.CHAR:
            case TypeHints.DOUBLE:
            case TypeHints.FLOAT:
            case TypeHints.INT:
            case TypeHints.LONG:
            case TypeHints.SHORT:
            case TypeHints.BOOLEAN:
                return true;
            default:
                return false;
        }
    }

    public static void inc(DebugCounter counter, byte hint){
        if (isPrimitiveTypeHint(hint) && DebugCounter.DebugCounters) {
            counter.inc();
        }
    }

    public static void inc(DebugCounter counter){
        if (DebugCounter.DebugCounters) {
            counter.inc();
        }
    }

    public static void incParamRefToPrimitive(byte hint){
        inc(PARAM_REF_TO_PRIMITIVE_COUNTER, hint);
    }

    public static void incAloadToPrimitive(byte hint){
        inc(ALOAD_TO_PRIMITIVE_COUNTER, hint);
    }

    public static void incAstoreToPrimitive(byte hint){
        inc(ASTORE_TO_PRIMITIVE_COUNTER, hint);
    }

    public static void incAreturnToPrimitive(byte hint){
        inc(ARETURN_TO_PRIMITIVE_COUNTER, hint);
    }

    public static void incGetfieldToPrimitive(byte originalType, byte hint){
        if (originalType == 'L') {
            inc(GETFIELD_TO_PRIMITIVE_COUNTER, hint);
        }
    }

    public static void incPutfieldToPrimitive(byte originalType, byte hint){
        if (originalType == 'L') {
            inc(PUTFIELD_TO_PRIMITIVE_COUNTER, hint);
        }
    }

    public static void incArrayApplyToPrimitive(byte hint) {
        inc(ARRAY_APPLY_TO_PRIMITIVE_COUNTER, hint);
    }

    public static void incArrayUpdateToPrimitive(byte hint) {
        inc(ARRAY_UPDATE_TO_PRIMITIVE_COUNTER, hint);
    }

    public static void incExtraBoxUnboxIgnored() {
        inc(EXTRA_BOX_UNBOX_IGNORED_COUNTER);
    }

    public static void incInterfaceAccessorCall() {
        inc(INTERFACE_ACCESSOR_CALL_COUNTER);
    }

    public static void log(TruffleLogger logger){
        if (!DebugCounter.DebugCounters) {
            return;
        }
        logCounter(logger, PARAM_REF_TO_PRIMITIVE_COUNTER, PARAM_REF_TO_PRIMITIVE);
        logCounter(logger, ALOAD_TO_PRIMITIVE_COUNTER, ALOAD_TO_PRIMITIVE);
        logCounter(logger, ASTORE_TO_PRIMITIVE_COUNTER, ASTORE_TO_PRIMITIVE);
        logCounter(logger, ARETURN_TO_PRIMITIVE_COUNTER, ARETURN_TO_PRIMITIVE);
        logCounter(logger, GETFIELD_TO_PRIMITIVE_COUNTER, GETFIELD_TO_PRIMITIVE);
        logCounter(logger, PUTFIELD_TO_PRIMITIVE_COUNTER, PUTFIELD_TO_PRIMITIVE);
        logCounter(logger, ARRAY_APPLY_TO_PRIMITIVE_COUNTER, ARRAY_APPLY_TO_PRIMITIVE);
        logCounter(logger, ARRAY_UPDATE_TO_PRIMITIVE_COUNTER, ARRAY_UPDATE_TO_PRIMITIVE);
        logCounter(logger, EXTRA_BOX_UNBOX_IGNORED_COUNTER, EXTRA_BOX_UNBOX_IGNORED);
        logCounter(logger, INTERFACE_ACCESSOR_CALL_COUNTER, INTERFACE_ACCESSOR_CALL);
    }

    private static void logCounter(TruffleLogger logger, DebugCounter counter, String message) {
        logger.info(message + ": " + counter.get());
    }
}
