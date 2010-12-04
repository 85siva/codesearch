/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.codesearch.org.eclipse.jface.text;


/**
 * Extension interface for {@link org.codesearch.org.eclipse.jface.text.IRepairableDocument}.
 * <p>
 * Adds the ability to query whether the repairable document needs to be
 * repaired.
 *
 * @see org.codesearch.org.eclipse.jface.text.IRepairableDocument
 * @since 3.4
 */
public interface IRepairableDocumentExtension {

	/**
	 * Tells whether the line information of the document implementing this
	 * interface needs to be repaired.
	 *
	 * @param offset the document offset
	 * @param length the length of the specified range
	 * @param text the substitution text to check
	 * @return <code>true</code> if the line information must be repaired
	 * @throws BadLocationException if the offset is invalid in this document
	 * @see IRepairableDocument#repairLineInformation()
	 */
	boolean isLineInformationRepairNeeded(int offset, int length, String text) throws BadLocationException;
}
