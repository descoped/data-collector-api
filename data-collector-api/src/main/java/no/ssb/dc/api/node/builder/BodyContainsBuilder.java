package no.ssb.dc.api.node.builder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import no.ssb.dc.api.node.Base;
import no.ssb.dc.api.node.BodyContains;

@JsonDeserialize(using = NodeBuilderDeserializer.class)
public class BodyContainsBuilder extends ResponsePredicateBuilder {

    private final QueryBuilder queryBuilder;
    private final String equalToStringLiteral;

    public BodyContainsBuilder(QueryBuilder queryBuilder, String equalToStringLiteral) {
        super(BuilderType.HttpResponseBodyContains);
        this.queryBuilder = queryBuilder;
        this.equalToStringLiteral = equalToStringLiteral;
    }

    @Override
    public <R extends Base> R build(BuildContext buildContext) {
        return null;
    }

    class BodyContainsNode extends LeafNode implements BodyContains {

    }

}
