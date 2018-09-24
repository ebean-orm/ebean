package io.ebeaninternal.server.idgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IdGenerator for java util UUID.
 *
 * It extends the UuidV1RndIdGenerator so that it can generate rfc4122 compliant Type 1 UUIDs.
 *
 * This generator produces real Type 1 UUIDs (best for sqlserver) - You should use this generator only,
 * if you can guarantee, that mac addess is uniqe.
 */
public class UuidV1IdGenerator extends UuidV1RndIdGenerator {

  private static final Logger logger = LoggerFactory.getLogger("io.ebean.IDGEN");

  private final File stateFile;

  private byte[] nodeId = null;

  private boolean canSaveState = true;

  private static final Map<File, UuidV1IdGenerator> INSTANCES = new ConcurrentHashMap<>();

  /**
   * Returns an instance for given file.
   */
  public static UuidV1IdGenerator getInstance(String file) {
    return INSTANCES.computeIfAbsent(new File(file), UuidV1IdGenerator::new);
  }

  /**
   * Returns an instance for given file.
   */
  public static UuidV1IdGenerator getInstance(File file) {
    return INSTANCES.computeIfAbsent(file, UuidV1IdGenerator::new);
  }

  /**
   * Returns an alternative node id - set with the 'ebean.uuid.nodeId' system property.
   */
  private static byte[] getAlternativeNodeId() {
    try {
      String altNodeId = System.getProperty("ebean.uuid.nodeId");
      if (altNodeId != null) {
        String[] components = altNodeId.split("-");
        if (components.length != 5) {
          throw new IllegalArgumentException("Invalid nodeId string: " + altNodeId);
        }
        byte[] nodeId = new byte[6];
        for (int i=0; i<5; i++) {
          nodeId[i] = Byte.decode("0x"+components[i]).byteValue();
        }
        return nodeId;
      }
    } catch (SecurityException se) {
      // ignore
    }
    return null;
  }

  /**
   * Find hardware ID.
   */
  private static byte[] getHardwareId() throws SocketException {
    final Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
    while (e.hasMoreElements()) {
      NetworkInterface network = e.nextElement();
      try {
        logger.trace("Probing interface {}", network);
        if (!network.isLoopback()) {
          byte[] addr = network.getHardwareAddress();
          if (addr != null) {
            logger.debug("Using interface {}", network);
            return addr;
          }
        }
      } catch (SocketException ex) {
        logger.debug("Skipping {}", network, ex);
      }
    }
    return null;
  }

  /**
   * Creates a new instance of UuidGenerator. Note that there should not be more
   * than one instance per stateFile.
   */
  private UuidV1IdGenerator(final File stateFile) {
    super();
    this.stateFile = stateFile;
    try {
      // See, if there is an alternative MAC address set.
      nodeId = getAlternativeNodeId();
      if (nodeId != null) {
        logger.info("Using alternative MAC {} to generate Type 1 UUIDs", getNodeIdentifier());
      } else {
        nodeId = getHardwareId();
        logger.info("Using MAC {} to generate Type 1 UUIDs", getNodeIdentifier());
      }
      if (nodeId == null) {
        canSaveState = false;
        // RFC 4.5 use random portion for node
        nodeId = super.getNodeIdBytes();
        logger.error("Have to fall back to random node identifier {} (Reason: No suitable network interface found)", getNodeIdentifier());

      } else {
        boolean flag = restoreState();
        UUID uuid = nextId(null);
        long ts = timeStamp.get();
        ts -= UUID_EPOCH_OFFSET;
        ts /= MILLIS_TO_UUID;

        saveState();
        logger.debug("RestoreState: {}, ClockSeq {}, Timestamp {}, uuid {}, stateFile: {})", flag, clockSeq.get(),
            new Date(ts), uuid, stateFile);
      }
    } catch (IOException e) {
      canSaveState = false;
      // RFC 4.5 use random portion for node
      nodeId = super.getNodeIdBytes();
      logger.error("Have to fall back to random node identifier {} (Reason: {} )", getNodeIdentifier(), e.getMessage());
    }
  }

  /**
   * Returns the Node-identifier (=MAC address) as string
   */
  public String getNodeIdentifier() {
    if (nodeId == null) {
      return "none";
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < nodeId.length; i++) {
      sb.append(String.format("%02X%s", nodeId[i], (i < nodeId.length - 1) ? "-" : ""));
    }
    return sb.toString();
  }

  /**
   * Restores the state from the state file.
   */
  private boolean restoreState() throws IOException {
    Properties prop = new Properties();
    if (stateFile.exists()) {
      try (InputStream is = new FileInputStream(stateFile)) {
        prop.load(is);
      }
    }
    if (getNodeIdentifier().equals(prop.getProperty("nodeId"))) {
      try {
        Integer seq = Integer.valueOf(prop.getProperty("clockSeq")) & 0x3FFF;
        Long ts = Long.valueOf(prop.getProperty("timeStamp"));
        clockSeq.set(seq);
        timeStamp.set(ts);
        logger.debug("Restored state from '{}'", stateFile);
        return true;
      } catch (NumberFormatException nfe) {
        // nop
      }
    }
    return false;
  }

  /**
   * Saves the state to the state file;
   */
  @Override
  protected void saveState() {
    if (!canSaveState) {
      return;
    }
    Properties prop = new Properties();
    prop.setProperty("nodeId", getNodeIdentifier());
    prop.setProperty("clockSeq", String.valueOf(clockSeq.get()));
    prop.setProperty("timeStamp", String.valueOf(timeStamp.get()));
    File dir = stateFile.getParentFile();
    if (dir != null) {
      dir.mkdirs();
    }
    try (OutputStream os = new FileOutputStream(stateFile)) {
      prop.store(os, "ebean uuid state file");
      logger.debug("Persisted state to '{}'", stateFile);
    } catch (IOException e) {
      logger.error("Could not persist uuid state to '{}'", stateFile, e);
    }
  }

  @Override
  protected byte[] getNodeIdBytes() {
    return nodeId;
  }

}
