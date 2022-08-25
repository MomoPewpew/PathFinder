package de.bossascrew.pathfinder.core.commands;

import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.core.commands.argument.CustomArgs;
import de.bossascrew.pathfinder.core.node.Groupable;
import de.bossascrew.pathfinder.core.node.Node;
import de.bossascrew.pathfinder.core.node.NodeType;
import de.bossascrew.pathfinder.core.roadmap.RoadMap;
import de.bossascrew.pathfinder.core.roadmap.RoadMapHandler;
import de.bossascrew.pathfinder.util.CommandUtils;
import de.bossascrew.pathfinder.util.NodeSelection;
import de.cubbossa.translations.FormattedMessage;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class WaypointCommand extends CommandTree {

	public WaypointCommand() {
		super("waypoint");

		withAliases("node");

		then(new LiteralArgument("info")
				.withPermission(PathPlugin.PERM_CMD_WP_INFO)
				.then(CustomArgs.nodeSelectionArgument("nodes")
						.executesPlayer((player, objects) -> {
							onInfo(player, (NodeSelection) objects[0]);
						})
				)
		);
		then(new LiteralArgument("list")
				.withPermission(PathPlugin.PERM_CMD_WP_LIST)
				.executesPlayer((player, objects) -> {
					onList(player, 1);
				})
				.then(new IntegerArgument("page", 1)
						.executesPlayer((player, objects) -> {
							onList(player, (Integer) objects[0]);
						})
				)
		);
		then(new LiteralArgument("create")
				.withPermission(PathPlugin.PERM_CMD_WP_CREATE)
				.executesPlayer((player, objects) -> {
					onCreate(player, RoadMapHandler.WAYPOINT_TYPE, player.getLocation().toVector().add(new Vector(0, 1, 0)));
				})
				.then(new LocationArgument("location")
						.executesPlayer((player, objects) -> {
							onCreate(player, RoadMapHandler.WAYPOINT_TYPE, ((Location) objects[0]).toVector());
						})
				)
				.then(CustomArgs.nodeTypeArgument("type")
						.executesPlayer((player, objects) -> {
							onCreate(player, (NodeType<? extends Node>) objects[0], player.getLocation().toVector().add(new Vector(0, 1, 0)));
						})
						.then(new LocationArgument("location")
								.executesPlayer((player, objects) -> {
									onCreate(player, (NodeType<? extends Node>) objects[0], ((Location) objects[1]).toVector());
								})
						)
				)
		);
		then(new LiteralArgument("delete")
				.withPermission(PathPlugin.PERM_CMD_WP_DELETE)
				.then(CustomArgs.nodeSelectionArgument("nodes")
						.executesPlayer((player, objects) -> {
							onDelete(player, (NodeSelection) objects[0]);
						})
				)
		);
		then(new LiteralArgument("tphere")
				.withPermission(PathPlugin.PERM_CMD_WP_TPHERE)
				.then(CustomArgs.nodeSelectionArgument("nodes")
						.executesPlayer((player, objects) -> {
							onTphere(player, (NodeSelection) objects[0]);
						})
				)
		);
		then(new LiteralArgument("tp")
				.withPermission(PathPlugin.PERM_CMD_WP_TP)
				.then(CustomArgs.nodeSelectionArgument("nodes")
						.then(new LocationArgument("location", LocationType.PRECISE_POSITION)
								.executesPlayer((player, objects) -> {
									onTp(player, (NodeSelection) objects[0], (Location) objects[1]);
								})
						)
				)
		);
		then(new LiteralArgument("connect")
				.withPermission(PathPlugin.PERM_CMD_WP_CONNECT)
				.then(CustomArgs.nodeSelectionArgument("start")
						.then(CustomArgs.nodeGroupArgument("end")
								.executesPlayer((player, objects) -> {
									onConnect(player, (NodeSelection) objects[0], (NodeSelection) objects[1]);
								})
						)
				)
		);
		then(new LiteralArgument("disconnect")
				.withPermission(PathPlugin.PERM_CMD_WP_DISCONNECT)
				.then(CustomArgs.nodeSelectionArgument("start")
						.executesPlayer((player, objects) -> {
							onDisconnect(player, (NodeSelection) objects[0], null);
						})
						.then(CustomArgs.nodeGroupArgument("end")
								.executesPlayer((player, objects) -> {
									onDisconnect(player, (NodeSelection) objects[0], (NodeSelection) objects[1]);
								})
						)
				)

		);
		then(new LiteralArgument("set")
				.then(new LiteralArgument("permission")
						.withPermission(PathPlugin.PERM_CMD_WP_SET_PERM)
						.then(CustomArgs.nodeSelectionArgument("nodes")
								.then(new GreedyStringArgument("permission").includeSuggestions(ArgumentSuggestions.strings("null"))
										.executesPlayer((player, objects) -> {
											onSetPermission(player, (NodeSelection) objects[0], (String) objects[1]);
										})
								)
						)
				)
				.then(new LiteralArgument("curve-length")
						.withPermission(PathPlugin.PERM_CMD_WP_SET_CURVE)
						.then(CustomArgs.nodeSelectionArgument("nodes")
								.then(new DoubleArgument("length", 0.001)
										.executesPlayer((player, objects) -> {
											onSetTangent(player, (NodeSelection) objects[0], (Double) objects[1]);
										})
								)
						)
				)
		);
	}


	public void onInfo(Player player, NodeSelection selection) {

		if (selection.size() > 1) {
			//TODO selection choice
			return;
		}
		Node node = selection.get(0);
		FormattedMessage message = Messages.CMD_N_INFO.format(TagResolver.builder()
				.tag("id", Tag.preProcessParsed(node.getNodeId() + ""))
				.tag("roadmap", Tag.inserting(Messages.formatKey(node.getRoadMapKey())))
				.tag("permission", Tag.inserting(Messages.formatPermission(node.getPermission())))
				.tag("groups", node instanceof Groupable groupable ?
						Tag.inserting(Messages.formatNodeGroups(player, groupable.getGroups())) :
						Tag.inserting(Component.text("none"))) //TODO as message
				.tag("position", Tag.inserting(Messages.formatVector(node.getPosition())))
				.tag("curve-length", Tag.preProcessParsed(node.getCurveLength() + ""))
				.tag("edge-count", Tag.preProcessParsed(node.getEdges().size() + ""))
				.build());

		TranslationHandler.getInstance().sendMessage(message, player);
	}

	public void onCreate(Player player, NodeType<? extends Node> type, Vector location) {
		RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);
		Node node = roadMap.createNode(type, location);

		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_CREATE
				.format(TagResolver.resolver("id", Tag.inserting(Component.text(node.getNodeId())))), player);
	}

	public void onDelete(Player player, NodeSelection selection) {
		RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);
		roadMap.removeNodes(selection);
		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_DELETE
				.format(TagResolver.resolver("selection", Tag.inserting(Messages.formatNodeSelection(player, selection)))), player);
	}

	public void onTphere(Player player, NodeSelection selection) {
		if (selection.size() == 0) {
			return;
		}
		RoadMap roadMap = RoadMapHandler.getInstance().getRoadMap(selection.get(0).getRoadMapKey());
		if (roadMap == null) {
			return;
		}
		Vector pos = player.getLocation().toVector();
		selection.forEach(node -> roadMap.setNodeLocation(node, pos));

		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_MOVED.format(TagResolver.builder()
				.tag("selection", Tag.inserting(Messages.formatNodeSelection(player, selection)))
				.tag("location", Tag.inserting(Messages.formatVector(player.getLocation().toVector())))
				.build()), player);
	}

	public void onTp(Player player, NodeSelection selection, Location location) {

		selection.forEach(node -> node.setPosition(location.toVector()));

		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_MOVED.format(TagResolver.builder()
				.tag("selection", Tag.inserting(Messages.formatNodeSelection(player, selection)))
				.tag("location", Tag.inserting(Messages.formatVector(location.toVector())))
				.build()), player);
	}

	public void onList(Player player, int pageInput) {
		RoadMap roadMap = CommandUtils.getSelectedRoadMap(player);

		TagResolver resolver = TagResolver.builder()
				.tag("roadmap", Tag.inserting(roadMap.getDisplayName()))
				.tag("page", Tag.preProcessParsed(pageInput + ""))
				.build();

		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_LIST_HEADER.format(resolver), player);

		PathPlugin.getInstance().getAudiences().player(player).sendMessage(Component.join(
				JoinConfiguration.separator(Component.text(", ", NamedTextColor.GRAY)),
				CommandUtils.subList(new ArrayList<>(roadMap.getNodes()), pageInput, 40).stream()
						.map(n -> {

							TagResolver r = TagResolver.builder()
									.tag("id", Tag.preProcessParsed(n.getNodeId() + ""))
									.tag("permission", Tag.preProcessParsed(n.getPermission() == null ? "null" : n.getPermission()))
									.tag("position", Tag.inserting(Messages.formatVector(n.getPosition())))
									.tag("groups", n instanceof Groupable groupable ?
											Tag.inserting(Messages.formatNodeGroups(player, groupable.getGroups())) :
											Tag.inserting(Component.text("none"))) //TODO as message
									.build();

							return TranslationHandler.getInstance().translateLine(Messages.CMD_N_LIST_ELEMENT.format(resolver, r), player);
						})
						.toList()));
		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_LIST_FOOTER.format(resolver), player);
	}

	public void onConnect(Player player, NodeSelection startSelection, NodeSelection endSelection) {

		for (Node start : startSelection) {
			for (Node end : endSelection) {
				TagResolver resolver = TagResolver.builder()
						.tag("start", Tag.inserting(Component.text(start.getNodeId())))
						.tag("end", Tag.inserting(Component.text(end.getNodeId())))
						.build();

				if (start.equals(end)) {
					TranslationHandler.getInstance().sendMessage(Messages.CMD_N_CONNECT_IDENTICAL.format(resolver), player);
					continue;
				}
				if (start.getEdges().stream().anyMatch(edge -> edge.getEnd().equals(end))) {
					TranslationHandler.getInstance().sendMessage(Messages.CMD_N_CONNECT_ALREADY_CONNECTED.format(resolver), player);
					continue;
				}
				start.connect(end);
				TranslationHandler.getInstance().sendMessage(Messages.CMD_N_CONNECT.format(resolver), player);
			}
		}
	}

	public void onDisconnect(Player player, NodeSelection startSelection, @Nullable NodeSelection endSelection) {

		for (Node start : startSelection) {
			if (endSelection == null) {
				RoadMapHandler.getInstance().getRoadMap(start.getRoadMapKey()).disconnectNode(start);
				continue;
			}
			for (Node end : endSelection) {
				TagResolver resolver = TagResolver.builder()
						.tag("start", Tag.inserting(Component.text(start.getNodeId())))
						.tag("end", Tag.inserting(Component.text(end.getNodeId())))
						.build();

				start.disconnect(end);
				TranslationHandler.getInstance().sendMessage(Messages.CMD_N_DISCONNECT.format(resolver), player);
			}
		}
	}

	public void onSetPermission(Player player, NodeSelection selection, String perm) {
		selection.forEach(node -> node.setPermission(perm.equalsIgnoreCase("null") ? null : perm));
		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_SET_PERMISSION.format(TagResolver.builder()
				.tag("selection", Tag.inserting(Messages.formatNodeSelection(player, selection)))
				.tag("permission", Tag.inserting(Component.text(perm)))
				.build()), player);
	}

	public void onSetTangent(Player player, NodeSelection selection, Double strength) {
		selection.forEach(node -> node.setCurveLength(strength));
		TranslationHandler.getInstance().sendMessage(Messages.CMD_N_SET_TANGENT.format(TagResolver.builder()
				.tag("selection", Tag.inserting(Messages.formatNodeSelection(player, selection)))
				.tag("length", Tag.inserting(Component.text(strength)))
				.build()), player);
	}

		/*TODO @Subcommand("group")
		@Syntax("<nodes> <group>")
		@CommandCompletion(PathPlugin.COMPLETE_NODE_SELECTION + " null|" + PathPlugin.COMPLETE_FINDABLE_GROUPS_BY_SELECTION)
		public void onSetGroup(CommandSender sender, NodeSelection selection, @Single NamespacedKey key) {
			RoadMap roadMap = CommandUtils.getSelectedRoadMap(sender);

			TagResolver resolver = TagResolver.builder()
					.tag("selection", Tag.inserting(Messages.formatNodeSelection(selection)))
					.tag("group", Tag.inserting(Messages.formatKey(key)))
					.build();

			NodeGroup group = roadMap.getNodeGroup(key);
			if (group == null) {
				TranslationHandler.getInstance().sendMessage(Messages.CMD_N_SET_GROUP_UNKNOWN.format(resolver), sender);
				return;
			}
			selection.forEach(node -> node.setGroupKey(key));
			TranslationHandler.getInstance().sendMessage(Messages.CMD_N_SET_GROUP.format(resolver), sender);
		}*/
}
