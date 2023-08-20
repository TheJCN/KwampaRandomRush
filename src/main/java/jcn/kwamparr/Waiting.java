package jcn.kwamparr;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Waiting implements Listener {
    private int CountNeedToStart;
    private FileConfiguration config;
    private List<Player> playerList = new ArrayList<>();
    private GameManager gameManager;
    private KwampaRR plugin;

    public Waiting(GameManager gameManager, KwampaRR plugin) {
        this.gameManager = gameManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void opPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (gameManager.getGameState() == GameState.Active ||  gameManager.getGameState() == GameState.Teleporting) {
            player.setGameMode(GameMode.SPECTATOR);
        }

        if (gameManager.getGameState() == GameState.Waiting) {
            player.teleport(new Location(Bukkit.getWorld("VoidWorld"), 0, 70, 0));
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            player.getActivePotionEffects().clear();
            player.getInventory().clear();
            config = plugin.getConfig();
            CountNeedToStart = config.getInt("PlayerNeedToStart");
            playerList.add(player);
            player.getInventory().clear();
            if (playerList.size() >= CountNeedToStart) {
                Bukkit.broadcastMessage("Игра начинается! Приготовтесь!");
                gameManager.setGameState(GameState.Teleporting);
                TimeBeforeGame(playerList);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        if(gameManager.getGameState() != GameState.Waiting) {
            return;
        }
        Player player = event.getPlayer();
        playerList.remove(player);
        if(playerList.size() >= CountNeedToStart){
            gameManager.setGameState(GameState.Waiting);
        }
    }

    public void TimeBeforeGame(List<Player> playerList){
        new BukkitRunnable() {
            int timer = 5;

            @Override
            public void run() {
                if(gameManager.getGameState() == GameState.Teleporting) {
                    if (timer > 0) {
                        Bukkit.broadcastMessage("Телепортируем через " + timer);
                        timer--;
                    } else {
                        Bukkit.broadcastMessage("Игра началалась!");
                        gameManager.setGameState(GameState.Active);
                        AcitveGame acitveGame = new AcitveGame(gameManager, playerList, plugin);
                        acitveGame.LogicGame();
                        this.cancel();
                    }
                }
                else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("KwampaRR")), 0,20L);
    }
}
