package de.cubbossa.disposables;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;

public interface BukkitDisposer extends Disposer {

  static BukkitDisposer disposer(Plugin plugin) {
    return new BukkitDisposerImpl(plugin);
  }

  default void register(World parent, Disposable disposable) {
    register(wrap(parent), disposable);
  }

  default void register(Entity parent, Disposable disposable) {
    register(wrap(parent), disposable);
  }

  default void register(Plugin parent, Disposable disposable) {
    register(wrap(parent), disposable);
  }

  default void register(InventoryView parent, Disposable disposable) {
    register(wrap(parent), disposable);
  }

  default void register(Disposable parent, World world) {
    register(parent, wrap(world));
  }

  default void register(Disposable parent, Entity entity) {
    register(parent, wrap(entity));
  }

  default void register(Disposable parent, Plugin plugin) {
    register(parent, wrap(plugin));
  }

  default void register(Disposable parent, InventoryView view) {
    register(parent, wrap(view));
  }

  Disposable wrap(World world);

  Disposable wrap(Entity entity);

  Disposable wrap(Plugin plugin);

  Disposable wrap(InventoryView plugin);
}
