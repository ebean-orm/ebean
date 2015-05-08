package com.avaje.ebeaninternal.server.deploy;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.avaje.ebeaninternal.server.lib.resource.ResourceContent;
import com.avaje.ebeaninternal.server.lib.resource.ResourceSource;
import com.avaje.ebeaninternal.server.lib.util.Dnode;
import com.avaje.ebeaninternal.server.lib.util.DnodeReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls the creation and caching of BeanManager's, BeanDescriptors,
 * BeanTable etc for both beans and tables(MapBeans).
 * <p>
 * Also supports some other deployment features such as type conversion.
 * </p>
 */
public class DeployOrmXml {

	private static final Logger logger = LoggerFactory.getLogger(DeployOrmXml.class);

	private final HashMap<String, DNativeQuery> nativeQueryCache;

	private final ArrayList<Dnode> ormXmlList;

	private final ResourceSource resSource;
	
	public DeployOrmXml(ResourceSource resSource) {

		this.resSource = resSource;
		this.nativeQueryCache = new HashMap<String, DNativeQuery>();
		this.ormXmlList = findAllOrmXml();

		
		initialiseNativeQueries();
	}

	/**
	 * Register all the native queries in ALL orm xml deployment.
	 */
	private void initialiseNativeQueries() {
		for (Dnode ormXml : ormXmlList) {
			initialiseNativeQueries(ormXml);
		}
	}

	/**
	 * Register the native queries in this particular orm xml deployment.
	 */
	private void initialiseNativeQueries(Dnode ormXml) {

		Dnode entityMappings = ormXml.find("entity-mappings");
		if (entityMappings != null) {
			List<Dnode> nq = entityMappings.findAll("named-native-query", 1);
			for (int i = 0; i < nq.size(); i++) {
				Dnode nqNode = nq.get(i);
				Dnode nqQueryNode = nqNode.find("query");
				if (nqQueryNode != null) {
					String queryContent = nqQueryNode.getNodeContent();
					String queryName = (String) nqNode.getAttribute("name");

					if (queryName != null && queryContent != null) {
						DNativeQuery query = new DNativeQuery(queryContent);
						nativeQueryCache.put(queryName, query);
					}
				}
			}
		}
	}
	
	/**
	 * Return a native named query.
	 * <p>
	 * These are loaded from the orm.xml deployment file.
	 * </p>
	 */
	public DNativeQuery getNativeQuery(String name) {
		return nativeQueryCache.get(name);
	}

	private ArrayList<Dnode> findAllOrmXml() {

		ArrayList<Dnode> ormXmlList = new ArrayList<Dnode>();


		String defaultFile = "orm.xml";
		readOrmXml(defaultFile, ormXmlList);
		
		if (!ormXmlList.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (Dnode ox : ormXmlList) {
				sb.append(", ").append(ox.getAttribute("ebean.filename"));
			}
			String loadedFiles = sb.toString().substring(2);
			logger.info("Deployment xml [" + loadedFiles + "]  loaded.");
		}

		return ormXmlList;
	}

	private boolean readOrmXml(String ormXmlName, ArrayList<Dnode> ormXmlList) {

		try {
			Dnode ormXml = null;
			ResourceContent content = resSource.getContent(ormXmlName);
			if (content != null) {
				// servlet resource or file system...
				ormXml = readOrmXml(content.getInputStream());

			} else {
				// try the classpath...
				ormXml = readOrmXmlFromClasspath(ormXmlName);
			}

			if (ormXml != null) {
				ormXml.setAttribute("ebean.filename", ormXmlName);
				ormXmlList.add(ormXml);
				return true;

			} else {
				return false;
			}
		} catch (IOException e) {
			logger.error("error reading orm xml deployment " + ormXmlName, e);
			return false;
		}
	}

	private Dnode readOrmXmlFromClasspath(String ormXmlName) throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(ormXmlName);
		if (is == null) {
			return null;
		} else {
			return readOrmXml(is);
		}
	}

	private Dnode readOrmXml(InputStream in) throws IOException {
		DnodeReader reader = new DnodeReader();
		Dnode ormXml = reader.parseXml(in);
		in.close();
		return ormXml;
	}

	/**
	 * Find the deployment xml for a given entity. This will return null if no
	 * matching deployment xml is found for this entity.
	 * <p>
	 * This searches all the ormXml files and returns the first match.
	 * </p>
	 */
	public Dnode findEntityDeploymentXml(String className) {

		for (Dnode ormXml : ormXmlList) {
			Dnode entityMappings = ormXml.find("entity-mappings");

			List<Dnode> entities = entityMappings.findAll("entity", "class", className, 1);
			if (entities.size() == 1) {
				return entities.get(0);
			}
		}

		return null;
	}

}
