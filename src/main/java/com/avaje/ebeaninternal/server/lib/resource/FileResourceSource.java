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
