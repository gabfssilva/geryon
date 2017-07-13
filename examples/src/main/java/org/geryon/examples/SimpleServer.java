package org.geryon.examples;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.geryon.Http.*;

/**
 * @author Gabriel Francisco <peo_gfsilva@uolinc.com>
 */
public class SimpleServer {
    public static void main(String[] args) {
        //defining the expose http port
        //default is 8080
        port(8888);

        //default response content-type
        //default is text/plain
        defaultContentType("application/json");

        //you can define how many thread numbers netty will use for its event loop
        eventLoopThreadNumber(1);

        get("/hello", r -> {
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
