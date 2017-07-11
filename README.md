# geryon
A simple reactive HTTP Server written in Java running on top of Netty 4

# How to use it

## Java
```java
import static org.geryon.Http.*;

public class Main {
    public static void main(String[] args) {
        port(9090);
        defaultContentType("text/plain");
        
        get("/hello", r -> supply(() -> "Hello, " + r.getQueryParameters().get("name")));
    }
}
```

## Kotlin

```kotlin
import org.geryon.Http.*

fun main(args: Array<String>) {
    port(9090)
    defaultContentType("text/plain")

    get("/test/:name") {
        supply { "hello, ${it.pathParameters()["name"]}" }
    }

    put("/test/withQueryParameter") {
        supply { "hello, ${it.queryParameters()["queryParameterName"]}" }
    }

    patch("/test/withBody") {
        supply { accepted("hello, ${it.body()}") }
    }

    post("/test/customResponse") {
        supply {
            response()
               .httpStatus(201)
               .header("Location", "/test/${it.body()}")
               .body("hello, ${it.body()}")
               .build()
            }
    }
}
```

You can also use filters and interceptos using Kotlin functions:

```kotlin
import org.geryon.Http.*

fun main(args: Array<String>) {
    port(9090)
    defaultContentType("text/plain")
    
    fun timer(f: () -> CompletableFuture<out Any>): CompletableFuture<out Any> {
        val init = System.currentTimeMillis()
        return f().thenApply {
             println("total request time in ms:" + (System.currentTimeMillis() - init))
             it
        }
    }
    
    get("/hello") { timer {
        supply { "Hello, ${it.queryParameters["name"]}" }
      }
    }
}
```



