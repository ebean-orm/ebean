package io.ebeaninternal.server.deploy.parse;

import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.sqlserver.SqlServer17Platform;
import io.ebeaninternal.server.core.bootup.BootupClasses;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.type.DefaultTypeManager;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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

  @SuppressWarnings("unchecked")
  private AnnotationClass createAnnotationClass(ServerConfig config) {

    DeployUtil deployUtil = new DeployUtil(new DefaultTypeManager(config, new BootupClasses()), config);

    DeployBeanInfo deployBeanInfo = new DeployBeanInfo(deployUtil, mock(DeployBeanDescriptor.class));
    ReadAnnotationConfig readAnnotationConfig = mock(ReadAnnotationConfig.class);

    return new AnnotationClass(deployBeanInfo, readAnnotationConfig);
  }

  private ServerConfig sqlServerPlatform(boolean allQuotedIdentifiers) {

    SqlServer17Platform sqlServer17Platform = new SqlServer17Platform();
    ServerConfig config = new ServerConfig();
    config.setDatabasePlatform(sqlServer17Platform);
    config.setAllQuotedIdentifiers(allQuotedIdentifiers);

    sqlServer17Platform.configure(config.getPlatformConfig());
    return config;
  }
}
