/**
 * Copyright 2010 David Froehlich   <david.froehlich@businesssoftware.at>,
 *                Samuel Kogler     <samuel.kogler@gmail.com>,
 *                Stephan Stiboller <stistc06@htlkaindorf.at>
 *
 * This file is part of Codesearch.
 *
 * Codesearch is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Codesearch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Codesearch.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.codesearch.indexer.server.exceptions;

/**
 * Thrown when the index location could not be found or accessed.
 * @author Samuel Kogler
 */
public class InvalidIndexLocationException extends Exception {

    /** . */
    private static final long serialVersionUID = -1046835425050030526L;


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
