package de.bossascrew.pathfinder.core.node.implementation;

import de.bossascrew.pathfinder.core.node.Edge;
import de.bossascrew.pathfinder.core.node.Node;
import de.bossascrew.pathfinder.core.roadmap.RoadMap;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

@Getter
public class PlayerNode implements Node {

    private final Player player;
    private final RoadMap roadMap;

    public PlayerNode(Player player, RoadMap roadMap) {
        this.player = player;
        this.roadMap = roadMap;
    }

    @Override
    public Collection<String> getSearchTerms() {
        return new HashSet<>();
    }

    @Override
    public Collection<Node> getGroup() {
        return new HashSet<>();
    }

    @Override
    public int getNodeId() {
        return -1;
    }

    @Override
    public NamespacedKey getRoadMapKey() {
        return roadMap.getKey();
    }

    @Override
    public Vector getPosition() {
        return player.getLocation().add(0, .5f, 0).toVector();
    }

    @Override
    public void setPosition(Vector position) {

    }

    @Override
    public Location getLocation() {
        return player.getLocation().add(0, .5f, 0);
    }

    @Override
    public Collection<Edge> getEdges() {
        return new HashSet<>();
    }

    @Override
    public @Nullable String getPermission() {
        return null;
    }

    @Override
    public void setPermission(@Nullable String permission) {

    }

    @Override
    public @Nullable Double getCurveLength() {
        return null;
    }

    @Override
    public void setCurveLength(Double value) {

    }

    @Override
    public Edge connect(Node target) {
        return null;
    }

    @Override
    public void disconnect(Node target) {

    }

    @Override
    public int compareTo(@NotNull Node o) {
        return 0;
    }

    @Override
    public String toString() {
        return "PlayerNode{" +
                "player=" + player.getName() +
                '}';
    }
}
