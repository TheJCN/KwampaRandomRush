package jcn.kwamparr;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class AcitveGame implements Listener {
    private FileConfiguration config;
    private GameManager gameManager;
    private List<Player> playerList;
    private KwampaRR plugin;
    private ConfigurationSection spawnCoordinates;
    private List<Location> spawnloc = new ArrayList<>();

    public AcitveGame(GameManager gameManager, List<Player> playerList, KwampaRR plugin) {
        this.gameManager = gameManager;
        this.playerList = playerList;
        this.plugin = plugin;
    }


    public void LogicGame() {
        Bukkit.getPluginManager().registerEvents(new AcitveGame(gameManager, playerList, plugin), plugin);
        Bukkit.getPluginManager().registerEvents(new ClickEvent(gameManager, playerList, plugin), plugin);
        config = plugin.getConfig();
        String worldName = config.getString("WorldName");
        String MapCenter = config.getString("MapCenter");
        int BorderSize = config.getInt("WorldBorderRadius");
        int TimeToShrink = config.getInt("TimeToShrink");
        String[] MapCenterCoordinates = MapCenter.split(", ");
        World world = Bukkit.getWorld(worldName);
        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setCenter(Double.parseDouble(MapCenterCoordinates[0]), Double.parseDouble(MapCenterCoordinates[1]));
        worldBorder.setSize(BorderSize);
        worldBorder.setDamageAmount(0.5);
        worldBorder.setSize(5, TimeToShrink);
        List<String> stringList = config.getStringList("SpawnCoordinates");
        for (String CordString : stringList) {
            String[] xyz = CordString.split(", ");
            spawnloc.add(new Location(Bukkit.getWorld(worldName), Double.parseDouble(xyz[0]), Double.parseDouble(xyz[1]), Double.parseDouble(xyz[2])));
        }
        int indexloc = 0;
        if (gameManager.getGameState() == GameState.Active) {
            for (Player player : playerList) {
                player.sendTitle("Одиночный режим", "Тимминг запрещен!");
                player.setGameMode(GameMode.SURVIVAL);
                player.setAllowFlight(false);
                player.teleport(spawnloc.get(indexloc));
                indexloc++;
            }
            RandomInPlugin randomInPlugin = new RandomInPlugin(gameManager, playerList, plugin);
            randomInPlugin.RandomItem();
        }
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
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
    public void onPlayerQuit(PlayerQuitEvent event){
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

    public void EndOfTheGame(){
        if(gameManager.getGameState() != GameState.PreRestart){
            return;
        }
        String winner = playerList.get(0).getName();
        for(Player player : Bukkit.getOnlinePlayers()){
            player.sendTitle(ChatColor.GOLD + "Победил - " + winner, ChatColor.WHITE + "Поздравляем его!");
        }
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
            config = plugin.getConfig();
            String worldName = config.getString("WorldName");
            String mapFileName = config.getString("MapName");
            String MapCenter = config.getString("MapCenter");
            String[] MapCenterCoordinates = MapCenter.split(", ");
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
            restartGame.LoadMap(mapFileName);
        }
    }
 }
