package fi.thl.termed.service.node.specification;

import static java.util.stream.Collectors.toSet;

import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.query.DependentSpecification;
import fi.thl.termed.util.query.LuceneSpecification;
import fi.thl.termed.util.query.Specification;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class NodesByReferencePath implements LuceneSpecification<NodeId, Node>,
    DependentSpecification<NodeId, Node> {

  private final String attributeId;
  private final Specification<NodeId, Node> valueSpecification;

  private Set<UUID> valueNodeIds;

  public NodesByReferencePath(String attributeId,
      Specification<NodeId, Node> valueSpecification) {
    this.attributeId = attributeId;
    this.valueSpecification = valueSpecification;
  }

  public String getAttributeId() {
    return attributeId;
  }

  public Specification<NodeId, Node> getValueSpecification() {
    return valueSpecification;
  }

  @Override
  public void resolve(Function<Specification<NodeId, Node>, List<NodeId>> resolver) {
    if (valueSpecification instanceof NodesByReferencePath) {
      ((NodesByReferencePath) valueSpecification).resolve(resolver);
    }
    valueNodeIds = resolver.apply(valueSpecification).stream().map(NodeId::getId).collect(toSet());
  }

  @Override
  public boolean test(NodeId nodeId, Node node) {
    Preconditions.checkArgument(Objects.equals(nodeId, new NodeId(node)));
    Preconditions.checkNotNull(valueNodeIds, "Value IDs not resolved.");

    return node.getReferences().get(attributeId).stream()
        .anyMatch(v -> valueNodeIds.contains(v.getId()));
  }

  @Override
  public Query luceneQuery() {
    Preconditions.checkNotNull(valueNodeIds, "Value IDs not resolved.");

    BooleanQuery.Builder clauses = new BooleanQuery.Builder();
    valueNodeIds.forEach(valueNodeId -> clauses.add(
        new TermQuery(new Term("references." + attributeId + ".id", valueNodeId.toString())),
        Occur.SHOULD));
    return clauses.build();
  }

}
