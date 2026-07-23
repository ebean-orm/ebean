import io.ebean.avajejsonb.mapper.ScalarJsonAvajeJsonbMapper;

module io.ebean.avajejsonb.mapper {

  requires io.avaje.jsonb;
  requires io.ebean.core.type;

  provides io.ebean.core.type.ScalarJsonMapper with ScalarJsonAvajeJsonbMapper;
}
