package no.ssb.dc.api;

import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExpressionLanguageTest {

    @Ignore
    @Test
    public void testName() {
        String expr = "${a-b}";

        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("a-b", "foo");
        ExpressionLanguage el = new ExpressionLanguage(variables);
        System.out.printf("%s%n", el.evaluateExpression(expr));
    }
}
