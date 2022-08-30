package de.cubbossa.pathfinder.module.visualizing;

import com.google.common.collect.Lists;
import de.bossascrew.splinelib.util.Spline;
import de.cubbossa.pathfinder.PathPlugin;
import de.cubbossa.pathfinder.core.node.Node;
import de.cubbossa.pathfinder.core.roadmap.RoadMapHandler;
import de.cubbossa.pathfinder.data.PathPlayer;
import de.cubbossa.pathfinder.data.PathPlayerHandler;
import de.cubbossa.pathfinder.module.visualizing.visualizer.PathVisualizer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Getter
public class ParticlePath extends ArrayList<Node> {

    private final UUID playerUuid;
    private final PathVisualizer<?> visualizer;

    private boolean active;
    private BukkitTask task;
    private final List<Location> calculatedPoints;

    public ParticlePath(UUID playerUuid, PathVisualizer<?> visualizer) {
        this.playerUuid = playerUuid;
        this.active = false;
        this.visualizer = visualizer;
        this.calculatedPoints = new ArrayList<>();
    }

    public void prepare(List<Node> path) {
        calculatedPoints.clear();

        //TODO has to be rewritten if portals are being introduced
        World world = path.get(0).getLocation().getWorld();
        Spline spline = visualizer.makeSpline(path.stream().collect(Collectors.toMap(
                o -> o,
                o -> o.getCurveLength() == null ? RoadMapHandler.getInstance().getRoadMap(o.getRoadMapKey()).getDefaultBezierTangentLength() : o.getCurveLength(),
                (aDouble, aDouble2) -> aDouble,
                LinkedHashMap::new)));
        List<Vector> curve = visualizer.transform(visualizer.interpolate(spline));
        calculatedPoints.addAll(visualizer.transform(curve).stream().map(vector -> vector.toLocation(world)).toList());
    }

    public void run() {
        run(playerUuid);
    }

    public void run(UUID uuid) {
        prepare(this);
        World world = calculatedPoints.get(0).getWorld();
        Bukkit.getScheduler().runTask(PathPlugin.getInstance(), () -> {
            cancelSync();
            PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(uuid);
            if (pathPlayer == null) {
                return;
            }
            this.active = true;

            AtomicInteger interval = new AtomicInteger(0);
            task = Bukkit.getScheduler().runTaskTimer(PathPlugin.getInstance(), () -> {
                Player searching = Bukkit.getPlayer(uuid);
                if (searching == null) {
                    return;
                }
                long fullTime = world.getFullTime();

                visualizer.play(calculatedPoints, new PathVisualizer.VisualizerContext(Lists.newArrayList(searching), interval.getAndIncrement(), fullTime));
            }, 0L, visualizer.getInterval());
        });
    }

    public void cancel() {
        Bukkit.getScheduler().runTask(PathPlugin.getInstance(), this::cancelSync);
    }

    public void cancelSync() {
        if(task == null) {
            return;
        }
        Bukkit.getScheduler().cancelTask(task.getTaskId());
        this.active = false;
    }
}