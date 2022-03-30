package io.ebean.platform.oracle;

/**
 * Oracle 12 platform using column alias.
 */
public class Oracle12Platform extends OraclePlatform {

  public Oracle12Platform() {
    super();
    //this.platform = Platform.ORACLE12;
    this.columnAliasPrefix = "c";
  }
}
