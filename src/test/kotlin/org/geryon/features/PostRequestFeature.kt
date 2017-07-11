package org.geryon.features

import com.mashape.unirest.http.Unirest
import io.kotlintest.Spec
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FeatureSpec
import org.geryon.Http.*

class PostHttpFeature : FeatureSpec({
    feature("http post request") {
        scenario("with path parameter") {
            val body = Unirest.post("http://localhost:8888/test/gabriel").asString().body
            body shouldBe "hello, gabriel"
        }

        scenario("with query parameter") {
            val body = Unirest.post("http://localhost:8888/test/withQueryParameter?queryParameterName=gabriel").asString().body
            body shouldBe "hello, gabriel"
        }

        scenario("with body") {
            val body = Unirest.post("http://localhost:8888/test/withBody").body("gabriel").asString().body
            body shouldBe "hello, gabriel"
        }

        scenario("with custom http status") {
            for (i in 0..200) {
                val response = Unirest.post("http://localhost:8888/test/withBody").body("gabriel").asString()

                response.status shouldBe 202
                response.body shouldBe "hello, gabriel"
            }
        }

        scenario("with custom response") {
            val response = Unirest.post("http://localhost:8888/test/customResponse").body("gabriel").asString()

            response.status shouldBe 201
            response.headers["Location"]!![0] shouldBe "/test/gabriel"
            response.body shouldBe "hello, gabriel"
        }
    }
}) {
    override fun interceptSpec(context: Spec, spec: () -> Unit) {
        port(8888)
        defaultContentType("text/plain")

        post("/test/:name") {
            supply { "hello, ${it.pathParameters()["name"]}" }
        }

        post("/test/withQueryParameter") {
            supply { "hello, ${it.queryParameters()["queryParameterName"]}" }
        }

        post("/test/withBody") {
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

        spec()
        stop()
    }
}