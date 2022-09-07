import io.ebean.jackson.jsonnode.JsonNodeTypeFactory;

module io.ebean.json.node {

  requires io.ebean.core.type;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;

  provides io.ebean.core.type.ScalarTypeSetFactory with JsonNodeTypeFactory;
}
