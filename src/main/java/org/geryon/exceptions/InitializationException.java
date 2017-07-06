package org.geryon.exceptions;

/**
 * @author Gabriel Francisco <peo_gfsilva@uolinc.com>
 */
public class InitializationException extends RuntimeException {
    public InitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
