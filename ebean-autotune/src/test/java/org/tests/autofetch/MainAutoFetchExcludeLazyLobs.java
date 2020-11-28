package org.tests.autofetch;

//import io.ebean.Ebean;
//import org.tests.model.basic.EBasicClob;

//import java.util.List;

public class MainAutoFetchExcludeLazyLobs {

//  public static void main(String[] args) {
//
//    EBasicClob a = new EBasicClob();
//    a.setName("name 1");
//    a.setTitle("a title");
//    a.setDescription("not that meaningful");
//
//    Ebean.save(a);
//
//    List<EBasicClob> list = Ebean.find(EBasicClob.class)
//      .setAutoTune(true)
//      .findList();
//
//    for (EBasicClob bean : list) {
//      bean.getName();
//      // although we read the description
//      // autofetch will not include it later
//      bean.getDescription();
//    }
//
//
//  }
}
