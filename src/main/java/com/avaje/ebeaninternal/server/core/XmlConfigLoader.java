package com.avaje.ebeaninternal.server.core;

import com.avaje.ebeaninternal.server.lib.util.Dnode;
import com.avaje.ebeaninternal.server.lib.util.DnodeReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Used to read the orm.xml and ebean-orm.xml configuration files.
 *
 * @author rbygrave
 * @author Richard Vowles - http://plus.google.com/RichardVowles
 */
public class XmlConfigLoader {

	private static final Logger logger = LoggerFactory.getLogger(XmlConfigLoader.class);

	private final ClassLoader classLoader;

	public XmlConfigLoader(ClassLoader classLoader) {

		if (classLoader == null) {
			classLoader = getClass().getClassLoader();
		}

		this.classLoader = classLoader;
	}

	public XmlConfig load() {
		List<Dnode> ormXml = search("META-INF/orm.xml");
		List<Dnode> ebeanOrmXml = search("META-INF/ebean-orm.xml");

		return new XmlConfig(ormXml, ebeanOrmXml);
	}

	public List<Dnode> search(String resourceName) {
		ArrayList<Dnode> xmlList = new ArrayList<Dnode>();

		try {
			Enumeration<URL> resources = classLoader.getResources(resourceName);

			while (resources.hasMoreElements()) {
				URL url = resources.nextElement();

				InputStream is = url.openStream();
				processInputStream(xmlList, is);
				is.close();
			}
		} catch (IOException e) {
			logger.error("Unable to find resources {}", resourceName);
		}

		return xmlList;
	}

	private void processInputStream(ArrayList<Dnode> xmlList, InputStream is) throws IOException {

		DnodeReader reader = new DnodeReader();
		Dnode xmlDoc = reader.parseXml(is);
		is.close();

		xmlList.add(xmlDoc);
	}
}
