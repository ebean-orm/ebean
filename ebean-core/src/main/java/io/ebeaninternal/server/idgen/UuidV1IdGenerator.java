package io.ebeaninternal.server.idgen;

import java.io.*;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.Logger.Level.*;

/**
 * IdGenerator for java util UUID.
 * <p>
 * It extends the UuidV1RndIdGenerator so that it can generate rfc4122 compliant Type 1 UUIDs.
 * <p>
 * This generator produces real Type 1 UUIDs (best for sqlserver) - You should use this generator only,
 * if you can guarantee, that mac addess is uniqe or specify 'ebean.uuidNodeId' in your config file.
 */
public class UuidV1IdGenerator extends UuidV1RndIdGenerator {

  private static final Map<File, UuidV1IdGenerator> INSTANCES = new ConcurrentHashMap<>();

  private final File stateFile;
  private byte[] nodeId;
  private boolean canSaveState = true;

  /**
   * Returns an instance for given file.
   */
  public static UuidV1IdGenerator getInstance(String file, String nodeId) {
    return getInstance(new File(file), nodeId);
  }

  /**
   * Returns an instance for given file.
   */
  public static UuidV1IdGenerator getInstance(File file, String nodeId) {
    return INSTANCES.computeIfAbsent(file, f -> new UuidV1IdGenerator(f, nodeId == null ? null : nodeId.toLowerCase()));
  }

  /**
   * Returns an alternative node id - set with the 'ebean.uuid.nodeId' system property.
   */
  private static byte[] parseAlternativeNodeId(String altNodeId) {
    String[] components = altNodeId.split("-");
    if (components.length != 6) {
      throw new IllegalArgumentException(altNodeId + " is invalid. Expected format: xx-xx-xx-xx-xx-xx");
    }
    try {
      byte[] nodeId = new byte[6];
      for (int i = 0; i < 6; i++) {
        // do not use Byte.parseByte
        // https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6259307
        nodeId[i] = (byte) Integer.parseInt(components[i], 16);
      }
      return nodeId;
    } catch (IllegalArgumentException iae) {
      throw new IllegalArgumentException(altNodeId + " is invalid.", iae);
    }
  }

  /**
   * Find hardware ID.
   * <p>
   * We check all non-loopback networkinterfaces for valid hardwareAddresses
   * We prefer to take the first non virtual "up" device. If no device (with valid addr) is up,
   * we take the first down device.
   */
  private static byte[] getHardwareId() throws SocketException {
    final Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
    byte[] fallbackAddr = null;
    while (e.hasMoreElements()) {
      NetworkInterface network = e.nextElement();
      try {
        if (!network.isLoopback()) {
          byte[] addr = network.getHardwareAddress();
          if (validAddr(addr)) {
            if (network.isUp() && !network.isVirtual()) {
              return addr;
            } else if (fallbackAddr == null) {
              fallbackAddr = addr;
            }
          }
        }
      } catch (SocketException ex) {
        log.log(DEBUG, "Skipping " + network, ex);
      }
    }
    return fallbackAddr;
  }

  /**
   * checks if addr is valid. Addr is valid if vendor is != 0x000000 or 0xFFFFFF
   */
  private static boolean validAddr(byte[] addr) {
    if (addr != null && addr.length >= 6) { // valid addr has 6 bytes
      // check if vendor is != 0x000000 or 0xFFFFFF
      return (addr[0] != 0x00 && addr[1] != 0x00 && addr[2] != 0x00)
        || (addr[0] != 0xFF && addr[1] != 0xFF && addr[2] != 0xFF);
    }
    return false;
  }

  /**
   * Creates a new instance of UuidGenerator. Note that there should not be more
   * than one instance per stateFile.
   */
  private UuidV1IdGenerator(final File stateFile, String altNodeId) {
    super();
    this.stateFile = stateFile;
    try {
      if (altNodeId == null) {
        // using hardware mode
        tryHardwareId();
      } else if (altNodeId.equals("generate")) {
        tryGenerateMode();
      } else if (altNodeId.equals("random")) {
        useRandomMode();
      } else {
        // See, if there is an alternative MAC address set.
        nodeId = parseAlternativeNodeId(altNodeId);
        restoreState();
        log.log(INFO, "Explicitly using ID {0} to generate Type 1 UUIDs", getNodeIdentifier());
      }
      UUID uuid = nextId(null);
      long ts = timeStamp.get();
      ts -= UUID_EPOCH_OFFSET;
      ts /= MILLIS_TO_UUID;

      saveState();
      log.log(DEBUG, "Saved state: clockSeq {0}, timestamp {1}, uuid {2}, stateFile: {3})", clockSeq.get(), new Date(ts), uuid, stateFile);
    } catch (IOException e) {
      log.log(ERROR, "There was a problem while detecting the nodeId. Falling back to random mode. Try using to specify 'ebean.uuidNodeId' property", e);
      useRandomMode();
    }
  }

  /**
   * Tries to initialize the generator by retrieving the MAC address from
   * hardware. If there is no suitable network interface found, it will fall back
   * to "generate" mode.
   *
   * @throws IOException if state file is not readable or tryGenerateMode also
   *                     fails.
   */
  private void tryHardwareId() throws IOException {
    try {
      nodeId = getHardwareId();
    } catch (IOException e) {
      log.log(ERROR, "Error while reading MAC address. Fall back to 'generate' mode", e);
      tryGenerateMode();
    }

    if (nodeId != null) {
      restoreState();
      log.log(INFO, "Using MAC {0} to generate Type 1 UUIDs", getNodeIdentifier());
      return;
    }
    log.log(WARNING, "No suitable network interface found. Fall back to 'generate' mode");
    tryGenerateMode();
  }

  /**
   * Tries the "generate" mode. A nodeId is generated once and saved to the state
   * file
   */
  private void tryGenerateMode() throws IOException {
    if (restoreState()) {
      log.log(INFO, "Using recently generated nodeId {0} to generate Type 1 UUIDs", getNodeIdentifier());
    } else {
      // RFC 4.5 use random portion for node
      nodeId = super.getNodeIdBytes();
      log.log(INFO, "Using a newly generated nodeId {0} to generate Type 1 UUIDs", getNodeIdentifier());
    }
  }

  /**
   * Random mode. A nodeId is generated every start up. no state file is
   * maintained. This should always work.
   */
  private void useRandomMode() {
    canSaveState = false;
    nodeId = super.getNodeIdBytes();
    log.log(INFO, "Explicitly using a new random ID {0} to generate Type 1 UUIDs", getNodeIdentifier());
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
    if (!stateFile.exists()) {
      return false;
    }
    try (InputStream is = new FileInputStream(stateFile)) {
      prop.load(is);
    }

    String propNodeId = prop.getProperty("nodeId");
    if (propNodeId == null || propNodeId.isEmpty()) {
      log.log(WARNING, "State file ''{0}'' is incomplete", stateFile);
      return false; // we cannot restore
    }
    try {
      if (nodeId == null) {
        nodeId = parseAlternativeNodeId(propNodeId);
      } else if (!getNodeIdentifier().equals(propNodeId)) {
        log.log(WARNING,
          "The nodeId in the state file ''{0}'' has changed from {1} to {2}. "
            + "This can happen when MAC address changes or when two containers share the same state file",
          stateFile, propNodeId, getNodeIdentifier());
        return false;
      }
      Integer seq = Integer.parseInt(prop.getProperty("clockSeq")) & 0x3FFF;
      Long ts = Long.valueOf(prop.getProperty("timeStamp"));
      clockSeq.set(seq);
      timeStamp.set(ts);
      return true;

    } catch (IllegalArgumentException nfe) {
      log.log(ERROR, "State file corrupt: " + stateFile, nfe);
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
    } catch (IOException e) {
      log.log(ERROR, "Could not persist uuid state to: " + stateFile, e);
    }
  }

  @Override
  protected byte[] getNodeIdBytes() {
    return nodeId;
  }

}
