package org.codesearch.searcher.server;

/**
 * Thrown when no valid index was found.
 * @author Samuel Kogler
 */
public class InvalidIndexLocationException extends Exception {

    /**
     * Creates a new instance of <code>InvalidIndexLocationException</code> without detail message.
     */
    public InvalidIndexLocationException() {
    }

    /**
     * Constructs an instance of <code>InvalidIndexLocationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public InvalidIndexLocationException(String msg) {
        super(msg);
    }
}
