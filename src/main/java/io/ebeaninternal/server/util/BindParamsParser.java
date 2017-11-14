package io.ebeaninternal.server.util;

import io.ebean.config.EncryptKey;
import io.ebeaninternal.api.BindParams;
import io.ebeaninternal.api.BindParams.OrderedList;
import io.ebeaninternal.api.BindParams.Param;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import javax.persistence.PersistenceException;
import java.util.Collection;

/**
 * Parses the BindParams if they are using named parameters.
 * <p>
 * This is a thread safe implementation.
 * </p>
 */
public class BindParamsParser {

  private static final String ENCRYPTKEY_PREFIX = "encryptkey_";
  private static final String ENCRYPTKEY_GAP = "___";

  private static final int ENCRYPTKEY_PREFIX_LEN = ENCRYPTKEY_PREFIX.length();
  private static final int ENCRYPTKEY_GAP_LEN = ENCRYPTKEY_GAP.length();

  /**
   * Used to parse sql looking for named parameters.
   */
  private static final String quote = "'";

  /**
   * Used to parse sql looking for named parameters.
   */
  private static final String colon = ":";

  private final BindParams params;
  private final String sql;

  private final BeanDescriptor<?> beanDescriptor;

  public static String parse(BindParams params, String sql) {
    return parse(params, sql, null);
  }

  public static String parse(BindParams params, String sql, BeanDescriptor<?> beanDescriptor) {
    return new BindParamsParser(params, sql, beanDescriptor).parseSql();
  }

  private BindParamsParser(BindParams params, String sql, BeanDescriptor<?> beanDescriptor) {
    this.params = params;
    this.sql = sql;
    this.beanDescriptor = beanDescriptor;
  }

  /**
   * Parse the sql changed named parameters to positioned parameters if required.
   * <p>
   * The sql is used when named parameters are used.
   * </p>
   * <p>
   * This is used in most cases of named parameters. The case it is NOT used for is
   * named parameters in a having clause. In this case some of the named parameters
   * could be for a where clause and some for the having clause.
   * </p>
   */
  private String parseSql() {

    if (params.isSameBindHash()) {
      String preparedSql = params.getPreparedSql();
      if (preparedSql != null && !preparedSql.isEmpty()) {
        // the sql has already been parsed and positionedParameters are set in order
        return preparedSql;
      }
    }

    String preparedSql;
    if (params.requiresNamedParamsPrepare()) {
      // convert named parameters into ordered list
      OrderedList orderedList = params.createOrderedList();
      parseNamedParams(orderedList);
      preparedSql = orderedList.getPreparedSql();
    } else {
      preparedSql = sql;
    }
    params.setPreparedSql(preparedSql);
    return preparedSql;
  }

  /**
   * Named parameters need to be parsed and replaced with ?.
   */
  private void parseNamedParams(OrderedList orderedList) {

    parseNamedParams(0, orderedList);
  }

  private void parseNamedParams(int startPos, OrderedList orderedList) {

    if (sql == null) {
      throw new PersistenceException("query does not contain any named bind parameters?");
    }
    if (startPos > sql.length()) {
      return;
    }

    // search for quotes and named params... in order...
    int beginQuotePos = sql.indexOf(quote, startPos);
    int nameParamStart = findNameStart(sql, startPos);
    if (beginQuotePos > 0 && beginQuotePos < nameParamStart) {
      // the quote precedes the named parameter...
      // find and add up to the end quote
      int endQuotePos = sql.indexOf(quote, beginQuotePos + 1);
      String sub = sql.substring(startPos, endQuotePos + 1);
      orderedList.appendSql(sub);

      // start again after the end quote
      parseNamedParams(endQuotePos + 1, orderedList);

    } else {
      if (nameParamStart < 0) {
        // no more params, add the rest
        String sub = sql.substring(startPos, sql.length());
        orderedList.appendSql(sub);

      } else {
        // find the end of the parameter name
        int endOfParam = nameParamStart + 1;
        do {
          char c = sql.charAt(endOfParam);
          if (c != '_' && !Character.isLetterOrDigit(c)) {
            break;
          }
          endOfParam++;
        } while (endOfParam < sql.length());

        // add the named parameter value to bindList
        String paramName = sql.substring(nameParamStart + 1, endOfParam);

        Param param;
        if (paramName.startsWith(ENCRYPTKEY_PREFIX)) {
          param = addEncryptKeyParam(paramName);
        } else {
          param = params.getParameter(paramName);
        }

        if (param == null) {
          String msg = "Bind value is not set or null for [" + paramName + "] in [" + sql + "]";
          throw new PersistenceException(msg);
        }

        String sub = sql.substring(startPos, nameParamStart);
        orderedList.appendSql(sub);

        // check if inValue is a Collection type...
        Object inValue = param.getInValue();
        if (inValue instanceof Collection<?>) {
          // Chop up Collection parameter into a number
          // of individual parameters and add each one individually
          Collection<?> collection = (Collection<?>) inValue;
          int c = 0;
          for (Object elVal : collection) {
            if (++c > 1) {
              orderedList.appendSql(",");
            }
            orderedList.appendSql("?");
            BindParams.Param elParam = new BindParams.Param();
            elParam.setInValue(elVal);
            orderedList.add(elParam);
          }

        } else {
          // its a normal scalar value parameter...
          orderedList.add(param);
          orderedList.appendSql("?");
        }

        // continue on after the end of the parameter
        parseNamedParams(endOfParam, orderedList);
      }
    }
  }

  /**
   * Find the next named parameter start position (based on colon).
   */
  static int findNameStart(String sql, int startPos) {
    while (true) {
      int colonPos = sql.indexOf(colon, startPos);
      if (colonPos > -1) {
        // validate the next character after the colon (ignore postgres cast)
        char c = sql.charAt(colonPos + 1);
        if (c == '_' || Character.isLetterOrDigit(c)) {
          return colonPos;
        } else {
          startPos = colonPos + 2;
          continue;
        }
      }
      return -1;
    }
  }

  /**
   * Add an encryption key bind parameter.
   */
  private Param addEncryptKeyParam(String keyNamedParam) {

    int pos = keyNamedParam.indexOf(ENCRYPTKEY_GAP, ENCRYPTKEY_PREFIX_LEN);

    String tableName = keyNamedParam.substring(ENCRYPTKEY_PREFIX_LEN, pos);
    String columnName = keyNamedParam.substring(pos + ENCRYPTKEY_GAP_LEN);

    EncryptKey key = beanDescriptor.getEncryptKey(tableName, columnName);
    String strKey = key.getStringValue();

    return params.setEncryptionKey(keyNamedParam, strKey);
  }

}
