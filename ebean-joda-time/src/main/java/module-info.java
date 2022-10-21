
module io.ebean.joda.time {

  requires io.ebean.core.type;
  requires org.joda.time;
  requires com.fasterxml.jackson.core;

  provides io.ebean.core.type.ExtraTypeFactory with io.ebean.joda.time.JodaExtraTypeFactory;
}
