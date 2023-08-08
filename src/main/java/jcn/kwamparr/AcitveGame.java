package jcn.kwamparr;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AcitveGame  extends JavaPlugin implements Listener{
    private GameManager gameManager;

    public AcitveGame(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void LogicGame(List<Player> playerList){
        if(gameManager.getGameState() == GameState.Active){
            for(Player player : playerList){
                player.sendTitle("Одиночный режим", "Тимминг запрещен!");
            }
            RandomItem(playerList);

        }
    }
    public void RandomItem(List<Player> playerList) {
        FileConfiguration config = getConfig();
        List<String> itemStrings = config.getStringList("List");
        List<Material> materials = new ArrayList<>();
        int ValueOfItems = materials.size();
        for (String itemString : itemStrings) {
            try {
                Material material = Material.valueOf(itemString);
                materials.add(material);
            } catch (IllegalArgumentException e) {
                getLogger().warning("Недопустимый предмет: " + itemString);
            }
        }
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
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("KwampaRR"), 0L, 200L);
    }
 }
