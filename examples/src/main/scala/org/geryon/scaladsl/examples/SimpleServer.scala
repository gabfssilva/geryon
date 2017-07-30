package org.geryon.scaladsl.examples

import java.util.UUID
import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Gabriel Francisco <gabfssilva@gmail.com>
  */
object SimpleServer extends App {

  import org.geryon.scaladsl._

  import scala.concurrent.ExecutionContext.Implicits.global

  //defining the expose http port
  //default is 8080
  port(8888)

  //default response content-type
  //default is text/plain
  defaultContentType("application/json")

  //you can define how many threads netty will use for its event loop
  eventLoopThreadNumber(1)

  // you can define some headers to be sent with any response
  defaultHeader("X-Powered-By" -> "Geryon")

  //you can also define exception handlers for exceptions occurred on the future completion
  handlerFor[RuntimeException] { (exception, request) =>
    internalServerError(s"ups, you called ${request.url} and it seems that an exception occurred: ${exception.getMessage}")
  }

  get("/hello") { request =>
    // since you cannot block your handler,
    // you need to return a Future with a response, instead of only a response
    Future {
      s"""
      {
          "name": ${request.queryParameters("name")}
      }
      """
    }
  }

  //over here we are using a matcher
  //a matcher is a function<request, boolean> which returns if the request
  //matched the route, so, you can have the same method and path mapped
  //to different routes, since you also implemented different matchers.
  get("/hello/withMatcher", r => "1".equals(r.headers("Version"))) { request =>
    // since you cannot block your handler,
    // you need to return a Future with a response, instead of only a response
    Future {
      s"""
      {
          "name": ${request.queryParameters("name")}
      }
      """
    }
  }

  get("/hello/:name") { request =>
    //getting the path parameter
    val name = request.pathParameters("name")

    // since you cannot block your handler,
    // you need to return a Future with a response, instead of only a response
    Future {
      s"""
      {
          "name": $name
      }
      """
    }
  }

  post("/hello") { request =>
    val jsonBody = request.body.orNull

    // since you cannot block your handler,
    // you need to return a Future with a response, instead of only a response
    Future {
      created(s"/hello/${UUID.randomUUID().toString}", jsonBody)
    }
  }

  put("/hello") { request =>
    //using a specific ExecutionContext for not blocking the global executor
    //this one uses only one thread
    implicit val ec = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

    val jsonBody = request.body.orNull

    // since you cannot block your handler,
    // you need to return a Future with a response, instead of only a response
    Future {
      accepted(jsonBody)
    }
  }
}
