package io.ebeaninternal.server.idgen;

import java.io.*;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IdGenerator for java util UUID.
 *
 * It extends the UuidV1RndIdGenerator so that it can generate rfc4122 compliant Type 1 UUIDs.
 *
 * This generator produces real Type 1 UUIDs (best for sqlserver) - You should use this generator only,
 * if you can guarantee, that mac addess is uniqe.
 */
public class UuidV1IdGenerator extends UuidV1RndIdGenerator {

  private static final Map<File, UuidV1IdGenerator> INSTANCES = new ConcurrentHashMap<>();

  private final File stateFile;
  private byte[] nodeId;
  private boolean canSaveState = true;

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
   *
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
        log.trace("Probing interface {}", network);
        if (!network.isLoopback()) {
          byte[] addr = network.getHardwareAddress();
          if (validAddr(addr)) {
            if (network.isUp() && !network.isVirtual()) {
              log.debug("Using interface {}", network);
              return addr;
            } else if (fallbackAddr == null) {
              log.debug("Using interface {} as fallback", network);
              fallbackAddr = addr;
            }
          }
        }
      } catch (SocketException ex) {
        log.debug("Skipping {}", network, ex);
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
          ||(addr[0] != 0xFF && addr[1] != 0xFF && addr[2] != 0xFF);
    }
    return false;
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
        log.info("Using alternative MAC {} to generate Type 1 UUIDs", getNodeIdentifier());
      } else {
        nodeId = getHardwareId();
        log.info("Using MAC {} to generate Type 1 UUIDs", getNodeIdentifier());
      }
      if (nodeId == null) {
        canSaveState = false;
        // RFC 4.5 use random portion for node
        nodeId = super.getNodeIdBytes();
        log.error("Have to fall back to random node identifier {} (Reason: No suitable network interface found)", getNodeIdentifier());

      } else {
        boolean flag = restoreState();
        UUID uuid = nextId(null);
        long ts = timeStamp.get();
        ts -= UUID_EPOCH_OFFSET;
        ts /= MILLIS_TO_UUID;

        saveState();
        log.debug("RestoreState: {}, ClockSeq {}, Timestamp {}, uuid {}, stateFile: {})", flag, clockSeq.get(),
            new Date(ts), uuid, stateFile);
      }
    } catch (IOException e) {
      canSaveState = false;
      // RFC 4.5 use random portion for node
      nodeId = super.getNodeIdBytes();
      log.error("Have to fall back to random node identifier {} (Reason: {} )", getNodeIdentifier(), e.getMessage());
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
        log.debug("Restored state from '{}'", stateFile);
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
      log.debug("Persisted state to '{}'", stateFile);
    } catch (IOException e) {
      log.error("Could not persist uuid state to '{}'", stateFile, e);
    }
  }

  @Override
  protected byte[] getNodeIdBytes() {
    return nodeId;
  }

}
