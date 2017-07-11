package org.geryon.features

import com.mashape.unirest.http.Unirest
import io.kotlintest.Spec
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FeatureSpec
import org.geryon.Http.*

class PutHttpFeature : FeatureSpec({
    feature("http put request") {
        scenario("with path parameter") {
            val body = Unirest.put("http://localhost:8888/test/put").asString().body
            body shouldBe "hello, put"
        }

        scenario("with query parameter") {
            val body = Unirest.put("http://localhost:8888/test/withQueryParameter?queryParameterName=put").asString().body
            body shouldBe "hello, put"
        }

        scenario("with body") {
            val body = Unirest.put("http://localhost:8888/test/withBody").body("put").asString().body
            body shouldBe "hello, put"
        }

        scenario("with custom http status") {
            val response = Unirest.put("http://localhost:8888/test/withBody").body("put").asString()

            response.status shouldBe 202
            response.body shouldBe "hello, put"
        }

        scenario("with custom response") {
            val response = Unirest.put("http://localhost:8888/test/customResponse").body("put").asString()

            response.status shouldBe 201
            response.headers["Location"]!![0] shouldBe "/test/put"
            response.body shouldBe "hello, put"
        }

        scenario("success with matcher") {
            val response = Unirest.put("http://localhost:8888/test/withMatcher/versionTest").header("X-Version", "1").body("put").asString()

            response.status shouldBe 202
            response.body shouldBe "accepted, put, with version X-Version = 1 ;)"
        }

        scenario("invalid with matcher") {
            val response = Unirest.put("http://localhost:8888/test/withMatcher/versionTest").header("X-Version", "2").body("put").asString()

            response.status shouldBe 404 //since the matcher returned false
        }
    }
}) {
    override fun interceptSpec(context: Spec, spec: () -> Unit) {
        port(8888)
        defaultContentType("text/plain")
        eventLoopThreadNumber(1)

        put("/test/:name") {
            supply { "hello, ${it.pathParameters()["name"]}" }
        }

        put("/test/withQueryParameter") {
            supply { "hello, ${it.queryParameters()["queryParameterName"]}" }
        }

        put("/test/withBody") {
            supply { accepted("hello, ${it.body()}") }
        }

        put("/test/customResponse") {
            supply {
                response()
                        .httpStatus(201)
                        .header("Location", "/test/${it.body()}")
                        .body("hello, ${it.body()}")
                        .build()
            }
        }

        put("/test/withMatcher/versionTest", { it.headers()["X-Version"] == "1" }) {
            supply { accepted("accepted, ${it.body()}, with version X-Version = 1 ;)") }
        }

        spec()
    }
}