package jcn.kwamparr;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Waiting implements Listener, CommandExecutor {
    private int countNeedToStart;
    private FileConfiguration config;
    private List<Player> playerList = new ArrayList<>();
    private GameManager gameManager;
    private KwampaRR plugin;
    private String worldName;
    private  String mapCenter;
    private int borderSize;
    private int timeToShrink;
    private String[] mapCenterCoordinates;
    private String mapName;
    private List<Location> spawncoord;
    private List<Material> materialList;
    private List<String> structureList;


    public Waiting(GameManager gameManager, KwampaRR plugin, String worldName, String mapCenter, int borderSize, int timeToShrink, String[] mapCenterCoordinates, String mapName, List<Location> spawncoord, List<Material> materialList, List<String> structureList, int countNeedToStart) {
        this.gameManager = gameManager;
        this.plugin = plugin;
        this.worldName = worldName;
        this.mapCenter = mapCenter;
        this.borderSize = borderSize;
        this.timeToShrink = timeToShrink;
        this.mapCenterCoordinates = mapCenterCoordinates;
        this.mapName = mapName;
        this.spawncoord = spawncoord;
        this.materialList = materialList;
        this.structureList = structureList;
        this.countNeedToStart = countNeedToStart;
    }

    public void registerCommand() {
        if (plugin != null) {
            plugin.getCommand("rrstart").setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        if(gameManager.getGameState() == GameState.Waiting) {
            if (playerList.size() > 1) {
                gameManager.setGameState(GameState.Teleporting);
                TimeBeforeGame(playerList);
                return true;
            }
            player.sendMessage(ChatColor.RED + "Kоличество игроков меньше 2!");
            return false;
        }
        player.sendMessage("Игра уже идет!");
        return false;
    }

    @EventHandler
    public void opPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (gameManager.getGameState() == GameState.Active ||  gameManager.getGameState() == GameState.Teleporting) {player.setGameMode(GameMode.SPECTATOR);}
        if (gameManager.getGameState() == GameState.Waiting) {
            player.teleport(new Location(Bukkit.getWorld(worldName),Double.parseDouble(mapCenterCoordinates[0]), 70, Double.parseDouble(mapCenterCoordinates[1])));
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            player.getActivePotionEffects().clear();
            player.getInventory().clear();
            player.setHealth(20);
            playerList.add(player);
            player.getInventory().clear();
            if (playerList.size() >= countNeedToStart) {
                Bukkit.broadcastMessage("Игра начинается! Приготовтесь!");
                gameManager.setGameState(GameState.Teleporting);
                TimeBeforeGame(playerList);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        if(gameManager.getGameState() != GameState.Waiting) {
            return;
        }
        Player player = event.getPlayer();
        playerList.remove(player);
        if(playerList.size() >= countNeedToStart){
            gameManager.setGameState(GameState.Waiting);
        }
    }

    public void TimeBeforeGame(List<Player> playerList){
        new BukkitRunnable() {
            int timer = 5;

            @Override
            public void run() {
                if(gameManager.getGameState() == GameState.Teleporting) {
                    if (timer > 0) {
                        for(Player player : playerList) {
                            player.sendTitle(ChatColor.GOLD + "Телепортируем через " + timer, ChatColor.RED +  "Не двигайтесь!");
                        }
                        timer--;
                    } else {
                        for(Player player : playerList) {
                            player.sendTitle(ChatColor.GOLD + "Игра начинается!", ChatColor.RED + "Не двигайтесь!");
                        }
                        gameManager.setGameState(GameState.Active);
                        AcitveGame acitveGame = new AcitveGame(gameManager, playerList, plugin, worldName, mapCenter, borderSize, timeToShrink, mapCenterCoordinates, mapName, spawncoord, materialList, structureList);
                        acitveGame.LogicGame();
                        this.cancel();
                    }
                }
                else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("KwampaRR")), 0,20L);
    }

    @EventHandler
    public void OffPvp(EntityDamageByEntityEvent event){
        if (gameManager.getGameState() == GameState.Waiting || gameManager.getGameState() == GameState.Teleporting){
            event.setCancelled(true);
        }
    }
}
