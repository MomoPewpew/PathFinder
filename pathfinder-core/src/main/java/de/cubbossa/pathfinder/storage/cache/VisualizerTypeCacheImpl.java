package de.cubbossa.pathfinder.storage.cache;

import de.cubbossa.pathapi.misc.NamespacedKey;
import de.cubbossa.pathapi.storage.cache.VisualizerTypeCache;
import de.cubbossa.pathapi.visualizer.PathVisualizer;
import de.cubbossa.pathapi.visualizer.VisualizerType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VisualizerTypeCacheImpl implements VisualizerTypeCache {

  private final Map<NamespacedKey, VisualizerType<?>> types;

  public VisualizerTypeCacheImpl() {
    types = new HashMap<>();
  }

  @Override
  public void write(Map.Entry<NamespacedKey, VisualizerType<?>> e) {
    types.put(e.getKey(), e.getValue());
  }

  @Override
  public void invalidate(Map.Entry<NamespacedKey, VisualizerType<?>> e) {
    types.remove(e.getKey());
  }

  @Override
  public void invalidateAll() {
    types.clear();
  }

  @Override
  public Map<NamespacedKey, VisualizerType<?>> getTypes(Collection<NamespacedKey> key,
                                                        Function<Collection<NamespacedKey>, Map<NamespacedKey, VisualizerType<?>>> loader) {
    Collection<NamespacedKey> keys = new HashSet<>(key);
    keys.removeAll(types.keySet());
    types.putAll(loader.apply(keys));
    return types.entrySet().stream()
        .filter(e -> keys.contains(e.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> VisualizerType<VisualizerT> getType(
      NamespacedKey key, Function<NamespacedKey, Optional<VisualizerType<VisualizerT>>> loader) {
    VisualizerType<VisualizerT> type = (VisualizerType<VisualizerT>) types.get(key);
    if (type == null) {
      type = loader.apply(key).orElseThrow(() ->
          new IllegalStateException(
              "Could not find visualizer-type-mapping for visualizer '" + key + "'.")
      );
      types.put(key, type);
    }
    return type;
  }

  @Override
  public <VisualizerT extends PathVisualizer<?, ?>> void write(NamespacedKey key,
                                                               VisualizerType<VisualizerT> type) {
    types.put(key, type);
  }
}
