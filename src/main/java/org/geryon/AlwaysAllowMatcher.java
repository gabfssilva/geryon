package org.geryon;

import java.util.function.Function;

/**
 * @author Gabriel Francisco <peo_gfsilva@uolinc.com>
 */
public class AlwaysAllowMatcher implements Function<Request, Boolean> {
    public static final AlwaysAllowMatcher MATCHER = new AlwaysAllowMatcher();

    private AlwaysAllowMatcher() {
    }

    @Override
    public Boolean apply(Request request) {
        return true;
    }
}
