package com.avaje.ebeaninternal.server.lib.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Content from a file system file.
 */
public class FileResourceContent implements ResourceContent {

    /**
     * The underlying file.
     */
    File file;

    String entryName;

    /**
     * Create with a File and the entryName.
     */
    public FileResourceContent(File file, String entryName) {
        this.file = file;
        this.entryName = entryName;
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
        return file.lastModified();
    }

    /**
     * Return the size of the file.
     */
    public long size() {
        return file.length();
    }

    /**
     * Return the input stream for this file.
     */
    public InputStream getInputStream() throws IOException {

        FileInputStream is = new FileInputStream(file);
        return is;
    }
}
