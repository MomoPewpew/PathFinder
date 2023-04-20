package de.cubbossa.pathfinder.editmode;

import de.cubbossa.menuframework.inventory.implementations.BottomInventoryMenu;
import de.cubbossa.pathfinder.Messages;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.api.PathFinder;
import de.cubbossa.pathfinder.api.PathFinderProvider;
import de.cubbossa.pathfinder.api.editor.GraphRenderer;
import de.cubbossa.pathfinder.api.editor.NodeGroupEditor;
import de.cubbossa.pathfinder.api.event.*;
import de.cubbossa.pathfinder.api.group.NodeGroup;
import de.cubbossa.pathfinder.api.misc.NamespacedKey;
import de.cubbossa.pathfinder.api.misc.PathPlayer;
import de.cubbossa.pathfinder.api.node.Groupable;
import de.cubbossa.pathfinder.api.node.Node;
import de.cubbossa.pathfinder.editmode.menu.EditModeMenu;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Getter
@Setter
@RequiredArgsConstructor
public class DefaultNodeGroupEditor implements NodeGroupEditor<Player>, GraphRenderer<Player> {

	private final PathFinder pathFinder;
	private final NamespacedKey key;

	private final Map<PathPlayer<Player>, BottomInventoryMenu> editingPlayers;
	private final Map<PathPlayer<Player>, GameMode> preservedGameModes;

	private final Collection<GraphRenderer<Player>> renderers;


	public DefaultNodeGroupEditor(NodeGroup group) {
		this.pathFinder = PathFinderProvider.get();
		this.key = group.getKey();

		this.renderers = new ArrayList<>();
		this.editingPlayers = new HashMap<>();
		this.preservedGameModes = new HashMap<>();

		EventDispatcher eventDispatcher = PathPlugin.getInstance().getEventDispatcher();

		Consumer<Node<?>> erase = node -> {
			for (PathPlayer<Player> player : editingPlayers.keySet()) {
				eraseNodes(player, List.of(node));
			}
		};
		Consumer<NodeEvent> render = event -> {
			if (!(event.getNode() instanceof Groupable<?> groupable)
					|| groupable.getGroups().stream().noneMatch(g -> g.getKey().equals(key))) {
				erase.accept(event.getNode());
				return;
			}
			for (PathPlayer<Player> player : editingPlayers.keySet()) {
				renderNodes(player, List.of(event.getNode()));
			}
		};
		eventDispatcher.listen(NodeCreateEvent.class, render);
		eventDispatcher.listen(NodeSaveEvent.class, render);

		eventDispatcher.listen(NodeDeleteEvent.class, nodeDeleteEvent -> {
			for (PathPlayer<Player> player : editingPlayers.keySet()) {
				eraseNodes(player, List.of(nodeDeleteEvent.getNode()));
			}
		});

		eventDispatcher.listen(NodeGroupDeleteEvent.class, nodeGroupDeleteEvent -> {
			if (!nodeGroupDeleteEvent.getGroup().getKey().equals(key)) {
				return;
			}
			for (PathPlayer<Player> player : editingPlayers.keySet()) {
				setEditMode(player, false);
				player.sendMessage(Messages.EDITM_NG_DELETED);
			}
			dispose();
		});
	}

	public void dispose() {
	}

	public boolean isEdited() {
		return !editingPlayers.isEmpty();
	}

	public boolean toggleEditMode(PathPlayer<Player> player) {
		boolean isEditing = isEditing(player);
		setEditMode(player, !isEditing);
		return !isEditing;
	}

	public void cancelEditModes() {
		for (PathPlayer<Player> player : editingPlayers.keySet()) {
			setEditMode(player, false);
		}
	}

	/**
	 * Sets a player into edit mode for this roadmap.
	 *
	 * @param player   the player to set the edit mode for
	 * @param activate activate or deactivate edit mode
	 */
	@Override
	public void setEditMode(PathPlayer<Player> player, boolean activate) {
		Player bukkitPlayer = player.unwrap();

		if (activate) {
			if (bukkitPlayer == null || !bukkitPlayer.isOnline()) {
				return;
			}

			BottomInventoryMenu menu = new EditModeMenu(pathFinder, key,
					PathPlugin.getInstance().getNodeTypeRegistry().getTypes()).createHotbarMenu(this, bukkitPlayer);
			editingPlayers.put(player, menu);
			menu.openSync(bukkitPlayer);

			preservedGameModes.put(player, bukkitPlayer.getGameMode());
			bukkitPlayer.setGameMode(GameMode.CREATIVE);

			pathFinder.getStorage().loadGroup(key).thenAccept(group -> {
				pathFinder.getStorage().loadNodes(group.orElseThrow()).thenAccept(n -> {
					renderNodes(player, n);
				});
			});
		} else {

			if (bukkitPlayer != null && bukkitPlayer.isOnline()) {
				BottomInventoryMenu menu = editingPlayers.get(player);
				if (menu != null) {
					menu.close(bukkitPlayer);
				}
				bukkitPlayer.setGameMode(preservedGameModes.getOrDefault(player, GameMode.SURVIVAL));
			}
			clear(player);
			editingPlayers.remove(player);
		}
	}

	public boolean isEditing(PathPlayer<Player> player) {
		return editingPlayers.containsKey(player);
	}

	public boolean isEditing(Player player) {
		return isEditing(PathPlugin.wrap(player));
	}

	@Override
	public CompletableFuture<Void> clear(PathPlayer<Player> player) {
		return CompletableFuture.allOf(renderers.stream()
				.map(r -> r.clear(player))
				.toArray(CompletableFuture[]::new));
	}

	@Override
	public CompletableFuture<Void> renderNodes(PathPlayer<Player> player, Collection<Node<?>> nodes) {
		return CompletableFuture.allOf(renderers.stream()
				.map(r -> r.renderNodes(player, nodes))
				.toArray(CompletableFuture[]::new));
	}

	@Override
	public CompletableFuture<Void> eraseNodes(PathPlayer<Player> player, Collection<Node<?>> nodes) {
		return CompletableFuture.allOf(renderers.stream()
				.map(r -> r.eraseNodes(player, nodes))
				.toArray(CompletableFuture[]::new));
	}
}
