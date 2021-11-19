package dev.sasukector.pvparena.controllers;

import dev.sasukector.pvparena.PVPArena;
import dev.sasukector.pvparena.helpers.ServerUtilities;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GameController {

    private static GameController instance = null;
    public @Getter Status currentStatus = Status.WAITING;
    private int gateTaskID = -1;

    public enum Status {
        WAITING, PLAYING
    }

    public static GameController getInstance() {
        if (instance == null) {
            instance =  new GameController();
        }
        return instance;
    }

    public void restartPlayer(Player player) {
        player.setGameMode(GameMode.SPECTATOR);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setExp(0);
        player.setLevel(0);
        player.setArrowsInBody(0);
        player.setFireTicks(0);
        player.setVisualFire(false);
        player.setAllowFlight(false);
        player.setCollidable(true);
        player.getActivePotionEffects().forEach(p -> player.removePotionEffect(p.getType()));
        player.getInventory().clear();
        player.updateInventory();
    }

    public void returnPlayerToSpawn(Player player) {
        World world = ServerUtilities.getOverworld();
        if (world != null) {
            player.teleport(new Location(world, 0, 70, 0));
        }
    }

    public void reviveAllPlayers() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (!TeamsController.getInstance().isMaster(player)) {
                this.restartPlayer(player);
                this.returnPlayerToSpawn(player);
                player.setGameMode(GameMode.ADVENTURE);
            }
        });
    }

    public void restartBarriers() {
        World world = ServerUtilities.getOverworld();
        if (world != null) {
            for (int z = -6; z <= 0; ++z) {
                for (int y = 67; y <= 71; ++y) {
                    world.getBlockAt(86, y, z).setType(Material.BARRIER);
                    world.getBlockAt(-88, y, z).setType(Material.BARRIER);
                }
            }
        }
    }

    public void clearBarriers() {
        World world = ServerUtilities.getOverworld();
        if (world != null) {
            for (int z = -6; z <= 0; ++z) {
                for (int y = 67; y <= 71; ++y) {
                    world.getBlockAt(86, y, z).setType(Material.AIR);
                    world.getBlockAt(-88, y, z).setType(Material.AIR);
                }
            }
        }
    }

    public void giveMenuItems(Player player) {
        ItemStack joinRed = new ItemStack(Material.RED_WOOL);
        ItemMeta joinRedMeta = joinRed.getItemMeta();
        joinRedMeta.displayName(Component.text("Unirse al equipo rojo", TextColor.color(0xF94144)));
        List<Component> joinRedLore = new ArrayList<>();
        joinRedLore.add(Component.text("Haz click derecho con la lana", TextColor.color(0xF94144)));
        joinRedLore.add(Component.text("para unirte al equipo rojo", TextColor.color(0xF94144)));
        joinRedMeta.lore(joinRedLore);
        joinRedMeta.setLocalizedName("join_red");
        joinRed.setItemMeta(joinRedMeta);
        player.getInventory().addItem(joinRed);

        ItemStack joinBlue = new ItemStack(Material.BLUE_WOOL);
        ItemMeta joinBlueMeta = joinBlue.getItemMeta();
        joinBlueMeta.displayName(Component.text("Unirse al equipo azul", TextColor.color(0x277DA1)));
        List<Component> joinBlueLore = new ArrayList<>();
        joinBlueLore.add(Component.text("Haz click derecho con la lana", TextColor.color(0x277DA1)));
        joinBlueLore.add(Component.text("para unirte al equipo azul", TextColor.color(0x277DA1)));
        joinBlueMeta.lore(joinBlueLore);
        joinBlueMeta.setLocalizedName("join_blue");
        joinBlue.setItemMeta(joinBlueMeta);
        player.getInventory().addItem(joinBlue);
    }

    public void handlePlayerJoin(Player player) {
        this.restartPlayer(player);
        this.returnPlayerToSpawn(player);
        if (this.currentStatus == Status.WAITING) {
            player.setGameMode(GameMode.ADVENTURE);
            this.giveMenuItems(player);
        }
    }

    public void handlePlayerLeave(Player player) {
        if (!TeamsController.getInstance().getNormalPlayers().contains(player) &&
            this.currentStatus == Status.PLAYING) {
            if (player.getGameMode() != GameMode.SPECTATOR) {
                ItemStack[] playerItems = player.getInventory().getContents().clone();
                for (ItemStack item : playerItems) {
                    player.getWorld().dropItem(player.getLocation(), item);
                }
                ServerUtilities.sendBroadcastMessage(ServerUtilities.getMiniMessage().parse(
                        "<bold><color:#E38486>" + player.getName() +
                                " </color></bold><color:#E38486>abandonó el combate</color>"
                ));
                this.validateTeamWin();
            }
        }
    }

    public void validateTeamWin() {
        long redAlive = TeamsController.getInstance().getAliveRedPlayers();
        long blueAlive = TeamsController.getInstance().getAliveBluePlayers();
        boolean validWin = false;
        if (redAlive == 0 && blueAlive == 0) {
            validWin = true;
            ServerUtilities.sendBroadcastTitle(
                    Component.text("EMPATE", TextColor.color(0x38A3A5)),
                    Component.text("Buena partida", TextColor.color(0xE9D8A6))
            );
            ServerUtilities.sendBroadcastMessage(Component.text("El juego terminó en EMPATE", TextColor.color(0x38A3A5)));
        } else if (redAlive == 0) {
            validWin = true;
            ServerUtilities.sendBroadcastTitle(
                    Component.text("Azul gana", TextColor.color(0x277DA1)),
                    Component.text("Buena partida", TextColor.color(0xE9D8A6))
            );
            ServerUtilities.sendBroadcastMessage(ServerUtilities.getMiniMessage().parse(
                    "El equipo <bold><color:#277DA1>AZUL</color></bold> ganó la partida"
            ));
        } else if (blueAlive == 0) {
            validWin = true;
            ServerUtilities.sendBroadcastTitle(
                    Component.text("Rojo gana", TextColor.color(0xF94144)),
                    Component.text("Buena partida", TextColor.color(0xE9D8A6))
            );
            ServerUtilities.sendBroadcastMessage(ServerUtilities.getMiniMessage().parse(
                    "El equipo <bold><color:#F94144>ROJO</color></bold> ganó la partida"
            ));
        }
        if (validWin) {
            ServerUtilities.playBroadcastSound("minecraft:entity.wither.death",  1f, 1.4f);
            this.currentStatus = Status.WAITING;
            this.reviveAllPlayers();
            if (this.gateTaskID != -1) {
                Bukkit.getScheduler().cancelTask(this.gateTaskID);
                this.gateTaskID = -1;
            }
        }
    }

    public void gameStart() {
        ServerUtilities.playBroadcastSound("minecraft:block.note_block.xylophone",  1f, 1f);
        ServerUtilities.sendBroadcastMessage(Component.text("Ha iniciado la partida", TextColor.color(0x38A3A5)));
        ServerUtilities.sendBroadcastTitle(
                Component.text("Preparación", TextColor.color(0x38A3A5)),
                Component.text("10 minutos restantes", TextColor.color(0xE9D8A6))
        );
        this.currentStatus = Status.PLAYING;
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (!TeamsController.getInstance().isMaster(player)) {
                player.getInventory().clear();
                player.updateInventory();
                if (TeamsController.getInstance().getNormalPlayers().contains(player)) {
                    player.setGameMode(GameMode.SPECTATOR);
                }
            }
        });
        this.restartBarriers();
        Location redSpawn = TeamsController.getInstance().getRedTeamSpawn();
        Location blueSpawn = TeamsController.getInstance().getBlueTeamSpawn();
        if (redSpawn != null) {
            TeamsController.getInstance().getRedPlayers().forEach(player -> player.teleport(redSpawn));
        }
        if (blueSpawn != null) {
            TeamsController.getInstance().getBluePlayers().forEach(player -> player.teleport(blueSpawn));
        }
        this.startGateCountDown();
    }

    public void gameStop() {
        ServerUtilities.sendBroadcastMessage(Component.text("Se ha detenido el juego", TextColor.color(0x38A3A5)));
        ServerUtilities.playBroadcastSound("minecraft:entity.wither.death",  1f, 1.4f);
        this.currentStatus = Status.WAITING;
        this.reviveAllPlayers();
        this.restartBarriers();
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (!TeamsController.getInstance().isMaster(player)) {
                this.giveMenuItems(player);
            }
        });
        if (this.gateTaskID != -1) {
            Bukkit.getScheduler().cancelTask(this.gateTaskID);
            this.gateTaskID = -1;
        }
    }

    public void startGateCountDown() {
        AtomicInteger remainingTime = new AtomicInteger(10 * 60);
        this.gateTaskID = new BukkitRunnable() {
            @Override
            public void run() {
                if (remainingTime.get() <= 0) {
                    clearBarriers();
                    ServerUtilities.playBroadcastSound("minecraft:block.end_portal.spawn",  1f, 1.4f);
                    ServerUtilities.sendBroadcastMessage(Component.text("Puertas abiertas, tienes 15 segundos para salir, de lo contrario serás eliminado", TextColor.color(0x38A3A5)));
                    ServerUtilities.sendBroadcastTitle(
                            Component.text("A combatir", TextColor.color(0x38A3A5)),
                            Component.text("15 segundos para salir", TextColor.color(0xE9D8A6))
                    );
                    gateTaskID = -1;
                    eliminatePlayersTimer();
                    cancel();
                } else {
                    if (remainingTime.get() <= 3) {
                        ServerUtilities.sendBroadcastTitle(
                                Component.text(remainingTime.get(), TextColor.color(0x38A3A5)),
                                Component.empty()
                        );
                        ServerUtilities.playBroadcastSound("minecraft:block.note_block.xylophone",  1f, 1f);
                    } else if (remainingTime.get() % 30 == 0) {
                        ServerUtilities.playBroadcastSound("minecraft:block.note_block.xylophone",  1f, 1f);
                    }
                    ServerUtilities.sendBroadcastAction(
                            Component.text("Se abren las puertas: " + remainingTime.get() + "s",
                                    TextColor.color(0xE9D8A6))
                    );
                    remainingTime.addAndGet(-1);
                }
            }
        }.runTaskTimer(PVPArena.getInstance(), 0L, 20L).getTaskId();
    }

    public void eliminatePlayersTimer() {
        AtomicInteger remainingTime = new AtomicInteger(15);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (remainingTime.get() <= 0) {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        int x = player.getLocation().getBlockX();
                        if (x < -88 || x > 88) {
                            if (player.getGameMode() != GameMode.SPECTATOR) {
                                player.setGameMode(GameMode.SPECTATOR);
                                validateTeamWin();
                                ServerUtilities.playBroadcastSound("minecraft:block.end_portal_frame.fill",  1f, 0.4f);
                                ServerUtilities.sendBroadcastMessage(ServerUtilities.getMiniMessage().parse(
                                        "<bold><color:#E38486>" + player.getName() +
                                                " </color></bold><color:#E38486>fue eliminado por seguir en la zona</color>"
                                ));
                            }
                        }
                    });
                    cancel();
                } else {
                    ServerUtilities.sendBroadcastAction(
                            Component.text("Tiempo para salir: " + remainingTime.get() + "s", TextColor.color(0xE9D8A6))
                    );
                    remainingTime.addAndGet(-1);
                }
            }
        }.runTaskTimer(PVPArena.getInstance(), 0L, 20L);
    }

}
