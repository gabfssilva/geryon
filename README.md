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

# Tutorial

For now on, every single available feature of Geryon will be showed over here. Since Geryon was developed just to be an HTTP layer for your application, not a fullstack framework, there's not much for you to learn.

## The obligatory Hello World

#### Java

```java
//this import does all the trick
import static org.geryon.Http.*;

public class Sample {
    public static void main(String[] args) {
        get("/hello", request -> supply(() -> "Hello, " + request.queryParameters().get("name")));
    }
}
```

#### Kotlin

```kotlin
//this import does all the trick
import org.geryon.Http.*

fun main(args: Array<String>) {
    get("/hello") { supply { "hello, ${it.queryParameters()["name"]}" } }
}
```

#### Scala

```scala
//this import does all the trick
import org.geryon.scaladsl._

object Sample extends App {
  get("/hello") { implicit request => supply { s"hello, ${param("name")}}" } }
}
```

Your app will be running at 8080.

#### More examples in:

[Simple Server in Java](https://github.com/gabfssilva/geryon/tree/master/examples/src/main/java/org/geryon/examples/SimpleServer.java) ~
[Simple Server in Kotlin](https://github.com/gabfssilva/geryon/tree/master/kotlin-examples/src/main/kotlin/org/geryon/examples/kotlin/SimpleServer.kt) ~
[Simple Server in Scala](https://github.com/gabfssilva/geryon/tree/master/scala-examples/src/main/scala/org/geryon/examples/scaladsl/SimpleServer.scala)

## Understanding HTTP handlers

### Available methods

For now, these are all the http methods available:

```
get
post
put
delete
patch
options
head
trace
connect
```

### Basic usage of a handler

#### Java

```java
httpMethod("/path", request -> futureResponse)
```

#### Kotlin

```kotlin
httpMethod("/path") { request -> futureResponse }
```

#### Scala

```scala
httpMethod("/path") { implicit request => futureResponse }
```

### Handler with matcher

Geryon offers a simple matcher for you implement your own validation, thus you can validate, based on the request, if the handler is the right one.

#### Java

```java
httpMethod("/path", request -> boolean, request -> futureResponse)
```

#### Kotlin

```kotlin
httpMethod("/path", request -> boolean) { request -> futureResponse }
```

#### Scala

```scala
httpMethod("/path", request => boolean) { request => futureResponse }
```

### Working with path parameters

#### Java

```java
httpMethod("/path/:myParameter", request -> {
   final String myParameter = request.pathParameters().get("myParameter");
   //do whatever you want to do over here
   return futureResponse;
})
```

#### Kotlin

```kotlin
httpMethod("/path/:myParameter") { //in Kotlin, you can also omit the parameter if a function has only one parameter
   //you can use the parameter using the keyword "it"
   val myParameter = it.pathParameters()["myParameter"]
   //do whatever you want to do over here
   futureResponse
}
```

#### Scala

```scala
httpMethod("/path/:myParameter") { implicit request =>
   val myParameter = pathParameter("myParameter") //or just param("myParameter")
   //do whatever you want to do over here
   futureResponse
}
```

### Working with query parameters

#### Java

```java
httpMethod("/path", request -> {
   final String myParameter = request.queryParameters().get("myParameter");
   //do whatever you want to do over here
   return futureResponse;
})
```

#### Kotlin

```kotlin
httpMethod("/path") { //in Kotlin, you can also omit the parameter if a function has only one parameter
   //you can use the parameter using the keyword "it"
   val myParameter = it.queryParameters()["myParameter"]
   //do whatever you want to do over here
   futureResponse
}
```

#### Scala

```scala
httpMethod("/path") { implicit request =>
   val myParameter = queryParameter("myParameter") //or just param("myParameter")
   //do whatever you want to do over here
   futureResponse
}
```

### Working with matrix parameters

#### Java

```java
httpMethod("/path", request -> {
   final String myParameter = request.matrixParameters().get("path").get("myParameter");
   //do whatever you want to do over here
   return futureResponse;
})
```

#### Kotlin

```kotlin
httpMethod("/path") { //in Kotlin, you can also omit the parameter if a function has only one parameter
   //you can use the parameter using the keyword "it"
   val myParameter = it.matrixParameters()["path"]!!["myParameter"]
   //do whatever you want to do over here
   futureResponse
}
```

#### Scala

```scala
httpMethod("/path") { implicit request =>
   val myParameter = matrixParameter("path" -> "matrixParameter")
   
   //if you have more than one matrix parameter per path, you can do this:
   //val (myParam1, myParam2) = matrixParameter("path" -> ("myParam1", "myParam2"))
   
   //do whatever you want to do over here
   futureResponse
}
```

### Working with headers

#### Java

```java
httpMethod("/path", request -> {
   final String myParameter = request.headers().get("myParameter");
   //do whatever you want to do over here
   return futureResponse;
})
```

#### Kotlin

```kotlin
httpMethod("/path") { //in Kotlin, you can also omit the parameter if a function has only one parameter
   //you can use the parameter using the keyword "it"
   val myParameter = it.headers()["myParameter"]
   //do whatever you want to do over here
   futureResponse
}
```

#### Scala

```scala
httpMethod("/path") { implicit request =>
   val myParameter = header("myParameter")
   //do whatever you want to do over here
   futureResponse
}
```

## Understand the models (Request and Response)

### Request

Basically, a Request contains the following fields:

```
url: String
rawPath: String
body: String
contentType: String
method: String
headers: Map[String, String]
queryParameters: Map[String, String]
pathParameters: Map[String, String]
matrixParameters: Map[String, Map[String, String]]
```

### ScalaDslRequest and the inherited request methods

In Scala, you can use a series of methods which takes an implicit request to avoid boilerplate code:

```scala
val pathParam = pathParam("pathParam")
val queryParam = queryParam("queryParam")
val pathOrQueryParam = param("param") //this method tries to get the path parameter with the informed key, if there is none, tries to get a query parameter
val header = header("header")
val (matrixParam1, matrixParam2) = matrixParameter("path" -> ("matrixParam1", "matrixParam2"))
```

### Response

```
body: String
httpStatus: Int
headers: Map[String, String]
contentType: String
```

### Response Builder

You always create a response using a response builder.
In Java, Kotlin or Scala, there's already a method called "response", which is a response builder that helps you build your response

#### Java or Kotlin

```java
response()
   .body("this is the response body")
   .httpStatus(201)
   .header("headerName", "headerValue")
   .header("headerName2", "headerValue2")
   .contentType("text/plain")
   .build();
```
#### Scala

```scala
response
   .body("this is the response body")
   .httpStatus(201)
   .header("headerName" -> "headerValue")
   .header("headerName2" -> "headerValue2")
   .contentType("text/plain")
   .build()
```

### Pre defined responses

There are a bunch of pre defined responses already created to help you return your response in a very easy way:

####Java or Kotlin

```java
ok();
ok("response body");

created("http://location");
created("http://location", "response body");

noContent();

accepted();
accepted("response body");

notFound();
notFound("response body");

conflict();
conflict("response body");

unauthorized();
unauthorized("response body");

internalServerError();
internalServerError("response body");
```

####Scala

```scala
ok
ok("response body")

created("http://location")
created("http://location", "response body")

noContent

accepted
accepted("response body")

notFound
notFound("response body")

conflict
conflict("response body")

unauthorized
unauthorized("response body")

internalServerError
internalServerError("response body")
```

Of course, if you feel like we could add other pre defined response, feel free to ask or to pull request. ;)

## Geryon configurations

### Changing the http port

#### Java, Kotlin or Scala

```java
port(9090);
```

### Using a default content type

#### Java, Kotlin or Scala

```java
defaultContentType("application/json");
```

### Changing the event loop thread number

#### Java, Kotlin or Scala

```java
eventLoopThreadNumber(2);
```

### Adding a default response header

#### Java or Kotlin

```java
defaultHeader("X-Powered-By", "geryon");
```

#### Scala

```scala
defaultHeader("X-Powered-By" -> "geryon")
```

### Adding an exception handler

#### Java

```java
handlerFor(Exception.class, (e, r) -> internalServerError(String.format("ups, you called %s and it seems that an exception occurred: %s", r.url(), e.getMessage())));
```

#### Kotlin

```kotlin
handlerFor(Exception::class.java) { exception, request ->
  internalServerError("ups, you called ${request.url()} and it seems that an exception occurred: ${exception.message}")
}
```

#### Scala

```scala
handlerFor[RuntimeException] { implicit request => exception =>
  internalServerError(s"ups, you called $url and it seems that an exception occurred: ${exception.getMessage}")
}
```

## How to contribute

//TODO

