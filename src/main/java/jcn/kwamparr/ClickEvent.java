package jcn.kwamparr;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ClickEvent implements Listener {
    private KwampaRR plugin;
    private  GameManager gameManager;
    private FileConfiguration config;
    private Clipboard clipboard;
    private  List<Player> playerList;

    public ClickEvent(GameManager gameManager, List<Player> playerList, KwampaRR plugin) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.playerList = playerList;
    }

    @EventHandler
    public void onClickEvent(PlayerInteractEvent event) {
        if (!(gameManager.getGameState() == GameState.Active)){
            return;
        }
        Player player = event.getPlayer();
        Action action = event.getAction();

        if(action == Action.RIGHT_CLICK_BLOCK){
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            Block clickedBlock = event.getClickedBlock();
            if(clickedBlock == null){
                return;
            }
            if(itemInHand.getType().equals(Material.STRUCTURE_BLOCK)) {
                player.sendTitle("Создание структуры", "");
                player.getInventory().remove(Material.STRUCTURE_BLOCK);
                RandomInPlugin randomInPlugin = new RandomInPlugin(gameManager, playerList, plugin);
                randomInPlugin.RandomStructure(clickedBlock);
            }
            if(itemInHand.getType().equals(Material.COMMAND_BLOCK)) {
                player.sendTitle("Использование комманды", "");
                player.getInventory().remove(Material.COMMAND_BLOCK);
                RandomInPlugin randomInPlugin = new RandomInPlugin(gameManager, playerList, plugin);
                randomInPlugin.RandomCommand(player, playerList);
            }
            if(itemInHand.getType().equals(Material.CHAIN_COMMAND_BLOCK)) {
                player.sendTitle("Использование комманды", "");
                player.getInventory().remove(Material.CHAIN_COMMAND_BLOCK);
                RandomInPlugin randomInPlugin = new RandomInPlugin(gameManager, playerList, plugin);
                randomInPlugin.RandomCommand(player, playerList);
            }
            if(itemInHand.getType().equals(Material.REPEATING_COMMAND_BLOCK)) {
                player.sendTitle("Использование комманды", "");
                player.getInventory().remove(Material.REPEATING_COMMAND_BLOCK);
                RandomInPlugin randomInPlugin = new RandomInPlugin(gameManager, playerList, plugin);
                randomInPlugin.RandomCommand(player, playerList);
            }
        }
    }
}
