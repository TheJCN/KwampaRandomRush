package jcn.kwamparr;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;

public class AcitveGame implements Listener {
    private GameManager gameManager;

    public AcitveGame(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void LogicGame(List<Player> playerList){
        if(gameManager.getGameState() == GameState.Active){
            for(Player player : playerList){
                player.sendTitle("Одиночный режим", "Тимминг запрещен!");
            }

        }
    }
    public void RandomItem(List<Player> playerList){
    }
 }
