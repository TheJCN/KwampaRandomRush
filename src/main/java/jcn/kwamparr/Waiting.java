package jcn.kwamparr;

import org.bukkit.Bukkit;
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

    public Waiting (GameManager gameManager){
        this.gameManager = gameManager;
    }

    @EventHandler
    public void opPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        playerList.add(player);
        if(playerList.size() == 2){
            Bukkit.broadcastMessage("Игра начинается! Приготевтесь!");
            gameManager.setGameState(GameState.Teleporting);
            TimeBefore(playerList);

        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        playerList.remove(player);
        if(playerList.size() < 2){
            gameManager.setGameState(GameState.Waiting);
        }
    }

    public void TimeBefore(List<Player> playerList){
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
                        AcitveGame acitveGame = new AcitveGame(gameManager);
                    }
                }
                else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("RandonRR"), 0,20L);
    }
}
