package jcn.kwamparr;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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

    private Logger logger = Bukkit.getLogger();
    private GameManager gameManager;
    private List<Player> playerList;
    private KwampaRR plugin;

    public AcitveGame(GameManager gameManager, List<Player> playerList, KwampaRR plugin) {
        this.gameManager = gameManager;
        this.playerList = playerList;
        this.plugin = plugin;

    }

    public void LogicGame(){
        List<Location> spawnloc = new ArrayList<>(); //todo Все локации надо брать из конфига!
        spawnloc.add(new Location(Bukkit.getServer().getWorld("VoidWorld"),15.5, 107, 30.5)); //1
        spawnloc.add(new Location(Bukkit.getServer().getWorld("VoidWorld"),26.5, 107, 26.5)); //2
        spawnloc.add(new Location(Bukkit.getServer().getWorld("VoidWorld"),30.5, 107, 15.5)); //3
        spawnloc.add(new Location(Bukkit.getServer().getWorld("VoidWorld"),26.5, 107, 4.5)); //4
        spawnloc.add(new Location(Bukkit.getServer().getWorld("VoidWorld"),15.5, 107, 0.5)); //5
        spawnloc.add(new Location(Bukkit.getServer().getWorld("VoidWorld"),4.5, 107, 4.5)); //6
        spawnloc.add(new Location(Bukkit.getServer().getWorld("VoidWorld"),0.5, 107, 15.5)); //7
        spawnloc.add(new Location(Bukkit.getServer().getWorld("VoidWorld"),4.5, 107, 26.5)); //8
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
        List<Material> materials = new ArrayList<>();
        materials.add(STONE);
        materials.add(DIAMOND_SWORD);
        materials.add(BREAD);

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
        playerList.remove(event.getEntity().getPlayer());
        logger.info(String.valueOf(playerList.remove(event.getEntity().getPlayer())));
        logger.info(playerList.toString());
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
            playerList.remove(event.getPlayer());
            logger.info(String.valueOf(playerList.remove(event.getPlayer())));
            logger.info(playerList.toString());
            logger.info(String.valueOf(playerList.size()));
            if (playerList.size() == 1) {
                gameManager.setGameState(GameState.PreRestart);
                EndOfTheGame();
            }
        }
        else {
            Logger logger = Bukkit.getLogger();
            logger.info("Кто-то юзает читы!!!!!");
        }
    }

    public void EndOfTheGame(){
        if(gameManager.getGameState() != GameState.PreRestart){
            return;
        }
        String winner = null;
        for(Player player : Bukkit.getOnlinePlayers()){
            if(player.getGameMode().equals(GameMode.SURVIVAL)){
                winner = player.getName();
            }
            else{
                winner = "никто";
            }
        }
        Bukkit.broadcastMessage("Выиграл - " + winner);
        TimerBeforekick();
    }

    public void TimerBeforekick(){
        if(gameManager.getGameState() != GameState.PreRestart){
            return;
        }
        new BukkitRunnable() {
            int timer = 10;

            @Override
            public void run() {
                if (timer > 0) {
                    Bukkit.broadcastMessage("До окончания " + timer);
                    timer--;
                }
                else{
                    End();
                    this.cancel();
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("KwampaRR"), 0L, 20L);
    }

    public void End(){
        if(gameManager.getGameState() == GameState.Active) {

            String mapFileName = "RR-1"; //todo Надо брать эту строку из config!
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
