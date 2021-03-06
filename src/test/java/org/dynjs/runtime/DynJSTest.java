/**
 *  Copyright 2012 Douglas Campos, and individual contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dynjs.runtime;

import org.dynjs.api.Function;
import org.dynjs.exception.ReferenceError;
import org.dynjs.runtime.fixtures.BypassFunction;
import org.dynjs.runtime.java.JavaRequireFunction;
import org.dynjs.runtime.java.SayHiToJava;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class DynJSTest {

    private DynJS dynJS;
    private DynThreadContext context;
    private DynJSConfig config;

    @Before
    public void setUp() {
        config = new DynJSConfig();
        dynJS = new DynJS(config);
        context = new DynThreadContext();
    }

    @Test
    public void assignsGlobalVariables() {
        dynJS.eval(context, "var x = 'test';");
        assertThat(context.getScope().resolve("x"))
                .isNotNull()
                .isInstanceOf(String.class)
                .isEqualTo("test");
    }

    @Test
    public void defineUnInitializedGlobalVariables() {
        dynJS.eval(context, "var x;");
        assertThat(context.getScope().resolve("x"))
                .isNotNull()
                .isEqualTo(DynThreadContext.UNDEFINED);
    }

    @Test
    public void assignsExprGlobalVariables() {
        check("var x = 2 + 1; var result = x == 3;");
    }

    @Test
    public void assignsExprMulGlobalVariables() {
        check("var x = 3 * 2; var result = x == 6;");
    }

    @Test
    public void assignsExprSubGlobalVariables() {
        check("var x = 3 - 1; var result = x == 2;");
    }

    @Test
    public void assignsComplexExprSubGlobalVariables() {
        check("var x = 3 * 2 - 1; var result = x == 5");
    }

    @Test
    public void assignsComplexparExprSubGlobalVariables() {
        check("var x = (3 * 2) - 1; var result = x == 5");
    }

    @Test
    public void assignsNamedEmptyFunction() {
        dynJS.eval(context, "function x(){};");
        assertThat(context.getScope().resolve("x"))
                .isNotNull()
                .isInstanceOf(Function.class);
    }

    @Test
    public void assignsAnonymousEmptyFunction() {
        dynJS.eval(context, "var x = function(a,b,c){};");
        assertThat(context.getScope().resolve("x"))
                .isNotNull()
                .isInstanceOf(Function.class);
    }

    @Test
    public void buildFunctionWithBody() {
        dynJS.eval(context, "var x = function(a,b){var w = (1 + 2) * 3;}");
        Object actual = context.getScope().resolve("x");
        assertThat(actual)
                .isNotNull()
                .isInstanceOf(Function.class);

        assertThat(((Function) actual).call(context, new Object[]{}))
                .isNull();
    }

    @Test
    public void buildFunctionWithMultipleStatementBody() {
        dynJS.eval(context, "var x = function(){var a = 1;var b = 2; var c = a + b;}");
        Object actual = context.getScope().resolve("x");
        assertThat(actual)
                .isNotNull()
                .isInstanceOf(Function.class);

        assertThat(((Function) actual).call(context, new Object[]{}))
                .isNull();
    }

    @Test
    public void buildFunctionWithReturn() {
        dynJS.eval(context, "var x = function(){return 1+1;};");
        Object actual = context.getScope().resolve("x");
        assertThat(actual)
                .isNotNull()
                .isInstanceOf(Function.class);

        assertThat(((Function) actual).call(context, new Object[]{}))
                .isNotNull()
                .isInstanceOf(Double.class)
                .isEqualTo(2.0);
    }

    @Test(expected = ReferenceError.class)
    public void throwsReferenceErrorWhenCallAnonExistingReference() {
        dynJS.eval(context, "print(x);");
    }

    @Test
    public void testRelationalOperators() {
        check("var result = 1 < 2;");
        check("var result = 2 > 1;");
        check("var result = 2 <= 2;");
        check("var result = 2 >= 2;");
    }

    @Test
    public void testBooleanLiterals() {
        check("var result = true;", true);
        check("var result = false;", false);
    }

    @Test
    public void testEquality() {
        check("var result = 1 == 1;");
        check("var result = 1 != 2;");
    }

    @Test
    public void testMathOperations() {
        check("var x = 1 + 1;var result = x == 2");
        check("var x = 1 - 1;var result = x == 0");
        check("var x = 3 * 7;var result = x == 21");
        check("var x = 3 / 2;var result = x == 1.5");
        check("var x = 3 % 2;var result = x == 1");
    }

    @Test
    public void testAssignments() {
        check("var x = 1;x += 1; var result = x == 2;");
        check("var x = 1;x -= 1; var result = x == 0;");
        check("var x = 1;x *= 3; var result = x == 3;");
        check("var x = 1;x /= 1; var result = x == 1;");
        check("var x = 2;x %= 1; var result = x == 0;");
    }

    @Test
    public void testLoop() {
        check("var x = 10;var y = 0; while(x < 10){x+=1;y+=1}; var result = y == 0;");
        check("var x = 10;var y = 0; do { x+=1;y+=1; } while(x < 10); var result = y == 1;");
    }

    @Test
    public void testFor() {
        check("var x = 0; for (var i =0;i < 10; i+=1){ x+=1;}; var result = x == 10");
    }

    @Test
    public void testNullLiteral() {
        dynJS.eval(context, "var result = null");
        assertThat(context.getScope().resolve("result")).isNull();
    }

    @Test
    public void testTernaryOperator() {
        check("var x = 1 > 2 ? 55 : 56; var result = x == 56");
    }

    @Test
    public void testLogicalOperators() {
        check("var result = false || true;", true);
        check("var result = true || false;", true);
        check("var result = false || false;", false);
        check("var result = true && false;", false);
        check("var result = false && true;", false);
        check("var result = true && true;", true);
    }

    @Test
    public void testEmptyObjectLiteral() {
        dynJS.eval(context, "var result = {};");
        assertThat(context.getScope().resolve("result"))
                .isNotNull()
                .isInstanceOf(DynObject.class);
    }

    @Test
    public void testBasicObjectLiteral() {
        dynJS.eval(context, "var result = {w:true};");
        final Object result = context.getScope().resolve("result");
        assertThat(result)
                .isNotNull()
                .isInstanceOf(DynObject.class);
        assertThat(((DynObject) result).resolve("w")).isInstanceOf(Boolean.class).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void testObjectLiteralPropertyAccess() {
        check("var x = {w:true}; var result = x.w;", true);
        check("var x = {'y':false}; var result = x.y;", false);
        check("var x = {'z':true}; var result = x['z'];", true);
    }

    @Test
    public void testLiteralArray() {
        check("var x = [1,2,3]; var result = x[0] == 1", true);
        check("var x = [1]; x[0] = 2; var result = x[0] == 2", true);
        check("var x = [1,2]; x[0] = 4; x[1]= 3; var result = x[0] == 4 && x[1] == 3", true);
        check("var x = []; x[33] = 'lol'; var result = x[33] == 'lol';");
    }

    @Test
    public void testBuiltinLoading() {
        config.addBuiltin("sample", new BypassFunction());
        check("var result = sample(true);");
    }

    @Test
    public void testEval() {
        check("eval('var result = true');");
    }

    @Test
    public void testTypeOf() {
        check("var result = typeof undefined == 'undefined';");
        check("var result = typeof null === 'object';");
        check("var result = typeof {} === 'object';");
        check("var result = typeof true === 'boolean';");
        check("var result = typeof 1.0 === 'number';");
        check("var result = typeof 'lol' === 'string';");
        check("function x(){}; var result = typeof x === 'function';");
    }

    @Test
    public void testVoid() {
        check("var result = undefined === undefined;");
    }

    private void check(String scriptlet) {
        check(scriptlet, true);
    }

    private void check(String scriptlet, Boolean expected) {
        dynJS.eval(context, scriptlet);
        Object result = context.getScope().resolve("result");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testJavaRequireFunctionLoading() {
        config.addBuiltin("javaRequire", new JavaRequireFunction());
        dynJS.eval(context, "var NiceClass = javaRequire('org.dynjs.runtime.java.SayHiToJava');");
        dynJS.eval(context, "var x = new NiceClass");

        assertThat(context.getScope().resolve("NiceClass"))
                .isNotNull()
                .isInstanceOf(Class.class)
                .isEqualTo(SayHiToJava.class);

        assertThat(context.getScope().resolve("x"))
                .isInstanceOf(SayHiToJava.class);

    }

}
