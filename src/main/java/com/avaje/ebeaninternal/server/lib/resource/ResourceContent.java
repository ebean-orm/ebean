package com.avaje.ebeaninternal.server.lib.resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents content that can be read via the ResourceManager.
 * <p>
 * Typically either content from a File or a URL.
 * </p>
 */
public interface ResourceContent {

    /**
     * The name of the content.
     */
    String getName();

    /**
     * The size of the content in bytes.
     */
    long size();

    /**
     * The last modified timestamp of the content.
     */
    long lastModified();

    /**
     * The content itself.
     */
    InputStream getInputStream() throws IOException;

}
