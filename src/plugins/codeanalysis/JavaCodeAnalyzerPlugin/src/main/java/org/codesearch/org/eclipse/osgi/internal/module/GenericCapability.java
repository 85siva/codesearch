/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.codesearch.org.eclipse.osgi.internal.module;

import org.codesearch.org.eclipse.osgi.service.resolver.BaseDescription;
import org.codesearch.org.eclipse.osgi.service.resolver.BundleDescription;

public class GenericCapability extends VersionSupplier {
	ResolverBundle resolverBundle;

	GenericCapability(ResolverBundle resolverBundle, BaseDescription base) {
		super(base);
		this.resolverBundle = resolverBundle;
	}

	public BundleDescription getBundle() {
		return getBaseDescription().getSupplier();
	}

	public boolean isFromFragment() {
		return resolverBundle.isFragment();
	}

	public ResolverBundle getResolverBundle() {
		return resolverBundle;
	}
}
