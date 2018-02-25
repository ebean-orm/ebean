package io.ebean.config.properties;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PropertyEvalTest {

	@Test
	public void eval_null() {
		assertNull(PropertyEval.eval(null));
	}

	@Test
	public void eval_empty() {
		assertEquals("", PropertyEval.eval(""));
	}

	@Test
	public void eval_noExpressions() {
		assertEquals("basic", PropertyEval.eval("basic"));
		assertEquals("{basic}", PropertyEval.eval("{basic}"));
	}

	@Test
	public void eval_singleExpression() {
		System.setProperty("foo", "Hello");
		assertEquals("Hello", PropertyEval.eval("${foo}"));
		assertEquals("preHello", PropertyEval.eval("pre${foo}"));
		assertEquals("HelloPost", PropertyEval.eval("${foo}Post"));
		assertEquals("beforeHelloAfter", PropertyEval.eval("before${foo}After"));
		System.clearProperty("foo");
	}

	@Test
	public void eval_singleExpression_withDefault() {

		System.setProperty("foo", "Hello");
		assertEquals("Hello", PropertyEval.eval("${foo:bart}"));
		assertEquals("beforeHelloAfter", PropertyEval.eval("before${foo:bart}After"));
		assertEquals("preHello", PropertyEval.eval("pre${foo:bart}"));
		assertEquals("HelloPost", PropertyEval.eval("${foo:bart}Post"));

		System.clearProperty("foo");
		assertEquals("bart", PropertyEval.eval("${foo:bart}"));
		assertEquals("before-bart-after", PropertyEval.eval("before-${foo:bart}-after"));
		assertEquals("pre-bart", PropertyEval.eval("pre-${foo:bart}"));
		assertEquals("bart-post", PropertyEval.eval("${foo:bart}-post"));

	}

	@Test
	public void eval_multiExpression_withDefault() {

		assertEquals("num1num2", PropertyEval.eval("${one:num1}${two:num2}"));
		assertEquals("num1-num2", PropertyEval.eval("${one:num1}-${two:num2}"));
		assertEquals("num1abnum2", PropertyEval.eval("${one:num1}ab${two:num2}"));
		assertEquals("anum1bcnum2d", PropertyEval.eval("a${one:num1}bc${two:num2}d"));

		System.setProperty("one", "first");
		System.setProperty("two", "second");

		assertEquals("firstsecond", PropertyEval.eval("${one:num1}${two:num2}"));
		assertEquals("first-second", PropertyEval.eval("${one:num1}-${two:num2}"));
		assertEquals("pre-first-second-post", PropertyEval.eval("pre-${one:num1}-${two:num2}-post"));
		assertEquals("AfirstBCsecondD", PropertyEval.eval("A${one:num1}BC${two:num2}D"));


		System.clearProperty("one");
		System.clearProperty("two");
	}
}
