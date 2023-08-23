package jcn.kwamparr;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;

public class CommandTop implements CommandExecutor {
    private Connection connection;
    public CommandTop(Connection connection){
        this.connection = connection;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        Statistic statistic = new Statistic(connection);
        player.sendMessage(statistic.getPlayerStats().toString());
        return false;
    }
}
