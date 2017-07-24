package test

import scala.concurrent.Future

/**
  * @author Gabriel Francisco <gabfssilva@gmail.com>
  */
object Sample extends App {
  import org.geryon.scaladsl._
  import scala.concurrent.ExecutionContext.Implicits.global

  port(9999)

  get("/hello") { request =>
    Future {
      s"hello, ${request.queryParameters().get("name")}"
    }
  }

  post("/hoho") { request =>
    Future {
      "aaaa"
    }
  }
}
