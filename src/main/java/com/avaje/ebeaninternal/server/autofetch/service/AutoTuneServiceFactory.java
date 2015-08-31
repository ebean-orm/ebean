package com.avaje.ebeaninternal.server.autofetch.service;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.autofetch.AutoTuneService;
import com.avaje.ebeaninternal.server.resource.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class AutoTuneServiceFactory {

  private static final Logger logger = LoggerFactory.getLogger(AutoTuneServiceFactory.class);

  public static AutoTuneService create(SpiEbeanServer server, ServerConfig serverConfig, ResourceManager resourceManager) {

    AutoTuneServiceFactory me = new AutoTuneServiceFactory();
    return me.createAutoFetchManager(server, serverConfig, resourceManager);
  }

  private AutoTuneService createAutoFetchManager(SpiEbeanServer server, ServerConfig serverConfig, ResourceManager resourceManager) {

    AutoTuneService manager = createAutoFetchManager(server.getName(), resourceManager);
    manager.setOwner(server, serverConfig);

    return manager;
  }

  private AutoTuneService createAutoFetchManager(String serverName, ResourceManager resourceManager) {

    File autoFetchFile = getAutoFetchFile(serverName, resourceManager);

    AutoTuneService autoFetchManager = null;

    boolean readFile = !"false".equalsIgnoreCase(System.getProperty("autofetch.readfromfile"));
    if (readFile) {
      autoFetchManager = deserializeAutoFetch(autoFetchFile);
    }

    if (autoFetchManager == null) {
      // not deserialized from file so create as empty
      // It will be populated automatically by querying the
      // database meta data
      autoFetchManager = new BaseAutoTuneService(autoFetchFile.getAbsolutePath());
    }

    return autoFetchManager;
  }

  private AutoTuneService deserializeAutoFetch(File autoFetchFile) {
    try {

      if (!autoFetchFile.exists()) {
        return null;
      }
      FileInputStream fi = new FileInputStream(autoFetchFile);
      ObjectInputStream ois = new ObjectInputStream(fi);
      AutoTuneService profListener = (AutoTuneService) ois.readObject();
      ois.close();

      logger.info("AutoFetch deserialized from file [" + autoFetchFile.getAbsolutePath() + "]");

      return profListener;

    } catch (Exception ex) {
      logger.error("Error loading autofetch file " + autoFetchFile.getAbsolutePath(), ex);
      return null;
    }
  }

  /**
   * Return the file name of the autoFetch meta data.
   */
  private File getAutoFetchFile(String serverName, ResourceManager resourceManager) {

    String fileName = ".ebean." + serverName + ".autofetch";

    File dir = resourceManager.getAutofetchDirectory();

    if (!dir.exists()) {
      // automatically create the directory if it does not exist.
      // this is probably a fairly reasonable thing to do
      if (!dir.mkdirs()) {
        String m = "Unable to create directory [" + dir + "] for autofetch file [" + fileName + "]";
        throw new PersistenceException(m);
      }
    }

    return new File(dir, fileName);
  }

}
