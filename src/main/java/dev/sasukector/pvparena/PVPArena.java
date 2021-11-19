package dev.sasukector.pvparena;

import dev.sasukector.pvparena.commands.GameCommand;
import dev.sasukector.pvparena.controllers.BoardController;
import dev.sasukector.pvparena.events.SpawnEvents;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class PVPArena extends JavaPlugin {

    private static @Getter PVPArena instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info(ChatColor.DARK_PURPLE + "PVPArena startup!");
        instance = this;

        // Register events
        this.getServer().getPluginManager().registerEvents(new SpawnEvents(), this);

        // Register commands
        Objects.requireNonNull(PVPArena.getInstance().getCommand("game")).setExecutor(new GameCommand());

        // Refresh scoreboard for online players
        Bukkit.getOnlinePlayers().forEach(player -> BoardController.getInstance().newPlayerBoard(player));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info(ChatColor.DARK_PURPLE + "PVPArena shutdown!");
    }
}
