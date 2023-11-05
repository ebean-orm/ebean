package io.ebeaninternal.server.deploy.parse;

import io.ebean.DatabaseBuilder;
import io.ebean.config.DatabaseConfig;
import io.ebean.platform.sqlserver.SqlServer17Platform;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.generatedproperty.GeneratedPropertyFactory;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.type.DefaultTypeManager;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnotationClassTest {

  @Test
  public void convertColumnNames_when_AllQuotedIdentifiersIsTrue() {

    AnnotationClass annotationClass = createAnnotationClass(sqlServerPlatform(true));

    String[] colNames = {"Col1", "Col2"};

    final String[] columnNames = annotationClass.convertColumnNames(colNames);

    assertThat(columnNames.length).isEqualTo(2);
    assertThat(columnNames[0]).isEqualTo("[Col1]");
    assertThat(columnNames[1]).isEqualTo("[Col2]");
  }

  @Test
  public void convertColumnNames_when_AllQuotedIdentifiersIsFalse() {

    AnnotationClass annotationClass = createAnnotationClass(sqlServerPlatform(false));

    String[] colNames = {"Col1", "`Col2`", "col3"};

    final String[] columnNames = annotationClass.convertColumnNames(colNames);

    assertThat(columnNames.length).isEqualTo(3);
    assertThat(columnNames[0]).isEqualTo("Col1");
    assertThat(columnNames[1]).isEqualTo("[Col2]");
    assertThat(columnNames[2]).isEqualTo("col3");
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private AnnotationClass createAnnotationClass(DatabaseBuilder.Settings config) {
    DeployUtil deployUtil = new DeployUtil(new DefaultTypeManager(config, new BootupClasses()), config);

    DeployBeanInfo deployBeanInfo = new DeployBeanInfo(deployUtil, new DeployBeanDescriptor<>(null, Customer.class, null));
    ReadAnnotationConfig readAnnotationConfig = new ReadAnnotationConfig(new GeneratedPropertyFactory(true, new DatabaseConfig(), Collections.emptyList()), "","", new DatabaseConfig());
    return new AnnotationClass(deployBeanInfo, readAnnotationConfig);
  }

  private DatabaseBuilder.Settings sqlServerPlatform(boolean allQuotedIdentifiers) {
    SqlServer17Platform sqlServer17Platform = new SqlServer17Platform();
    var config = new DatabaseConfig().settings();
    config.setDatabasePlatform(sqlServer17Platform);
    config.setAllQuotedIdentifiers(allQuotedIdentifiers);

    sqlServer17Platform.configure(config.getPlatformConfig());
    return config;
  }
}
