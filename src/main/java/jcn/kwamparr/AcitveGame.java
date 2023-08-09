package jcn.kwamparr;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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

public class AcitveGame implements Listener{

    private GameManager gameManager;
    private List<Player> playerList;
    private KwampaRR plugin;

    public AcitveGame(GameManager gameManager, List<Player> playerList, KwampaRR plugin) {
        this.gameManager = gameManager;
        this.playerList = playerList;
        this.plugin = plugin;

    }

    public void LogicGame(){
        if(gameManager.getGameState() == GameState.Active){
            for(Player player : playerList){
                player.sendTitle("Одиночный режим", "Тимминг запрещен!");
            }
            RandomItem();

        }
    }
    public void RandomItem() {
        FileConfiguration config = plugin.getConfig();
        List<String> itemStrings = config.getStringList("List");
        List<Material> materials = new ArrayList<>();

        for (String itemString : itemStrings) {
            try {
                Material material = Material.valueOf(itemString);
                materials.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Недопустимый предмет: " + itemString);
            }
        }

        int ValueOfItems = materials.size(); // Вычисляем размер списка materials

        new BukkitRunnable(){
            @Override
            public void run() {
                if(gameManager.getGameState() == GameState.Active){
                    for(Player player : playerList){
                        Random random = new Random();
                        int randomIndex = random.nextInt(ValueOfItems);
                        player.getInventory().setItemInMainHand(new ItemStack(materials.get(randomIndex)));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 200L);
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getEntity().getPlayer();
        playerList.remove(player);
        player.setGameMode(GameMode.SPECTATOR);
        if(playerList.size() == 0){
            EndOfTheGame();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        if(player.getGameMode() == GameMode.SURVIVAL){
            playerList.remove(player);
            if (playerList.size() == 0){
                EndOfTheGame();
            }
        }
        else {
            Logger logger = Bukkit.getLogger();
            logger.info("Кто-то юзает читы!!!!!");
        }
    }

    public void EndOfTheGame(){
        Bukkit.broadcastMessage("Выиграл - " + playerList.get(0).getPlayer().getDisplayName());
        gameManager.setGameState(GameState.Restart);
        TimerBeforekick();
    }

    public void TimerBeforekick(){
        new BukkitRunnable() {
            int timer = 10;

            @Override
            public void run() {
                if (timer > 0) {
                    Bukkit.broadcastMessage("До окончания " + timer);
                    timer--;
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("KwampaRR"), 0L, 20L);
        for(Player player : Bukkit.getOnlinePlayers()){
            player.kickPlayer("Игра законченна");
        }
        gameManager.setGameState(GameState.Restart);
    }
 }
