package io.ebeaninternal.server.dto;

/**
 * A column in the resultSet that we want to map to a bean property.
 */
public class DtoColumn {

  private final String label;

  public DtoColumn(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public String toString() {
    return label;
  }

}
