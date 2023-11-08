package com.comduck.chatbot.database;

import java.sql.*;
import java.util.HashMap;
import org.mariadb.jdbc.*;

public class MariaDB {

    static final String url = "jdbc:mariadb://w34538y.asuscomm.com:3306/kiba";
    static final String username = "kiba";
    static final String password = "Kiba_bot12!@";
    static Connection connection;

    static public void Connect() throws SQLException, ClassNotFoundException {
        Class.forName("org.mariadb.jdbc.Driver");
        connection = DriverManager.getConnection(url, username, password);
    }

    static public void Add(Table table, HashMap<String, Object> data) throws SQLException, ClassNotFoundException {
        if(connection == null || connection.isClosed()) Connect();

        String col = String.join(",", data.keySet());

        String val = "";
        int i = 0;
        for (Object obj : data.values())
        {
            if (obj.getClass().equals(String.class)) {
                val += "\"" + obj + "\"";
            }
            else {
                val += obj.toString();
            }
            if(data.values().stream().count()-1 > i) val += ", ";

            i++;
        }

        String insertQuery = String.format("INSERT INTO %s (%s) VALUES (%s)", table.name(), col, val);
        System.out.println(insertQuery);
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.executeUpdate();
    }

    static public ResultSet Get(Table table, String search_col, String value) throws SQLException, ClassNotFoundException {
        if(connection == null || connection.isClosed()) Connect();

        String insertQuery = String.format("SELECT * FROM %s WHERE %s = \"%s\"", table.name(), search_col, value);
        System.out.println(insertQuery);
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        return preparedStatement.executeQuery();
    }

    static public void Set(Table table, String search_col, String search_val, String set_col, String set_val) throws SQLException, ClassNotFoundException {
        if(connection == null || connection.isClosed()) Connect();

        String insertQuery = String.format("UPDATE %s SET %s = \"%s\" WHERE %s = \"%s\"", table.name(), set_col, set_val, search_col, search_val);
        System.out.println(insertQuery);
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.executeUpdate();
    }

    static public void Find(Table table, String search_col, String value)
    {

    }

    static public void Set(Table table, String search_col, String search_val, String set_col, boolean set_val) throws SQLException, ClassNotFoundException {
        if(connection == null || connection.isClosed()) Connect();

        String insertQuery = String.format("UPDATE %s SET %s = \"%s\" WHERE %s = %s", table.name(), set_col, set_val, search_col, search_val);
        System.out.println(insertQuery);
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.executeUpdate();
    }
    static public void Set(Table table, String search_col, String search_val, String set_col, int set_val) throws SQLException, ClassNotFoundException {
        if(connection == null || connection.isClosed()) Connect();

        String insertQuery = String.format("UPDATE %s SET %s = \"%s\" WHERE %s = %s", table.name(), set_col, set_val, search_col, search_val);
        System.out.println(insertQuery);
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.executeUpdate();
    }
}
