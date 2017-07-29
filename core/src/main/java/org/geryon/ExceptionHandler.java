package org.geryon;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Gabriel Francisco <gabfssilva@gmail.com>
 */
public class ExceptionHandler<T extends Throwable> implements BiFunction<T, Request, Response> {
    private Class<T> exception;
    private BiFunction<T, Request, Response> func;

    public ExceptionHandler(Class<T> exception, BiFunction<T, Request, Response> func) {
        this.exception = exception;
        this.func = func;
    }

    public Class<T> exception() {
        return exception;
    }

    public Boolean match(Class<? extends Throwable> t){
        return exception.isAssignableFrom(t);
    }

    @Override
    public Response apply(T t, Request request) {
        return func.apply(t, request);
    }
}
