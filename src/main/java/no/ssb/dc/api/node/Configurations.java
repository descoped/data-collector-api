package no.ssb.dc.api.node;

import no.ssb.dc.api.node.builder.BuildContext;
import no.ssb.dc.api.node.builder.FlowContextBuilder;
import no.ssb.dc.api.node.builder.SecurityBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Configurations {

    final Map<Class<? extends Configuration>, Configuration> configurationMap;

    public Configurations(Map<Class<? extends Configuration>, Configuration> configurationMap) {
        this.configurationMap = configurationMap;
    }

    public FlowContext flowContext() {
        return (FlowContext) configurationMap.get(FlowContext.class);
    }

    public Security security() {
        return (Security) configurationMap.get(Security.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Configurations that = (Configurations) o;
        return Objects.equals(configurationMap, that.configurationMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configurationMap);
    }

    @Override
    public String toString() {
        return "Configurations{" +
                "configurationMap=" + configurationMap +
                '}';
    }

    public static class Builder {
        final Map<Class<? extends Configuration>, Configuration> configurationMap = new LinkedHashMap<>();

        public Builder add(Configuration configuration) {
            Class<? extends Configuration> anInterface = (Class<? extends Configuration>) configuration.getClass().getInterfaces()[0];
            configurationMap.put(anInterface, configuration);
            return this;
        }

        void createDefaultConfigurationIfAbsent() {
            if (!configurationMap.containsKey(FlowContext.class)) {
                FlowContextBuilder flowContextBuilder = new FlowContextBuilder();
                if (flowContextBuilder.globalState("global.topic") == null) {
                    flowContextBuilder.globalState("global.topic", "topic");
                }
                configurationMap.put(FlowContext.class, flowContextBuilder.build(BuildContext.empty()));
            }

            if (!configurationMap.containsKey(Security.class)) {
                SecurityBuilder securityBuilder = new SecurityBuilder();
                configurationMap.put(Security.class, securityBuilder.build(BuildContext.empty()));
            }
        }

        public Configurations build() {
            createDefaultConfigurationIfAbsent();
            return new Configurations(configurationMap);
        }
    }
}
