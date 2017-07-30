package org.geryon.examples;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;
import static org.geryon.Http.*;

/**
 * @author Gabriel Francisco <gabfssilva@gmail.com>
 */
public class SimpleServer {
    public static void main(String[] args) {
        //defining the expose http port
        //default is 8080
        port(8888);

        //default response content-type
        //default is text/plain
        defaultContentType("application/json");

        //you can define how many threads netty will use for its event loop
        eventLoopThreadNumber(1);

        // you can define some headers to be sent with any response
        defaultHeader("X-Powered-By", "geryon");

        //you can also define exception handlers for exceptions occurred on the future exception
        handlerFor(Exception.class, (e, r) -> {
            String message = format(
                    "ups, you called %s and it seems that an exception occurred: %s", r.url(), e.getMessage()
            );

            return internalServerError(message);
        });

        get("/hello", r -> {
            final String name = r.queryParameters().get("name");
            // since you cannot block your handler,
            // you need to return a CompletableFuture with a response, instead of only a response
            return supply(() -> ok("{\"name\": \"" + name + "\"}"));
        });

        //over here we are using a matcher
        //a matcher is a function<request, boolean> which returns if the request
        //matched the route, so, you can have the same method and path mapped
        //to different routes, since you also implemented different matchers.
        get("/hello/withMatcher", r -> "1".equals(r.headers().get("Version")), r -> {
            final String name = r.queryParameters().get("name");
            // since you cannot block your handler,
            // you need to return a CompletableFuture with a response, instead of only a response
            return supply(() -> ok("{\"name\": \"" + name + "\"}"));
        });

        get("/hello/:name", r -> {
            //getting the path parameter
            final String name = r.pathParameters().get("name");
            // since you cannot block your handler,
            // you need to return a CompletableFuture with a response, instead of only a response
            return supply(() -> ok("{\"name\": \"" + name + "\"}"));
        });

        post("/hello", r -> {
            final String jsonBody = r.body();
            // since you cannot block your handler,
            // you need to return a CompletableFuture with a response, instead of only a response
            return supply(() -> created("/hello/" + UUID.randomUUID().toString(), jsonBody));
        });

        final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

        put("/hello", r -> {
            final String jsonBody = r.body();
            //in this case, we are using a specific executor service to avoid blocking operations in the global executor
            return supply(singleThreadExecutor, () -> accepted(jsonBody));
        });
    }
}
