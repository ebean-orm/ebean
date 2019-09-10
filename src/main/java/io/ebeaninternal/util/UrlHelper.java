package io.ebeaninternal.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Helper to open URL content without file descriptor caching (by underlying JDK JarURLConnection).
 */
public class UrlHelper {

  /**
   * Open the URL content without using caching returning the resulting InputStream.
   */
  public static InputStream openNoCache(URL url) throws IOException {

    URLConnection urlConnection = url.openConnection();
    urlConnection.setUseCaches(false);
    return urlConnection.getInputStream();
  }
}
