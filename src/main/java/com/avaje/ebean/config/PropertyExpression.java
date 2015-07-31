package com.avaje.ebean.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Helper used to evaluate expressions such as ${CATALINA_HOME}.
 * <p>
 * The expressions can contain environment variables, system properties or JNDI
 * properties. JNDI expressions take the form ${jndi:propertyName} where you
 * substitute propertyName with the name of the jndi property you wish to
 * evaluate.
 * </p>
 */
final class PropertyExpression {

  private static final Logger logger = LoggerFactory.getLogger(PropertyExpression.class);

  /**
   * Prefix for looking up JNDI Environment variable.
   */
  private static final String JAVA_COMP_ENV = "java:comp/env/";

  /**
   * Used to detect the start of an expression.
   */
  private static final String START = "${";

  /**
   * Used to detect the end of an expression.
   */
  private static final String END = "}";

  /**
   * Specify the PropertyHolder.
   */
  private PropertyExpression() {
  }

  /**
   * Return the property value evaluating and replacing any expressions such as
   * ${CATALINA_HOME}.
   */
  static String eval(String val, PropertyMap map) {
    if (val == null) {
      return null;
    }
    int sp = val.indexOf(START);
    if (sp > -1) {
      int ep = val.indexOf(END, sp + 1);
      if (ep > -1) {
        return eval(val, sp, ep, map);
      }
    }
    return val;
  }

  /**
   * Convert the expression using JNDI, Environment variables, System Properties
   * or existing an property in SystemProperties itself.
   */
  private static String evaluateExpression(String exp, PropertyMap map) {

    if (isJndiExpression(exp)) {
      // JNDI property lookup...
      String val = getJndiProperty(exp);
      if (val != null) {
        return val;
      }
    }

    // check Environment Variables first
    String val = System.getenv(exp);
    if (val == null) {
      // then check system properties
      val = System.getProperty(exp);
    }
    if (val == null && map != null) {
      // then check PropertyMap
      val = map.get(exp);
    }

    if (val != null) {
      return val;

    } else {
      // unable to evaluate yet... but maybe later based on the order
      // in which properties are being set/loaded. You can use
      // GlobalProperties.evaluateExpressions() to get any unresolved
      // expressions to be evaluated
      logger.debug("Unable to evaluate expression [" + exp + "]");
      return null;
    }
  }

  private static String eval(String val, int sp, int ep, PropertyMap map) {

    StringBuilder sb = new StringBuilder();
    sb.append(val.substring(0, sp));

    String cal = evalExpression(val, sp, ep, map);
    sb.append(cal);

    eval(val, ep + 1, sb, map);

    return sb.toString();
  }

  private static void eval(String val, int startPos, StringBuilder sb, PropertyMap map) {
    if (startPos < val.length()) {
      int sp = val.indexOf(START, startPos);
      if (sp > -1) {
        // append what is between the last token and the new one (if startPos ==
        // sp nothing gets added)
        sb.append(val.substring(startPos, sp));
        int ep = val.indexOf(END, sp + 1);
        if (ep > -1) {
          String cal = evalExpression(val, sp, ep, map);
          sb.append(cal);
          eval(val, ep + 1, sb, map);
          return;
        }
      }
    }
    // append what is left...
    sb.append(val.substring(startPos));
  }

  private static String evalExpression(String val, int sp, int ep, PropertyMap map) {
    // trim off start and end ${ and }
    String exp = val.substring(sp + START.length(), ep);

    // evaluate the variable
    String evaled = evaluateExpression(exp, map);
    if (evaled != null) {
      return evaled;
    } else {
      // unable to evaluate at this stage (maybe later)
      return START + exp + END;
    }
  }

  private static boolean isJndiExpression(String exp) {
    return exp.startsWith("JNDI:") || exp.startsWith("jndi:");
  }

  /**
   * Returns null if JNDI is not setup or if the property is not found.
   * 
   * @param key
   *          the key of the JNDI Environment property including a JNDI: prefix.
   */
  private static String getJndiProperty(String key) {

    try {
      // remove the JNDI: prefix
      key = key.substring(5);

      return (String) getJndiObject(key);

    } catch (NamingException ex) {
      return null;
    }
  }

  /**
   * Similar to getProperty but throws NamingException if JNDI is not setup or
   * if the property is not found.
   */
  private static Object getJndiObject(String key) throws NamingException {

    InitialContext ctx = new InitialContext();
    return ctx.lookup(JAVA_COMP_ENV + key);
  }

}