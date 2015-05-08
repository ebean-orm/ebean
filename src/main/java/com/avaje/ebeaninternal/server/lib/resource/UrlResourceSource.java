package com.avaje.ebeaninternal.server.lib.resource;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import com.avaje.ebeaninternal.server.lib.util.GeneralException;

/**
 * A file system directory represented as a FileSource.
 */
public class UrlResourceSource extends AbstractResourceSource implements ResourceSource {

        
    ServletContext sc;
    
    String basePath;
    
    String realPath;
    
    /**
     * Create the source based on a directory name.
     */
    public UrlResourceSource(ServletContext sc, String basePath){
        this.sc = sc;
        if (basePath == null){
        	this.basePath = "/";
        } else {
        	this.basePath = "/"+basePath+"/";
        }
        this.realPath = sc.getRealPath(basePath);
    }
    
    /**
     * Returns the "real path" from the ServletContext root.
     * <p>
     * This can be null for unpacked WAR deployment.
     * </p>
     */
    public String getRealPath() {
    	return realPath;
    }
    
    /**
     * Search for the given URL resource and return as ResourceContent.
     * <p>
     * Returns null if the resource is not found.
     * </p>
     */
    public ResourceContent getContent(String entry) {
        
    	try {
	    	URL url = sc.getResource(basePath+entry);
	    	if (url != null){
	    		return new UrlResourceContent(url, entry);
	    	}
	        return null;
	        
    	} catch (MalformedURLException ex){
    		throw new GeneralException(ex);
    	}
    }
}
