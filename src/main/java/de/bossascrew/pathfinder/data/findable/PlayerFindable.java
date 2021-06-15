package de.bossascrew.pathfinder.data.findable;

import de.bossascrew.pathfinder.data.FindableGroup;
import de.bossascrew.pathfinder.data.RoadMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

@Getter
public class PlayerFindable implements Findable {

    private final String name;
    private final Location location;
    private final RoadMap roadMap;

    public PlayerFindable(Player player, RoadMap roadMap) {
        this.name = player.getName();
        this.location = player.getLocation();
        this.roadMap = roadMap;
    }

    public int getDatabaseId() {
        return -1;
    }

    public int getRoadMapId() {
        return roadMap.getDatabaseId();
    }

    public Vector getVector() {
        return location.toVector();
    }

    public List<Integer> getEdges() {
        return null;
    }

    public String getPermission() {
        return "none";
    }

    public int getNodeGroupId() {
        return -1;
    }

    public FindableGroup getFindableGroup() {
        return null;
    }

    public void removeFindableGroup() {}

    public Double getBezierTangentLength() {
        return roadMap.getDefaultBezierTangentLength();
    }

    public double getBezierTangentLengthOrDefault() {
        return roadMap.getDefaultBezierTangentLength();
    }
}
