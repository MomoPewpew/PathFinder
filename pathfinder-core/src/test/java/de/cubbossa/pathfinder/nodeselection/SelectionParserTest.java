package de.cubbossa.pathfinder.nodeselection;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import de.cubbossa.pathfinder.node.selection.NumberRange;
import de.cubbossa.pathfinder.util.SelectionParser;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SelectionParserTest {

  public static final TestParser.Argument<UUID> UUID = new TestParser.Argument<>("uuid", r -> {
    UUID uuid = java.util.UUID.fromString(r.getRemaining());
    System.out.println(uuid);
    return uuid;
  })
      .execute(SelectionParser.ArgumentContext::getScope);
  public static final TestParser.Argument<Integer> LETTER_COUNT = new TestParser.Argument<>("lettercount",
      IntegerArgumentType.integer(1, 10)
  ).execute(c -> c.getScope().stream()
      .filter(s -> {
        Matcher matcher = Pattern.compile("[a-zA-Z]").matcher(s);
        int counter = 0;
        while (matcher.find()) {
          counter++;
        }
        return counter == c.getValue();
      })
      .collect(Collectors.toList()));
  public static final TestParser.Argument<NumberRange> LENGTH = new TestParser.Argument<>("length",
      reader -> {
        try {
          return NumberRange.parse(reader.getRemaining() == null
              ? ""
              : reader.getRemaining());
        } catch (ParseException e) {
          throw new RuntimeException(e);
        }
      })
      .execute(context -> context.getScope().stream()
          .filter(s -> (s.length() >= context.getValue().getStart().doubleValue())
              && (s.length() <= context.getValue().getEnd().doubleValue()))
          .collect(Collectors.toList()));
  public static final TestParser.Argument<CharacterType> TYPE =
      new TestParser.Argument<>("type", reader -> switch (reader.getRemaining()) {
        case "letter" -> CharacterType.LETTER;
        case "number" -> CharacterType.NUMBER;
        default -> CharacterType.MISC;
      })
          .execute(context -> switch (context.getValue()) {
            case LETTER -> context.getScope().stream()
                .filter(o -> o.matches("[a-zA-Z]+"))
                .collect(Collectors.toList());
            case NUMBER -> context.getScope().stream()
                .filter(s -> s.matches("[0-9]+"))
                .collect(Collectors.toList());
            case MISC -> context.getScope();
          })
          .suggestStrings(List.of("letter", "number"));
  private static final List<String> SCOPE = Lists.newArrayList(
      "A", "B", "C", "D", "E",
      "Word", "OtherWord", "XYZ",
      "123", "00000000",
      "            ", " ", "",
      "More words than one", "Another sentence"
  );
  private static TestParser parser;

  @BeforeAll
  static void setup() {
    parser = new TestParser("s");
    parser.addResolver(UUID);
    parser.addResolver(LENGTH);
    parser.addResolver(TYPE);
    parser.addResolver(LETTER_COUNT);
  }

  @Test
  @SneakyThrows
  public void testParseSelection1a() {

    Assertions.assertEquals(
        Lists.newArrayList("A", "B", "C", "D", "E", " ", ""),
        parser.parse("@s[length=..1]", SCOPE, SelectionParser.ArgumentContext::new));
  }

  @Test
  @SneakyThrows
  public void testParseSelection1b() {

    Assertions.assertEquals(
        Lists.newArrayList("A", "B", "C", "D", "E", " "),
        parser.parse("@s[length=1]", SCOPE, SelectionParser.ArgumentContext::new));
  }

  @Test
  @SneakyThrows
  public void testParseSelection2() {

    Assertions.assertEquals(
        Lists.newArrayList("OtherWord", "00000000", "            ", "More words than one",
            "Another sentence"),
        parser.parse("@s[length=5..]", SCOPE, SelectionParser.ArgumentContext::new));
  }

  @Test
  @SneakyThrows
  public void testParseSelection3() {

    Assertions.assertEquals(
        Lists.newArrayList("OtherWord"),
        parser.parse("@s[length=5..,type=letter]", SCOPE, SelectionParser.ArgumentContext::new));
  }

  @Test
  @SneakyThrows
  public void testParseSelection4() {

    Assertions.assertEquals(
        Lists.newArrayList("123"),
        parser.parse("@s[length=..5,type=number]", SCOPE, SelectionParser.ArgumentContext::new));
  }

  @Test
  @SneakyThrows
  public void testParseSelection5() {

    Assertions.assertEquals(
        Lists.newArrayList("A", "B", "C", "D", "E"),
        parser.parse("@s[lettercount=1]", SCOPE, SelectionParser.ArgumentContext::new));
  }

  @Test
  @SneakyThrows
  public void testParseSelection6a() {

    Assertions.assertDoesNotThrow(() -> parser.parse("@s[uui=abcdefg]", SCOPE, SelectionParser.ArgumentContext::new));
    Assertions.assertDoesNotThrow(() -> parser.parse("@s[uui=0123456]", SCOPE, SelectionParser.ArgumentContext::new));
    Assertions.assertDoesNotThrow(() -> parser.parse("@s[uui=a-a]", SCOPE, SelectionParser.ArgumentContext::new));
    Assertions.assertDoesNotThrow(() -> parser.parse("@s[uui=a1]", SCOPE, SelectionParser.ArgumentContext::new));
    Assertions.assertDoesNotThrow(() -> parser.parse("@s[uui=1a]", SCOPE, SelectionParser.ArgumentContext::new));
    Assertions.assertDoesNotThrow(() -> parser.parse("@s[uui=68cf83b1]", SCOPE, SelectionParser.ArgumentContext::new));
  }

  @Test
  @SneakyThrows
  public void testParseSelection6b() {

    Assertions.assertDoesNotThrow(() -> parser.parse("@s[uui=68cf83b1-1f01-4c56-88bb-c2bec7105714]", SCOPE, SelectionParser.ArgumentContext::new));
  }

  @Test
  public void testInvalidClassifier() {
    Assertions.assertThrows(IllegalStateException.class,
        () -> parser.parse("@invalid", SCOPE, SelectionParser.ArgumentContext::new));
  }

  @Test
  @SneakyThrows
  public void testOnlyClassifier() {
    Assertions.assertEquals(SCOPE, parser.parse("@s", SCOPE, SelectionParser.ArgumentContext::new));
  }

  @Test
  void applySuggestions() throws ExecutionException, InterruptedException {

    System.out.println(parser.applySuggestions(null, "123", "").get());
    System.out.println(parser.applySuggestions(null, "", "@").get());
    System.out.println(parser.applySuggestions(null, "", "@abc[").get());
    System.out.println(parser.applySuggestions(null, "", "@n[le").get());
    System.out.println(parser.applySuggestions(null, "", "@n[type=").get());
    System.out.println(parser.applySuggestions(null, "", "@n[type=le").get());
    System.out.println(parser.applySuggestions(null, "", "@n[type=le,").get());
    System.out.println(parser.applySuggestions(null, "", "@s[]").get());
    System.out.println(parser.applySuggestions(null, "", "@n").get());
  }

  private enum CharacterType {
    LETTER, NUMBER, MISC
  }

  public static class TestParser
      extends SelectionParser<String, SelectionParser.ArgumentContext<?, String>> {

    public TestParser(String identifier, String... alias) {
      super(identifier, alias);
    }

    public static class Argument<V>
        extends SelectionParser.Argument<V, String, ArgumentContext<V, String>, Argument<V>> {

      @Getter
      private final String key;

      public Argument(String key, ArgumentType<V> type) {
        super(type);
        this.key = key;
      }

      @Override
      public SelectionModification modificationType() {
        return SelectionModification.FILTER;
      }
    }
  }
}