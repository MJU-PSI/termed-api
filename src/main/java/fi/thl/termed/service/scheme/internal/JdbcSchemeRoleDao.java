package fi.thl.termed.service.scheme.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;

import static com.google.common.base.Preconditions.checkState;

public class JdbcSchemeRoleDao extends AbstractJdbcDao<SchemeRole, Empty> {

  public JdbcSchemeRoleDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(SchemeRole id, Empty value) {
    jdbcTemplate.update(
        "insert into scheme_role (scheme_id, role) values (?, ?)",
        id.getSchemeId(), id.getRole());
  }

  @Override
  public void update(SchemeRole id, Empty value) {
    // NOP (scheme role doesn't have a separate value)
  }

  @Override
  public void delete(SchemeRole id) {
    checkState(Objects.equals(id.getSchemeId(), id.getSchemeId()));

    jdbcTemplate.update(
        "delete from scheme_role where scheme_id = ? and role = ?",
        id.getSchemeId(), id.getRole());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from scheme_role", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<SchemeRole, Empty> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from scheme_role where %s",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(SchemeRole id) {
    return jdbcTemplate.queryForObject(
        "select count(*) from scheme_role where scheme_id = ? and role = ?",
        Long.class,
        id.getSchemeId(),
        id.getRole()) > 0;
  }

  @Override
  protected <E> Optional<E> get(SchemeRole id, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        "select * from scheme_role where scheme_id = ? and role = ?",
        mapper,
        id.getSchemeId(),
        id.getRole()).stream().findFirst();
  }

  @Override
  protected RowMapper<SchemeRole> buildKeyMapper() {
    return (rs, rowNum) -> new SchemeRole(new SchemeId(UUIDs.fromString(rs.getString("scheme_id"))),
                                          rs.getString("role"));
  }

  @Override
  protected RowMapper<Empty> buildValueMapper() {
    return (rs, rowNum) -> Empty.INSTANCE;
  }

}
