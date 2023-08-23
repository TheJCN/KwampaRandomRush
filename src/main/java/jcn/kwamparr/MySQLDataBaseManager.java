package jcn.kwamparr;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDataBaseManager{
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private Connection connection;
    public MySQLDataBaseManager(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public boolean connect() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
