package jcn.kwamparr;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
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
    private FileConfiguration config;
    private String worldName;
    private String mapName;
    private  String mapCenter;
    private int borderSize;
    private int timeToShrink;
    private List<String> listMaterial;
    private String[] mapCenterCoordinates;
    private int countNeedToStart;
    private List<String> structureList;
    private List<String> stringList;
    private List<Material> materialList;
    private List<Location> spawncoord;
    @Override
    public void onEnable() {
        logger.info(PLUGINPREFIX + " запущен");

        config = getConfig();
        worldName = config.getString("WorldName");
        mapName = config.getString("MapName");
        mapCenter = config.getString("MapCenter");
        borderSize = config.getInt("WorldBorderRadius");
        timeToShrink = config.getInt("TimeToShrink");
        mapCenterCoordinates = mapCenter.split(", ");
        countNeedToStart = config.getInt("PlayerNeedToStart");
        listMaterial = config.getStringList("ListOfAllItem");
        for (String Item : listMaterial) {materialList.add(Material.valueOf(Item));}
        structureList = config.getStringList("Structures");
        stringList = config.getStringList("SpawnCoordinates");
        for (String CordString : stringList) {
            String[] xyz = CordString.split(", ");
            spawncoord.add(new Location(Bukkit.getWorld(worldName), Double.parseDouble(xyz[0]), Double.parseDouble(xyz[1]), Double.parseDouble(xyz[2])));
        }

        this.gameManager = new GameManager(this);

        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        File mapsFolder = new File(getDataFolder(), "Maps");
        if (!mapsFolder.exists()){
            mapsFolder.mkdir();
        }

        File structuresFolder = new File(getDataFolder(), "Structures");
        if (!structuresFolder.exists()){
            structuresFolder.mkdir();
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        this.waiting = new Waiting(gameManager, this, worldName, mapCenter, borderSize, timeToShrink, mapCenterCoordinates, mapName, spawncoord, materialList, structureList);
        waiting.registerCommand();
        Bukkit.getPluginManager().registerEvents(waiting, this);

    }

    @Override
    public void onDisable() {
        logger.info(PLUGINPREFIX + " отключен");
    }
}
