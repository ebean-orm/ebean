package io.ebean.config.properties;

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
final class PropertyEval {

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
	private PropertyEval() {
	}

	/**
	 * Return the property value evaluating and replacing any expressions such as
	 * ${CATALINA_HOME}.
	 */
	static String eval(String val) {
		if (val == null) {
			return null;
		}
		int sp = val.indexOf(START);
		if (sp > -1) {
			int ep = val.indexOf(END, sp + 1);
			if (ep > -1) {
				return eval(val, sp, ep);
			}
		}
		return val;
	}

	/**
	 * Convert the expression using JNDI, Environment variables, System Properties
	 * or existing an property in SystemProperties itself.
	 */
	private static String evaluateExpression(String exp) {

		if (isJndiExpression(exp)) {
			// JNDI property lookup...
			String val = getJndiProperty(exp);
			if (val != null) {
				return val;
			}
		}
		String val = System.getProperty(exp);
		if (val == null) {
			val = System.getenv(exp);
		}
		return val;
	}

	private static String eval(String val, int sp, int ep) {

		return new EvalBuffer(val, sp, ep).process();
	}


	private static boolean isJndiExpression(String exp) {
		return exp.startsWith("JNDI:") || exp.startsWith("jndi:");
	}

	/**
	 * Returns null if JNDI is not setup or if the property is not found.
	 *
	 * @param key the key of the JNDI Environment property including a JNDI: prefix.
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


	private static class EvalBuffer {

		private final StringBuilder buf = new StringBuilder();
		private final String original;
		private int position;
		private int start;
		private int end;

		private String expression;
		private String defaultValue;

		EvalBuffer(String val, int start, int ep) {
			this.original = val;
			this.start = start;
			this.end = ep;
			this.position = 0;
			moveToStart();
		}

		void moveToStart() {
			if (start > position) {
				buf.append(original.substring(position, start));
				position = start;
			}
		}

		void parseForDefault() {
			int colonPos = original.lastIndexOf(':', end);
			if (colonPos > start) {
				expression = original.substring(start + START.length(), colonPos);
				defaultValue = original.substring(colonPos + 1, end);
			} else {
				expression = original.substring(start + START.length(), end);
			}
		}

		void evaluate() {
			String eval = evaluateExpression(expression);
			if (eval != null) {
				buf.append(eval);
			} else {
				if (defaultValue != null) {
					buf.append(defaultValue);
				} else {
					buf.append(START).append(expression).append(END);
				}
			}
		}

		String end() {
			if (end < original.length() - 1) {
				buf.append(original.substring(end + 1));
			}
			return buf.toString();
		}

		boolean next() {
			if (end < original.length()) {
				int startPos = original.indexOf(START, end + 1);
				if (startPos > -1) {
					int endPos = original.indexOf(END, startPos + 1);
					if (endPos > -1) {
						if (startPos > end + 1) {
							buf.append(original.substring(end + 1, startPos));
						}
						this.start = startPos;
						this.end = endPos;
						return true;
					}
				}
			}
			return false;
		}

		private void evalNext() {
			parseForDefault();
			evaluate();
		}

		String process() {
			evalNext();
			while (next()) {
				evalNext();
			}
			return end();
		}

	}

}
