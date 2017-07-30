# geryon
A Sinatra inspired framework, Geryon is a library that runs on top of Netty 4, helping you build reactive HTTP services in the JVM.

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

## The obligatory Hello World in Java


```java
//this import does all the trick
import static org.geryon.Http.*;

public class Main {
    public static void main(String[] args) {
        get("/hello", r -> supply(() -> "Hello, " + r.queryParameters().get("name")));
    }
}
```

Your app will be running at 8080.

#### More examples in:

[Simple Server in Java](https://github.com/gabfssilva/geryon/tree/master/examples/src/main/java/org/geryon/examples/SimpleServer.java)

## The obligatory Hello World in Kotlin

```kotlin
//this import does all the trick
import org.geryon.Http.*

fun main(args: Array<String>) {
    get("/hello") {
        supply { "hello, ${it.queryParameters()["name"]}" }
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

#### The obligatory Hello World in Scala

```scala
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object Sample extends App {
  //this import does all the trick
  import org.geryon.scaladsl._

  get("/hello") { request =>
    Future {
      s"hello, ${request.queryParameters("name")}"
    }
  }
}
```

#### More examples in:
[Simple Server in Scala](https://github.com/gabfssilva/geryon/tree/master/scala-examples/src/main/scala/org/geryon/examples/scaladsl/SimpleServer.scala)


