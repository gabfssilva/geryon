package org.geryon.exceptions;

/**
 * @author Gabriel Francisco <gabfssilva@gmail.com>
 */
public class InitializationException extends RuntimeException {
    public InitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
