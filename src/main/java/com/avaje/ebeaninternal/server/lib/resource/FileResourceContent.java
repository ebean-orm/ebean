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
