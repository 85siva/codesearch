/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.codesearch.org.eclipse.swt.internal.image;


final class JPEGRestartInterval extends JPEGFixedSizeSegment {

	public JPEGRestartInterval(LEDataInputStream byteStream) {
		super(byteStream);
	}
	
	public int signature() {
		return JPEGFileFormat.DRI;
	}
	
	public int getRestartInterval() {
		return ((reference[4] & 0xFF) << 8 | (reference[5] & 0xFF));
	}

	public int fixedSize() {
		return 6;
	}
}
