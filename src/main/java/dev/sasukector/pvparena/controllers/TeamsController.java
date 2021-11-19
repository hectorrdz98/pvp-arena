package dev.sasukector.pvparena.controllers;

import dev.sasukector.pvparena.helpers.ServerUtilities;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeamsController {

    private static TeamsController instance = null;
    private @Getter Team masterTeam;
    private @Getter Team redTeam;
    private @Getter Team blueTeam;
    private @Getter Location redTeamSpawn;
    private @Getter Location blueTeamSpawn;

    public static TeamsController getInstance() {
        if (instance == null) {
            instance = new TeamsController();
        }
        return instance;
    }

    public TeamsController() {
        this.createOrLoadTeams();
        World world = ServerUtilities.getOverworld();
        if (world != null) {
            this.redTeamSpawn = new Location(world, 92, 68, -3);
            this.blueTeamSpawn = new Location(world, -94, 68, -3);
        }
    }

    public void createOrLoadTeams() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        masterTeam = scoreboard.getTeam("master");
        redTeam = scoreboard.getTeam("red");
        blueTeam = scoreboard.getTeam("blue");

        if (masterTeam == null) {
            masterTeam = scoreboard.registerNewTeam("master");
            masterTeam.color(NamedTextColor.AQUA);
            masterTeam.prefix(Component.text("♔ "));
            masterTeam.setAllowFriendlyFire(false);
        }

        if (redTeam == null) {
            redTeam = scoreboard.registerNewTeam("red");
            redTeam.color(NamedTextColor.RED);
            redTeam.setAllowFriendlyFire(false);
        }

        if (blueTeam == null) {
            blueTeam = scoreboard.registerNewTeam("blue");
            blueTeam.color(NamedTextColor.BLUE);
            blueTeam.setAllowFriendlyFire(false);
        }
    }

    public List<Player> getMasters() {
        List<Player> players = new ArrayList<>();
        this.masterTeam.getEntries().forEach(entry -> {
            Player player = Bukkit.getPlayer(entry);
            if (player != null) {
                players.add(player);
            }
        });
        return players;
    }

    public List<Player> getRedPlayers() {
        List<Player> players = new ArrayList<>();
        this.redTeam.getEntries().forEach(entry -> {
            Player player = Bukkit.getPlayer(entry);
            if (player != null) {
                players.add(player);
            }
        });
        return players;
    }

    public List<Player> getBluePlayers() {
        List<Player> players = new ArrayList<>();
        this.blueTeam.getEntries().forEach(entry -> {
            Player player = Bukkit.getPlayer(entry);
            if (player != null) {
                players.add(player);
            }
        });
        return players;
    }

    public List<Player> getNormalPlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getScoreboard().getTeams().stream()
                        .noneMatch(t -> t.getEntries().contains(p.getName())))
                .collect(Collectors.toList());
    }

    public long getAliveRedPlayers() {
        return this.getRedPlayers().stream().filter(p -> p.getGameMode() != GameMode.SPECTATOR).count();
    }

    public long getAliveBluePlayers() {
        return this.getBluePlayers().stream().filter(p -> p.getGameMode() != GameMode.SPECTATOR).count();
    }

    public boolean isMaster(Player player) {
        return this.getMasters().contains(player);
    }

    public boolean isRedPlayer(Player player) {
        return this.getRedPlayers().contains(player);
    }

    public boolean isBluePlayer(Player player) {
        return this.getBluePlayers().contains(player);
    }

}
