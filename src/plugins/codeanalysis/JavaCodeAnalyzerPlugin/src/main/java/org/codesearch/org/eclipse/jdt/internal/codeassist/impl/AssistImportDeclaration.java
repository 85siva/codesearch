/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.codesearch.org.eclipse.jdt.internal.codeassist.impl;

import java.util.Map;

import org.codesearch.org.eclipse.core.runtime.IProgressMonitor;
import org.codesearch.org.eclipse.jdt.core.JavaModelException;
import org.codesearch.org.eclipse.jdt.internal.core.ImportContainer;
import org.codesearch.org.eclipse.jdt.internal.core.ImportDeclaration;

public class AssistImportDeclaration extends ImportDeclaration {
	private Map infoCache;
	public AssistImportDeclaration(ImportContainer parent, String name, boolean isOnDemand, Map infoCache) {
		super(parent, name, isOnDemand);
		this.infoCache = infoCache;
	}

	public Object getElementInfo(IProgressMonitor monitor) throws JavaModelException {
		return this.infoCache.get(this);
	}
}