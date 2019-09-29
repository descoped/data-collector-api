package no.ssb.dc.api.el;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionLanguage {

    private static final Logger LOG = LoggerFactory.getLogger(ExpressionLanguage.class);

    static final Pattern EXPRESSION_REGEX = Pattern.compile("\\$\\{([^}]+)}");
    static final Pattern MULTI_EXPRESSION_REGEX = Pattern.compile("\\$\\{(.*?)}+");

    private final Map<String, Object> variables;

    private static class Jexl {
        private static final JexlEngine jexlEngine = new JexlBuilder()
                .cache(512)
                .strict(true)
                .silent(false)
                .create();

        static JexlEngine engine() {
            return jexlEngine;
        }
    }

    public ExpressionLanguage(Map<String, Object> variables) {
        Objects.requireNonNull(variables);
        this.variables = variables;
    }

    public boolean isExpression(String expr) {
        Matcher m = EXPRESSION_REGEX.matcher(expr);
        return m.find();
    }

    public String getExpression(String expr) {
        Matcher m = EXPRESSION_REGEX.matcher(expr);
        if (m.find()) {
            return m.group(1);
        }
        return expr;
    }

    public Object evaluateExpression(String expr) {
        try {
            final String expression = (isExpression(expr) ? getExpression(expr) : expr);
            List<String> operators = List.of("+" ,"-", "*", "/");
//            if (operators.stream().anyMatch(expression::contains)) {
//                LOG.warn("Arithmetic operators like {} WILL BE evaluated by Jexl!", operators);
//            }
            JexlExpression e = Jexl.engine().createExpression(expression);
            JexlContext jexlContext = new MapContext(variables);
            jexlContext.set("cast", new Cast());
            return e.evaluate(jexlContext);
        } catch (Exception e) {
            LOG.error("Unable to resolve expr: {}", expr);
            throw e;
        }
    }

    public String evaluateExpressions(String expr) {
        Matcher m = MULTI_EXPRESSION_REGEX.matcher(expr);

        StringBuffer buf = new StringBuffer();
        int last = 0;
        while (m.find()) {
            for (int n = 0; n < m.groupCount(); n++) {
                String group = m.group(n);
                int current = m.start();
                String prev = expr.substring(last, current);
                buf.append(prev);
                buf.append(evaluateExpression(group));
                last = m.end();
            }
        }
        buf.append(expr.substring(last));
        return buf.toString();
    }

}
