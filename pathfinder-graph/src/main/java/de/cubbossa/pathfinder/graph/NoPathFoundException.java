package de.cubbossa.pathfinder.graph;

import org.jetbrains.annotations.Nullable;

/**
 * An exception to throw if a {@link PathSolver} was not able to find any path from start to target.
 */
public class NoPathFoundException extends Exception {

  public NoPathFoundException() {
    this(null, null);
  }

  public NoPathFoundException(@Nullable Object from, @Nullable Object to) {
    super((from == null ? "" : "\n - Start Node: " + from + " ") + (to == null ? "" : "\n - End Node: " + to));
  }
}
