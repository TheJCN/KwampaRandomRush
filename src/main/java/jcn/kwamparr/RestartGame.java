package jcn.kwamparr;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.List;

public class RestartGame {
    private GameManager gameManager;
    private KwampaRR plugin;
    private Clipboard clipboard;
    private FileConfiguration config;

    public RestartGame(KwampaRR plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    public void LoadMap(String mapFileName) {
        if(gameManager.getGameState() == GameState.Restart){
            File schematicFile = new File(plugin.getDataFolder(), "maps/" + mapFileName + ".schematic");
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                clipboard = reader.read();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            PasteMap();
        }
    }

    public void PasteMap() {
        config = plugin.getConfig();
        String worldName = config.getString("WorldName");
        org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        com.sk89q.worldedit.world.World worldEditWorld = BukkitAdapter.adapt(bukkitWorld);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(worldEditWorld)) {
            Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
                    .to(BlockVector3.at(0, 64, 0)).build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }
        BukkitRunnable bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                gameManager.setGameState(GameState.Waiting);
            }
        };
        bukkitRunnable.runTaskLater(plugin, 20L);
        ClearingMap();
        gameManager.setGameState(GameState.Waiting);
    }

    public void ClearingMap(){
        config = plugin.getConfig();
        String worldName = config.getString("WorldName");
        List<Entity> entityList = Bukkit.getWorld(worldName).getEntities();
        for(Entity entity : entityList){
            if(!entity.getType().equals(EntityType.PLAYER)){
                entity.remove();
            }
        }
        World world = Bukkit.getWorld(worldName);
        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setSize(1000);
    }
}
