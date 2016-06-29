package fi.thl.termed.dao.jdbc;

import com.google.common.collect.Iterables;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.spesification.sql.SqlSpecification;
import fi.thl.termed.util.UUIDs;

public class JdbcTextAttributePermissionsDao
    extends AbstractJdbcDao<ObjectRolePermission<TextAttributeId>, Void> {

  public JdbcTextAttributePermissionsDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(ObjectRolePermission<TextAttributeId> id, Void value) {
    TextAttributeId textAttributeId = id.getObjectId();
    ClassId textAttributeDomainId = textAttributeId.getDomainId();
    jdbcTemplate.update(
        "insert into text_attribute_permission (text_attribute_scheme_id, text_attribute_domain_id, text_attribute_id, role, permission) values (?, ?, ?, ?, ?)",
        textAttributeDomainId.getSchemeId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getRole(),
        id.getPermission().toString());
  }

  @Override
  public void update(ObjectRolePermission<TextAttributeId> id, Void value) {
    // NOP (permission doesn't have a separate value)
  }

  @Override
  public void delete(ObjectRolePermission<TextAttributeId> id) {
    TextAttributeId textAttributeId = id.getObjectId();
    ClassId textAttributeDomainId = textAttributeId.getDomainId();
    jdbcTemplate.update(
        "delete from text_attribute_permission where text_attribute_scheme_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and role = ? and permission = ?",
        textAttributeDomainId.getSchemeId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getRole(),
        id.getPermission().toString());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from text_attribute_permission", mapper);
  }

  @Override
  protected <E> List<E> get(
      SqlSpecification<ObjectRolePermission<TextAttributeId>, Void> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from text_attribute_permission where %s",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(ObjectRolePermission<TextAttributeId> id) {
    TextAttributeId textAttributeId = id.getObjectId();
    ClassId textAttributeDomainId = textAttributeId.getDomainId();
    return jdbcTemplate.queryForObject(
        "select count(*) from text_attribute_permission where text_attribute_scheme_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and role = ? and permission = ?",
        Long.class,
        textAttributeDomainId.getSchemeId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getRole(),
        id.getPermission().toString()) > 0;
  }

  @Override
  protected <E> E get(ObjectRolePermission<TextAttributeId> id, RowMapper<E> mapper) {
    TextAttributeId textAttributeId = id.getObjectId();
    ClassId textAttributeDomainId = textAttributeId.getDomainId();
    return Iterables.getFirst(jdbcTemplate.query(
        "select * from text_attribute_permission text_attribute_scheme_id = ? and text_attribute_domain_id = ? and text_attribute_id = ? and role = ? and permission = ?",
        mapper,
        textAttributeDomainId.getSchemeId(),
        textAttributeDomainId.getId(),
        textAttributeId.getId(),
        id.getRole(),
        id.getPermission().toString()), null);
  }

  @Override
  protected RowMapper<ObjectRolePermission<TextAttributeId>> buildKeyMapper() {
    return new RowMapper<ObjectRolePermission<TextAttributeId>>() {
      @Override
      public ObjectRolePermission<TextAttributeId> mapRow(ResultSet rs, int rowNum)
          throws SQLException {

        ClassId domainId =
            new ClassId(UUIDs.fromString(rs.getString("text_attribute_scheme_id")),
                        rs.getString("text_attribute_domain_id"));

        TextAttributeId textAttributeId =
            new TextAttributeId(domainId, rs.getString("text_attribute_id"));

        return new ObjectRolePermission<TextAttributeId>(
            textAttributeId, rs.getString("role"), Permission.valueOf(rs.getString("permission")));
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
