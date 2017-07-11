package org.geryon.exceptions;

/**
 * @author Gabriel Francisco <gabfssilva@gmail.com>
 */
public class GeryonException extends RuntimeException {
    public GeryonException(String message) {
        super(message);
    }

    public GeryonException(String message, Throwable cause) {
        super(message, cause);
    }
}
