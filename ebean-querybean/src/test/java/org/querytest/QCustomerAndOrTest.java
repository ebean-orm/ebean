package org.querytest;

import org.example.domain.Customer;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class QCustomerAndOrTest {

  @Test
  public void testEndAnd() {

    Date fiveDays = fiveDaysAgo();

    Customer.find.typed()
        .status.equalTo(Customer.Status.GOOD)
        .or()
          .id.greaterThan(1000)
          .and()
            .name.startsWith("super")
            .registered.after(fiveDays)
            .endAnd()
        .orderBy().id.desc()
        .findList();
  }

  @Test
  public void testQuery() {

    Date fiveDays = fiveDaysAgo();

    Customer.find.typed().name.like("DoesNotExist").delete();

    List<Customer> customers =

        Customer.find.typed()
          .status.equalTo(Customer.Status.GOOD)
          .or()
            .id.greaterThan(1000)
            .and()
              .name.startsWith("super")
              .registered.after(fiveDays)
              .endJunction()
          .orderBy().id.desc()
          .findList();

//    where t0.status = ?  and (t0.id > ?  or (t0.name like ?  and t0.registered > ? ) )  order by t0.id desc; --bind(GOOD,1000,super%,Wed Jul 22 00:00:00 NZST 2015)
//    where t0.status = ?  and (t0.id > ?  or (t0.name like ?  and t0.registered > ? ) )  order by t0.id; --bind(GOOD,1000,super%,Wed Jul 22 00:00:00 NZST 2015)
//    //where t0.id > ?  and (t0.id < ?  or (t0.name like ?  and t0.name like ? ) )  order by t0.id; --bind(12,1234,one,two)
//

  }

  private Date fiveDaysAgo() {
    LocalDateTime fiveDaysAgo = LocalDate.now().atStartOfDay().minusDays(5);
    return new Date(fiveDaysAgo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
  }
}
