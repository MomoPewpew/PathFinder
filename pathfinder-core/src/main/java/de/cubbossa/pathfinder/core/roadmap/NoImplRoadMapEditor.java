package de.cubbossa.pathfinder.core.roadmap;

import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class NoImplRoadMapEditor implements RoadMapEditor {

  @Getter
  private final NamespacedKey key;

  @Override
  public void dispose() {

  }

  @Override
  public boolean isEdited() {
    return false;
  }

  @Override
  public boolean toggleEditMode(UUID uuid) {
    throw new IllegalStateException("Cannot use roadmap editor: no editor type registered. Are you using an API version of PathFinder?");
  }

  @Override
  public void cancelEditModes() {

  }

  @Override
  public void setEditMode(UUID uuid, boolean activate) {
    throw new IllegalStateException("Cannot use roadmap editor: no editor type registered. Are you using an API version of PathFinder?");
  }

  @Override
  public void showArmorStands(Player player) {

  }

  @Override
  public void hideArmorStands(Player player) {

  }

  @Override
  public boolean isEditing(UUID uuid) {
    return false;
  }

  @Override
  public boolean isEditing(Player player) {
    return false;
  }

  @Override
  public void updateEditModeParticles() {

  }
}
