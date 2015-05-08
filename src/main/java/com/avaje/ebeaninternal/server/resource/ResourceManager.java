package com.avaje.ebeaninternal.server.resource;

import java.io.File;

import com.avaje.ebeaninternal.server.lib.resource.ResourceSource;

/**
 * The ResourceManager implementation.
 */
public class ResourceManager {

	final ResourceSource resourceSource;
	
	final File autofetchDir;
	
	public ResourceManager(ResourceSource resourceSource, File autofetchDir) {
		this.resourceSource = resourceSource;
		this.autofetchDir = autofetchDir;
	}
	
	public ResourceSource getResourceSource() {
		return resourceSource;
	}
	
	public File getAutofetchDirectory() {
		return autofetchDir;
	}

}
