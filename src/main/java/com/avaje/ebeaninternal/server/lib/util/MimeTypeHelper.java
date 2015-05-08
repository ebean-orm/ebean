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
