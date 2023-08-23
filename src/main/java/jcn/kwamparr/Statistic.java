package jcn.kwamparr;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Statistic {
    private Connection connection;
    public Statistic(Connection connection){
        this.connection = connection;
    }
    public void addWins(Player player) throws SQLException {
        String playername = player.getName();
        PreparedStatement statement = connection.prepareStatement("UPDATE Stats SET wins = wins + 1  WHERE playername = ?");
        statement.setString(1, playername);
        statement.executeUpdate();
        statement.close();
    }

    public void addKills(Player player) throws SQLException {
        String playername = player.getName();
        PreparedStatement statement = connection.prepareStatement("UPDATE Stats SET kills = kills + 1  WHERE playername = ?");
        statement.setString(1, playername);
        statement.executeUpdate(); // Выполняем запрос к базе данных
        statement.close(); // Закрываем PreparedStatement
    }

    public void addPlayerInDataBases(Player player) {
        String playerName = player.getName();
        String query = "SELECT COUNT(*) FROM Stats WHERE playername = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, playerName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) == 0) {
                    String insertQuery = "INSERT INTO Stats (playername, wins, kills) VALUES (?, 0, 0)";
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                        insertStatement.setString(1, playerName);
                        insertStatement.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getPlayerStats() {
        List<String> playerStats = new ArrayList<>();

        String query = "SELECT playername, kills, wins FROM Stats";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String playerName = resultSet.getString("playername");
                int kills = resultSet.getInt("kills");
                int wins = resultSet.getInt("wins");

                String statsLine = playerName + " - " + kills + " - " + wins;
                playerStats.add(statsLine);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playerStats;
    }
}
