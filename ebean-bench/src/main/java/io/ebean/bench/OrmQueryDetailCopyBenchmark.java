package io.ebean.bench;

import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
@State(Scope.Thread)
public class OrmQueryDetailCopyBenchmark {

  private OrmQueryDetail source;
  private OrmQueryDetail existing;
  private StringBuilder hashBuilder;

  @Setup
  public void setup() {
    source = new OrmQueryDetail();
    source.select("id,name,status,version");
    source.fetch("billingAddress", "line1,city,country", null);
    source.fetch("shippingAddress", "line1,city,country", null);
    source.fetch("contacts", "firstName,lastName,email", null);
    source.fetch("contacts.phoneNumbers", "number,type", null);

    existing = new OrmQueryDetail();
    existing.fetch("contacts", "firstName,lastName", null);
    hashBuilder = new StringBuilder(256);
  }

  @Benchmark
  public OrmQueryDetail copyNull() {
    return source.copy(null);
  }

  @Benchmark
  public OrmQueryDetail copyExisting() {
    return source.copy(existing);
  }

  @Benchmark
  public int queryPlanHash() {
    hashBuilder.setLength(0);
    source.queryPlanHash(hashBuilder);
    return hashBuilder.length();
  }
}
