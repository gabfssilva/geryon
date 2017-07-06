package org.geryon;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * @author Gabriel Francisco <peo_gfsilva@uolinc.com>
 */
public class RequestHandler {
    private Function<Request, CompletableFuture<?>> func;
    private String produces;

    public RequestHandler(Function<Request, CompletableFuture<?>> func, String produces) {
        this.func = func;
        this.produces = produces;
    }

    public Function<Request, CompletableFuture<?>> func() {
        return func;
    }

    public String produces() {
        return produces;
    }
}
