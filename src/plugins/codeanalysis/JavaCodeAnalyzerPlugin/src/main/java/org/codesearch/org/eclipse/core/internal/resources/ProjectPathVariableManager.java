/*******************************************************************************
 * Copyright (c) 2008, 2010 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.codesearch.org.eclipse.core.internal.resources;

import org.codesearch.org.eclipse.core.resources.IResource;

import org.codesearch.org.eclipse.core.runtime.IPath;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import org.codesearch.org.eclipse.core.filesystem.URIUtil;
import org.codesearch.org.eclipse.core.internal.utils.Messages;
import org.codesearch.org.eclipse.core.internal.utils.Policy;
import org.codesearch.org.eclipse.core.resources.*;
import org.codesearch.org.eclipse.core.runtime.*;
import org.codesearch.org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.codesearch.org.eclipse.osgi.util.NLS;

/**
 * The {@link IPathVariableManager} for a single project
 * @see IProject#getPathVariableManager()
 */
public class ProjectPathVariableManager implements IPathVariableManager, IManager {

	private Resource resource;
	private ProjectVariableProviderManager.Descriptor variableProviders[] = null;

	/**
	 * Constructor for the class.
	 */
	public ProjectPathVariableManager(Resource resource) {
		this.resource = resource;
		variableProviders = ProjectVariableProviderManager.getDefault().getDescriptors();
	}

	PathVariableManager getWorkspaceManager() {
		return (PathVariableManager) resource.getWorkspace().getPathVariableManager();
	}

	/**
	 * Throws a runtime exception if the given name is not valid as a path
	 * variable name.
	 */
	private void checkIsValidName(String name) throws CoreException {
		IStatus status = validateName(name);
		if (!status.isOK())
			throw new CoreException(status);
	}

	/**
	 * Throws an exception if the given path is not valid as a path variable
	 * value.
	 */
	private void checkIsValidValue(URI newValue) throws CoreException {
		IStatus status = validateValue(newValue);
		if (!status.isOK())
			throw new CoreException(status);
	}

	/**
	 * @see org.codesearch.org.eclipse.core.resources.IPathVariableManager#getPathVariableNames()
	 */
	public String[] getPathVariableNames() {
		List result = new LinkedList();
		HashMap map;
		try {
			map = ((ProjectDescription) resource.getProject().getDescription()).getVariables();
		} catch (CoreException e) {
			return new String[0];
		}
		for (int i = 0; i < variableProviders.length; i++) {
			String[] variableHints = variableProviders[i].getVariableNames(variableProviders[i].getName(), resource);
			if (variableHints != null && variableHints.length > 0)
				for (int k = 0; k < variableHints.length; k++)
					result.add(variableProviders[i].getVariableNames(variableProviders[i].getName(), resource)[k]);
		}
		if (map != null)
			result.addAll(map.keySet());
		result.addAll(Arrays.asList(getWorkspaceManager().getPathVariableNames()));
		return (String[]) result.toArray(new String[0]);
	}

	/**
	 * @deprecated
	 */
	public IPath getValue(String varName) {
		URI uri = getURIValue(varName);
		if (uri != null)
			return URIUtil.toPath(uri);
		return null;
	}
	
	/**
	 * If the variable is not listed in the project description, we fall back on
	 * the workspace variables.
	 * 
	 * @see org.codesearch.org.eclipse.core.resources.IPathVariableManager#getURIValue(String)
	 */
	public URI getURIValue(String varName) {
		String value = internalGetValue(varName);
		if (value != null) {
			if (value.indexOf("..") != -1) { //$NON-NLS-1$
				// if the path is 'reducible', lets resolve it first.
				int index = value.indexOf(IPath.SEPARATOR);
				if (index > 0) { // if its the first character, its an
					// absolute path on unix, so we don't
					// resolve it
					URI resolved = resolveVariable(value);
					if (resolved != null)
						return resolved;
				}
			}
			try {
				return URI.create(value);
			} catch (IllegalArgumentException e) {
				IPath path = Path.fromPortableString(value);
				return URIUtil.toURI(path);
			}
		}
		return getWorkspaceManager().getURIValue(varName);
	}

	public String internalGetValue(String varName) {
		HashMap map;
		try {
			map = ((ProjectDescription) resource.getProject().getDescription()).getVariables();
		} catch (CoreException e) {
			return null;
		}
		if (map != null && map.containsKey(varName))
			return ((VariableDescription) map.get(varName)).getValue();

		String name;
		int index = varName.indexOf('-');
		if (index != -1)
			name = varName.substring(0, index);
		else
			name = varName;
 		for (int i = 0; i < variableProviders.length; i++) {
 			if (variableProviders[i].getName().equals(name))
 				return variableProviders[i].getValue(varName, resource);
		}
 		for (int i = 0; i < variableProviders.length; i++) {
 			if (name.startsWith(variableProviders[i].getName()))
 				return variableProviders[i].getValue(varName, resource);
		}
		return null;
	}

	/**
	 * @see org.codesearch.org.eclipse.core.resources.IPathVariableManager#isDefined(String)
	 */
	public boolean isDefined(String varName) {
		for (int i = 0; i < variableProviders.length; i++) {
//			if (variableProviders[i].getName().equals(varName))
//				return true;
			
			if (varName.startsWith(variableProviders[i].getName()))
				return true;
		}
		
		try {
			HashMap map = ((ProjectDescription) resource.getProject().getDescription()).getVariables();
			if (map != null) {
				Iterator it = map.keySet().iterator();
				while(it.hasNext()) {
					String name = (String) it.next();
					if (name.equals(varName))
						return true;
				}
			}
		} catch (CoreException e) {
			return false;
		}
		boolean value = getWorkspaceManager().isDefined(varName);
		if (!value) {
			// this is to handle variables with encoded arguments
			int index = varName.indexOf('-');
			if (index != -1) {
				String newVarName = varName.substring(0, index);
				value = isDefined(newVarName);
			}
		}
		return value;
	}

	/**
	 * @deprecated
	 */
	public IPath resolvePath(IPath path) {
		if (path == null || path.segmentCount() == 0 || path.isAbsolute() || path.getDevice() != null)
			return path;
		URI value = resolveURI(URIUtil.toURI(path));
		return value == null ? path : URIUtil.toPath(value);
	}

	public URI resolveVariable(String variable) {
		LinkedList variableStack = new LinkedList();

		String value = resolveVariable(variable, variableStack);
		if (value != null) {
			try {
				return URI.create(value);
			} catch (IllegalArgumentException e) {
				return URIUtil.toURI(Path.fromPortableString(value));
			}
		}
		return null;
	}

	public String resolveVariable(String value, LinkedList variableStack) {
		if (variableStack == null)
			variableStack = new LinkedList();

		String tmp = internalGetValue(value);
		if (tmp == null) {
			URI result = getWorkspaceManager().getURIValue(value);
			if (result != null)
				return result.toASCIIString();
		} else
			value = tmp;

		while (true) {
			String stringValue;
			try {
				URI uri = URI.create(value);
				if (uri != null) {
					IPath path = URIUtil.toPath(uri);
					if (path != null)
						stringValue = path.toPortableString();
					else
						stringValue = value;
				} else
					stringValue = value;
			} catch (IllegalArgumentException e) {
				stringValue = value;
			}
			// we check if the value contains referenced variables with ${VAR}
			int index = stringValue.indexOf("${"); //$NON-NLS-1$
			if (index != -1) {
				int endIndex = PathVariableUtil.getMatchingBrace(stringValue, index);
				String macro = stringValue.substring(index + 2, endIndex);
				String resolvedMacro = ""; //$NON-NLS-1$
				if (!variableStack.contains(macro)) {
					variableStack.add(macro);
					resolvedMacro = resolveVariable(macro, variableStack);
					if (resolvedMacro == null)
						resolvedMacro = ""; //$NON-NLS-1$
				}
				if (stringValue.length() > endIndex)
					stringValue = stringValue.substring(0, index) + resolvedMacro + stringValue.substring(endIndex + 1);
				else
					stringValue = resolvedMacro;
				value = stringValue;
			} else
				break;
		}
		return value;
	}

	public URI resolveURI(URI uri) {
		if (uri == null || uri.isAbsolute() || (uri.getSchemeSpecificPart() == null))
			return uri;
		IPath raw = new Path(uri.getSchemeSpecificPart());
		if (raw == null || raw.segmentCount() == 0 || raw.isAbsolute() || raw.getDevice() != null)
			return URIUtil.toURI(raw);
		URI value = resolveVariable(raw.segment(0));
		if (value == null)
			return uri;
		
		String path = value.getPath();
		if (path != null) {
			IPath p = Path.fromPortableString(path);
			p = p.append(raw.removeFirstSegments(1));
			try {
				value = new URI(value.getScheme(), value.getHost(), p.toPortableString(), value.getFragment());
			} catch (URISyntaxException e) {
				return uri;
			}
			return value;
		}
		return uri;
	}

	/**
	 * @deprecated
	 */
	public void setValue(String varName, IPath newValue) throws CoreException {
		if (newValue == null)
			setURIValue(varName, (URI) null);
		else
			setURIValue(varName, URIUtil.toURI(newValue));
			
	}
		/**
		 * @see org.codesearch.org.eclipse.core.resources.IPathVariableManager#setValue(String,
		 *      IPath)
		 */
	public void setURIValue(String varName, URI newValue) throws CoreException {
		checkIsValidName(varName);
		checkIsValidValue(newValue);
		// read previous value and set new value atomically in order to generate
		// the right event
		boolean changeWorkspaceValue = false;
		Project project = (Project) resource.getProject();
		int eventType = 0;
		synchronized (this) {
			String value = internalGetValue(varName);
			URI currentValue = null;
			if (value == null)
				currentValue = getWorkspaceManager().getURIValue(varName);
			else {
				try {
					currentValue = URI.create(value);
				} catch (IllegalArgumentException e) {
					currentValue = null;
				}
			}
			boolean variableExists = currentValue != null;
			if (!variableExists && newValue == null)
				return;
			if (variableExists && currentValue.equals(newValue))
				return;

			for (int i = 0; i < variableProviders.length; i++) {
//				if (variableProviders[i].getName().equals(varName))
//					return;
				
				if (varName.startsWith(variableProviders[i].getName()))
					return;
			}

			if (value == null && variableExists)
				changeWorkspaceValue = true;
			else {
				IProgressMonitor monitor = new NullProgressMonitor();
				final ISchedulingRule rule = resource.getProject(); // project.workspace.getRuleFactory().modifyRule(project);
				try {
					project.workspace.prepareOperation(rule, monitor);
					project.workspace.beginOperation(true);
					// save the location in the project description
					ProjectDescription description = project.internalGetDescription();
					if (newValue == null) {
						description.setVariableDescription(varName, null);
						eventType = IPathVariableChangeEvent.VARIABLE_DELETED;
					} else {
						description.setVariableDescription(varName, new VariableDescription(varName, newValue.toASCIIString()));
						eventType = variableExists ? IPathVariableChangeEvent.VARIABLE_CHANGED : IPathVariableChangeEvent.VARIABLE_CREATED;
					}
					project.writeDescription(IResource.NONE);
				} finally {
					project.workspace.endOperation(rule, true, Policy.subMonitorFor(monitor, Policy.endOpWork));
				}
			}
		}
		if (changeWorkspaceValue)
			getWorkspaceManager().setURIValue(varName, newValue);
		else {
			// notify listeners from outside the synchronized block to avoid deadlocks
			getWorkspaceManager().fireVariableChangeEvent(project, varName, newValue != null? URIUtil.toPath(newValue):null, eventType);
		}
	}

	/**
	 * @see org.codesearch.org.eclipse.core.internal.resources.IManager#shutdown(IProgressMonitor)
	 */
	public void shutdown(IProgressMonitor monitor) {
		// nothing to do here
	}

	/**
	 * @see org.codesearch.org.eclipse.core.internal.resources.IManager#startup(IProgressMonitor)
	 */
	public void startup(IProgressMonitor monitor) {
		// nothing to do here
	}

	/**
	 * @see org.codesearch.org.eclipse.core.resources.IPathVariableManager#validateName(String)
	 */
	public IStatus validateName(String name) {
		String message = null;
		if (name.length() == 0) {
			message = Messages.pathvar_length;
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		char first = name.charAt(0);
		if (!Character.isLetter(first) && first != '_') {
			message = NLS.bind(Messages.pathvar_beginLetter, String.valueOf(first));
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		for (int i = 1; i < name.length(); i++) {
			char following = name.charAt(i);
			if (Character.isWhitespace(following))
				return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, Messages.pathvar_whitespace);
			if (!Character.isLetter(following) && !Character.isDigit(following) && following != '_') {
				message = NLS.bind(Messages.pathvar_invalidChar, String.valueOf(following));
				return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
			}
		}
		// check

		return Status.OK_STATUS;
	}

	/**
	 * @see IPathVariableManager#validateValue(IPath)
	 */
	public IStatus validateValue(IPath value) {
		// accept any format
		return Status.OK_STATUS;
	}

	/**
	 * @see IPathVariableManager#validateValue(URI)
	 */
	public IStatus validateValue(URI value) {
		// accept any format
		return Status.OK_STATUS;
	}

	/**
	 * @throws CoreException 
	 * @see IPathVariableManager#convertToRelative(URI, boolean, String)
	 */
	public URI convertToRelative(URI path, boolean force, String variableHint) throws CoreException {
		return PathVariableUtil.convertToRelative(this, path, resource, force, variableHint);
	}

	/**
	 * @see IPathVariableManager#convertToUserEditableFormat(String, boolean)
	 */
	public String convertToUserEditableFormat(String value, boolean locationFormat) { 
		return PathVariableUtil.convertToUserEditableFormatInternal(value, locationFormat);
	}
	
	public String convertFromUserEditableFormat(String userFormat, boolean locationFormat) {
		return PathVariableUtil.convertFromUserEditableFormatInternal(this, userFormat, locationFormat);
	}
	
	public void addChangeListener(IPathVariableChangeListener listener) {
		getWorkspaceManager().addChangeListener(listener, resource.getProject());
	}

	public void removeChangeListener(IPathVariableChangeListener listener) {
		getWorkspaceManager().removeChangeListener(listener, resource.getProject());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IPathVariableManager#getVariableRelativePathLocation(IResource, URI)
	 */
	public URI getVariableRelativePathLocation(URI location) {
		try {
			URI result = convertToRelative(location, false, null);
			if (!result.equals(location))
				return result;
		} catch (CoreException e) {
			// handled by returning null
		}
		return null;
	}

	/*
	 * Return the resource of this manager.
	 */
	public IResource getResource() {
		return resource;
	}

	public boolean isUserDefined(String name) {
		return ProjectVariableProviderManager.getDefault().findDescriptor(name) == null;
	}
}