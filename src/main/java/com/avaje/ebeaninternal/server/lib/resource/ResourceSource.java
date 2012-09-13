/**
 *  Copyright (C) 2006  Robin Bygrave
 *  
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
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
