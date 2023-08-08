package jcn.kwamparr;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public final class KwampaRR extends JavaPlugin {
    private static  String PLUGINPREFIX = "[KwampaRR]";
    private Logger logger;

    private GameManager gameManager;

    @Override
    public void onEnable() {

        logger.info(PLUGINPREFIX + " запущен");

        this.gameManager = new GameManager(this);

        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        logger.info(PLUGINPREFIX + " отключен");
    }
}
