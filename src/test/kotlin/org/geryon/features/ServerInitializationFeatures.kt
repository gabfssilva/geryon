package org.geryon.features

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.FeatureSpec
import org.geryon.Http.*
import org.geryon.RequestHandlersHolder
import org.geryon.exceptions.AmbiguousRoutingException

class ServerInitializationFeatures : FeatureSpec({
    feature("server initialization") {
        scenario("ambiguous handlers") {
            port(8888)
            defaultContentType("text/plain")
            eventLoopThreadNumber(1)

            post("/test/:name") {
                supply { "hello, ${it.pathParameters()["name"]}" }
            }

            val exception = shouldThrow<AmbiguousRoutingException> {
                post("/test/:id") {
                    supply { "hello, ${it.pathParameters()["id"]}" }
                }
            }

            exception.message shouldBe "There is more than one handler mapped for path /test(/.+)"

            stop()
            RequestHandlersHolder.requestHandlers().clear()
        }
    }
})