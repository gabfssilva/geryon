# geryon
Geryon is a library that runs on top of Netty 4, helping you build reactive HTTP services in the JVM.

## Using in your project

#### Importing the library using Gradle
```groovy
repositories {
    maven {
        url  "http://dl.bintray.com/geryon/releases"
    }
}

dependencies {
    compile "org.geryon:geryon:0.0.4"
}
```

## A simple GET in Java.


```java
//this import does all the trick
import static org.geryon.Http.*;

public class Main {
    public static void main(String[] args) {
        port(9090);
        defaultContentType("text/plain");
        
        handlerFor(Exception.class, (e, r) -> {
            String message = format(
                "ups, you called %s and it seems that an exception occurred: %s", r.url(), e.getMessage()
            );
        
            return internalServerError(message);
        });
        
        get("/hello", r -> supply(() -> "Hello, " + r.queryParameters().get("name")));
    }
}
```

#### More examples in:

[Simple Server in Java](https://github.com/gabfssilva/geryon/tree/master/examples/src/main/java/org/geryon/examples/SimpleServer.java)

## Examples in Kotlin

```kotlin
//this import does all the trick
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

Since Kotlin has functions, it is possible to build interceptors around your service in a very elegant way:

```kotlin
//this import does all the trick
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

#### More examples in:
//TODO

## Scala


#### SBT

```scala
 libraryDependencies +=
  "org.geryon" %% "geryon-scala" % "0.0.4"
```

#### Gradle

```groovy
dependencies {
    compile "org.geryon:geryon-scala_2.12:0.0.4"
}
```

#### Sample http server

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Sample extends App {
  //this import does all the trick
  import org.geryon.scaladsl._

  port(9999)
  
  handlerFor[RuntimeException] { (exception, request) =>
    internalServerError(s"ups, you called ${request.url} and it seems that an exception occurred: ${exception.getMessage}")
  }

  get("/hello") { request =>
    Future {
      s"hello, ${request.queryParameters().get("name")}"
    }
  }
}
```

