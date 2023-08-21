package jcn.kwamparr;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public final class KwampaRR extends JavaPlugin {

    private static  String PLUGINPREFIX = "[KwampaRR]";
    private Logger logger = Bukkit.getLogger();
    private GameManager gameManager;
    private Waiting waiting;


    @Override
    public void onEnable() {

        logger.info(PLUGINPREFIX + " запущен");

        this.gameManager = new GameManager(this);


        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        File schematicsFolder = new File(getDataFolder(), "maps");
        if (!schematicsFolder.exists()){
            schematicsFolder.mkdir();
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        this.waiting = new Waiting(gameManager, this);
        waiting.registerCommand();
        Bukkit.getPluginManager().registerEvents(waiting, this);

    }

    @Override
    public void onDisable() {
        logger.info(PLUGINPREFIX + " отключен");
    }
}
