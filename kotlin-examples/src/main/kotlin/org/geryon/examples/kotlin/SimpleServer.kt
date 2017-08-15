package org.geryon.examples.kotlin

import java.util.*
import java.util.concurrent.Executors

import org.geryon.Http.*

/**
 * @author Gabriel Francisco <gabfssilva@gmail.com>
 */
object SimpleServer {
    @JvmStatic
    fun main(args: Array<String>) {
        //defining the expose http port
        //default is 8080
        port(8888)

        //default response content-type
        //default is text/plain
        defaultContentType("application/json")

        //you can define how many threads netty will use for its event loop
        eventLoopThreadNumber(1)

        // you can define some headers to be sent with any response
        defaultHeader("X-Powered-By", "geryon")

        //you can also define exception handlers for exceptions occurred on the future completion
        handlerFor(Exception::class.java) { exception, request ->
            internalServerError("ups, you called ${request.url()} and it seems that an exception occurred: ${exception.message}")
        }

        get("/hello") { r ->
            val name = r.queryParameters().get("name")
            // since you cannot block your handler,
            // you need to return a CompletableFuture with a response, instead of only a response
            supply { ok("{\"name\": \"$name\"}") }
        }

        //over here we are using a matcher
        //a matcher is a function<request, boolean> which returns if the request
        //matched the route, so, you can have the same method and path mapped
        //to different routes, since you also implemented different matchers.
        get("/hello/withMatcher", { r -> "1" == r.headers().get("Version") }) { r ->
            val name = r.queryParameters().get("name")
            // since you cannot block your handler,
            // you need to return a CompletableFuture with a response, instead of only a response
            supply { ok("{\"name\": \"$name\"}") }
        }

        get("/hello/:name") { r ->
            //getting the path parameter
            val name = r.pathParameters()["name"]
            // since you cannot block your handler,
            // you need to return a CompletableFuture with a response, instead of only a response
            supply { ok("{\"name\": \"$name\"}") }
        }

        //this example is using matrix parameters
        //matrix parameters are evaluated lazily, since getting them is more costly than other parameters
        get("/hello/matrix") { r ->
            //so, if the client sends a GET: http://host:port/hello;name=gabriel;age=24/matrix
            //we are extracting over here name and size parameters from "hello" path.
            val name = r.matrixParameters()["hello"]!!["name"]
            val age = r.matrixParameters()["hello"]!!["age"]

            supply { ok("{\"name\": \"$name\", \"age\":$age}") }
        }

        post("/hello") { r ->
            val jsonBody = r.body()
            // since you cannot block your handler,
            // you need to return a CompletableFuture with a response, instead of only a response
            supply { created("/hello/" + UUID.randomUUID().toString(), jsonBody) }
        }

        val singleThreadExecutor = Executors.newSingleThreadExecutor()

        put("/hello") { r ->
            val jsonBody = r.body()
            //in this case, we are using a specific executor service to avoid blocking operations in the global executor
            supply(singleThreadExecutor) { accepted(jsonBody) }
        }
    }
}

