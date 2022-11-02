module io.ebean.core.type {

  exports io.ebean.core.type;

  requires transitive java.sql;
  requires transitive io.ebean.api;
  requires static org.postgresql.jdbc;

  requires static com.fasterxml.jackson.core;

}
