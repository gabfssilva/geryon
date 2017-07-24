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
import static org.geryon.Http.*;

public class Main {
    public static void main(String[] args) {
        port(9090);
        defaultContentType("text/plain");
        
        get("/hello", r -> supply(() -> "Hello, " + r.queryParameters().get("name")));
    }
}
```

#### More examples in:

[Simple Server in Java](https://github.com/gabfssilva/geryon/tree/master/examples/src/main/java/org/geryon/examples/SimpleServer.java)

## Examples in Kotlin

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

Since Kotlin has functions, it is possible to build interceptors around your service in a very elegant way:

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

object Sample extends App {
  import scala.concurrent.Future
  import org.geryon.scaladsl._
  import scala.concurrent.ExecutionContext.Implicits.global

  port(9999)

  get("/hello") { request =>
    Future {
      s"hello, ${request.queryParameters().get("name")}"
    }
  }
}
```

