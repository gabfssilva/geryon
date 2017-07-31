package org.geryon.examples.scaladsl

import java.util.UUID
import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author Gabriel Francisco <gabfssilva@gmail.com>
  */
object SimpleServer extends App {
  import org.geryon.scaladsl._

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
  handlerFor[RuntimeException] { implicit request => exception =>
    internalServerError(s"ups, you called $url and it seems that an exception occurred: ${exception.getMessage}")
  }

  get("/hello") { implicit request =>
    // since you cannot block your handler,
    // you need to return a Future with a response, instead of only a response"
    //the method supply method is just a future with a default ExecutionContext

    //this way we are getting a query parameter
    supply {
      s"""
      {
          "name": "${param("name")}"
      }
      """
    }
  }

  //over here we are using a matcher
  //a matcher is a function<request, boolean> which returns if the request
  //matched the route, so, you can have the same method and path mapped
  //to different routes, since you also implemented different matchers.
  get("/hello/withMatcher", implicit r => "1".equals(header("Version"))) { implicit request =>
    supply {
      s"""
      {
          "name": "${param("name")}"
      }
      """
    }
  }

  get("/hello/:name") { implicit request =>
    //getting the path parameter
    val name = pathParam("name")

    supply {
      s"""
      {
          "name": $name
      }
      """
    }
  }

  post("/hello") { implicit request =>
    //getting a body
    //since a body is not obligatory, it is inside of an Option[String]
    val jsonBody = body.orNull

    supply {
      created(s"/hello/${UUID.randomUUID().toString}", jsonBody)
    }
  }

  put("/hello") { implicit request =>
    //implicitly using a specific ExecutionContext for not blocking the global executor
    //this one uses a single thread
    implicit val ec = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

    val jsonBody = body.orNull

    supply {
      accepted(jsonBody)
    }
  }
}
