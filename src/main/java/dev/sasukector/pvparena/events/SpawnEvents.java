package dev.sasukector.pvparena.events;

import dev.sasukector.pvparena.controllers.BoardController;
import dev.sasukector.pvparena.controllers.GameController;
import dev.sasukector.pvparena.controllers.TeamsController;
import dev.sasukector.pvparena.helpers.ServerUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class SpawnEvents implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(
                Component.text("+ ", TextColor.color(0x84E3A4))
                        .append(Component.text(player.getName(), TextColor.color(0x84E3A4)))
        );
        BoardController.getInstance().newPlayerBoard(player);
        GameController.getInstance().handlePlayerJoin(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BoardController.getInstance().removePlayerBoard(player);
        event.quitMessage(
                Component.text("- ", TextColor.color(0xE38486))
                        .append(Component.text(player.getName(), TextColor.color(0xE38486)))
        );
        GameController.getInstance().handlePlayerLeave(player);
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            if (GameController.getInstance().getCurrentStatus() != GameController.Status.PLAYING) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (GameController.getInstance().getCurrentStatus() != GameController.Status.PLAYING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (GameController.getInstance().getCurrentStatus() != GameController.Status.PLAYING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (GameController.getInstance().getCurrentStatus() != GameController.Status.PLAYING) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (GameController.getInstance().getCurrentStatus() != GameController.Status.PLAYING) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockChestInteract(PlayerInteractEvent event) {
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof InventoryHolder) {
                if (GameController.getInstance().getCurrentStatus() != GameController.Status.PLAYING) {
                    if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
                        event.setCancelled(true);
                }
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (GameController.getInstance().currentStatus == GameController.Status.WAITING) {
                Player player = event.getPlayer();
                ItemStack itemStack = player.getEquipment().getItemInMainHand();
                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLocalizedName()) {
                    String localizedName = itemStack.getItemMeta().getLocalizedName();
                    switch (localizedName) {
                        case "join_red" -> {
                            player.playSound(player.getLocation(), "minecraft:block.note_block.xylophone",  1f, 1f);
                            TeamsController.getInstance().getRedTeam().addEntry(player.getName());
                            ServerUtilities.sendServerMessage(player, ServerUtilities.getMiniMessage().parse(
                                    "Te uniste al equipo <color:#F94144>ROJO</color>"
                            ));
                        }
                        case "join_blue" -> {
                            player.playSound(player.getLocation(), "minecraft:block.note_block.xylophone",  1f, 1f);
                            TeamsController.getInstance().getBlueTeam().addEntry(player.getName());
                            ServerUtilities.sendServerMessage(player, ServerUtilities.getMiniMessage().parse(
                                    "Te uniste al equipo <color:#277DA1>AZUL</color>"
                            ));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDroppedItem(PlayerDropItemEvent event) {
        if (GameController.getInstance().getCurrentStatus() != GameController.Status.PLAYING) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemEaten(PlayerItemConsumeEvent event) {
        if (GameController.getInstance().getCurrentStatus() != GameController.Status.PLAYING) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickedUpItems(PlayerAttemptPickupItemEvent event) {
        if (GameController.getInstance().getCurrentStatus() != GameController.Status.PLAYING) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (GameController.getInstance().currentStatus == GameController.Status.PLAYING) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
            GameController.getInstance().validateTeamWin();
            ServerUtilities.playBroadcastSound("minecraft:block.end_portal_frame.fill",  1f, 0.4f);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (GameController.getInstance().getCurrentStatus() == GameController.Status.WAITING) {
            Player player = event.getPlayer();
            ItemStack itemStack = player.getInventory().getContents()[0];
            if (itemStack == null || itemStack.getType() != Material.RED_WOOL) {
                if (!TeamsController.getInstance().isMaster(player)) {
                    GameController.getInstance().handlePlayerJoin(player);
                }
            }
        }
    }

}
