package fi.thl.termed.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public final class NodeId implements Serializable {

  private final UUID id;

  private final TypeId type;

  public NodeId(Node node) {
    this(node.getId(), node.getTypeId(), node.getTypeGraphId());
  }

  public NodeId(UUID id, String typeId, UUID graphId) {
    this(id, new TypeId(typeId, new GraphId(graphId)));
  }

  public NodeId(UUID id, TypeId type) {
    this.id = checkNotNull(id, "id can't be null in %s", getClass());
    this.type = checkNotNull(type, "type can't be null in %s", getClass());
  }

  public UUID getId() {
    return id;
  }

  public TypeId getType() {
    return type;
  }

  public UUID getTypeGraphId() {
    return type != null ? type.getGraphId() : null;
  }

  public String getTypeId() {
    return type != null ? type.getId() : null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeId that = (NodeId) o;
    return Objects.equals(id, that.id) && Objects.equals(type, that.type);

  }

  @Override
  public int hashCode() {
    return Objects.hash(type, id);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("type", type)
        .toString();
  }

}
