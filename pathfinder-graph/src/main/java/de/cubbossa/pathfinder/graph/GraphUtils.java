package de.cubbossa.pathfinder.graph;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class GraphUtils {

  public static <N, V> MutableValueGraph<N, V> mutable(ValueGraph<N, V> graph) {
    if (graph instanceof MutableValueGraph<N, V> mGraph) {
      return mGraph;
    }
    MutableValueGraph<N, V> g = ValueGraphBuilder.from(graph).build();
    graph.nodes().forEach(g::addNode);
    for (EndpointPair<N> e : graph.edges()) {
      g.edgeValue(e.nodeU(), e.nodeV()).ifPresent(v -> g.putEdgeValue(e.nodeU(), e.nodeV(), v));
    }
    return g;
  }

  public static <N, V> ValueGraph<N, V> merge(Iterable<ValueGraph<N, V>> islands) {
    var iterator = islands.iterator();
    MutableValueGraph<N, V> g = mutable(iterator.next());
    while (iterator.hasNext()) {
      var other = iterator.next();
      for (N node : other.nodes()) {
        g.addNode(node);
      }
      for (EndpointPair<N> edge : other.edges()) {
        V val = other.edgeValue(edge.nodeU(), edge.nodeV()).orElseThrow();
        g.putEdgeValue(edge.nodeU(), edge.nodeV(), val);
      }
    }
    return g;
  }

  public static <N, V> ValueGraph<N, V> merge(ValueGraph<N, V> a, ValueGraph<N, V> b) {
    MutableValueGraph<N, V> ma = mutable(a);
    for (N node : b.nodes()) {
      ma.addNode(node);
    }
    for (EndpointPair<N> edge : b.edges()) {
      V val = b.edgeValue(edge.nodeU(), edge.nodeV()).orElseThrow();
      ma.putEdgeValue(edge.nodeU(), edge.nodeV(), val);
    }
    return ma;
  }

  public static <N, V> Collection<ValueGraph<N, V>> islands(ValueGraph<N, V> graph) {
    Set<N> all = new HashSet<>(graph.nodes());
    Collection<ValueGraph<N, V>> results = new ArrayList<>();

    while (!all.isEmpty()) {
      N any = all.stream().findFirst().orElse(null);
      if (any == null) {
        return List.of(graph);
      }
      Set<N> island = Graphs.reachableNodes(graph.asGraph(), any);
      all.removeAll(island);
      results.add(Graphs.inducedSubgraph(graph, island));
    }
    return results;
  }
}
