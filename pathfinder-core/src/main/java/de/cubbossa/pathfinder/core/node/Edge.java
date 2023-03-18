package de.cubbossa.pathfinder.core.node;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
public class Edge {

  private Node<?> start;
  private Node<?> end;
  private float weightModifier;

  private Location center;

  public Edge(Node<?> start, Node<?> end, float weightModifier) {
    this.start = start;
    this.end = end;
    this.weightModifier = weightModifier;
    refreshCenter();
  }

  public void setStart(Node<?> start) {
    this.start = start;
    refreshCenter();
  }

  public void setEnd(Node<?> end) {
    this.end = end;
    refreshCenter();
  }

  public double getWeightedLength() {
    return start.getLocation().distance(end.getLocation()) * weightModifier;
  }

  private void refreshCenter() {
    center = start.getLocation().clone()
        .add(end.getLocation().clone().subtract(start.getLocation()).multiply(.5f));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Edge edge)) {
      return false;
    }

    if (Float.compare(edge.weightModifier, weightModifier) != 0) {
      return false;
    }
    if (!Objects.equals(start, edge.start)) {
      return false;
    }
    return Objects.equals(end, edge.end);
  }

  @Override
  public int hashCode() {
    int result = start != null ? start.hashCode() : 0;
    result = 31 * result + (end != null ? end.hashCode() : 0);
    result = 31 * result + (weightModifier != +0.0f ? Float.floatToIntBits(weightModifier) : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Edge{" +
        "start=" + start +
        ", end=" + end +
        ", weightModifier=" + weightModifier +
        '}';
  }
}
