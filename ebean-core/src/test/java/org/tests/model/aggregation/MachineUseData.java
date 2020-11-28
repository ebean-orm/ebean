package org.tests.model.aggregation;

import io.ebean.Ebean;
import io.ebean.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;

public class MachineUseData {

  public static DOrg load() {

    int count = Ebean.find(DMachineUse.class).findCount();
    if (count == 0) {
      return new MachineUseData().loadData();
    } else {
      return Ebean.find(DOrg.class).where().eq("name", "org0").findOne();
    }
  }

  @Transactional(batchSize = 50)
  private DOrg loadData() {

    DOrg org = new DOrg("org0");
    org.save();

    for (int i = 0; i < 5; i++) {
      DMachine machine = new DMachine(org, "org0-m" + i);
      machine.save();

      LocalDate startDate = LocalDate.of(2018, 1, 1);

      for (int j = 0; j < 4; j++) {
        startDate = startDate.plusDays(5);
        createUseFor(machine, startDate, i, j);
      }
    }

    return org;
  }

  private void createUseFor(DMachine machine, LocalDate date, int machineIndex, int dayIndex) {

    DMachineUse use = new DMachineUse(machine, date);
    int kms = 100 * machineIndex + dayIndex;
    use.setDistanceKms(kms);
    use.setFuel(BigDecimal.valueOf(kms).multiply(new BigDecimal(0.1), new MathContext(4)));
    use.setTimeSecs(10000 * machineIndex);
    use.save();

    String[] aux = {"Aux1", "Aux2"};
    if (machineIndex % 2 != 0) {
      aux = new String[]{"Aux3"};
    }

    int count = 1;
    for (String auxName : aux) {
      DMachineAuxUse auxUse = new DMachineAuxUse(machine, date, auxName);
      auxUse.setUseSecs(10 * machineIndex * 10 + (dayIndex + count++));
      auxUse.setFuel(BigDecimal.valueOf((dayIndex + machineIndex * 7) + count));
      auxUse.save();
    }

  }
}
