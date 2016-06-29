package fi.thl.termed.dao.jdbc;

import com.google.common.collect.Iterables;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import fi.thl.termed.domain.UserSchemeRoleId;
import fi.thl.termed.spesification.sql.SqlSpecification;
import fi.thl.termed.util.UUIDs;

public class JdbcUserSchemeRoleDao extends AbstractJdbcDao<UserSchemeRoleId, Void> {

  public JdbcUserSchemeRoleDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(UserSchemeRoleId id, Void value) {
    jdbcTemplate.update(
        "insert into user_scheme_role (username, scheme_id, role) values (?, ?, ?)",
        id.getUsername(), id.getSchemeId(), id.getRole());
  }

  @Override
  public void update(UserSchemeRoleId id, Void value) {
    // NOP (user scheme role doesn't have a separate value)
  }

  @Override
  public void delete(UserSchemeRoleId id) {
    jdbcTemplate.update(
        "delete from user_scheme_role where username = ? and scheme_id = ? and role = ?",
        id.getUsername(), id.getSchemeId(), id.getRole());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from user_scheme_role", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<UserSchemeRoleId, Void> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from user_scheme_role where %s",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(UserSchemeRoleId id) {
    return jdbcTemplate.queryForObject(
        "select count(*) from user_scheme_role where username = ? and scheme_id = ? and role = ?",
        Long.class,
        id.getUsername(),
        id.getSchemeId(),
        id.getRole()) > 0;
  }

  @Override
  protected <E> E get(UserSchemeRoleId id, RowMapper<E> mapper) {
    return Iterables.getFirst(jdbcTemplate.query(
        "select * from user_scheme_role username = ? and scheme_id = ? and role = ?",
        mapper,
        id.getUsername(),
        id.getSchemeId(),
        id.getRole()), null);
  }

  @Override
  protected RowMapper<UserSchemeRoleId> buildKeyMapper() {
    return new RowMapper<UserSchemeRoleId>() {
      @Override
      public UserSchemeRoleId mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new UserSchemeRoleId(rs.getString("username"),
                                    UUIDs.fromString(rs.getString("scheme_id")),
                                    rs.getString("role"));
      }
    };
  }

  @Override
  protected RowMapper<Void> buildValueMapper() {
    return new RowMapper<Void>() {
      @Override
      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        return null;
      }
    };
  }

}
