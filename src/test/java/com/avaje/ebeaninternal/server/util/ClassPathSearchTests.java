package com.avaje.ebeaninternal.server.util;

import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * This ensures that the ClassPathSearch supports normal file:file.jar files as well as jar/war files
 * with bang paths. Bang paths typically look like this as a url:
 *
 * jar:file:/path/to/file.war!/WEB-INF/classes
 *
 * author: Richard Vowles - http://plus.google.com/RichardVowles
 */
public class ClassPathSearchTests {

	private static final String WEB_INF_CLASSES = "WEB-INF/classes/";

	private void runAsserts(List<Class<?>> found, URLClassLoader cl, ClassPathSearch search,
	                        URL jar, URL jarBang) throws ClassNotFoundException {

		assert found.size() == 2;
		assert found.contains(cl.loadClass(SimpleJarBangClass.class.getName()));
		assert found.contains(cl.loadClass(SimpleJarClass.class.getName()));

		assert search.getJarHits().contains(new File(jar.getFile()).getName());
		assert search.getJarHits().contains(new File(jarBang.getFile().split("!")[0]).getName());
	}

	@Test
	public void ensureClassPathFindsClassesInBangPaths() throws IOException, ClassNotFoundException {
		URL jar = setupJar();
		URL jarBang = setupJarBang();

		URLClassLoader cl = new URLClassLoader(new URL[] { jar, jarBang});

		ClassPathSearchFilter filter = new ClassPathSearchFilter();
		filter.includePackage("com.avaje.ebeaninternal.server");
		filter.includeJar("WEB-INF");
		filter.setDefaultJarMatch(false);

		ClassPathSearch search = new ClassPathSearch(cl, filter, new ClassPathSearchMatcher() {
			@Override
			public boolean isMatch(Class<?> cls) {
				return true;
			}
		});

		List<Class<?>> found = search.findClasses();

		Assert.assertEquals(1, found.size());
		Assert.assertTrue(found.contains(cl.loadClass(SimpleJarBangClass.class.getName())));

		Assert.assertTrue(search.getJarHits().contains(new File(jarBang.getFile().split("!")[0]).getName()));

		filter = new ClassPathSearchFilter();
		filter.includePackage("com.avaje.ebeaninternal.server");
		filter.includeJar("bang");

		search = new ClassPathSearch(cl, filter, new ClassPathSearchMatcher() {
			@Override
			public boolean isMatch(Class<?> cls) {
				return true;
			}
		});

		runAsserts(search.findClasses(), cl, search, jar, jarBang);
	}

	private URL createJar(Class<?> clazz, File jarFile, String offset) throws IOException {
		FileOutputStream stream = new FileOutputStream(jarFile);
		JarOutputStream jarOutputStream = new JarOutputStream(stream);
		String clazzPath = clazz.getPackage().getName().replace(".", "/") + "/" + clazz.getSimpleName() + ".class";
		JarEntry entry = new JarEntry(offset + clazzPath);
		jarOutputStream.putNextEntry(entry);
		InputStream classStream = getClass().getResourceAsStream("/" + clazzPath);
		IOUtils.copy(classStream, jarOutputStream);

		if (offset != null) { // copy the same file in, with a different offset
			entry = new JarEntry("random/" + clazzPath);
			jarOutputStream.putNextEntry(entry);
			classStream = getClass().getResourceAsStream("/" + clazzPath);
			IOUtils.copy(classStream, jarOutputStream);
		}

		jarOutputStream.close();
		stream.close();

		if (offset.length() > 0) {
			return new URL("jar:" + jarFile.toURI().toString() + "!/" + offset);
		} else {
			return jarFile.toURI().toURL();
		}
	}

	private URL setupJarBang() throws IOException {
		File jarFile = File.createTempFile("bang", ".war");
		return createJar(SimpleJarBangClass.class, jarFile, WEB_INF_CLASSES);
	}

	private URL setupJar() throws IOException {
		File jarFile = File.createTempFile("nobang", ".jar");
		return createJar(SimpleJarClass.class, jarFile, "");
	}
}
