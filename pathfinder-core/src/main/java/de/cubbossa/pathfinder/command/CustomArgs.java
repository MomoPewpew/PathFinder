package de.cubbossa.pathfinder.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Keyed;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.Pagination;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.storage.Storage;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import de.cubbossa.pathfinder.BukkitPathFinder;
import de.cubbossa.pathfinder.module.FindModule;
import de.cubbossa.pathfinder.navigationquery.FindQueryParser;
import de.cubbossa.pathfinder.nodegroup.modifier.DiscoverableModifier;
import de.cubbossa.pathfinder.nodegroup.modifier.NavigableModifier;
import de.cubbossa.pathfinder.util.BukkitUtils;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.util.SelectionUtils;
import de.cubbossa.pathfinder.util.VectorUtils;
import de.cubbossa.pathfinder.visualizer.VisualizerHandler;
import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.arguments.*;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.cubbossa.pathfinder.command.CommandArgument.arg;

/**
 * A collection of custom command arguments for the CommandAPI.
 */
@UtilityClass
public class CustomArgs {

  private static final Collection<String> TAGS =
      Lists.newArrayList("<rainbow>", "<gradient>", "<click>", "<hover>",
          "<rainbow:", "<gradient:", "<click:", "<hover:");
  private static final Pattern MINI_FINISH = Pattern.compile(".*(</?[^<>]*)");
  private static final Pattern MINI_CLOSE = Pattern.compile(".*<([^/<>:]+)(:[^/<>]+)?>[^/<>]*");
  private static final List<Character> LIST_SYMBOLS = Lists.newArrayList('!', '&', '|', ')', '(');
  private static final List<String> LIST_SYMBOLS_STRING =
      Lists.newArrayList("!", "&", "|", ")", "(");

  static {
    TAGS.addAll(NamedTextColor.NAMES.keys().stream()
        .map(s -> "<" + s + ">").toList());
    TAGS.addAll(TextDecoration.NAMES.keys().stream()
        .map(s -> "<" + s + ">").toList());
  }

  public CommandArgument<Player, PlayerArgument> player(String node) {
    return new CommandArgument<>(new PlayerArgument(node));
  }

  public CommandArgument<PathPlayer<Player>, CustomArgument<PathPlayer<Player>, Player>> pathPlayer(String node) {
    return new CommandArgument<>(new CustomArgument<>(new PlayerArgument(node), info -> {
      return BukkitUtils.wrap(info.currentInput());
    }));
  }

  public CommandArgument<de.cubbossa.pathapi.misc.Location, CustomArgument<de.cubbossa.pathapi.misc.Location, Location>> location(
      String node, LocationType type) {
    return arg(new CustomArgument<>(new LocationArgument(node, type), customArgumentInfo -> {
      return VectorUtils.toInternal(customArgumentInfo.currentInput());
    }));
  }

  public CommandArgument<de.cubbossa.pathapi.misc.Location, CustomArgument<de.cubbossa.pathapi.misc.Location, Location>> location(
      String node) {
    return location(node, LocationType.PRECISE_POSITION);
  }


  public CustomLiteralArgument literal(String literal) {
    return new CustomLiteralArgument(literal);
  }

  public CommandArgument<Integer, IntegerArgument> integer(String node) {
    return new CommandArgument<>(new IntegerArgument(node));
  }

  public CommandArgument<Integer, IntegerArgument> integer(String node, int min) {
    return new CommandArgument<>(new IntegerArgument(node, min));
  }

  public CommandArgument<Integer, IntegerArgument> integer(String node, int min, int max) {
    return new CommandArgument<>(new IntegerArgument(node, min, max));
  }

  /**
   * An argument that suggests and resolves enum fields in lower case syntax.
   *
   * @param nodeName The name of the command argument in the command structure
   * @param scope    The enum class instance
   * @param <E>      The enum type
   * @return The argument
   */
  public <E extends Enum<E>> Argument<E> enumArgument(String nodeName, Class<E> scope) {
    return arg(new CustomArgument<>(new StringArgument(nodeName), info -> {
      try {
        return Enum.valueOf(scope, info.input().toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new CustomArgument.CustomArgumentException("Invalid input value: " + info.input());
      }
    })).includeSuggestions((suggestionInfo, suggestionsBuilder) -> {
      Arrays.stream(scope.getEnumConstants())
          .map(Enum::toString)
          .map(String::toLowerCase)
          .forEach(suggestionsBuilder::suggest);
      return suggestionsBuilder.buildFuture();
    });
  }

  public CommandArgument<Pagination, CustomArgument<Pagination, Integer>> pagination(int size) {
    return arg(new CustomArgument<>(new IntegerArgument("page", 1), p -> {
      return Pagination.page(p.currentInput() - 1, size);
    }));
  }

  /**
   * Provides a MiniMessage Argument, which autocompletes xml tags and contains all default tags
   * that come along with MiniMessage.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return a MiniMessage argument instance
   */
  public Argument<String> miniMessageArgument(String nodeName) {
    return miniMessageArgument(nodeName, suggestionInfo -> new ArrayList<>());
  }

  /**
   * Provides a MiniMessage Argument, which autocompletes xml tags and contains all default tags
   * that come along with MiniMessage.
   *
   * @param nodeName The name of the command argument in the command structure
   * @param supplier Used to insert custom tags into the suggestions
   * @return a MiniMessage argument instance
   */
  public Argument<String> miniMessageArgument(
      String nodeName,
      Function<SuggestionInfo, Collection<String>> supplier
  ) {
    return arg(new GreedyStringArgument(nodeName)).replaceSuggestions((info, builder) -> {

      int offset = builder.getInput().length();
      String[] splits = info.currentInput().split(" ", -1);
      String in = splits[splits.length - 1];
      StringRange range = StringRange.between(offset - in.length(), offset);

      List<Suggestion> suggestions = new ArrayList<>();
      StringRange r = range;
      supplier.apply(info).stream()
          .filter(string -> string.startsWith(in))
          .map(string -> new Suggestion(r, string))
          .forEach(suggestions::add);

      Matcher m = MINI_FINISH.matcher(info.currentInput());
      if (m.matches()) {
        MatchResult result = m.toMatchResult();
        range = StringRange.between(result.start(1), result.end(1));
        String filter = result.group(1);
        StringRange finalRange = range;
        TAGS.stream()
            .filter(s -> s.startsWith(filter))
            .map(s -> new Suggestion(finalRange, s))
            .forEach(suggestions::add);

        suggestions.add(new Suggestion(range, m.group(1) + ">"));
      } else {
        Matcher matcher = MINI_CLOSE.matcher(info.currentArg());
        if (matcher.matches()) {
          suggestions.add(new Suggestion(StringRange.at(offset), "</" + matcher.group(1) + ">"));
        }
      }
      return CompletableFuture.completedFuture(Suggestions.create(builder.getInput(), suggestions));
    });
  }

  /**
   * Provides a path visualizer argument, which suggests the namespaced keys for all path
   * visualizers and resolves the user input into the actual visualizer instance.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return a path visualizer argument instance
   */
  public Argument<? extends PathVisualizer<?, ?>> pathVisualizerArgument(String nodeName) {
    return arg(new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {
      Optional<?> vis =
          PathFinderProvider.get().getStorage()
              .loadVisualizer(BukkitPathFinder.convert(customArgumentInfo.currentInput()))
              .join();
      if (vis.isEmpty()) {
        throw new CustomArgument.CustomArgumentException("There is no visualizer with this key.");
      }
      return (PathVisualizer<?, ?>) vis.get();
    })).includeSuggestions(suggestNamespacedKeys(sender ->
        PathFinderProvider.get().getStorage().loadVisualizers().thenApply(v -> v.stream()
            .map(Keyed::getKey)
            .toList()))
    );
  }

  /**
   * Provides a path visualizer argument, which suggests the namespaced keys for all path
   * visualizers of the provided visualizer type and resolves the user input into the actual
   * visualizer instance.
   *
   * @param nodeName The name of the command argument in the command structure
   * @param type     The type that all suggested and parsed visualizers are required to have
   * @return a path visualizer argument instance
   */
  public <T extends PathVisualizer<?, ?>> Argument<T> pathVisualizerArgument(String nodeName,
                                                                             VisualizerType<T> type) {
    return arg(new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {
      Optional<T> vis = (Optional<T>) PathFinderProvider.get().getStorage()
          .loadVisualizer(BukkitPathFinder.convert(customArgumentInfo.currentInput())).join();
      if (vis.isEmpty()) {
        throw new CustomArgument.CustomArgumentException("There is no visualizer with this key.");
      }
      return (T) vis.get();
    })).includeSuggestions(suggestNamespacedKeys(sender ->
        PathFinderProvider.get().getStorage().loadVisualizers(type)
            .thenApply(pathVisualizers -> pathVisualizers.values().stream()
                .map(Keyed::getKey)
                .toList()))
    );
  }

  /**
   * Suggests a set of NamespacedKeys where the completion also includes matches for only the
   * key.
   *
   * @return the argument suggestions object to insert
   */
  public ArgumentSuggestions<CommandSender> suggestNamespacedKeys(
      Function<CommandSender, CompletableFuture<Collection<NamespacedKey>>> keysSupplierFuture) {
    return (suggestionInfo, suggestionsBuilder) -> {
      return keysSupplierFuture.apply(suggestionInfo.sender()).thenApply(keys -> {
        String[] splits = suggestionInfo.currentInput().split(" ", -1);
        String in = splits[splits.length - 1];
        int len = suggestionInfo.currentInput().length();
        StringRange range = StringRange.between(len - in.length(), len);

        List<Suggestion> suggestions = keys.stream()
            .filter(key -> key.getKey().startsWith(in) || key.getNamespace().startsWith(in))
            .map(NamespacedKey::toString)
            .map(s -> new Suggestion(range, s))
            .collect(Collectors.toList());

        return Suggestions.create(suggestionsBuilder.getInput(), suggestions);
      });
    };
  }

  /**
   * Provides a node type argument, which suggests the keys of all registered node types and
   * resolves the user input into the node type instance.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return a node type argument instance
   */
  public <N extends Node> Argument<NodeType<N>> nodeTypeArgument(
      String nodeName) {
    return arg(new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {
      NodeType<N> type =
          PathFinderProvider.get().getNodeTypeRegistry()
              .getType(BukkitPathFinder.convert(customArgumentInfo.currentInput()));
      if (type == null) {
        throw new CustomArgument.CustomArgumentException(
            "Node type with key '" + customArgumentInfo.currentInput() + "' does not exist.");
      }
      return type;
    })).includeSuggestions(
        suggestNamespacedKeys(sender -> CompletableFuture.completedFuture(
            PathFinderProvider.get().getNodeTypeRegistry().getTypeKeys())));
  }

  /**
   * Provides a node selection argument.
   * This comes with a custom syntax that is a copy of the vanilla entity selectors.
   * There are a variety of filters to apply to the search, an example user input could be
   * "@n[distance=..10]", which returns all nodes within a range of 10 blocks.
   * This includes ALL nodes of all roadmaps.
   * All filters can be seen in {@link SelectionUtils#SELECTORS}
   *
   * @param nodeName The name of the command argument in the command structure
   * @return a node selection argument instance
   */
  public CommandArgument<NodeSelection, CustomArgument<NodeSelection, String>> nodeSelectionArgument(
      String nodeName) {
    return (CommandArgument<NodeSelection, CustomArgument<NodeSelection, String>>) arg(
        new CustomArgument<>(new TextArgument(nodeName), info -> {
          if (info.sender() instanceof Player player) {
            try {
              return SelectionUtils.getNodeSelection(player,
                  info.input().substring(1, info.input().length() - 1));
            } catch (CommandSyntaxException | ParseCancellationException e) {
              throw new CustomArgument.CustomArgumentException(e.getMessage());
            }
          }
          return new NodeSelection();
        })).includeSuggestions(SelectionUtils::getNodeSelectionSuggestions);
  }

  /**
   * Provides a node group argument, which suggests the keys of all node groups and resolves the
   * user input into the node group instance.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return a node group argument instance
   */
  public CommandArgument<NodeGroup, CustomArgument<NodeGroup, NamespacedKey>> nodeGroupArgument(
      String nodeName) {
    return (CommandArgument<NodeGroup, CustomArgument<NodeGroup, NamespacedKey>>) arg(
        new CustomArgument<>(new NamespacedKeyArgument(nodeName), info -> {
          return PathFinderProvider.get().getStorage()
              .loadGroup(BukkitPathFinder.convert(info.currentInput())).join()
              .orElseThrow();
        })
    ).replaceSuggestions(suggestNamespacedKeys(
        sender -> PathFinderProvider.get().getStorage().loadAllGroups().thenApply(nodeGroups ->
            nodeGroups.stream().map(NodeGroup::getKey).toList())
    ));
  }

  /**
   * A command argument that resolves and suggests Discoverables as their namespaced keys.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return The CustomArgument instance
   */
  public CommandArgument<NamespacedKey, Argument<NamespacedKey>> discoverableArgument(
      String nodeName) {
    return arg(new CustomArgument<>(new NamespacedKeyArgument(nodeName),
        i -> BukkitPathFinder.convert(i.currentInput())).includeSuggestions(
        suggestNamespacedKeys(sender -> PathFinderProvider.get().getStorage().loadAllGroups()
            .thenApply(nodeGroups -> nodeGroups.stream()
                .filter(g -> g.hasModifier(DiscoverableModifier.class))
                .map(NodeGroup::getKey)
                .collect(Collectors.toList())))));
  }

  /**
   * A command argument that resolves a navigation query and suggests the according search terms.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return The CustomArgument instance
   */
  public Argument<NodeSelection> navigateSelectionArgument(String nodeName) {
    return arg(new CustomArgument<>(new GreedyStringArgument(nodeName), context -> {
      if (!(context.sender() instanceof Player player)) {
        throw new CustomArgument.CustomArgumentException("Only for players");
      }
      String search = context.currentInput();
      Storage storage = PathFinderProvider.get().getStorage();
      List<Node> scope = storage.loadNodes().join().stream().filter(node -> {
        // Create context for request
        FindModule.NavigationRequestContext c = new FindModule.NavigationRequestContext(player.getUniqueId(), node);
        // Find a node that matches all required filters
        // return FindModule.getInstance().getNavigationFilter().stream().allMatch(predicate -> predicate.test(c));
        return true;
      }).toList();

      try {
        Map<Node, NavigableModifier> map = storage.loadNodes(NavigableModifier.class).join();
        Collection<Node> target = new FindQueryParser().parse(search, scope, n -> {
          NavigableModifier mod = map.get(n);
          return mod == null ? Collections.emptySet() : mod.getSearchTerms();
        });
        return new NodeSelection(target);
      } catch (Throwable t) {
        t.printStackTrace();
        throw new RuntimeException(t);
      }
    }))
        .includeSuggestions((suggestionInfo, suggestionsBuilder) -> {
          if (!(suggestionInfo.sender() instanceof Player)) {
            return suggestionsBuilder.buildFuture();
          }
          String input = suggestionsBuilder.getInput();

          int lastIndex = LIST_SYMBOLS.stream()
              .map(input::lastIndexOf)
              .mapToInt(value -> value)
              .max()
              .orElse(0);
          lastIndex = Integer.max(
              suggestionsBuilder.getInput().length() - suggestionsBuilder.getRemaining().length(),
              lastIndex + 1);

          StringRange range = StringRange.between(lastIndex, input.length());
          List<Suggestion> suggestions = new ArrayList<>();

          StringRange finalRange = range;
          String inRange = finalRange.get(input);

          Collection<String> allTerms = new HashSet<>();
          PathFinderProvider.get().getStorage().loadNodes(NavigableModifier.class).thenAccept(map -> {
            map.forEach((node, navigableModifier) -> {
              allTerms.addAll(navigableModifier.getSearchTermStrings());
            });
          }).join();

          allTerms.stream().filter(s -> s.startsWith(inRange))
              .map(s -> new Suggestion(finalRange, s))
              .forEach(suggestions::add);


          if (suggestions.isEmpty()) {
            range = StringRange.at(suggestionInfo.currentInput().length() - 1);
            for (String s : LIST_SYMBOLS_STRING) {
              suggestions.add(new Suggestion(range, s));
            }
          }

          return CompletableFuture.completedFuture(new Suggestions(range, suggestions));
        });
  }

  /**
   * Provides a visualizer type argument, which suggests the keys of all registered visualizer types
   * and resolves the user input into the visualizer type instance.
   *
   * @param nodeName The name of the command argument in the command structure
   * @return a visualizer type argument instance
   */
  public Argument<VisualizerType<PathVisualizer<?, ?>>> visualizerTypeArgument(
      String nodeName) {
    return arg(new CustomArgument<>(new NamespacedKeyArgument(nodeName), customArgumentInfo -> {

      Optional<VisualizerType<PathVisualizer<?, ?>>> type =
          VisualizerHandler.getInstance()
              .getType(BukkitPathFinder.convert(customArgumentInfo.currentInput()));
      if (type.isEmpty()) {
        throw new CustomArgument.CustomArgumentException(
            "Unknown type: '" + customArgumentInfo.currentInput() + "'.");
      }
      return type.get();
    })).includeSuggestions(suggestNamespacedKeys(sender -> {
      return CompletableFuture.completedFuture(PathFinderProvider.get().getVisualizerTypeRegistry().getTypes().keySet());
    }));
  }
}
