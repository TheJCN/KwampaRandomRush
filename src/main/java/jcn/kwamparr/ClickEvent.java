package jcn.kwamparr;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import jdk.tools.jlink.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.stringtemplate.v4.misc.Coordinate;

import javax.security.auth.login.Configuration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class ClickEvent implements Listener {
    private KwampaRR plugin;
    private  GameManager gameManager;
    private FileConfiguration config;
    private Clipboard clipboard;

    public ClickEvent(KwampaRR plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    Logger logger = Bukkit.getLogger();
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
                GenerateRandomStructure(clickedBlock);
            }
        }
    }

    private void GenerateRandomStructure(Block clickedBlock){
        Double x = clickedBlock.getLocation().getX();
        Double y = clickedBlock.getLocation().getY();
        Double z = clickedBlock.getLocation().getZ();
        config = plugin.getConfig();
        List<String> structureList = config.getStringList("Structures");
        Random random = new Random();
        int Index = random.nextInt(structureList.size());
        File structure = new File(plugin.getDataFolder(), "Structures/" + structureList.get(Index) + ".schem");

        ClipboardFormat format = ClipboardFormats.findByFile(structure);
        try (ClipboardReader reader = format.getReader(new FileInputStream(structure))) {
            clipboard = reader.read();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        org.bukkit.World bukkitWorld = Bukkit.getWorld("VoidWorld");
        com.sk89q.worldedit.world.World worldEditWorld = BukkitAdapter.adapt(bukkitWorld);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(worldEditWorld)) {
            Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
                    .to(BlockVector3.at(x, y, z)).build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }
    }
}
