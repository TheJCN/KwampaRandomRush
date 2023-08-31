package jcn.kwamparr;

import jdk.jpackage.internal.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
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
    private int countNeedToStart;
    private List<String> blackListMaterial = new ArrayList<>();
    private List<String> structureList = new ArrayList<>();
    private List<String> stringList = new ArrayList<>();
    private List<Material> materialList = new ArrayList<>();
    private List<Location> spawncoord = new ArrayList<>();
    private String[] mapCenterCoordinates;
    private MySQLDataBaseManager databaseManager;
    private Connection connection;
    @Override
    public void onEnable() {
        logger.info(PLUGINPREFIX + " запущен");

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        File mapsFolder = new File(getDataFolder(), "Maps");
        if (!mapsFolder.exists()){
            mapsFolder.mkdir();
        }

        File mapFile = new File(getDataFolder(), "RR-NewMap.schem");
        if (!mapFile.exists()) {
            try {
                saveMapToMaps("RR-NewMap.schem");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File structuresFolder = new File(getDataFolder(), "Structures");
        if (!structuresFolder.exists()){
            structuresFolder.mkdir();
        }

        File BigCastle = new File(getDataFolder(), "RR-BigCastle.schem");
        if (!BigCastle.exists()){
            try {
                saveMapToStructure("RR-BigCastle.schem");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File BrokenTexture = new File(getDataFolder(), "RR-BrokenTexture.schem");
        if (!BrokenTexture.exists()){
            try {
                saveMapToStructure("RR-BrokenTexture.schem");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File Caves = new File(getDataFolder(), "RR-Caves.schem");
        if (!Caves.exists()){
            try {
                saveMapToStructure("RR-Caves.schem");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File RandomTexture = new File(getDataFolder(), "RR-RandomTexture.schem");
        if (!RandomTexture.exists()){
            try {
                saveMapToStructure("RR-RandomTexture.schem");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File Ruins = new File(getDataFolder(), "RR-Ruins.schem");
        if (!Ruins.exists()){
            try {
                saveMapToStructure("RR-Ruins.schem");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File SmallCastle = new File(getDataFolder(), "RR-SmallCastle.schem");
        if (!SmallCastle.exists()){
            try {
                saveMapToStructure("RR-SmallCastle.schem");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File Well = new File(getDataFolder(), "RR-Well.schem");
        if (!Well.exists()){
            try {
                saveMapToStructure("RR-Well.schem");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        config = getConfig();
        worldName = config.getString("WorldName");
        mapName = config.getString("MapName");


        mapCenter = config.getString("MapCenter");
        borderSize = config.getInt("WorldBorderRadius");
        timeToShrink = config.getInt("TimeToShrink");
        mapCenterCoordinates = mapCenter.split(", ");
        countNeedToStart = config.getInt("PlayerNeedToStart");
        blackListMaterial = config.getStringList("BlackList");
        materialList = new ArrayList<>(Arrays.asList(Material.values()));
        for (String Item : blackListMaterial) {materialList.remove(Material.valueOf(Item));}
        structureList = config.getStringList("Structures");
        stringList = config.getStringList("SpawnCoordinates");
        for (String CordString : stringList) {
            String[] xyz = CordString.split(", ");
            spawncoord.add(new Location(Bukkit.getWorld(worldName), Double.parseDouble(xyz[0]), Double.parseDouble(xyz[1]), Double.parseDouble(xyz[2])));
        }

        if (!setupDatabase()) {
            logger.info(PLUGINPREFIX + " Failed to connect to the database. The plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.gameManager = new GameManager(this);


        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Stats (id INTEGER PRIMARY KEY AUTO_INCREMENT, playername VARCHAR(255), wins BIGINT)");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.waiting = new Waiting(gameManager, this, worldName, mapCenter, borderSize, timeToShrink, mapCenterCoordinates, mapName, spawncoord, materialList, structureList, countNeedToStart, connection);
        waiting.registerCommand();
        Bukkit.getPluginManager().registerEvents(waiting, this);
        getCommand("top").setExecutor(new CommandTop(connection));

    }

    @Override
    public void onDisable() {
        logger.info(PLUGINPREFIX + " is disabled.");
    }

    private boolean setupDatabase() {
        String host = getConfig().getString("mysql.host");
        int port = getConfig().getInt("mysql.port");
        String database = getConfig().getString("mysql.database");
        String username = getConfig().getString("mysql.username");
        String password = getConfig().getString("mysql.password");
        databaseManager = new MySQLDataBaseManager(host, port, database, username, password);
        if (!databaseManager.connect()) {
            return false;
        }
        connection = databaseManager.getConnection();
        return true;
    }

    private void saveMapToMaps(String resourceName) throws IOException {
        InputStream inputStream = getResource(resourceName);
        FileOutputStream out = new FileOutputStream(getDataFolder() + "/Maps/" + resourceName);

        inputStream.transferTo(out);

        out.close();
        inputStream.close();
    }

    private void saveMapToStructure(String resourceName) throws IOException {
        InputStream inputStream = getResource(resourceName);
        FileOutputStream out = new FileOutputStream(getDataFolder() + "/Structures/" + resourceName);

        inputStream.transferTo(out);

        out.close();
        inputStream.close();
    }
}
