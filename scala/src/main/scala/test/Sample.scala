package test

import scala.concurrent.Future

import org.geryon.scaladsl._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * @author Gabriel Francisco <gabfssilva@gmail.com>
  */
object Sample extends App {
  port(9999)

  defaultHeader("X-Powered-By", "Geryon")

  handlerFor[RuntimeException] { (exception, request) =>
    internalServerError(s"ups, you called ${request.url} and it seems that an exception occurred: ${exception.getMessage}")
  }

  get("/hello") { request =>
    Future { s"hello, ${request.queryParameters("name")}" }
  }

  post("/hoho") { request =>
    Future { "this is a post" }
  }
}
