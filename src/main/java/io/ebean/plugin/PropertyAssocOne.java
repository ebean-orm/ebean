package io.ebean.plugin;

public interface PropertyAssocOne extends PropertyAssoc {

  boolean isOneToOne();

  Property findMatchImport(String matchDbColumn);

  boolean isEmbedded();

  Property[] getProperties();

  boolean isOneToOneExported();
}
