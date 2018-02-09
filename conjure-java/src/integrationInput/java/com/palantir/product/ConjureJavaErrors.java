package com.palantir.product;

import com.palantir.remoting.api.errors.ErrorType;
import com.palantir.remoting.api.errors.ServiceException;
import javax.annotation.Generated;

@Generated("com.palantir.conjure.gen.java.types.ErrorGenerator")
public final class ConjureJavaErrors {
    /** Failed to compile Conjure definition to Java code. */
    public static final ErrorType JAVA_COMPILATION_FAILED =
            ErrorType.create(ErrorType.Code.INTERNAL, "ConjureJava:JavaCompilationFailed");

    private ConjureJavaErrors() {}

    public static ServiceException javaCompilationFailed() {
        return new ServiceException(JAVA_COMPILATION_FAILED);
    }

    /**
     * Throws a {@link ServiceException} with type JavaCompilationFailed when condition is false.
     *
     * @param condition Cause the method to throw when false
     */
    public static void checkJavaCompilationFailed(boolean condition) {
        if (!condition) {
            throw javaCompilationFailed();
        }
    }
}
