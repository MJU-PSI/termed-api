package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Identifiable;
import fi.thl.termed.util.dao.Dao2;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Can be used to create simple service backed by a single DAO.
 */
public class DaoForwardingRepository2<K extends Serializable, V extends Identifiable<K>>
    extends AbstractRepository2<K, V> {

  private Dao2<K, V> delegate;

  public DaoForwardingRepository2(Dao2<K, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  protected void insert(K id, V value, WriteOptions opts, User user) {
    delegate.insert(id, value, user);
  }

  @Override
  protected void update(K id, V value, WriteOptions opts, User user) {
    delegate.update(id, value, user);
  }

  @Override
  public void delete(K id, WriteOptions opts, User user) {
    delegate.delete(id, user);
  }

  @Override
  public Stream<V> values(Query<K, V> query, User user) {
    return delegate.getValues(query.getWhere(), user);
  }

  @Override
  public Stream<K> keys(Query<K, V> query, User user) {
    return delegate.getKeys(query.getWhere(), user);
  }

  @Override
  public boolean exists(K key, User user) {
    return delegate.exists(key, user);
  }

  @Override
  public Optional<V> get(K id, User user, Select... selects) {
    return delegate.get(id, user);
  }

}
