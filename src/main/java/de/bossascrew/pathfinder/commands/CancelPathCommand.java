package de.bossascrew.pathfinder.commands;

import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import de.bossascrew.pathfinder.Messages;
import de.bossascrew.pathfinder.PathPlugin;
import de.bossascrew.pathfinder.commands.argument.CustomArgs;
import de.bossascrew.pathfinder.data.PathPlayer;
import de.bossascrew.pathfinder.data.PathPlayerHandler;
import de.bossascrew.pathfinder.roadmap.RoadMap;
import de.cubbossa.translations.TranslationHandler;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.entity.Player;

public class CancelPathCommand extends CommandAPICommand {

    public CancelPathCommand() {
        super("cancelpath");

        withArguments(CustomArgs.roadMapArgument("roadmap"));
        executesPlayer((player, args) -> {
            RoadMap roadMap = (RoadMap) args[0];
            PathPlayer pathPlayer = PathPlayerHandler.getInstance().getPlayer(player.getUniqueId());

            if (roadMap == null) {
                pathPlayer.cancelPaths();

            } else {
                pathPlayer.cancelPath(roadMap);
            }
            TranslationHandler.getInstance().sendMessage(Messages.CMD_CANCEL, player);
        });
    }
}
