package io.ebean.csv.reader;

import io.ebean.Database;
import io.ebean.bean.EntityBean;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.ExpressionPath;
import io.ebean.text.StringParser;
import io.ebean.text.TextException;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of the CsvReader
 */
public class CsvReader<T> {

  private final Database server;

  private final BeanType<T> descriptor;

  private final List<CsvColumn> columnList = new ArrayList<>();

  private final CsvColumn ignoreColumn = new CsvColumn();

  private boolean hasHeader;

  private int logInfoFrequency = 1000;

  /**
   * The batch size used for JDBC statement batching.
   */
  protected int persistBatchSize = 30;

  private boolean addPropertiesFromHeader;

  public CsvReader(Database server, Class<T> type) {
    this.server = server;
    this.descriptor = server.pluginApi().beanType(type);
  }

  public void setPersistBatchSize(int persistBatchSize) {
    this.persistBatchSize = persistBatchSize;
  }

  public void setIgnoreHeader() {
    setHasHeader(true, false);
  }

  public void setAddPropertiesFromHeader() {
    setHasHeader(true, true);
  }

  public void setHasHeader(boolean hasHeader, boolean addPropertiesFromHeader) {
    this.hasHeader = hasHeader;
    this.addPropertiesFromHeader = addPropertiesFromHeader;
  }

  public void setLogInfoFrequency(int logInfoFrequency) {
    this.logInfoFrequency = logInfoFrequency;
  }

  public void addIgnore() {
    columnList.add(ignoreColumn);
  }

  public void addProperty(String propertyName) {
    addProperty(propertyName, null);
  }


  public void addProperty(String propertyName, StringParser parser) {
    ExpressionPath elProp = descriptor.expressionPath(propertyName);
    if (parser == null) {
      parser = elProp.stringParser();
    }
    CsvColumn column = new CsvColumn(elProp, parser);
    columnList.add(column);
  }

  public void process(Reader reader) throws Exception {
    DefaultCsvCallback<T> callback = new DefaultCsvCallback<>(persistBatchSize, logInfoFrequency);
    process(reader, callback);
  }

  public void process(Reader reader, CsvCallback<T> callback) throws Exception {
    if (reader == null) {
      throw new NullPointerException("reader is null?");
    }
    if (callback == null) {
      throw new NullPointerException("callback is null?");
    }

    CsvUtilReader utilReader = new CsvUtilReader(reader);

    callback.begin(server);

    int row = 0;

    if (hasHeader) {
      String[] line = utilReader.readNext();
      if (addPropertiesFromHeader) {
        addPropertiesFromHeader(line);
      }
      callback.readHeader(line);
    }

    try {
      do {
        ++row;
        String[] line = utilReader.readNext();
        if (line == null) {
          --row;
          break;
        }

        if (callback.processLine(row, line)) {
          // the line content is expected to be ok for processing
          if (line.length != columnList.size()) {
            // we have not got the expected number of columns
            String msg = "Error at line " + row + ". Expected [" + columnList.size() + "] columns "
              + "but instead we have [" + line.length + "].  Line[" + Arrays.toString(line) + "]";
            throw new TextException(msg);
          }

          T bean = buildBeanFromLineContent(row, line);

          callback.processBean(row, line, bean);

        }
      } while (true);

      callback.end(row);

    } catch (Exception e) {
      // notify that an error occurred so that any
      // transaction can be rolled back if required
      callback.endWithError(row, e);
      throw e;
    }
  }

  private void addPropertiesFromHeader(String[] line) {
    for (String headerElement : line) {
      ExpressionPath elProp = descriptor.expressionPath(headerElement);
      if (elProp == null) {
        throw new TextException("Property [" + headerElement + "] not found");
      }
      addProperty(headerElement);
    }
  }

  protected T buildBeanFromLineContent(int row, String[] line) {
    try {
      T bean = descriptor.createBean();
      EntityBean entityBean = (EntityBean) bean;
      for (int columnPos = 0; columnPos < line.length; columnPos++) {
        convertAndSetColumn(columnPos, line[columnPos], entityBean);
      }
      return bean;

    } catch (RuntimeException e) {
      String msg = "Error at line: " + row + " line[" + Arrays.toString(line) + "]";
      throw new RuntimeException(msg, e);
    }
  }

  protected void convertAndSetColumn(int columnPos, String strValue, EntityBean bean) {
    strValue = strValue.trim();
    if (strValue.isEmpty()) {
      return;
    }
    CsvColumn c = columnList.get(columnPos);
    c.convertAndSet(strValue, bean);
  }

  /**
   * Processes a column in the csv content.
   */
  public static class CsvColumn {

    private final ExpressionPath path;
    private final StringParser parser;

    /**
     * Constructor for the IGNORE column.
     */
    private CsvColumn() {
      this.path = null;
      this.parser = null;
    }

    /**
     * Construct with a property and parser.
     */
    public CsvColumn(ExpressionPath path, StringParser parser) {
      this.path = path;
      this.parser = parser;
    }

    /**
     * Convert the string to the appropriate value and set it to the bean.
     */
    public void convertAndSet(String strValue, EntityBean bean) {
      if (parser != null && path != null) {
        Object value = parser.parse(strValue);
        path.pathSet(bean, value);
      }
    }
  }

}
