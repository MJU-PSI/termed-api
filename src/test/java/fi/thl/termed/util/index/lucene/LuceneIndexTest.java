package fi.thl.termed.util.index.lucene;

import com.google.gson.Gson;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;

import fi.thl.termed.util.specification.LuceneSpecification;
import fi.thl.termed.util.specification.Query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LuceneIndexTest {

  private LuceneIndex<Integer, TestObject> index;

  @Before
  public void setUp() {
    this.index = new LuceneIndex<>("", new DocumentJsonConverter<>(new Gson(), TestObject.class));
    index.reindex(1, new TestObject(1, "First", "This is an example body about dogs"));
    index.reindex(2, new TestObject(2, "Second", "This is an example body about cats"));
    index.reindex(3, new TestObject(3, "Third", "This is an example body about horses"));
    index.refreshBlocking();
  }

  @Test
  public void shouldFindById() {
    assertEquals("This is an example body about cats", index.query(term("id", "2")).get(0).body);
  }

  @Test
  public void shouldFindByContents() {
    assertEquals("First", index.query(term("title", "First")).get(0).title);
    assertEquals(new Integer(3), index.query(term("body", "horses")).get(0).id);
  }

  @Test
  public void shouldNotFindDeleted() {
    assertEquals(new Integer(3), index.query(term("body", "horses")).get(0).id);
    index.deleteFromIndex(3);
    index.refreshBlocking();
    assertTrue(index.query(term("body", "horses")).isEmpty());
  }

  private <K extends Serializable, V> Query<K, V> term(String field, String value) {
    return new Query<>(
        new RawLuceneSpecification<>(
            new TermQuery(new Term(field, value))));
  }

  private class TestObject {

    private Integer id;
    private String title;
    private String body;

    public TestObject(Integer id, String title, String body) {
      this.id = id;
      this.title = title;
      this.body = body;
    }

  }

  private class RawLuceneSpecification<K extends Serializable, V>
      implements LuceneSpecification<K, V> {

    private org.apache.lucene.search.Query query;

    public RawLuceneSpecification(org.apache.lucene.search.Query query) {
      this.query = query;
    }

    @Override
    public org.apache.lucene.search.Query luceneQuery() {
      return query;
    }

    @Override
    public boolean test(K k, V v) {
      throw new UnsupportedOperationException();
    }
  }

}