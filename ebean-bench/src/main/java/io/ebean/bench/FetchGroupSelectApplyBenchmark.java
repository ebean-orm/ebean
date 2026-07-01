package io.ebean.bench;

import io.ebean.FetchGroup;
import io.ebean.service.SpiFetchGroupQuery;
import io.ebeaninternal.api.SpiExpressionList;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;
import io.ebeaninternal.server.querydefn.SpiFetchGroup;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;
import java.lang.reflect.Proxy;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
@State(Scope.Thread)
public class FetchGroupSelectApplyBenchmark {

  private FetchGroup<DummyBean> fetchGroup;
  private SpiFetchGroup<DummyBean> spiFetchGroup;
  private SpiFetchGroupQuery<DummyBean> reusableQueryNoFilter;
  private OrmQueryDetail existingDetailNoFilter;
  private OrmQueryDetail existingDetailWithFilter;
  private SpiExpressionList<?> postApplyFilter;

  @Setup(Level.Trial)
  public void setup() {
    fetchGroup = FetchGroup.of(DummyBean.class)
      .select("id,name,status,version")
      .fetch("billingAddress", "line1,city,country")
      .fetch("shippingAddress", "line1,city,country")
      .fetch("contacts", "firstName,lastName,email")
      .fetch("contacts.phoneNumbers", "number,type")
      .build();
    spiFetchGroup = (SpiFetchGroup<DummyBean>) fetchGroup;

    reusableQueryNoFilter = FetchGroup.queryFor(DummyBean.class);

    existingDetailNoFilter = new OrmQueryDetail();

    existingDetailWithFilter = new OrmQueryDetail();
    existingDetailWithFilter.fetch("contacts", "firstName,lastName", null);
    existingDetailWithFilter.getChunk("contacts", false).setFilterMany(dummyFilterMany());
    postApplyFilter = dummyFilterMany();
  }

  @Benchmark
  public SpiFetchGroupQuery<DummyBean> selectOnReusableQuery_noFilter() {
    reusableQueryNoFilter.select(fetchGroup);
    return reusableQueryNoFilter;
  }

  @Benchmark
  public OrmQueryDetail applyToExistingDetail_noFilter() {
    return spiFetchGroup.detail(existingDetailNoFilter);
  }

  @Benchmark
  public OrmQueryDetail applyToExistingDetail_withFilter() {
    return spiFetchGroup.detail(existingDetailWithFilter);
  }

  @Benchmark
  public OrmQueryDetail applyToNewEmptyDetail_thenAddFilterMany() {
    OrmQueryDetail detail = spiFetchGroup.detail(new OrmQueryDetail());
    detail.getChunk("contacts", true).setFilterMany(postApplyFilter);
    return detail;
  }

  @SuppressWarnings("unused")
  private static final class DummyBean {
  }

  private static SpiExpressionList<?> dummyFilterMany() {
    return (SpiExpressionList<?>) Proxy.newProxyInstance(
      FetchGroupSelectApplyBenchmark.class.getClassLoader(),
      new Class<?>[]{SpiExpressionList.class},
      (proxy, method, args) -> {
        Class<?> returnType = method.getReturnType();
        if (returnType == boolean.class) {
          return false;
        }
        if (returnType == int.class) {
          return 0;
        }
        if (returnType == long.class) {
          return 0L;
        }
        if (returnType == float.class) {
          return 0f;
        }
        if (returnType == double.class) {
          return 0d;
        }
        return null;
      });
  }
}
