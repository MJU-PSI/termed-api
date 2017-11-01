package fi.thl.termed.util.query;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import fi.thl.termed.util.collect.ListUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class SpecificationUtils {

  private SpecificationUtils() {
  }

  /**
   * Transforms specification to conjunctive normal form. Can be used for example to debug and test
   * specifications. Transformation may produce results of exponential in size and therefore
   * transformation may take exponential time.
   */
  public static <K extends Serializable, V> AndSpecification<K, V> toCnf(
      Specification<K, V> spec) {
    if (spec instanceof AndSpecification) {
      return toCnf((AndSpecification<K, V>) spec);
    }
    if (spec instanceof OrSpecification) {
      return toCnf((OrSpecification<K, V>) spec);
    }
    if (spec instanceof NotSpecification) {
      return toCnf((NotSpecification<K, V>) spec);
    }
    // wrap literal spec
    return AndSpecification.and(spec);
  }

  private static <K extends Serializable, V> AndSpecification<K, V> toCnf(
      AndSpecification<K, V> specs) {
    List<Specification<K, V>> results = new ArrayList<>();
    // flatten ANDs
    specs.forEach(spec -> toCnf(spec).forEach(results::add));
    return AndSpecification.and(results);
  }

  private static <K extends Serializable, V> AndSpecification<K, V> toCnf(
      OrSpecification<K, V> spec) {

    List<List<Specification<K, V>>> conjunctions = new ArrayList<>();
    spec.forEach(s -> conjunctions.add(toCnf(s).getSpecifications()));

    List<Specification<K, V>> results = new ArrayList<>();

    for (List<Specification<K, V>> distributed : ListUtils.distribute(conjunctions)) {
      List<Specification<K, V>> result = new ArrayList<>();

      // add results and flatten nested OR as needed
      distributed.forEach(s -> result.addAll(s instanceof OrSpecification ?
          ((OrSpecification) s).getSpecifications() : singletonList(s)));

      results.add(OrSpecification.or(result));
    }

    return AndSpecification.and(results);
  }

  private static <K extends Serializable, V> AndSpecification<K, V> toCnf(
      NotSpecification<K, V> spec) {

    Specification<K, V> innerSpec = spec.getSpecification();

    if (innerSpec instanceof NotSpecification) {
      return toCnf(((NotSpecification<K, V>) innerSpec).getSpecification());
    }
    if (innerSpec instanceof AndSpecification) {
      List<Specification<K, V>> inverted = ((AndSpecification<K, V>) innerSpec)
          .getSpecifications().stream().map(NotSpecification::not).collect(toList());
      return toCnf(OrSpecification.or(inverted));
    }
    if (innerSpec instanceof OrSpecification) {
      List<Specification<K, V>> inverted = ((OrSpecification<K, V>) innerSpec)
          .getSpecifications().stream().map(NotSpecification::not).collect(toList());
      return toCnf(AndSpecification.and(inverted));
    }

    return AndSpecification.and(spec);
  }

  /**
   * Transforms specification to disjunctive normal form. Can be used for example to debug and test
   * specifications. Transformation may produce results of exponential in size and therefore
   * transformation may take exponential time.
   */
  public static <K extends Serializable, V> OrSpecification<K, V> toDnf(
      Specification<K, V> spec) {
    if (spec instanceof AndSpecification) {
      return toDnf((AndSpecification<K, V>) spec);
    }
    if (spec instanceof OrSpecification) {
      return toDnf((OrSpecification<K, V>) spec);
    }
    if (spec instanceof NotSpecification) {
      return toDnf((NotSpecification<K, V>) spec);
    }
    // wrap literal spec
    return OrSpecification.or(spec);
  }

  private static <K extends Serializable, V> OrSpecification<K, V> toDnf(
      OrSpecification<K, V> specs) {
    List<Specification<K, V>> results = new ArrayList<>();
    // flatten Ors
    specs.forEach(spec -> toDnf(spec).forEach(results::add));
    return OrSpecification.or(results);
  }

  private static <K extends Serializable, V> OrSpecification<K, V> toDnf(
      AndSpecification<K, V> spec) {

    List<List<Specification<K, V>>> conjunctions = new ArrayList<>();
    spec.forEach(s -> conjunctions.add(toDnf(s).getSpecifications()));

    List<Specification<K, V>> results = new ArrayList<>();

    for (List<Specification<K, V>> distributed : ListUtils.distribute(conjunctions)) {
      List<Specification<K, V>> result = new ArrayList<>();

      // add results and flatten nested AND as needed
      distributed.forEach(s -> result.addAll(s instanceof AndSpecification ?
          ((AndSpecification) s).getSpecifications() : singletonList(s)));

      results.add(AndSpecification.and(result));
    }

    return OrSpecification.or(results);
  }

  private static <K extends Serializable, V> OrSpecification<K, V> toDnf(
      NotSpecification<K, V> spec) {

    Specification<K, V> innerSpec = spec.getSpecification();

    if (innerSpec instanceof NotSpecification) {
      return toDnf(((NotSpecification<K, V>) innerSpec).getSpecification());
    }
    if (innerSpec instanceof AndSpecification) {
      List<Specification<K, V>> inverted = ((AndSpecification<K, V>) innerSpec)
          .getSpecifications().stream().map(NotSpecification::not).collect(toList());
      return toDnf(OrSpecification.or(inverted));
    }
    if (innerSpec instanceof OrSpecification) {
      List<Specification<K, V>> inverted = ((OrSpecification<K, V>) innerSpec)
          .getSpecifications().stream().map(NotSpecification::not).collect(toList());
      return toDnf(AndSpecification.and(inverted));
    }

    return OrSpecification.or(spec);
  }

}
