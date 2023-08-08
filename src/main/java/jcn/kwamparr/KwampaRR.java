package jcn.kwamparr;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

import static org.bukkit.configuration.file.YamlConfiguration.loadConfiguration;

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

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }

    }

    @Override
    public void onDisable() {
        logger.info(PLUGINPREFIX + " отключен");
    }
}
