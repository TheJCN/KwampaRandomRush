package jcn.kwamparr;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Waiting implements Listener {
    private List<Player> playerList = new ArrayList<>();
    private  GameManager gameManager;
    private  KwampaRR plugin;

    public Waiting (GameManager gameManager, KwampaRR plugin){
        this.gameManager = gameManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void opPlayerJoin(PlayerJoinEvent event){
        if(gameManager.getGameState() != GameState.Waiting) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
            return;
        }
        Player player = event.getPlayer();
        playerList.add(player);
        if(playerList.size() >= 2){
            Bukkit.broadcastMessage("Игра начинается! Приготовтесь!");
            gameManager.setGameState(GameState.Teleporting);
            TimeBeforeGame(playerList);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        if(gameManager.getGameState() != GameState.Waiting) {
            return;
        }
        Player player = event.getPlayer();
        playerList.remove(player);
        if(playerList.size() >= 2){
            gameManager.setGameState(GameState.Waiting);
        }
    }

    public void TimeBeforeGame(List<Player> playerList){
        new BukkitRunnable() {
            int timer = 10;

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
                    }
                }
                else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("KwampaRR"), 0,20L);
    }
}
