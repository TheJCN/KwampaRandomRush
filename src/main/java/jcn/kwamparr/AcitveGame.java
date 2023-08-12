package jcn.kwamparr;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static org.bukkit.Material.*;

public class AcitveGame implements Listener{
    private FileConfiguration config;
    private Logger logger = Bukkit.getLogger();
    private GameManager gameManager;
    private List<Player> playerList;
    private KwampaRR plugin;
    private ConfigurationSection spawnCoordinates;
    private List<Material> materials = new ArrayList<>();
    private List<Location> spawnloc = new ArrayList<>();

    public AcitveGame(GameManager gameManager, List<Player> playerList, KwampaRR plugin) {
        this.gameManager = gameManager;
        this.playerList = playerList;
        this.plugin = plugin;
    }


    public void LogicGame(){
        config = plugin.getConfig();
        String worldName = config.getString("WorldName");
        String MapCenter = config.getString("MapCenter");
        int BorderSize = config.getInt("WorldBorderRadius");
        int TimeToShrink = config.getInt("TimeToShrink");
        String [] MapCenterCoordinates = MapCenter.split(", ");
        World world = Bukkit.getWorld(worldName);
        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setCenter(Double.parseDouble(MapCenterCoordinates[0]), Double.parseDouble(MapCenterCoordinates[1]));
        worldBorder.setSize(BorderSize);
        worldBorder.setDamageAmount(0.5);
        worldBorder.setSize(5, TimeToShrink * 20L);
        Bukkit.getPluginManager().registerEvents(new AcitveGame(gameManager, playerList, plugin), plugin);
        List<String> stringList2 = config.getStringList("ListOfAllItem");
        for(String Item : stringList2){
            materials.add(Material.valueOf(Item));
        }
        List<String> stringList = config.getStringList("SpawnCoordinates");
        for (String CordString : stringList) {
            String[] xyz =  CordString.split(", ");
            System.out.println(Double.parseDouble(xyz[0]));
            System.out.println(Double.parseDouble(xyz[1]));
            System.out.println(Double.parseDouble(xyz[2]));
            System.out.println(Bukkit.getWorld(worldName));
            spawnloc.add(new Location(Bukkit.getWorld(worldName), Double.parseDouble(xyz[0]), Double.parseDouble(xyz[1]), Double.parseDouble(xyz[2])));
        }
        int indexloc = 0;
        if(gameManager.getGameState() == GameState.Active){
            for(Player player : playerList){
                logger.info(playerList.toString());
                player.sendTitle("Одиночный режим", "Тимминг запрещен!");
                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().clear();
                player.teleport(spawnloc.get(indexloc));
                indexloc++;
            }
            RandomItem();
        }
    }

    public void RandomItem() {
        int ValueOfItems = materials.size(); // Вычисляем размер списка materials

        new BukkitRunnable(){
            @Override
            public void run() {
                if(gameManager.getGameState() == GameState.Active){
                    for(Player player : playerList){
                        Random random = new Random();
                        int randomIndex = random.nextInt(ValueOfItems);
                        player.getInventory().addItem(new ItemStack(materials.get(randomIndex)));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 100L);
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        if (gameManager.getGameState() != GameState.Active){
            return;
        }
        Player player = event.getEntity().getPlayer();

        System.out.println("1" + playerList);

        playerList.remove(player);

        System.out.println("2" + playerList);

        logger.info(String.valueOf(playerList.size()));
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
            logger.info(String.valueOf(playerList.size()));
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
        Bukkit.broadcastMessage("Выиграл - " + winner);
        TimerBeforekick();
    }

    public void TimerBeforekick() {
        if (gameManager.getGameState() == GameState.PreRestart) {
            new BukkitRunnable() {
                int timer = 10;

                @Override
                public void run() {
                    if (timer > 0) {
                        Bukkit.broadcastMessage("До окончания " + timer);
                        timer--;
                    } else {
                        End();
                        this.cancel();
                    }
                }
            }.runTaskTimer(Bukkit.getPluginManager().getPlugin("KwampaRR"), 0L, 20L);
        }
    }

    public void End(){
        if(gameManager.getGameState() == GameState.PreRestart) {
            config = plugin.getConfig();
            String mapFileName = config.getString("MapName");
            logger.info(mapFileName);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.kickPlayer("Игра законченна");
            }
            gameManager.setGameState(GameState.Restart);
            RestartGame restartGame = new RestartGame(plugin, gameManager);
            restartGame.LoadMap(mapFileName);
            playerList.clear();
        }
    }
 }
