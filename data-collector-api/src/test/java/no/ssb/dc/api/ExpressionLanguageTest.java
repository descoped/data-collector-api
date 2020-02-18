package no.ssb.dc.api;

import no.ssb.dc.api.context.ExecutionContext;
import no.ssb.dc.api.el.ExpressionLanguage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExpressionLanguageTest {

    @Disabled
    @Test
    public void testSimpleExpression() {
        String expr = "${a-b}";

        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("a", "1");
        variables.put("b", "2");
        ExpressionLanguage el = new ExpressionLanguage(new ExecutionContext.Builder().variables(variables).build());
        System.out.printf("%s%n", el.evaluateExpression(expr));
    }
}
