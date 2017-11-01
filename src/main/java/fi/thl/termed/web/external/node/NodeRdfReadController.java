package fi.thl.termed.web.external.node;

import static fi.thl.termed.util.StringUtils.tokenize;
import static fi.thl.termed.util.rdf.RdfMediaTypes.LD_JSON_VALUE;
import static fi.thl.termed.util.rdf.RdfMediaTypes.RDF_XML_VALUE;
import static java.util.stream.Collectors.toList;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByPropertyPrefix;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.jena.JenaRdfModel;
import fi.thl.termed.util.query.AndSpecification;
import fi.thl.termed.util.query.OrSpecification;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetRdfMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import fi.thl.termed.web.external.node.transform.NodesToRdfModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graphs/{graphId}")
public class NodeRdfReadController {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<NodeId, Node> nodeService;

  @Autowired
  private Service<TypeId, Type> typeService;

  private Specification<NodeId, Node> toPrefixQuery(List<TextAttribute> attrs, String q) {
    List<Specification<NodeId, Node>> orClauses = new ArrayList<>();
    tokenize(q).forEach(
        t -> attrs.forEach(a -> orClauses.add(new NodesByPropertyPrefix(a.getId(), t))));
    return OrSpecification.or(orClauses);
  }

  @GetMapping(value = "/nodes", produces = {LD_JSON_VALUE, RDF_XML_VALUE})
  public Model get(@PathVariable("graphId") UUID graphId,
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @AuthenticationPrincipal User user) {
    log.info("Exporting RDF-model {} (user: {})", graphId, user.getUsername());
    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);

    List<Specification<NodeId, Node>> orClauses = new ArrayList<>();

    typeService.getValueStream(new TypesByGraphId(graphId), user).forEach(type -> {
      List<Specification<NodeId, Node>> typeSpec = new ArrayList<>();
      typeSpec.add(new NodesByTypeId(type.getId()));
      typeSpec.add(new NodesByGraphId(type.getGraphId()));
      if (!query.isEmpty()) {
        typeSpec.add(toPrefixQuery(type.getTextAttributes(), query));
      }
      orClauses.add(AndSpecification.and(typeSpec));
    });

    OrSpecification<NodeId, Node> spec = OrSpecification.or(orClauses);

    List<Node> nodes = nodeService.getValueStream(spec, user).collect(toList());
    List<Type> types = typeService.getValueStream(new TypesByGraphId(graphId), user)
        .collect(toList());

    return new JenaRdfModel(new NodesToRdfModel(
        types, nodeId -> nodeService.get(nodeId, user)).apply(nodes)).getModel();
  }

  @GetRdfMapping("/types/{typeId}/nodes/{id}")
  public Model get(@PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @AuthenticationPrincipal User user) {
    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);

    List<Node> node = nodeService.get(new NodeId(id, typeId, graphId), user)
        .map(Collections::singletonList).orElseThrow(NotFoundException::new);
    List<Type> types = typeService.getValueStream(new TypesByGraphId(graphId), user)
        .collect(toList());

    return new JenaRdfModel(new NodesToRdfModel(
        types, nodeId -> nodeService.get(nodeId, user)).apply(node)).getModel();
  }

}
