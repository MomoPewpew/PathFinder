package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.PathFinder;
import de.cubbossa.pathapi.PathFinderProvider;
import de.cubbossa.pathapi.event.EventDispatcher;
import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.misc.Vector;
import de.cubbossa.pathapi.misc.World;
import de.cubbossa.pathapi.node.NodeTypeRegistry;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.messages.Messages;
import de.cubbossa.pathfinder.node.NodeHandler;
import de.cubbossa.pathfinder.node.NodeTypeRegistryImpl;
import de.cubbossa.pathfinder.node.WaypointType;
import de.cubbossa.pathfinder.nodegroup.ModifierRegistryImpl;
import de.cubbossa.pathfinder.storage.StorageImpl;
import de.cubbossa.pathfinder.storage.implementation.RemoteSqlStorage;
import de.cubbossa.pathfinder.storage.implementation.SqliteStorage;
import de.cubbossa.pathfinder.storage.implementation.WaypointStorage;
import de.cubbossa.pathfinder.storage.implementation.YmlStorage;
import de.cubbossa.pathfinder.util.VectorSplineLib;
import de.cubbossa.pathfinder.visualizer.VisualizerTypeRegistryImpl;
import de.cubbossa.splinelib.SplineLib;
import de.cubbossa.translations.GlobalTranslations;
import de.cubbossa.translations.MessageBundle;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.kyori.adventure.platform.AudienceProvider;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.util.Locale;
import java.util.UUID;

@Getter
public abstract class CommonPathFinder implements PathFinder {

  private static CommonPathFinder instance;

  public static CommonPathFinder getInstance() {
    return instance;
  }

  private static final NamespacedKey GLOBAL_GROUP_KEY = pathfinder("global");
  private static final NamespacedKey DEFAULT_VISUALIZER_KEY = pathfinder("default_visualizer");
  public static final SplineLib<Vector> SPLINES = new VectorSplineLib();

  protected NodeTypeRegistry nodeTypeRegistry;
  protected VisualizerTypeRegistry visualizerTypeRegistry;
  protected ModifierRegistry modifierRegistry;
  protected ExtensionsRegistry extensionRegistry;
  protected ConfigFileLoader configFileLoader;
  protected AudienceProvider audiences;
  protected MiniMessage miniMessage;
  protected File effectsFile;
  protected StorageImpl storage;
  @Setter
  protected PathFinderConf configuration;
  protected EventDispatcher<?> eventDispatcher;
  protected MessageBundle translations;


  public static NamespacedKey globalGroupKey() {
    return GLOBAL_GROUP_KEY;
  }

  public static NamespacedKey defaultVisualizerKey() {
    return DEFAULT_VISUALIZER_KEY;
  }

  public static NamespacedKey pathfinder(String key) {
    return new NamespacedKey("pathfinder", key);
  }

  public abstract World getWorld(UUID worldId);

  public abstract <PlayerT> PathPlayer<PlayerT> wrap(UUID playerId);

  public abstract <PlayerT> PathPlayer<PlayerT> wrap(PlayerT player);

  @SneakyThrows
  public void onLoad() {
    instance = this;
    PathFinderProvider.setPathFinder(this);

    nodeTypeRegistry = new NodeTypeRegistryImpl();
    visualizerTypeRegistry = new VisualizerTypeRegistryImpl();
    modifierRegistry = new ModifierRegistryImpl();

    storage = new StorageImpl(nodeTypeRegistry);

    configFileLoader = new ConfigFileLoader(getDataFolder(), this::saveResource);
    extensionRegistry = new ExtensionsRegistry();
    extensionRegistry.findServiceExtensions(this.getClassLoader());
    eventDispatcher = provideEventDispatcher();

    loadConfig();

    extensionRegistry.loadExtensions(this);
  }

  @SneakyThrows
  public void onEnable() {
    effectsFile = new File(getDataFolder(), "effects.nbo");

    miniMessage = MiniMessage.miniMessage();

    audiences = provideAudiences();
    Messages.setAudiences(audiences);

    // Data
    translations = GlobalTranslations.builder("PathFinder")
        .withDefaultLocale(Locale.forLanguageTag(configuration.language.fallbackLanguage))
        .withEnabledLocales(Locale.getAvailableLocales())
        .withPreferClientLanguage()
        .withLogger(getLogger())
        .withPropertiesStorage(new File(getDataFolder(), "lang"))
        .build();

    miniMessage = MiniMessage.builder()
        .editTags(builder -> builder.resolvers(translations.getResolvers()))
        .build();

    translations.addMessagesClass(Messages.class);
    translations.writeLocale(Locale.ENGLISH);

    translations.addStyle("c-brand", Style.style(TextColor.color(0x0C72C0)));
    translations.addStyle("c-brand-light", Style.style(TextColor.color(0x2c97e8)));
    translations.addStyle("c-brand-dark", Style.style(TextColor.color(0x3E4C5E)));

    translations.addStyle("c-offset", Style.style(TextColor.color(0xFF8C42)));
    translations.addStyle("c-offset-light", Style.style(TextColor.color(0xffa266)));
    translations.addStyle("c-offset-dark", Style.style(TextColor.color(0xF26419)));

    translations.addStyle("c-accent", Style.style(TextColor.color(0xABDF75)));
    translations.addStyle("c-accent-light", Style.style(TextColor.color(0xcaf79c)));
    translations.addStyle("c-accent-dark", Style.style(TextColor.color(0x74b035)));

    translations.addStyle("t", Style.style(NamedTextColor.GRAY));
    translations.addStyle("t-light", Style.style(NamedTextColor.WHITE));
    translations.addStyle("t-dark", Style.style(NamedTextColor.DARK_GRAY));
    translations.addStyle("t-warm", Style.style(TextColor.color(0xE5D4C0)));
    translations.addStyle("t-highlight", translations.getStyles().get("c-offset"));
    translations.addStyle("t-hl", translations.getStyles().get("c-offset"));

    translations.addStyle("bg-light", Style.style(NamedTextColor.GRAY));
    translations.addStyle("bg", Style.style(NamedTextColor.DARK_GRAY));
    translations.addStyle("bg-dark", Style.style(NamedTextColor.BLACK));

    translations.addStyle("c-empty", Style.style(TextColor.color(0x554640)));
    translations.addStyle("c-warn", Style.style(NamedTextColor.YELLOW));
    translations.addStyle("c-negative", Style.style(NamedTextColor.RED));

    Messages.formatter().setMiniMessage(miniMessage);
    Messages.formatter().setNullStyle(translations.getStyles().get("c-offset-dark"));
    Messages.formatter().setTextStyle(translations.getStyles().get("c-offset"));
    Messages.formatter().setNumberStyle(translations.getStyles().get("c-offset-light"));

    new File(getDataFolder(), "data/").mkdirs();
    StorageImplementation impl = switch (configuration.database.type) {
      case SQLITE -> new SqliteStorage(configuration.database.embeddedSql.file, nodeTypeRegistry,
          modifierRegistry, visualizerTypeRegistry);
      case REMOTE_SQL -> new RemoteSqlStorage(configuration.database.remoteSql, nodeTypeRegistry,
          modifierRegistry, visualizerTypeRegistry);
      default -> new YmlStorage(new File(getDataFolder(), "data/"), nodeTypeRegistry,
          visualizerTypeRegistry, modifierRegistry);
    };
    impl.setWorldLoader(this::getWorld);
    impl.setLogger(getLogger());

    storage.setImplementation(impl);
    storage.setEventDispatcher(eventDispatcher);
    storage.setLogger(getLogger());
    storage.init();

    nodeTypeRegistry.register(new WaypointType(
        new WaypointStorage(storage),
        miniMessage
    ));

    new NodeHandler(this);
    extensionRegistry.enableExtensions(this);
  }

  @SneakyThrows
  public void onDisable() {
    NodeHandler.getInstance().cancelAllEditModes();
    extensionRegistry.disableExtensions(this);
    storage.shutdown();
  }

  public void loadConfig() {
    configuration = configFileLoader.loadConfig();
  }

  @Override
  public VisualizerTypeRegistry getVisualizerTypeRegistry() {
    return VisualizerTypeRegistryImpl.getInstance();
  }

  abstract AudienceProvider provideAudiences();

  abstract EventDispatcher provideEventDispatcher();

  abstract void saveResource(String name, boolean override);
}
