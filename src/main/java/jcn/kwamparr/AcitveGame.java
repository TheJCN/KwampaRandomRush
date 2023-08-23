package jcn.kwamparr;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AcitveGame implements Listener {
    private Logger logger;
    private GameManager gameManager;
    private List<Player> playerList;
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
    private Connection connection;

    public AcitveGame(GameManager gameManager, List<Player> playerList, KwampaRR plugin, String worldName, String mapCenter, int borderSize, int timeToShrink, String[] mapCenterCoordinates, String mapName, List<Location> spawncoord, List<Material> materialList, List<String> structureList, Connection connection) {
        this.gameManager = gameManager;
        this.playerList = playerList;
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
        this.connection = connection;
    }


    public void LogicGame() {
        Bukkit.getPluginManager().registerEvents(new AcitveGame(gameManager, playerList, plugin, worldName, mapCenter, borderSize, timeToShrink, mapCenterCoordinates, mapName, spawncoord, materialList, structureList, connection), plugin);
        Bukkit.getPluginManager().registerEvents(new ClickEvent(gameManager, playerList, plugin, materialList, worldName, structureList, logger), plugin);
        World world = Bukkit.getWorld(worldName);
        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setCenter(Double.parseDouble(mapCenterCoordinates[0]), Double.parseDouble(mapCenterCoordinates[1]));
        worldBorder.setSize(borderSize);
        worldBorder.setDamageAmount(0.5);
        worldBorder.setSize(5, timeToShrink);
        int indexloc = 0;
        if (gameManager.getGameState() == GameState.Active) {
            for (Player player : playerList) {
                player.sendTitle("Одиночный режим", "Тимминг запрещен!");
                player.setGameMode(GameMode.SURVIVAL);
                player.setAllowFlight(false);
                player.teleport(spawncoord.get(indexloc));
                indexloc++;
            }
            RandomInPlugin randomInPlugin = new RandomInPlugin(gameManager, playerList, plugin, materialList, worldName, structureList, logger);
            randomInPlugin.RandomItem();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) throws SQLException {
        Player killer = event.getEntity().getPlayer().getKiller();
        if (killer != null) {
            Statistic statistic = new Statistic(connection);
            statistic.addKills(killer);
        }

        if (gameManager.getGameState() != GameState.Active){
            return;
        }

        Player player = event.getEntity().getPlayer();
        playerList.remove(player);
        player.setGameMode(GameMode.SPECTATOR);

        if(playerList.size() == 1){
            gameManager.setGameState(GameState.PreRestart);
            EndOfTheGame();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) throws SQLException {
        if (gameManager.getGameState() != GameState.Active){
            return;
        }
        Player player = event.getPlayer();
        if(player.getGameMode() == GameMode.SURVIVAL){
            playerList.remove(player);
            if (playerList.size() == 1) {
                gameManager.setGameState(GameState.PreRestart);
                EndOfTheGame();
            }
        }
    }

    public void EndOfTheGame() throws SQLException {
        if(gameManager.getGameState() != GameState.PreRestart){
            return;
        }
        Player playerwinner = playerList.get(0);
        String winner = playerList.get(0).getName();
        Statistic statistic = new Statistic(connection);
        for(Player player : Bukkit.getOnlinePlayers()){
            player.sendTitle(ChatColor.GOLD + "Победил - " + winner, ChatColor.WHITE + "Поздравляем его!");
        }
        statistic.addWins(playerwinner);
        new BukkitRunnable() {
            @Override
            public void run() {
                TimerBeforekick();
            }
        }.runTaskLater(plugin, 20);
    }

    public void TimerBeforekick() {
        if (gameManager.getGameState() == GameState.PreRestart) {
            new BukkitRunnable() {
                int timer = 5;

                @Override
                public void run () {
                    if (timer > 0) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.sendTitle("До окончания " + timer, "");
                        }
                        timer--;
                    } else {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.sendTitle(ChatColor.GOLD + "Загрузка карты!", "Пожалуйста подождите");
                        }
                        End();
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L,20L);
        }
    }

    public void End(){
        if(gameManager.getGameState() == GameState.PreRestart) {
            String[] MapCenterCoordinates = mapCenter.split(", ");
            playerList.clear();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerList.add(player);
                player.setGameMode(GameMode.ADVENTURE);
                player.teleport(new Location(Bukkit.getWorld(worldName),Double.parseDouble(MapCenterCoordinates[0]), 70, Double.parseDouble(MapCenterCoordinates[1])));
                player.setHealth(20);
                player.setSaturation(20);
                player.getInventory().clear();
                player.setAllowFlight(true);
            }
            gameManager.setGameState(GameState.Restart);
            RestartGame restartGame = new RestartGame(plugin, gameManager);
            restartGame.LoadMap(mapName);
        }
    }
 }
