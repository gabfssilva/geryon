package org.geryon;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gabriel Francisco <peo_gfsilva@uolinc.com>
 */
public class RequestHandlersHolder {
    private static List<RequestHandler> requestHandlers = new ArrayList<>();

    private RequestHandlersHolder() {
    }

    public static List<RequestHandler> requestHandlers() {
        return requestHandlers;
    }

    public static void addHandler(RequestHandler handler) {
        requestHandlers.add(handler);
    }
}
