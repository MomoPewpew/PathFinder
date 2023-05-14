package de.cubbossa.pathfinder.events.path;

import de.cubbossa.pathapi.event.PathCancelledEvent;
import de.cubbossa.pathfinder.visualizer.CommonVisualizerPath;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

@Getter
@Setter
public class PathCancelEvent extends Event implements Cancellable, PathCancelledEvent<Player> {

  private static final HandlerList handlers = new HandlerList();

  private final UUID playerId;
  private final CommonVisualizerPath<Player> path;
  private boolean cancelled = false;

  public PathCancelEvent(UUID playerId, CommonVisualizerPath<Player> path) {
    this.playerId = playerId;
    this.path = path;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public HandlerList getHandlers() {
    return handlers;
  }
}
