package no.ssb.dc.api.node;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Configurations {

    final Map<Class<? extends Configuration>, Configuration> configurationMap;

    public Configurations(Map<Class<? extends Configuration>, Configuration> configurationMap) {
        this.configurationMap = configurationMap;
    }

    public Optional<FlowContext> flowContext() {
        FlowContext configuration = (FlowContext) configurationMap.get(FlowContext.class);
        return Optional.ofNullable(configuration);
    }

    public static class Builder {
        final Map<Class<? extends Configuration>, Configuration> configurationMap = new LinkedHashMap<>();

        public Builder add(Configuration configuration) {
            Class<? extends Configuration> anInterface = (Class<? extends Configuration>) configuration.getClass().getInterfaces()[0];
            configurationMap.put(anInterface, configuration);
            return this;
        }

        public Configurations build() {
            return new Configurations(configurationMap);
        }
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
}
