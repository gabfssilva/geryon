package org.geryon;

import org.geryon.exceptions.AmbiguousRoutingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Gabriel Francisco <peo_gfsilva@uolinc.com>
 */
public class RequestHandlersHolder {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private static List<RequestHandler> requestHandlers = new ArrayList<>();

    private RequestHandlersHolder() {
    }

    public static List<RequestHandler> requestHandlers() {
        return requestHandlers;
    }

    public static void addHandler(RequestHandler handler) {
        requestHandlers.forEach(r -> {
            if (!Objects.equals(r.method(), handler.method())) {
                return;
            }

            if (r.pathAsPattern().equals(handler.pathAsPattern())) {
                throw new AmbiguousRoutingException("There is more than one handler mapped for path " + r.pathAsPattern());
            }
        });

        requestHandlers.add(handler);
        logger.info("Listening to " + handler.path() + " - " + handler.method());
    }
}
