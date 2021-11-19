package dev.sasukector.pvparena.commands;

import dev.sasukector.pvparena.controllers.GameController;
import dev.sasukector.pvparena.helpers.ServerUtilities;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player && player.isOp()) {
            if (args.length > 0) {
                String option = args[0];
                if (validOptions().contains(option)) {
                    switch (option) {
                        case "start" -> {
                            if (GameController.getInstance().currentStatus == GameController.Status.WAITING) {
                                GameController.getInstance().gameStart();
                            } else {
                                ServerUtilities.sendServerMessage(player, "§cYa hay una partida en curso");
                            }
                        }
                        case "stop" -> {
                            if (GameController.getInstance().currentStatus == GameController.Status.PLAYING) {
                                GameController.getInstance().gameStop();
                            } else {
                                ServerUtilities.sendServerMessage(player, "§cNo hay una partida en curso");
                            }
                        }
                    }
                } else {
                    ServerUtilities.sendServerMessage(player, "§cSelecciona una opción válida");
                }
            } else {
                ServerUtilities.sendServerMessage(player, "§cSelecciona una opción");
            }
        } else if (sender instanceof Player player) {
            ServerUtilities.sendServerMessage(player, "§cPermisos insuficientes");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if(sender instanceof Player) {
            if (args.length == 1) {
                String partialItem = args[0];
                StringUtil.copyPartialMatches(partialItem, validOptions(), completions);
            }
        }

        Collections.sort(completions);

        return completions;
    }

    public List<String> validOptions() {
        List<String> valid = new ArrayList<>();
        valid.add("start");
        valid.add("stop");
        Collections.sort(valid);
        return valid;
    }

}
