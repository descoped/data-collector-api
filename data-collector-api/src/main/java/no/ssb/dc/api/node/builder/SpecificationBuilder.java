package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.Specification;
import no.ssb.dc.api.node.Configurations;
import no.ssb.dc.api.node.Node;
import no.ssb.dc.api.util.JsonParser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class SpecificationBuilder extends AbstractBuilder {

    static final String GLOBAL_CONFIGURATION = "GLOBAL_CONFIGURATION";

    @JsonProperty String name;
    @JsonProperty String startFunctionId;

    @JsonProperty("configure") List<ConfigurationBuilder> configurationBuilders = new ArrayList<>();
    @JsonProperty("functions") Map<String, NodeBuilder> nodeBuilderById = new LinkedHashMap<>();

    public SpecificationBuilder(String name, String startFunctionId) {
        super(BuilderType.Specification);
        this.name = name;
        this.startFunctionId = startFunctionId;
    }

    public String name() {
        return name;
    }

    public SpecificationBuilder configure(ConfigurationBuilder builder) {
        configurationBuilders.add(builder);
        return this;
    }

    public SpecificationBuilder function(NodeWithIdBuilder builder) {
        nodeBuilderById.put(builder.getId(), builder);
        return this;
    }

    /*
     * see NodeBuilder.build()
     */
    public Specification end() {
        BuildContext buildContext = BuildContext.fromNodeBuilderById(nodeBuilderById);

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

        return Specification.create(name, configurations, buildContext.getInstance(startFunctionId), buildContext.nodeInstanceById());
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
        return name.equals(that.name) &&
                startFunctionId.equals(that.startFunctionId) &&
                Objects.equals(nodeBuilderById, that.nodeBuilderById);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, startFunctionId, nodeBuilderById);
    }

    @Override
    public String toString() {
        return "SpecificationBuilder{" +
                "name='" + name + '\'' +
                ", startFunctionId='" + startFunctionId + '\'' +
                ", nodeBuilderById=" + nodeBuilderById +
                '}';
    }
}
