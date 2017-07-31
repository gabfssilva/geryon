package org.geryon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author Gabriel Francisco <gabfssilva@gmail.com>
 */
public class ExceptionHandlers {
    private static List<ExceptionHandler<? extends Throwable>> exceptionHandlers = new ArrayList<>();

    private static BiFunction<Throwable, Request, Response> defaultHandler =
            (t, r) -> new Response.Builder().body(t.getMessage()).contentType("text/plain").httpStatus(500).build();

    private ExceptionHandlers(){
    }

    public static<T extends Throwable> void addHandler(Class<T> forException, BiFunction<T, Request, Response> handler){
        exceptionHandlers.add(new ExceptionHandler<>(forException, handler));
    }

    public static<T extends Throwable> BiFunction<T, Request, Response> getHandler(Class<? extends Throwable> forException){
        for (ExceptionHandler<? extends Throwable> handler : exceptionHandlers) {
            if(handler.match(forException)) {
                return (BiFunction<T, Request, Response>) handler;
            }
        }

        return (BiFunction<T, Request, Response>) defaultHandler;
    }

    public static List<ExceptionHandler<? extends Throwable>> handlers() {
        return exceptionHandlers;
    }
}
