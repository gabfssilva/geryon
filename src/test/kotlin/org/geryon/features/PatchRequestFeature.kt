package org.geryon.features

import com.mashape.unirest.http.Unirest
import io.kotlintest.Spec
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FeatureSpec
import org.geryon.Http.*

class PatchHttpFeature : FeatureSpec({
    feature("http patch request") {
        scenario("with path parameter") {
            val body = Unirest.patch("http://localhost:8888/test/gabriel").asString().body
            body shouldBe "hello, gabriel"
        }

        scenario("with query parameter") {
            val body = Unirest.patch("http://localhost:8888/test/withQueryParameter?queryParameterName=gabriel").asString().body
            body shouldBe "hello, gabriel"
        }

        scenario("with body") {
            val body = Unirest.patch("http://localhost:8888/test/withBody").body("gabriel").asString().body
            body shouldBe "hello, gabriel"
        }

        scenario("with custom http status") {
            for (i in 0..200) {
                val response = Unirest.patch("http://localhost:8888/test/withBody").body("gabriel").asString()

                response.status shouldBe 202
                response.body shouldBe "hello, gabriel"
            }
        }

        scenario("with custom response") {
            val response = Unirest.patch("http://localhost:8888/test/customResponse").body("gabriel").asString()

            response.status shouldBe 201
            response.headers["Location"]!![0] shouldBe "/test/gabriel"
            response.body shouldBe "hello, gabriel"
        }

        scenario("success with matcher") {
            val response = Unirest.patch("http://localhost:8888/test/withMatcher/versionTest").header("X-Version", "1").body("gabriel").asString()

            response.status shouldBe 202
            response.body shouldBe "accepted, gabriel, with version X-Version = 1 ;)"
        }

        scenario("invalid with matcher") {
            val response = Unirest.patch("http://localhost:8888/test/withMatcher/versionTest").header("X-Version", "2").body("gabriel").asString()

            response.status shouldBe 404 //since the matcher returned false
        }
    }
}) {
    override fun interceptSpec(context: Spec, spec: () -> Unit) {
        port(8888)
        defaultContentType("text/plain")
        eventLoopThreadNumber(1)

        patch("/test/:name") {
            supply { "hello, ${it.pathParameters()["name"]}" }
        }

        patch("/test/withQueryParameter") {
            supply { "hello, ${it.queryParameters()["queryParameterName"]}" }
        }

        patch("/test/withBody") {
            supply { accepted("hello, ${it.body()}") }
        }

        patch("/test/customResponse") {
            supply {
                response()
                        .httpStatus(201)
                        .header("Location", "/test/${it.body()}")
                        .body("hello, ${it.body()}")
                        .build()
            }
        }

        patch("/test/withMatcher/versionTest", { it.headers()["X-Version"] == "1"}) {
            supply { accepted("accepted, ${it.body()}, with version X-Version = 1 ;)") }
        }

        spec()
    }
}