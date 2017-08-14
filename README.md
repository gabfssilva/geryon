# geryon
Inspired by Sinatra, Geryon is a library that runs on top of Netty 4, helping you build reactive HTTP services in the JVM.

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

#### If you are using Scala and SBT

```scala
 libraryDependencies +=
  "org.geryon" %% "geryon-scala" % "0.0.4"
```

## The obligatory Hello World in Java


```java
//this import does all the trick
import static org.geryon.Http.*;

public class Sample {
    public static void main(String[] args) {
        get("/hello", request -> supply(() -> "Hello, " + request.queryParameters().get("name")));
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
    get("/hello") { supply { "hello, ${it.queryParameters()["name"]}" } }
}
```

Your app will be running at 8080.

#### More examples in Kotlin:
[Simple Server in Kotlin](https://github.com/gabfssilva/geryon/tree/master/kotlin-examples/src/main/kotlin/org/geryon/examples/kotlin/SimpleServer.kt)

## The obligatory Hello World in Scala

```scala
//this import does all the trick
import org.geryon.scaladsl._
  
object Sample extends App {
  get("/hello") { implicit request => supply { s"hello, ${param("name")}}" } }
}
```

Your app will be running at 8080.

#### More examples in:
[Simple Server in Scala](https://github.com/gabfssilva/geryon/tree/master/scala-examples/src/main/scala/org/geryon/examples/scaladsl/SimpleServer.scala)

## Changing the http port

#### Java, Kotlin or Scala
```java
port(9090);
```

## Using a default content type

#### Java, Kotlin or Scala
```java
defaultContentType("application/json");
```

## Changing the event loop thread number

#### Java, Kotlin or Scala
```java
eventLoopThreadNumber(2);
```

## Adding a default response header

#### Java or Kotlin
```java
defaultHeader("X-Powered-By", "geryon");
```

#### Scala
```java
defaultHeader("X-Powered-By" -> "geryon")
```


