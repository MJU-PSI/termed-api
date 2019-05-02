package fi.thl.termed.service.node.select;

import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.util.RegularExpressions.UUID;
import static org.jparsercombinator.ParserCombinators.regex;
import static org.jparsercombinator.ParserCombinators.regexMatchResult;

import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.SelectAll;
import java.util.List;
import org.jparsercombinator.Parser;
import org.jparsercombinator.ParserCombinator;

class SelectParser implements Parser<List<Select>> {

  public static void main(String[] args) {
    System.out.println(new SelectParser().apply("Concept.number"));
  }


  private Parser<List<Select>> parser;

  SelectParser() {
    String qualifier = "(" + UUID + "\\." + CODE + "\\.|" + CODE + "\\.|)";

    ParserCombinator<Select> selectAll =
        regex("\\*").map(m -> new SelectAll());

    ParserCombinator<Select> selectIdParser =
        regex("(node\\.id|nodeId|id)")
            .map(m -> new SelectId());
    ParserCombinator<Select> selectTypeParser =
        regex("type")
            .map(m -> new SelectType());

    ParserCombinator<Select> selectCodeParser =
        regex("code")
            .map(m -> Select.field("code"));
    ParserCombinator<Select> selectUriParser =
        regex("uri")
            .map(m -> Select.field("uri"));
    ParserCombinator<Select> selectNumberParser =
        regex("(number|n)")
            .map(m -> Select.field("number"));
    ParserCombinator<Select> selectCreatedByParser =
        regex("createdBy")
            .map(m -> Select.field("createdBy"));
    ParserCombinator<Select> selectCreatedDateParser =
        regex("createdDate")
            .map(m -> Select.field("createdDate"));
    ParserCombinator<Select> selectLastModifiedByParser =
        regex("lastModifiedBy")
            .map(m -> Select.field("lastModifiedBy"));
    ParserCombinator<Select> selectLastModifiedDateParser =
        regex("lastModifiedDate")
            .map(m -> Select.field("lastModifiedDate"));

    ParserCombinator<Select> selectAllProperties =
        regexMatchResult(qualifier + "(properties|props|p)\\.\\*")
            .map(m -> new SelectAllProperties(m.group(1)));
    ParserCombinator<Select> selectProperty =
        regexMatchResult(qualifier + "(properties|props|p)\\.(" + CODE + ")")
            .map(m -> new SelectProperty(m.group(1), m.group(3)));
    ParserCombinator<Select> selectAllReferences =
        regexMatchResult(qualifier + "(references|refs|r)\\.\\*")
            .map(m -> new SelectAllReferences(m.group(1)));
    ParserCombinator<Select> selectReference =
        regexMatchResult(qualifier + "(references|refs|r)\\.(" + CODE + ")(:([0-9]+))?")
            .map(m -> new SelectReference(m.group(1), m.group(3), parseIntOrNullToOne(m.group(5))));
    ParserCombinator<Select> selectAllReferrers =
        regexMatchResult(qualifier + "(referrers|refrs)\\.\\*")
            .map(m -> new SelectAllReferrers(m.group(1)));
    ParserCombinator<Select> selectReferrer =
        regexMatchResult(qualifier + "(referrers|refrs)\\.(" + CODE + ")(:([0-9]+))?")
            .map(m -> new SelectReferrer(m.group(1), m.group(3), parseIntOrNullToOne(m.group(5))));

    ParserCombinator<Select> primitiveSelectParser =
        selectAll
            .or(selectIdParser)
            .or(selectCodeParser)
            .or(selectUriParser)
            .or(selectNumberParser)
            .or(selectCreatedByParser)
            .or(selectCreatedDateParser)
            .or(selectLastModifiedByParser)
            .or(selectLastModifiedDateParser)
            .or(selectTypeParser)
            .or(selectAllProperties)
            .or(selectProperty)
            .or(selectAllReferences)
            .or(selectReference)
            .or(selectAllReferrers)
            .or(selectReferrer);

    this.parser = primitiveSelectParser.many(regex("\\s*,\\s*")).end();
  }

  private Integer parseIntOrNullToOne(String s) {
    return s != null ? Integer.parseInt(s) : 1;
  }

  @Override
  public List<Select> apply(String select) {
    return parser.apply(select);
  }

}
