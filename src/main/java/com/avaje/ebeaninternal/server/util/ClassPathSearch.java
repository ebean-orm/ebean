package com.avaje.ebeaninternal.server.util;

import com.avaje.ebeaninternal.api.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Can search the class path for classes using a ClassPathSearchMatcher. A
 * ClassPathSearch should only be used once in a single threaded manor. It is
 * not safe for multithreaded use.
 * <p>
 * For example, used to find all the Entity beans and ScalarTypes for Ebean.
 * </p>
 */
public class ClassPathSearch {

  private static final Logger logger = LoggerFactory.getLogger(ClassPathSearch.class);

  private ClassLoader classLoader;

  private List<Object> classPath = new ArrayList<Object>();
  
  private ClassPathSearchFilter filter;

  private ClassPathSearchMatcher matcher;

  private ArrayList<Class<?>> matchList = new ArrayList<Class<?>>();

  private HashSet<String> jarHits = new HashSet<String>();

  private HashSet<String> packageHits = new HashSet<String>();

  private ClassPathReader classPathReader = new DefaultClassPathReader();

  private ArrayList<URI> scannedUris = new ArrayList<URI>();

  public ClassPathSearch(ClassLoader classLoader, ClassPathSearchFilter filter, ClassPathSearchMatcher matcher, String classPathReaderClassName) {
    this.classLoader = classLoader;
    this.filter = filter;
    this.matcher = matcher;
    initClassPaths(classPathReaderClassName);
  }

  private void initClassPaths(String classPathReaderCN) {

    try {

      if (classPathReaderCN != null) {
        // use a user defined classPathReader
        logger.info("Using [" + classPathReaderCN + "] to read the searchable class path");
        classPathReader = (ClassPathReader) ClassUtil.newInstance(classPathReaderCN, this.getClass());
      }

      Object[] rawClassPaths = classPathReader.readPath(classLoader);

      if (rawClassPaths == null || rawClassPaths.length == 0) {
        logger.warn("ClassPath is EMPTY using ClassPathReader [" + classPathReader + "]");
        return;
      } 

      for (int i = 0; i < rawClassPaths.length; i++) {
        // check for a jarfile with a manifest classpath (e.g. maven surefire)
        List<URI> classPathFromManifest = getClassPathFromManifest(rawClassPaths[i]);
        if (classPathFromManifest.isEmpty()) {
          classPath.add(rawClassPaths[i]);
        } else {
          classPath.addAll(classPathFromManifest);
        }
      }
      
      if (rawClassPaths.length == 1) {
        // look to add an 'outer' jar when it contains a manifest classpath
        if (!classPath.contains(rawClassPaths[0])) {
          classPath.add(rawClassPaths[0]);
        }
      }
      
      if (logger.isDebugEnabled()) {
        for (Object entry : classPath) {
          logger.debug("Classpath Entry: {}",entry);
        }
      }

    } catch (Exception e) {
      throw new RuntimeException("Error trying to read the classpath entries", e);
    }
  }

  /**
   * Return the set of jars that contained classes that matched.
   */
  public Set<String> getJarHits() {
    return jarHits;
  }

  /**
   * Return the set of packages that contained classes that matched.
   */
  public Set<String> getPackageHits() {
    return packageHits;
  }

  /**
   * Register where matching classes where found.
   * <p>
   * Could use this info to speed up future searches.
   * </p>
   */
  private void registerHit(String jarFileName, Class<?> cls) {
    if (jarFileName != null) {
      jarHits.add(jarFileName);
    }
    Package pkg = cls.getPackage();
    if (pkg != null) {
      packageHits.add(pkg.getName());
    } else {
      packageHits.add("");
    }
  }

  /**
   * Searches the class path for all matching classes.
   */
  public List<Class<?>> findClasses() throws IOException {

    if (classPath.isEmpty()) {
      // returning an empty list
      return matchList;
    }

    int classPathSize = classPath.size();
    for (int i = 0; i < classPathSize; i++) {

      ClassPathElement element = getClassPathElement(classPath.get(i));

      if (element.isDirectory()) {
        scanDirectory(element);

      } else if (element.isJarOrWar()) {
        // search name including the ! offset if it is there
        if (classPathSize == 1 || filter.isSearchJar(element.getJarNameWithOffset())) {
          scanJar(element);
        }

      } else {
        logger.error("Error: expected classPath entry [" + element+ "] to be a directory or a .jar file but it is not either of those?");
      }
    }

    if (matchList.isEmpty()) {
      logger.warn("No Entities found in ClassPath using ClassPathReader [" + classPathReader + "] Classpath Searched[" + classPath + "]");
    }

    return matchList;
  }

  private ClassPathElement getClassPathElement(Object classPathEntry) throws MalformedURLException {

    URL fileUrl = null;
    
    if (URI.class.isInstance(classPathEntry)) {
      fileUrl = ((URI)classPathEntry).toURL();
      
    } else if (!URL.class.isInstance(classPathEntry)) {
      // assumed to be a file path
      return new ClassPathElement(classPathEntry.toString());
    
    } else {
      fileUrl = (URL) classPathEntry;
    }
    
    if (!fileUrl.getPath().contains("!")) {
      return new ClassPathElement(new File(fileUrl.getFile()));
    }

    // jar:file:..../file.war!/WEB-INF/classes typically
    String[] parts = fileUrl.getPath().split("!");
    String fileName = parts[0];
    String jarOffset = parts[1];
    if (fileName.startsWith("file:")) {
      fileName = fileName.substring("file:".length());
    }
    return new ClassPathElement(new File(fileName), jarOffset);
  }

  private void scanDirectory(ClassPathElement classPathEntry) {
    scanDirectory(classPathEntry.classPath);
  }

  private void scanDirectory(File directory) {
    List<String> directoryFiles = getDirectoryFiles(directory);
    searchFiles(Collections.enumeration(directoryFiles), null, null);
  }

  private void scanUri(URI uri) throws IOException {

    if (uri.getScheme().equals("file") && scannedUris.add(uri)) {
      File file = new File(uri);
      if (file.exists()) {
        if (file.isDirectory()) {
          scanDirectory(file);
        } else {
          scanJar(new ClassPathElement(file));
        }
      }
    }
  }


  private void scanJar(ClassPathElement classPathEntry) throws IOException {

    JarFile module = null;
    try {
      // our resource is a jar
      File file = classPathEntry.classPath;
      module = new JarFile(file);

      List<URI> classPathFromManifest = getClassPathFromManifest(file, module.getManifest());
      for (URI uri : classPathFromManifest) {
        scanUri(uri);
      }
      
      searchFiles(module.entries(), classPathEntry.getJarName(), classPathEntry.jarOffset);

    } catch (MalformedURLException ex) {
      throw new IOException("Bad classpath error: ", ex);

    } finally {
      if (module != null) {
        try {
          // close the jar if it was used
          module.close();
        } catch (IOException e) {
          throw new IOException("Error closing jar", e);
        }
      }
    }
  }

  private List<String> getDirectoryFiles(File classPath) {

    // list of file names (latter checked as Classes)
    ArrayList<String> fileNameList = new ArrayList<String>();

    Set<String> includePkgs = filter.getIncludePackages();
    if (includePkgs.size() > 0) {
      // just search the relevant directories based on the
      // list of included packages
      for (String pkg : includePkgs) {
        String relativePath = pkg.replace('.', '/');
        File dir = new File(classPath, relativePath);
        if (dir.exists()) {
          recursivelyListDir(fileNameList, dir, new StringBuilder(relativePath));
        }
      }

    } else {
      // get a recursive listing of this classPath
      recursivelyListDir(fileNameList, classPath, new StringBuilder());
    }

    return fileNameList;
  }

  /**
   * Searches through the Java Archive (jar or war file) looking for classes
   * that match our requirements.
   * 
   * @param files
   *          - all of the files in the Java Archive, this is an enumeration
   *          provided by the Jar file
   * @param jarFileName
   *          - the name of the java archive
   * @param jarOffset
   *          - an offset inside the archive to chop off the name of the class -
   *          this is used when we have bang path offsets (e.g.
   *          file:///myfile.war!/WEB-INF/classes)
   */
  private void searchFiles(Enumeration<?> files, String jarFileName, String jarOffset) {

    if (files == null) {
      return;
    }
    /*
     * Strips the first character off as all entries in a jar file have no /
     * prefix. We want to come out with a name like WEB-INF/classes/ to ensure
     * we filter the contents of the war/jar file by this.
     */
    if (jarOffset != null) {
      if (jarOffset.startsWith("/")) {
        jarOffset = jarOffset.substring(1);
      }

      if (!jarOffset.endsWith("/")) {
        jarOffset += "/";
      }
    }

    while (files.hasMoreElements()) {

      String fileName = files.nextElement().toString();

      // we only want the class files
      if (fileName.endsWith(".class") && (jarOffset == null || fileName.startsWith(jarOffset))) {

        if (jarOffset != null) {
          // we got through here only if there is an offset and we
          // matched it, so strip it off the file
          // as we are trying to find the className
          fileName = fileName.substring(jarOffset.length());
        }

        String className = fileName.replace('/', '.').substring(0, fileName.length() - 6);
        int lastPeriod = className.lastIndexOf(".");

        String pckgName;
        if (lastPeriod > 0) {
          pckgName = className.substring(0, lastPeriod);
        } else {
          pckgName = "";
        }

        if (filter.isSearchPackage(pckgName)) {
          // get the class for our class name
          try {
            Class<?> theClass = Class.forName(className, false, classLoader);

            if (matcher.isMatch(theClass)) {
              matchList.add(theClass);
              registerHit(jarFileName, theClass);
            }

          } catch (ClassNotFoundException e) {
            // expected to get this hence trace
            logger.trace("Error searching classpath" + e.getMessage());
          } catch (NoClassDefFoundError e) {
            // expected to get this hence trace
            logger.trace("Error searching classpath" + e.getMessage());
          }
        }
      }
    }
  }

  private void recursivelyListDir(List<String> fileNameList, File dir, StringBuilder relativePath) {

    if (!dir.isDirectory()) {
      // add class fileName to the list
      fileNameList.add(relativePath.toString());

    } else {

      File[] files = dir.listFiles();
      for (int i = 0; i < files.length; i++) {
        // store our original relative path string length
        int prevLen = relativePath.length();
        relativePath.append(prevLen == 0 ? "" : "/").append(files[i].getName());

        recursivelyListDir(fileNameList, files[i], relativePath);

        // delete sub directory from our relative path
        relativePath.delete(prevLen, relativePath.length());
      }
    }
  }

  /**
   * If URL and actually a jarfile with manifest return the derived classpath.
   */
  private static List<URI> getClassPathFromManifest(Object classPathElement) {

    try {
      if (classPathElement instanceof URL) {
        File file = new File(((URL)classPathElement).getFile());
        if (file.isDirectory()) {
          return Collections.emptyList();
        }
        JarFile jarFile = new JarFile(file);
        try {
          return getClassPathFromManifest(file, jarFile.getManifest());
        } finally {
          jarFile.close();
        }
      }
      return Collections.emptyList();
      
    } catch (IOException e) {
      return Collections.emptyList();
    }
  }
  
  /**
   * If a jarfile with a manifest claspath return that.
   */
  private static List<URI> getClassPathFromManifest(File jarFile, Manifest manifest) {

    if (manifest == null) {
      return Collections.emptyList();
    }
    List<URI> list = new ArrayList<URI>();
    String classpathAttribute = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH.toString());

    if (classpathAttribute != null) {
      String[] split = classpathAttribute.split(" ");
      for (String path : split) {
        try {
          path = path.trim();
          if (path.length() > 0) {
            URI uri = getClassPathEntry(jarFile, path);
            list.add(uri);
          }
        } catch (URISyntaxException e) {
          // Ignore bad entry
          logger.warn("Invalid Class-Path entry: " + path);
        }
      }
    }
    return list;
  }

  private static URI getClassPathEntry(File jarFile, String path) throws URISyntaxException {
    URI uri = new URI(path);
    if (uri.isAbsolute()) {
      return uri;
    } else {
      return new File(jarFile.getParentFile(), path.replace('/', File.separatorChar)).toURI();
    }
  }

  private static File decodePath(File classPath) {

    try {
      String charsetName = Charset.defaultCharset().name();

      // URL Decode the path replacing %20 to space characters.
      String path = URLDecoder.decode(classPath.getAbsolutePath(), charsetName);
      return new File(path);

    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Element that has both underlying file and ! jarOffset.
   */
  private static class ClassPathElement {

    private final File classPath;
    private final String jarOffset;

    ClassPathElement(String path) {
      this(new File(path));
    }

    ClassPathElement(File file) {
      this(file, null);
    }

    ClassPathElement(File file, String jarOffset) {
      classPath = decodePath(file);
      this.jarOffset = jarOffset;
    }

    public String toString() {
      return classPath.getAbsolutePath();
    }

    boolean isDirectory() {
      return classPath.isDirectory();
    }

    boolean isJarOrWar() {
      return classPath.getName().endsWith(".jar") || classPath.getName().endsWith(".war");
    }

    String getJarName() {
      return classPath.getName();
    }

    String getJarNameWithOffset() {
      return (jarOffset == null) ? classPath.getName() : classPath.getName() + "!" + jarOffset;
    }
  }
}