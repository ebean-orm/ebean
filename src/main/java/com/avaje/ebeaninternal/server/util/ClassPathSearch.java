package com.avaje.ebeaninternal.server.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.ClassDefinition;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebeaninternal.api.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	ClassLoader classLoader;

	Object[] classPaths;

	ClassPathSearchFilter filter;

	ClassPathSearchMatcher matcher;

	ArrayList<Class<?>> matchList = new ArrayList<Class<?>>();

  /**
   * For class-reloading, forcing them through the transformer
   */
  ArrayList<ClassDefinition> matchDefinitions = new ArrayList<ClassDefinition>();

	HashSet<String> jarHits = new HashSet<String>();

	HashSet<String> packageHits = new HashSet<String>();

	ClassPathReader classPathReader = new DefaultClassPathReader();
	
	public ClassPathSearch(ClassLoader classLoader, ClassPathSearchFilter filter, ClassPathSearchMatcher matcher) {
		this.classLoader = classLoader;
		this.filter = filter;
		this.matcher = matcher;
		initClassPaths();
	}

	private void initClassPaths() {
		
		try {
			
			String cn = GlobalProperties.get("ebean.classpathreader", null);
			if (cn != null){
				// use a user defined classPathReader
				logger.info("Using ["+cn+"] to read the searchable class path");
				classPathReader = (ClassPathReader)ClassUtil.newInstance(cn, this.getClass());
			}
			
			classPaths = classPathReader.readPath(classLoader);
				
			if (classPaths == null || classPaths.length == 0){
				String msg = "ClassPath is EMPTY using ClassPathReader ["+classPathReader+"]";
				logger.warn(msg);
			}
			
			boolean debug = GlobalProperties.getBoolean("ebean.debug.classpath", false);
			if (debug || logger.isTraceEnabled()) {
				String msg = "Classpath " + Arrays.toString(classPaths);
				logger.info(msg);
			}
			
		} catch (Exception e) {
			String msg = "Error trying to read the classpath entries";
			throw new RuntimeException(msg, e);
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
		if (pkg != null){
			packageHits.add(pkg.getName());			
		} else {
			packageHits.add("");
		}
	}

	/**
	 * Searches the class path for all matching classes.
	 */
	public List<Class<?>> findClasses() throws ClassNotFoundException {

		if (classPaths == null || classPaths.length == 0){
			// returning an empty list
			return matchList;
		}
		
		String charsetName = Charset.defaultCharset().name();

		for (int h = 0; h < classPaths.length; h++) {
			
			String jarFileName = null;
			Enumeration<?> files = null;
			JarFile module = null;

			// for each class path ...
			File classPath;
			String jarOffset = null; // used for war files with bang paths, e.g. !/WEB-INF/classes
			if (URL.class.isInstance(classPaths[h])){
				URL fileUrl = (URL)classPaths[h];
				if (fileUrl.getPath().contains("!")) {
					String[] parts = fileUrl.getPath().split("!");
					if (parts[0].startsWith("file:")) {  // jar:file:..../file.war!/WEB-INF/classes typically
						classPath = new File(parts[0].substring("file:".length()));
					} else {
						classPath = new File(parts[0]);
					}
					jarOffset = parts[1];
				} else {
					classPath = new File(fileUrl.getFile());
				}
			} else {
				classPath = new File(classPaths[h].toString());
			}
			
			try {
				// URL Decode the path replacing %20 to space characters.
				String path = URLDecoder.decode(classPath.getAbsolutePath(), charsetName);
				classPath = new File(path);
				
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}

			if (classPath.isDirectory()) {
				files = getDirectoryEnumeration(classPath);

			} else if (classPath.getName().endsWith(".jar") || classPath.getName().endsWith(".war")) {
				jarFileName = classPath.getName();

				// search name needs to include the offset if it is there, in case it contains interesting info
				if (!filter.isSearchJar(jarFileName + ((jarOffset == null) ? "" : ("!" + jarOffset)))) {
					// skip any jars not list in the filter
					continue;
				}
				try {
					// our resource is a jar
					module = new JarFile(classPath);
					files = module.entries();

				} catch (MalformedURLException ex) {
					throw new ClassNotFoundException("Bad classpath. Error: ", ex);

				} catch (IOException ex) {
					String msg = "jar file '" + classPath.getAbsolutePath()
							+ "' could not be instantiate from file path. Error: ";
					throw new ClassNotFoundException(msg, ex);
				}
			} else {
				// this is not expected
				String msg = "Error: expected classPath entry ["+classPath.getAbsolutePath()
				+"] to be a directory or a .jar file but it is not either of those?";
				logger.error(msg);
			}

			searchFiles(files, jarFileName, jarOffset);

			if (module != null) {
				try {
					// close the jar if it was used
					module.close();
				} catch (IOException e) {
					String msg = "Error closing jar";
					throw new ClassNotFoundException(msg, e);
				}
			}
		}
		
		if (matchList.isEmpty()){
			String msg = "No Entities found in ClassPath using ClassPathReader ["
				+classPathReader+"] Classpath Searched[" + Arrays.toString(classPaths)+"]";
			logger.warn(msg);
		}

		return matchList;
	}

	private Enumeration<?> getDirectoryEnumeration(File classPath) {

		// list of file names (latter checked as Classes)
		ArrayList<String> fileNameList = new ArrayList<String>();

		Set<String> includePkgs = filter.getIncludePackages();
		if (includePkgs.size() > 0) {
			// just search the relevant directories based on the
			// list of included packages
			Iterator<String> it = includePkgs.iterator();
			while (it.hasNext()) {
				String pkg = it.next();
				String relPath = pkg.replace('.', '/');
				File dir = new File(classPath, relPath);
				if (dir.exists()) {
					recursivelyListDir(fileNameList, dir, new StringBuilder(relPath));
				}
			}

		} else {
			// get a recursive listing of this classpath
			recursivelyListDir(fileNameList, classPath, new StringBuilder());
		}

		return Collections.enumeration(fileNameList);
	}

	/**
	 * Searches through the Java Archive (jar or war file) looking for classes that match our requirements.
	 *
	 * @param files - all of the files in the Java Archive, this is an enumeration provided by the Jar file
	 * @param jarFileName - the name of the java archive
	 * @param jarOffset - an offset inside the archive to chop off the name of the class - this is used when
	 *                  we have bang path offsets (e.g. file:///myfile.war!/WEB-INF/classes)
	 */
	private void searchFiles(Enumeration<?> files, String jarFileName, String jarOffset) {
		/*
		* Strips the first character off as all entries in a jar file have no / prefix. We want to come out with a name
		* like WEB-INF/classes/ to ensure we filter the contents of the war/jar file by this.
		 */
		if (jarOffset != null) {
			if (jarOffset.startsWith("/")) {
				jarOffset = jarOffset.substring(1);
			}

			if (!jarOffset.endsWith("/")) {
				jarOffset += "/";
			}
		}

		while (files != null && files.hasMoreElements()) {

			String fileName = files.nextElement().toString();

			// we only want the class files
			if (fileName.endsWith(".class") && (jarOffset == null || fileName.startsWith(jarOffset))) {

				if (jarOffset != null) {
					// we got through here only if there is an offset and we matched it, so strip it off the file
					// as we are trying to find the classname
					fileName = fileName.substring(jarOffset.length());
				}

				String className = fileName.replace('/', '.').substring(0, fileName.length() - 6);
				int lastPeriod = className.lastIndexOf(".");
				
				String pckgName;
				if (lastPeriod > 0){
					pckgName = className.substring(0, lastPeriod);
				} else {
					pckgName = "";
				}

				if (!filter.isSearchPackage(pckgName)) {
					continue;
				}

				// get the class for our class name
				Class<?> theClass = null;
				try {
					theClass = Class.forName(className, false, classLoader);

					if (matcher.isMatch(theClass)) {

						matchList.add(theClass);
						registerHit(jarFileName, theClass);
					}

				} catch (ClassNotFoundException e) {
					// expected to get this hence finer
					logger.trace("Error searching classpath" + e.getMessage());
					continue;

				} catch (NoClassDefFoundError e) {
					// expected to get this hence finer
					logger.trace("Error searching classpath: " + e.getMessage());
					continue;
				}
			}
		}
	}

	private void recursivelyListDir(List<String> fileNameList, File dir, StringBuilder relativePath) {

		int prevLen;

		if (dir.isDirectory()) {
			File[] files = dir.listFiles();

			for (int i = 0; i < files.length; i++) {
				// store our original relative path string length
				prevLen = relativePath.length();
				relativePath.append(prevLen == 0 ? "" : "/").append(files[i].getName());

				recursivelyListDir(fileNameList, files[i], relativePath);

				// delete sub directory from our relative path
				relativePath.delete(prevLen, relativePath.length());
			}
		} else {
			// add class fileName to the list
			fileNameList.add(relativePath.toString());
		}
	}
}
