package io.descoped.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.descoped.dc.api.ConfigSetter;
import io.descoped.dc.api.Specification;
import io.descoped.dc.api.node.Configurations;
import io.descoped.dc.api.node.Node;
import io.descoped.dc.api.util.JsonParser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class SpecificationBuilder extends AbstractBuilder {

    public static final String GLOBAL_CONFIGURATION = "GLOBAL_CONFIGURATION";

    @JsonProperty("id")
    final String specificationId;
    @JsonProperty
    final String name;
    @JsonProperty
    final String startFunctionId;

    @JsonProperty("configure")
    List<ConfigurationBuilder> configurationBuilders = new ArrayList<>();
    @JsonProperty("functions")
    Map<String, NodeBuilder> nodeBuilderById = new LinkedHashMap<>();

    @ConfigSetter
    public SpecificationBuilder(String specificationId, String name, String startFunctionId) {
        super(BuilderType.Specification);
        this.specificationId = specificationId;
        this.name = name;
        this.startFunctionId = startFunctionId;
    }

    public String getId() {
        return specificationId;
    }

    public String getName() {
        return name;
    }

    @ConfigSetter
    public SpecificationBuilder configure(ConfigurationBuilder builder) {
        configurationBuilders.add(builder);
        return this;
    }

    @ConfigSetter
    public SpecificationBuilder function(NodeWithIdBuilder builder) {
        nodeBuilderById.put(builder.getId(), builder);
        return this;
    }

    /*
     * see NodeBuilder.build()
     */
    public Specification end() {
        BuildContext buildContext = BuildContext.fromNodeBuilderById(nodeBuilderById);

        // add configuration leaf nodes
        Configurations.Builder configurationsBuilder = new Configurations.Builder();
        for (ConfigurationBuilder configurationBuilder : configurationBuilders) {
            configurationsBuilder.add(configurationBuilder.build(buildContext));
        }
        Configurations configurations = configurationsBuilder.build();
        buildContext.cacheInstance(GLOBAL_CONFIGURATION, configurations);

        // add child nodes recursively to buildContext.nodeInstanceById map
        for (Map.Entry<String, NodeBuilder> entry : nodeBuilderById.entrySet()) {
            String nodeBuilderId = entry.getKey();
            NodeBuilder nodeBuilder = entry.getValue();
            Node nodeInstance = nodeBuilder.build(buildContext);
            buildContext.cacheInstance(nodeBuilderId, nodeInstance);
        }

        return Specification.create(specificationId, name, configurations, buildContext.getInstance(startFunctionId), buildContext.nodeInstanceById());
    }

    public NodeBuilder get(String nodeId) {
        return Optional.ofNullable(nodeBuilderById.get(nodeId)).orElseThrow();
    }

    public String serialize() {
        return JsonParser.createJsonParser().toPrettyJSON(this);
    }

    public String serializeAsYaml() {
        return JsonParser.createYamlParser().toPrettyJSON(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SpecificationBuilder that = (SpecificationBuilder) o;
        return specificationId.equals(that.specificationId) &&
                name.equals(that.name) &&
                startFunctionId.equals(that.startFunctionId) &&
                Objects.equals(configurationBuilders, that.configurationBuilders) &&
                Objects.equals(nodeBuilderById, that.nodeBuilderById);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), specificationId, name, startFunctionId, configurationBuilders, nodeBuilderById);
    }

    @Override
    public String toString() {
        return "SpecificationBuilder{" +
                "specificationId='" + specificationId + '\'' +
                ", name='" + name + '\'' +
                ", startFunctionId='" + startFunctionId + '\'' +
                '}';
    }
}
