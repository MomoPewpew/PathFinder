package de.cubbossa.pathfinder.util;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SelectionParserTest {

	public static final SelectionParser.Filter<String, SelectionParser.Context> LENGTH = new SelectionParser.Filter<>("length", Pattern.compile("(\\.\\.)?[0-9]+(\\.\\.)?"), (in, context) -> {
		boolean smaller = context.value().startsWith("..");
		boolean larger = context.value().endsWith("..");
		if (smaller && larger) {
			return in;
		}
		String arg = context.value();
		if (smaller) {
			arg = arg.substring(2);
		}
		if (larger) {
			arg = arg.substring(0, arg.length() - 2);
		}
		float req = Integer.parseInt(arg);
		return in.stream().filter(words -> {
			int len = words.length();
			return smaller && len <= req || larger && len >= req || len == req;
		}).collect(Collectors.toList());
	}, c -> Lists.newArrayList("..1", "3", "2.."));

	public static final SelectionParser.Filter<String, SelectionParser.Context> TYPE = new SelectionParser.Filter<>("type", Pattern.compile("letter|number"), (strings, context) -> {
		return switch (context.value()) {
			case "letter" -> strings.stream().filter(string -> string.matches("[a-zA-Z]+")).collect(Collectors.toList());
			case "number" -> strings.stream().filter(string -> string.matches("[0-9]+")).collect(Collectors.toList());
			default -> strings;
		};
	}, c -> Lists.newArrayList("letter", "number"));

	private static final List<String> SCOPE = Lists.newArrayList(
			"A", "B", "C", "D", "E",
			"Word", "OtherWord", "XYZ",
			"123", "00000000",
			"            ", " ", "",
			"More words than one", "Another sentence"
	);
	private static SelectionParser<String, SelectionParser.Context> parser;

	@BeforeAll
	static void setup() {
		parser = new SelectionParser<>(SelectionParser.Context::new, "s");
		parser.addSelector(LENGTH);
		parser.addSelector(TYPE);
	}

	@Test
	@SneakyThrows
	public void testParseSelection1() {

		Assertions.assertEquals(
				Lists.newArrayList("A", "B", "C", "D", "E", " ", ""),
				parser.parseSelection(SCOPE, "@s[length=..1]", ArrayList::new));
	}

	@Test
	@SneakyThrows
	public void testParseSelection2() {

		Assertions.assertEquals(
				Lists.newArrayList("OtherWord", "00000000", "            ", "More words than one", "Another sentence"),
				parser.parseSelection(SCOPE, "@s[length=5..]", ArrayList::new));
	}

	@Test
	@SneakyThrows
	public void testParseSelection3() {

		Assertions.assertEquals(
				Lists.newArrayList("OtherWord"),
				parser.parseSelection(SCOPE, "@s[length=5..,type=letter]", ArrayList::new));
	}

	@Test
	@SneakyThrows
	public void testParseSelection4() {

		Assertions.assertEquals(
				Lists.newArrayList("123"),
				parser.parseSelection(SCOPE, "@s[length=..5,type=number]", ArrayList::new));
	}
}