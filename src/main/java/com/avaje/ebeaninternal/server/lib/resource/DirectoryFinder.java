package com.avaje.ebeaninternal.server.lib.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Helper object used to find directories typically from the current working
 * directory.
 */
public class DirectoryFinder {

	private static final Logger logger = LoggerFactory.getLogger(DirectoryFinder.class);
	
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
		if (logger.isTraceEnabled()){
			logger.trace("search; " + f.getPath());
		}
		return sub.exists();

	}
}
