package com.avaje.ebeaninternal.server.util;

import com.avaje.ebeaninternal.api.ClassPathSearchService;
import com.avaje.ebeaninternal.api.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.loader.jar.JarEntryData;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
public class ClassPathSearch implements ClassPathSearchService {

  private static final Logger logger = LoggerFactory.getLogger(ClassPathSearch.class);

  private ClassLoader classLoader;

  private final List<Object> classPath = new ArrayList<Object>();

  private ClassPathSearchFilter filter;

  private ClassPathSearchMatcher matcher;

  private final ArrayList<Class<?>> matchList = new ArrayList<Class<?>>();

  private final HashSet<String> jarHits = new HashSet<String>();

  private final HashSet<String> packageHits = new HashSet<String>();

  private ClassPathReader classPathReader = new DefaultClassPathReader();

  private final ArrayList<URI> scannedUris = new ArrayList<URI>();

  public ClassPathSearch() {
    // Default Construct
  }

  @Override
  public void init(ClassLoader classLoader, ClassPathSearchFilter filter, ClassPathSearchMatcher matcher, String classPathReaderClassName) {
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
          logger.debug("Classpath Entry: {}", entry);
        }
      }

    } catch (Exception e) {
      throw new RuntimeException("Error trying to read the classpath entries", e);
    }
  }

  /**
   * Return the set of jars that contained classes that matched.
   */
  @Override
  public Set<String> getJarHits() {
    return jarHits;
  }

  /**
   * Return the set of packages that contained classes that matched.
   */
  @Override
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
  @Override
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
        if (classPathSize == 1 || filter.isSearchJar(element.getJarNameWithOffset(), element.getJarOffset())) {
          scanJar(element);
        }

      } else {
        logger.error("Error: expected classPath entry [" + element + "] to be a directory or a .jar file but it is not either of those?");
      }
    }

    if (matchList.isEmpty()) {
      logger.warn("No Entities found in ClassPath using ClassPathReader [" + classPathReader + "] Classpath Searched[" + classPath + "]");
    }

    return matchList;
  }

  private ClassPathElement getClassPathElement(Object classPathEntry) throws MalformedURLException {

    URL fileUrl;

    if (URI.class.isInstance(classPathEntry)) {
      fileUrl = ((URI) classPathEntry).toURL();

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
    searchFiles(Collections.enumeration(directoryFiles), null, null, null);
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

      logger.trace("scanJar file:{}", file);

      List<URI> classPathFromManifest = getClassPathFromManifest(file, module.getManifest());
      for (URI uri : classPathFromManifest) {
        scanUri(uri);
      }

      searchFiles(module.entries(), classPathEntry.getJarName(), classPathEntry.jarOffset, file);

    } catch (MalformedURLException ex) {
      throw new IOException("Bad classpath error: ", ex);

    } finally {
      if (module != null) {
        try {
          // close the jar if it was used
          module.close();
        } catch (IOException e) {
          logger.error("Error closing jar", e);
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
   * @param entries       - all of the entries in the Java Archive, this is an enumeration
   *                    provided by the Jar file
   * @param jarFileName - the name of the java archive
   * @param jarOffset   - an offset inside the archive to chop off the name of the class -
   *                    this is used when we have bang path offsets (e.g.
   * @param module the containing jar/war file (used for spring boot embedded jar scanning)
   */
  private void searchFiles(Enumeration<?> entries, String jarFileName, String jarOffset, File module) {

    if (entries == null) {
      return;
    }

    logger.debug("searchFiles jarFileName:{}  jarOffset:{}", jarFileName, jarOffset);

    // Strips the first character off as all entries in a jar file have no /
    // prefix. We want to come out with a name like WEB-INF/classes/ to ensure
    // we filter the contents of the war/jar file by this.

    if ("/".equals(jarOffset)) {
      // root level for runnable jar (spring boot etc)
      jarOffset = null;

    } else if (jarOffset != null) {
      if (jarOffset.startsWith("/")) {
        jarOffset = jarOffset.substring(1);
      }

      if (!jarOffset.endsWith("/")) {
        jarOffset += "/";
      }
    }

    while (entries.hasMoreElements()) {

      Object element = entries.nextElement();
      String entryName = element.toString();

      if (isEntryEmbeddedJar(module, entryName)) {
        scanSpringBootEmbeddedJar(jarFileName, module, entryName);
      }

      if (isEntryClass(jarOffset, entryName)) {
        // check if it an 'interesting' class - entity etc
        registerScannedClass(jarFileName, jarOffset, entryName);
      }
    }
  }

  /**
   * Return true if this is an embedded jar that should be scanned.
   */
  private boolean isEntryEmbeddedJar(File module, String entryName) {
    return entryName.endsWith(".jar") && module != null && filter.isSearchJar(entryName, null);
  }

  /**
   * Return true if this is a class that should be checked (for entity, interesting interface etc).
   */
  private boolean isEntryClass(String jarOffset, String entryName) {
    return entryName.endsWith(".class") && (jarOffset == null || entryName.startsWith(jarOffset));
  }

  private void scanSpringBootEmbeddedJar(String jarFileName, File module, String fileName) {
    // spring boot embedded jar
    logger.debug("spring boot embedded:{} : module:{}", fileName, module.getAbsoluteFile());
    try {
      org.springframework.boot.loader.jar.JarFile jarFile = new org.springframework.boot.loader.jar.JarFile(module);
      org.springframework.boot.loader.jar.JarFile jarEntryFile = jarFile.getNestedJarFile(jarFile.getJarEntryData(fileName));
      Iterator<JarEntryData> iterator = jarEntryFile.iterator();
      while (iterator.hasNext()) {
        JarEntryData jarEntryData = iterator.next();
        if (jarEntryData.getName().toString().endsWith(".class")) {
          logger.debug("... spring boot class entry:{}", jarEntryData.getName().toString());
          registerScannedClass(jarFileName, null, jarEntryData.getName().toString());
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void registerScannedClass(String jarFileName, String jarOffset, String fileName) {

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
        File file = new File(((URL) classPathElement).getFile());
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

    String getJarOffset() {
      return jarOffset;
    }
  }

}