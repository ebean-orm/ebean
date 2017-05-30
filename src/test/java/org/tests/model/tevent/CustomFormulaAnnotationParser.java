package org.tests.model.tevent;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.ebean.annotation.Formula;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.parse.AnnotationParser;
import io.ebeaninternal.server.deploy.parse.DeployBeanInfo;

/**
 * Custom Annotation parser which parses &#64;Count annotation
 * 
 * @author Roland Praml, FOCONIS AG
  */
public class CustomFormulaAnnotationParser extends AnnotationParser {

  private int counter;
  
  
  @Target(FIELD) 
  @Retention(RUNTIME)
  @Formula(select="TODO", join = "TODO") // meta-formula
  public @interface Count {
    String value();
  }
  
  public CustomFormulaAnnotationParser(DeployBeanInfo<?> info, boolean validationAnnotations) {
    super(info, validationAnnotations);
  }

  @Override
  public void parse() {
    for (DeployBeanProperty prop : descriptor.propertiesAll()) {
      readField(prop);
    }
  }

  private void readField(DeployBeanProperty prop) {
   Count countAnnot = get(prop, Count.class);
    if (countAnnot != null) {
      // @Count found, so build the (complex) count formula
      DeployBeanPropertyAssocMany<?> countProp =  (DeployBeanPropertyAssocMany<?>) descriptor.getBeanProperty(countAnnot.value());
      counter++;
      String tmpTable = "f"+counter;
      String sqlSelect = "coalesce(" + tmpTable + ".child_count, 0)";
      String parentId = countProp.getMappedBy() + "_id";
      String tableName = countProp.getBeanTable().getBaseTable();
      String sqlJoin = "left join (select " + parentId +", count(*) as child_count from " + tableName + " GROUP BY " + parentId + " )"
          + " as " + tmpTable + " on " + tmpTable + "." +parentId + " = ${ta}." + descriptor.propertiesId().get(0).getDbColumn();
      prop.setSqlFormula(sqlSelect, sqlJoin);
//      prop.setSqlFormula("f1.child_count", 
//          "join (select parent_id, count(*) as child_count from child_entity GROUP BY parent_id) as f1 on f1.parent_id = ${ta}.id");
    }
  }

}
