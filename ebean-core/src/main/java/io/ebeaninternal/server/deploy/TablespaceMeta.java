package io.ebeaninternal.server.deploy;

import java.util.Objects;

/**
 * Meta data for table spaces. If table space is configured, tablespaceName, indexTablespace, lobTablespace is never null;
 * 
 * @author Noemi Szemenyei, FOCONIS AG
 *
 */
public final class TablespaceMeta {

  private final String tablespaceName;
  private final String indexTablespace;
  private final String lobTablespace;

  public TablespaceMeta(String tablespaceName, String indexTablespace, String lobTablespace) {
    this.tablespaceName = tablespaceName;
    this.indexTablespace = indexTablespace;
    this.lobTablespace = lobTablespace;
  }

  public String getTablespaceName() {
    return tablespaceName;
  }

  public String getIndexTablespace() {
    return indexTablespace;
  }

  public String getLobTablespace() {
    return lobTablespace;
  }

  @Override
  public int hashCode() {
    return Objects.hash(indexTablespace, tablespaceName, lobTablespace);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TablespaceMeta other = (TablespaceMeta) obj;
    return Objects.equals(indexTablespace, other.indexTablespace)
      && Objects.equals(tablespaceName, other.tablespaceName)
      && Objects.equals(lobTablespace, other.lobTablespace);
  }

  @Override
  public String toString() {
    return "tablespace=" + tablespaceName + ", indexTablespace=" + indexTablespace + ", lobTablespace=" + lobTablespace;
  }

}
