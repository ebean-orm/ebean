package com.avaje.ebeaninternal.server.lib.resource;

import java.io.IOException;

/**
 * A Source for ResourceManager.
 * <p>
 * Typically a File System Directory based source or a ServletContext URL
 * resource based source (for Servlet WAR files).
 * </p>
 */
public interface ResourceSource {

	/**
	 * Return the File System path of the root of the ResourceSource.
	 * <p>
	 * This will return null IF the ResourceSource is an unpacked WAR file.
	 * </p>
	 */
	public String getRealPath();
	
	/**
	 * Find the content with a given entry name. This will return null if no
	 * matching content was found.
	 */
	public ResourceContent getContent(String entry);

    /**
     * Return the content as a String.
     */
	public String readString(ResourceContent content, int bufSize) throws IOException;

	/**
	 * Return the content as a byte[].
	 */
	public byte[] readBytes(ResourceContent content, int bufSize) throws IOException;
}
