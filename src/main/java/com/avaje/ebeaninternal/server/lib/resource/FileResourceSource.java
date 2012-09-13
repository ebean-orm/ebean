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

/**
 * A file system directory represented as a FileSource.
 */
public class FileResourceSource extends AbstractResourceSource implements ResourceSource {

    /**
     * The directory name.
     */
    String directory;
    
    String baseDir;
        
    /**
     * Create the source based on a directory name.
     */
    public FileResourceSource(String directory){
        this.directory = directory;
        this.baseDir = directory+File.separator;
    }
    
    /**
     * Create the source based on a directory file.
     */
    public FileResourceSource(File dir){
        this(dir.getPath());
    }
    
    
    public String getRealPath() {
		return directory;
	}

	/**
     * Search for the given file and return as IoContent.
     */
    public ResourceContent getContent(String entry) {
         
        String fullPath = baseDir+entry;
        
        File f = new File(fullPath);
        if (f.exists()){
            FileResourceContent content = new FileResourceContent(f, entry);
            return content;
        }
        return null;
    }
}
