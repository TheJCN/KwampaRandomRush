package jcn.kwamparr;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.logging.Logger;

public class ClickEvent implements Listener {
    private Logger logger;
    private KwampaRR plugin;
    private  GameManager gameManager;
    private Clipboard clipboard;
    private  List<Player> playerList;
    private List<Material> materialList;
    private String worldName;
    private List<String> structureList;

    public ClickEvent(GameManager gameManager, List<Player> playerList, KwampaRR plugin, List<Material> materialList, String worldName, List<String> structureList, Logger logger) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        this.playerList = playerList;
        this.materialList = materialList;
        this.worldName = worldName;
        this.structureList = structureList;
        this.logger = logger;
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
                RandomInPlugin randomInPlugin = new RandomInPlugin(gameManager, playerList, plugin, materialList, worldName, structureList, logger);
                randomInPlugin.RandomStructure(clickedBlock);
            }
            if(itemInHand.getType().equals(Material.COMMAND_BLOCK)) {
                player.sendTitle("Использование комманды", "");
                player.getInventory().remove(Material.COMMAND_BLOCK);
                RandomInPlugin randomInPlugin = new RandomInPlugin(gameManager, playerList, plugin, materialList, worldName, structureList, logger);
                randomInPlugin.RandomCommand(player, playerList);
            }
            if(itemInHand.getType().equals(Material.CHAIN_COMMAND_BLOCK)) {
                player.sendTitle("Использование комманды", "");
                player.getInventory().remove(Material.CHAIN_COMMAND_BLOCK);
                RandomInPlugin randomInPlugin = new RandomInPlugin(gameManager, playerList, plugin, materialList, worldName, structureList, logger);
                randomInPlugin.RandomCommand(player, playerList);
            }
            if(itemInHand.getType().equals(Material.REPEATING_COMMAND_BLOCK)) {
                player.sendTitle("Использование комманды", "");
                player.getInventory().remove(Material.REPEATING_COMMAND_BLOCK);
                RandomInPlugin randomInPlugin = new RandomInPlugin(gameManager, playerList, plugin, materialList, worldName, structureList, logger);
                randomInPlugin.RandomCommand(player, playerList);
            }
        }
    }
}
