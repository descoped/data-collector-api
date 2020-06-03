package no.ssb.dc.api.el;

import no.ssb.dc.api.ConfigurationMap;
import no.ssb.dc.api.context.ExecutionContext;
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

    @Test
    public void testCustomContext() {
        ExecutionContext context = ExecutionContext.empty();
        Map<String, String> map = new LinkedHashMap<>();
        map.put("foo", "bar");
        ConfigurationMap config = new ConfigurationMap(map);
        context.services().register(ConfigurationMap.class, config);
        ExpressionLanguage el = new ExpressionLanguage(context);
        Object result = el.evaluateExpression("ENV.foo");
        System.out.printf("eval: %s", result);
    }
}
