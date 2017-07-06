# geryon
A simple reactive HTTP Server written in Java

# How to use it

## Java
```java
import static org.geryon.Http.*;

public class Main {
    public static void main(String[] args) {
        port(9090);
        defaultContentType("text/plain");
        
        get("/", r -> ok(() -> "Hello, " + r.getQueryParameters().get("name")));
    }
}
```

## Kotlin

```kotlin
import org.geryon.Http.*

fun main(args: Array<String>) {
    port(9090)
    defaultContentType("text/plain")

    get("/hello/default") { ok { "Hello, ${it.queryParameters["name"]}" } }
}
```

