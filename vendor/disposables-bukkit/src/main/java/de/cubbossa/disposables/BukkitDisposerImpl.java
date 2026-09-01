package de.cubbossa.disposables;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;

public class BukkitDisposerImpl extends DisposerImpl implements BukkitDisposer {

  private final Plugin plugin;
  private final PlayerQuitListener playerQuitListener;
  private final WorldUnloadListener worldUnloadListener;
  private final PluginDisableListener pluginDisableListener;
  private final InventoryCloseListener inventoryCloseListener;

  protected BukkitDisposerImpl(Plugin plugin) {
    this.plugin = plugin;

    playerQuitListener = new PlayerQuitListener(this);
    worldUnloadListener = new WorldUnloadListener(this);
    pluginDisableListener = new PluginDisableListener(this);
    inventoryCloseListener = new InventoryCloseListener(this);
  }

  @Override
  public Disposable wrap(World world) {
    if (world instanceof Disposable) {
      return (Disposable) world;
    }
    Disposable disposable = new Disposable() {
      @Override
      public void dispose() {
        Bukkit.unloadWorld(world, true);
      }
    };
    worldUnloadListener.disposables.put(world.getUID(), disposable);
    return disposable;
  }

  @Override
  public Disposable wrap(Entity entity) {
    if (entity instanceof Disposable) {
      return (Disposable) entity;
    }
    if (entity instanceof Player) {
      Disposable disposable = new Disposable() {
        @Override
        public void dispose() {
          ((Player) entity).kickPlayer("Player disposed");
        }
      };
      playerQuitListener.disposables.put(entity.getUniqueId(), disposable);
      return disposable;
    }
    return null;
  }

  @Override
  public Disposable wrap(Plugin plugin) {
    if (plugin instanceof Disposable) {
      return (Disposable) plugin;
    }
    Disposable disposable = new Disposable() {
      @Override
      public void dispose() {
        Bukkit.getPluginManager().disablePlugin(plugin);
      }
    };
    pluginDisableListener.disposables.put(plugin.getName(), disposable);
    return disposable;
  }

  @Override
  public Disposable wrap(InventoryView view) {
    if (view instanceof Disposable) {
      return (Disposable) view;
    }
    Disposable disposable = new Disposable() {
      @Override
      public void dispose() {
        view.close();
      }
    };
    inventoryCloseListener.disposables.put(view, disposable);
    return disposable;
  }

  private class EventListener implements Listener, Disposable {

    final Disposer disposer;

    public EventListener(Disposer disposer) {
      this.disposer = disposer;
      register(disposer, this);
    }
  }

  private class WorldUnloadListener extends BukkitDisposerImpl.EventListener {

    final HashMap<UUID, Disposable> disposables = new HashMap<>();

    public WorldUnloadListener(Disposer disposer) {
      super(disposer);
    }

    @EventHandler
    void onUnload(WorldUnloadEvent e) {
      Disposable present = disposables.remove(e.getWorld().getUID());
      if (present != null) {
        disposer.dispose(present);
      }
    }

    @Override
    public void dispose() {
      disposables.clear();
      WorldUnloadEvent.getHandlerList().unregister(this);
    }
  }

  private class PlayerQuitListener extends BukkitDisposerImpl.EventListener {

    final HashMap<UUID, Disposable> disposables = new HashMap<>();

    public PlayerQuitListener(Disposer disposer) {
      super(disposer);
    }

    @EventHandler
    void onQuit(PlayerQuitEvent e) {
      Disposable present = disposables.remove(e.getPlayer().getUniqueId());
      if (present != null) {
        disposer.dispose(present);
      }
    }

    @Override
    public void dispose() {
      disposables.clear();
      PlayerQuitEvent.getHandlerList().unregister(this);
    }
  }

  private class PluginDisableListener extends BukkitDisposerImpl.EventListener {

    final HashMap<String, Disposable> disposables = new HashMap<>();

    public PluginDisableListener(Disposer disposer) {
      super(disposer);
    }

    @EventHandler
    void onQuit(PluginDisableEvent e) {
      if (disposer instanceof BukkitDisposerImpl) {
        if (e.getPlugin() == ((BukkitDisposerImpl) disposer).plugin) {
          disposer.dispose(disposer);
        }
      }
      Disposable present = disposables.remove(e.getPlugin().getName());
      if (present != null) {
        disposer.dispose(present);
      }
    }

    @Override
    public void dispose() {
      disposables.clear();
      PluginDisableEvent.getHandlerList().unregister(this);
    }
  }

  private class InventoryCloseListener extends BukkitDisposerImpl.EventListener {

    final HashMap<InventoryView, Disposable> disposables = new HashMap<>();

    public InventoryCloseListener(Disposer disposer) {
      super(disposer);
    }

    @EventHandler
    void onQuit(InventoryCloseEvent e) {
      Disposable present = disposables.remove(e.getView());
      if (present != null) {
        disposer.dispose(present);
      }
    }

    @Override
    public void dispose() {
      disposables.clear();
      InventoryCloseEvent.getHandlerList().unregister(this);
    }
  }
}
