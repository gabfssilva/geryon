package org.geryon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
        logger.info("Listening to " + handler.path() + " - " + handler.method());
        requestHandlers.add(handler);
    }
}
