package org.geryon;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 * @author Gabriel Francisco <gabfssilva@gmail.com>
 */
public class RequestHandler {
    private String method;
    private String path;
    private String produces;
    private Function<Request, CompletableFuture<?>> func;
    private Function<Request, Boolean> condition;

    public RequestHandler(String produces, Function<Request, CompletableFuture<?>> func) {
        this.produces = produces;
        this.func = func;
        this.condition = request -> true;
    }

    public RequestHandler(String method, String path, String produces, Function<Request, CompletableFuture<?>> func, Function<Request, Boolean> condition) {
        this.method = method;
        this.path = path;
        this.produces = produces;
        this.func = func;
        this.condition = condition == null ? request -> true : condition;
    }

    public String method() {
        return method;
    }

    public String path() {
        return path;
    }

    public String produces() {
        return produces;
    }

    public Function<Request, CompletableFuture<?>> func() {
        return func;
    }

    public Function<Request, Boolean> condition() {
        return condition;
    }
}
