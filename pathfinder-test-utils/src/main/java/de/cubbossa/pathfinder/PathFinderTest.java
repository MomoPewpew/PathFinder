package de.cubbossa.pathfinder;

import de.cubbossa.pathapi.group.ModifierRegistry;
import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.Location;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.World;
import de.cubbossa.pathapi.node.Edge;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.node.NodeType;
import de.cubbossa.pathapi.storage.StorageImplementation;
import de.cubbossa.pathapi.storage.WorldLoader;
import de.cubbossa.pathapi.visualizer.VisualizerTypeRegistry;
import de.cubbossa.pathfinder.node.NodeTypeRegistryImpl;
import de.cubbossa.pathfinder.node.SimpleEdge;
import de.cubbossa.pathfinder.node.WaypointType;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.nodegroup.ModifierRegistryImpl;
import de.cubbossa.pathfinder.storage.InternalVisualizerDataStorage;
import de.cubbossa.pathfinder.storage.StorageImpl;
import de.cubbossa.pathfinder.storage.cache.CacheLayerImpl;
import de.cubbossa.pathfinder.storage.implementation.SqlStorage;
import de.cubbossa.pathfinder.storage.implementation.WaypointStorage;
import de.cubbossa.pathfinder.util.NodeSelection;
import de.cubbossa.pathfinder.visualizer.VisualizerTypeRegistryImpl;
import de.cubbossa.pathfinder.visualizer.impl.InternalVisualizerStorage;
import lombok.SneakyThrows;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.Nullable;
import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.Assertions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.logging.Logger;

public abstract class PathFinderTest {

  public static final WorldLoader WORLD_LOADER = uuid -> new World() {
    @Override
    public UUID getUniqueId() {
      return uuid;
    }

    @Override
    public String getName() {
      return getUniqueId().toString();
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof World w && uuid.equals(w.getUniqueId());
    }
  };
  protected static World world;
  protected static Logger logger = Logger.getLogger("TESTS");
  protected static MiniMessage miniMessage;
  protected StorageImpl storage;
  protected NodeTypeRegistryImpl nodeTypeRegistry;
  protected VisualizerTypeRegistry visualizerTypeRegistry;
  protected ModifierRegistry modifierRegistry;
  protected NodeType<Waypoint> waypointNodeType;
  protected TestVisualizerType visualizerType;

  public void setupWorldMock() {
    UUID uuid = UUID.randomUUID();
    world = WORLD_LOADER.loadWorld(uuid);
  }

  public void setupMiniMessage() {
    miniMessage = MiniMessage.miniMessage();
  }

  @SneakyThrows
  public void setupStorage(boolean cached, Supplier<StorageImplementation> factory) {
    nodeTypeRegistry = new NodeTypeRegistryImpl();
    modifierRegistry = new ModifierRegistryImpl();
    modifierRegistry.registerModifierType(new TestModifierType());
    visualizerTypeRegistry = new VisualizerTypeRegistryImpl();

    storage = new StorageImpl(nodeTypeRegistry);
    StorageImplementation implementation = factory.get();
    implementation.setLogger(logger);
    implementation.setWorldLoader(WORLD_LOADER);

    storage.setImplementation(implementation);
    storage.setLogger(logger);
    storage.setCache(cached ? new CacheLayerImpl() : CacheLayerImpl.empty());

    waypointNodeType = new WaypointType(new WaypointStorage(storage), miniMessage);
    nodeTypeRegistry.register(waypointNodeType);

    visualizerType = new TestVisualizerType(CommonPathFinder.pathfinder("particle"));
    if (implementation instanceof InternalVisualizerDataStorage visualizerDataStorage) {
      visualizerType.setStorage(new InternalVisualizerStorage<>(visualizerType, visualizerDataStorage));
    }
    visualizerTypeRegistry.registerVisualizerType(visualizerType);

    storage.init();
    storage.createGlobalNodeGroup(visualizerType).join();
  }

  public StorageImplementation inMemoryStorage() {
    SqlStorage implementation = new SqlStorage(SQLDialect.H2, nodeTypeRegistry, modifierRegistry, visualizerTypeRegistry) {
      @Override
      public ConnectionProvider getConnectionProvider() {
        final Connection connection;
        try {
          Class.forName("org.h2.jdbcx.JdbcDataSource");
          connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        } catch (SQLException | ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
        return new ConnectionProvider() {
          @Override
          public @Nullable Connection acquire() throws DataAccessException {
            return connection;
          }

          @Override
          public void release(Connection connection) throws DataAccessException {
          }
        };
      }

      @Override
      public void shutdown() {
        try {
          getConnectionProvider().acquire().prepareStatement("DROP ALL OBJECTS").execute();
          getConnectionProvider().acquire().close();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    };
    implementation.setLogger(Logger.getLogger("TESTS"));
    return implementation;
  }

  public void setupInMemoryStorage() {
    setupStorage(true, this::inMemoryStorage);
  }

  public void shutdownStorage() {
    storage.shutdown();
  }

  protected <T> T assertResult(Supplier<CompletableFuture<T>> supplier) {
    CompletableFuture<T> future = supplier.get();
    T element = future.join();

    Assertions.assertFalse(future.isCompletedExceptionally());
    Assertions.assertNotNull(element);
    return element;
  }

  protected void assertFuture(Supplier<CompletableFuture<Void>> supplier) {
    CompletableFuture<Void> future = supplier.get();
    future.join();
    Assertions.assertFalse(future.isCompletedExceptionally());
  }

  protected Waypoint makeWaypoint() {
    return assertResult(() -> storage
        .createAndLoadNode(waypointNodeType, new Location(1, 2, 3, world))
        .thenCompose(storage::insertGlobalGroupAndSave));
  }

  protected void deleteWaypoint(Waypoint waypoint) {
    assertFuture(() -> storage.deleteNodes(new NodeSelection(waypoint).ids()));
  }

  protected <N extends Node> N assertNodeExists(UUID node) {
    return (N) storage.loadNode(node).join().orElseThrow();
  }

  protected void assertNodeNotExists(UUID node) {
    Assertions.assertThrows(Exception.class,
        () -> storage.loadNode(node).join().orElseThrow());
  }

  protected void assertNodeCount(int count) {
    Collection<Node> nodesAfter = storage.loadNodes().join();
    Assertions.assertEquals(count, nodesAfter.size());
  }

  protected Edge makeEdge(Waypoint start, Waypoint end) {
    Edge edge = new SimpleEdge(start.getNodeId(), end.getNodeId(), 1.23f);
    assertFuture(() -> storage.modifyNode(start.getNodeId(), node -> {
      node.getEdges().add(edge);
    }));
    assertEdge(start.getNodeId(), end.getNodeId());
    return edge;
  }

  protected void assertEdge(UUID start, UUID end) {
    Assertions.assertTrue(storage.loadNode(start).join().orElseThrow().hasConnection(end));
  }

  protected void assertNoEdge(UUID start, UUID end) {
    Optional<Node> node = storage.loadNode(start).join();
    if (node.isEmpty()) {
      return;
    }
    Assertions.assertFalse(node.get().hasConnection(end));
  }

  protected NodeGroup makeGroup(NamespacedKey key) {
    return assertResult(() -> storage.createAndLoadGroup(key));
  }

  protected void deleteGroup(NamespacedKey key) {
    assertFuture(() -> storage.loadGroup(key)
        .thenAccept(group -> storage.deleteGroup(group.orElseThrow()).join()));
  }

  protected NodeGroup assertGroupExists(NamespacedKey key) {
    return assertOptResult(() -> storage.loadGroup(key));
  }

  protected void assertGroupNotExists(NamespacedKey key) {
    Assertions.assertThrows(Exception.class,
        () -> storage.loadGroup(key).join().orElseThrow());
  }

  protected TestVisualizer makeVisualizer(NamespacedKey key) {
    return assertResult(() -> storage.createAndLoadVisualizer(visualizerType, key));
  }

  protected <T> T assertOptResult(Supplier<CompletableFuture<Optional<T>>> supplier) {
    return assertResult(supplier).orElseThrow();
  }
}