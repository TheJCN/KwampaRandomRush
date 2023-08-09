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
        List<Location> spawnloc = new ArrayList<>();
        spawnloc.add(new Location(Bukkit.getServer().getWorld("world"),10, 0, 10)); //1
        spawnloc.add(new Location(Bukkit.getServer().getWorld("world"),10, 0, 0)); //2
        spawnloc.add(new Location(Bukkit.getServer().getWorld("world"),0, 0, 10)); //3
        spawnloc.add(new Location(Bukkit.getServer().getWorld("world"),0, 0, 0)); //4
        spawnloc.add(new Location(Bukkit.getServer().getWorld("world"),5, 0, 5)); //5
        spawnloc.add(new Location(Bukkit.getServer().getWorld("world"),5, 0, 0)); //6
        spawnloc.add(new Location(Bukkit.getServer().getWorld("world"),0, 0, 5)); //7
        spawnloc.add(new Location(Bukkit.getServer().getWorld("world"),15, 0, 0)); //8
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
        }.runTaskTimer(plugin, 0L, 20L);
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getEntity().getPlayer();
        playerList.remove(player);
        logger.info(String.valueOf(playerList.size()));
        player.setGameMode(GameMode.SPECTATOR);
        if(playerList.isEmpty()){
            EndOfTheGame();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        if(player.getGameMode() == GameMode.SURVIVAL){
            playerList.remove(player);
            logger.info(String.valueOf(playerList.size()));
            if (playerList.isEmpty()) {
                EndOfTheGame();
            }
        }
        else {
            Logger logger = Bukkit.getLogger();
            logger.info("Кто-то юзает читы!!!!!");
        }
    }

    public void EndOfTheGame(){
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
        gameManager.setGameState(GameState.Restart);
        TimerBeforekick();
    }

    public void TimerBeforekick(){
        new BukkitRunnable() {
            int timer = 10;

            @Override
            public void run() {
                if (timer > 0) {
                    if(timer == 1){
                        for(Player player : Bukkit.getOnlinePlayers()) {
                            player.kickPlayer("Игра законченна");
                            gameManager.setGameState(GameState.Restart);
                        }
                    }
                    else {
                        Bukkit.broadcastMessage("До окончания " + timer);
                        timer--;
                    }
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("KwampaRR"), 0L, 20L);
        gameManager.setGameState(GameState.Restart);
    }
 }
