package com.avaje.ebeaninternal.server.lib.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

/**
 * Content from a URL Resource.
 */
public class UrlResourceContent implements ResourceContent {

    /**
     * The underlying resource.
     */
    //URL url;

    String entryName;

    URLConnection con;
    
    /**
     * Create with a File and the entryName.
     */
    public UrlResourceContent(URL url, String entryName) {
        //this.url = url;
        this.entryName = entryName;
        try {
        	con = url.openConnection();
        } catch (IOException ex){
        	throw new RuntimeException(ex);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[").append(getName());
        sb.append("] size[").append(size());
        sb.append("] lastModified[").append(new Date(lastModified()));
        sb.append("]");
        return sb.toString();
    }

    /**
     * Returns the entry name which contains the path from the base directory.
     * <p>
     * This does not return the full path of the file, but the path relative to
     * the FileIoSource directory.
     * </p>
     */
    public String getName() {
        return entryName;
    }

    /**
     * Return the time the file was last modified.
     */
    public long lastModified() {
    	return con.getLastModified();
    }

    /**
     * Return the size of the file.
     */
    public long size() {
    	return con.getContentLength();
    }

    /**
     * Return the input stream for this file.
     */
    public InputStream getInputStream() throws IOException {

    	return con.getInputStream();
    }
}
