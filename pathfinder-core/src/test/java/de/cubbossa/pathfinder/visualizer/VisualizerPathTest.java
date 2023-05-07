package de.cubbossa.pathfinder.visualizer;

import de.cubbossa.pathapi.group.NodeGroup;
import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.misc.PathPlayer;
import de.cubbossa.pathapi.node.Node;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathfinder.PathFinderTest;
import de.cubbossa.pathfinder.node.implementation.Waypoint;
import de.cubbossa.pathfinder.nodegroup.modifier.VisualizerModifier;
import de.cubbossa.pathfinder.test.TestPlayer;
import de.cubbossa.pathfinder.visualizer.impl.ParticleVisualizer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VisualizerPathTest extends PathFinderTest {

  private final Map<PathVisualizer<?, ?>, NodeGroup> groupMap;

  public VisualizerPathTest() {
    groupMap = new HashMap<>();
    setupMiniMessage();
    setupWorldMock("test");
  }

  private Waypoint pathNode(PathVisualizer<?, ?>... visualizers) {
    Waypoint waypoint = makeWaypoint();
    for (PathVisualizer<?, ?> vis : visualizers) {
      NodeGroup group = groupMap.computeIfAbsent(vis, v -> {
        NodeGroup g = makeGroup(v.getKey());
        g.addModifier(new VisualizerModifier(v));
        storage.saveGroup(g).join();
        return g;
      });
      waypoint.addGroup(group);
    }
    storage.saveNode(waypoint).join();
    return waypoint;
  }

  private ParticleVisualizer particle(String key) {
    return new ParticleVisualizer(NamespacedKey.fromString("pathfinder:" + key), "test");
  }

  @AfterAll
  public static void afterAll() {
    runAfterAll();
  }

  @BeforeEach
  public void beforeEach() {
    setupInMemoryStorage();
  }

  @AfterEach
  public void afterEach() {
    shutdownStorage();
  }

  @Test
  void prepare1() {
    ParticleVisualizer vis = particle("a");
    List<Node> path = List.of(
        pathNode(vis),
        pathNode(vis)
    );

    PathPlayer<Object> p = new TestPlayer();
    VisualizerPath<Object> visPath = new VisualizerPath<>(p);
    assertFalse(visPath.isActive());
    assertEquals(0, visPath.paths.size());

    visPath.prepare(path);
    assertFalse(visPath.isActive());
    assertEquals(1, visPath.paths.size());
    assertEquals(vis.getKey(), visPath.paths.stream().findAny().orElseThrow().visualizer.getKey());
  }

  @Test
  void prepare2() {
    ParticleVisualizer a = particle("a");
    ParticleVisualizer b = particle("b");
    List<Node> path = List.of(
        pathNode(a),
        pathNode(a, b),
        pathNode(b)
    );

    PathPlayer<Object> p = new TestPlayer();
    VisualizerPath<Object> visPath = new VisualizerPath<>(p);
    assertFalse(visPath.isActive());
    assertEquals(0, visPath.paths.size());

    visPath.prepare(path);
    assertFalse(visPath.isActive());
    assertEquals(2, visPath.paths.size());
    assertTrue(visPath.paths.stream().anyMatch(subPath -> subPath.visualizer.equals(a)));
    assertTrue(visPath.paths.stream().anyMatch(subPath -> subPath.visualizer.equals(b)));
  }

  @Test
  void prepare3() {
    ParticleVisualizer a = particle("a");
    ParticleVisualizer b = particle("b");
    List<Node> path = List.of(
        pathNode(a),
        pathNode(b),
        pathNode(a)
    );

    PathPlayer<Object> p = new TestPlayer();
    VisualizerPath<Object> visPath = new VisualizerPath<>(p);
    assertFalse(visPath.isActive());
    assertEquals(0, visPath.paths.size());

    visPath.prepare(path);
    assertFalse(visPath.isActive());
    assertEquals(3, visPath.paths.size());
    assertEquals(2, visPath.paths.stream().filter(subPath -> subPath.visualizer.equals(a)).count());
    assertEquals(1, visPath.paths.stream().filter(subPath -> subPath.visualizer.equals(b)).count());
  }
}