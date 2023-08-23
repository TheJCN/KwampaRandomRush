package jcn.kwamparr;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
    private List<String> listMaterial = new ArrayList<>();
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

        if (!setupDatabase()) {
            logger.info(PLUGINPREFIX + "Не удалось подключиться к базе данных. Плагин будет отключен.");
            getServer().getPluginManager().disablePlugin(this);
            return;
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

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Stats (id INTEGER PRIMARY KEY AUTO_INCREMENT, playername VARCHAR(255), kills BIGINT, wins BIGINT)");
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
        logger.info(PLUGINPREFIX + " отключен");
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
}
