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
package com.avaje.ebeaninternal.server.lib.util;


import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Helper methods to determine the mime type based on a file name.
 */
public class MimeTypeHelper {

	/**
	 * Return the mimeType for a given file path.
	 * This will extract the file extension, and then use that
	 * to look up an appropriate mime type (from the mimetypes.props file).
	 *
	 * To add a new mime type, add it to the mimetype.props file.
	 */
	public static String getMimeType(String filePath) {

		int lastPeriod = filePath.lastIndexOf(".");
		if (lastPeriod > -1) {
			filePath = filePath.substring(lastPeriod+1);
		}

		try {
			return resources.getString(filePath.toLowerCase());

		} catch (MissingResourceException e) {
			return null;
			//String m = "Unable to locate mimetype for ["+filePath.toLowerCase()+"] in mimetypes.properties";
			//throw new NotFoundException(m);
		}

	}

	private static ResourceBundle resources = ResourceBundle.getBundle("com.avaje.lib.util.mimetypes");


};
