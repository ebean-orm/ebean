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

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper object used to find directories typically from the current working
 * directory.
 */
public class DirectoryFinder {

	private static final Logger logger = Logger.getLogger(DirectoryFinder.class.getName());
	
	/**
	 * Find a directory by search through subdirectories.
	 * <p>
	 * For example, used to find the WEB-INF directory starting from the current
	 * working directory.
	 * </p>
	 * 
	 * <pre class="code">
	 * 
	 * // search to a depth of 3 from the current working directory
	 * // looking for a directory WEB-INF that contains the subdirectory
	 * // data
	 * 
	 * File dir = DirectoryFinder.find(null, &quot;WEB-INF/data&quot;, 3);
	 * if (dir != null) {
	 * 	//found the directory
	 * }
	 * </pre>
	 */
	public static File find(File startDir, String match, int maxDepth) {

		String matchSub = null;
		int slashPos = match.indexOf('/');
		if (slashPos > -1) {
			// match has sub directories
			matchSub = match.substring(slashPos + 1);
			match = match.substring(0, slashPos);
		}

		// search for the directory
		File found = find(startDir, match, matchSub, 0, maxDepth);

		if (found != null && matchSub != null) {
			// match has sub directories
			return new File(found, matchSub);
		}
		return found;
	}

	private static File find(File dir, String match, String matchSub, int depth, int maxDepth) {

		if (dir == null) {
			String curDir = System.getProperty("user.dir");
			dir = new File(curDir);
		}

		if (dir.exists()) {
			File[] list = dir.listFiles();
			if (list != null){
    			for (int i = 0; i < list.length; i++) {
    				if (isMatch(list[i], match, matchSub)) {
    					return list[i];
    				}
    			}
	
				// go through the directories again
				// Aka *NOT* a depth first search
				if (depth < maxDepth) {
					for (int i = 0; i < list.length; i++) {
						if (list[i].isDirectory()) {
							File found = find(list[i], match, matchSub, depth + 1, maxDepth);
							if (found != null) {
								return found;
							}
						}
					}
				}
			}
		}
		return null;
	}

	private static boolean isMatch(File f, String match, String matchSub) {
		if (f == null) {
			return false;
		}
		if (!f.isDirectory()) {
			return false;
		}
		if (!f.getName().equalsIgnoreCase(match)) {
			return false;
		}
		if (matchSub == null) {
			return true;
		}
		File sub = new File(f, matchSub);
		if (logger.isLoggable(Level.FINEST)){
			logger.finest("search; " + f.getPath());
		}
		return sub.exists();

	}
}
