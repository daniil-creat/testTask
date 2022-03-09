package dev.simpleapi.demo.userDAO;

import dev.simpleapi.demo.model.User;
import lombok.Data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserDao {
    private String jdbcUrl = "jdbc:h2:file:./usersdb";
    private String jdbcName = "sa";
    private String jdbcPassword = "";


    private static final String SELECT_USER_BY_NAME = "SELECT * FROM users WHERE name = ?";
    private static final String SELECT_ALL_USER = "SELECT * FROM users";
    private static final String UPDATE_USER = "UPDATE users SET name =?,  surname = ?, age = ? WHERE id = ?";

    public Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(jdbcUrl, jdbcName, jdbcPassword);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return connection;
    }

    public void insertIntoUser(String content) {
        try (Connection connection = getConnection();
             Statement preparedStatement = connection.createStatement()) {
            preparedStatement.executeUpdate(content);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public User updateUser(User user) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_USER)) {
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getSurname());
            preparedStatement.setInt(3, user.getAge());
            preparedStatement.setInt(4, user.getId());
            preparedStatement.executeUpdate();

        }
        return searchUserByName(user.getName());
    }

    public User searchUserByName(String name) {
        User user = null;
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_BY_NAME)) {
            preparedStatement.setString(1, name);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                String surname = rs.getString("surname");
                int age = rs.getInt("age");
                int id = rs.getInt("id");
                user = new User(id, name, surname, age);

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return user;
    }

    public List<User> searchAllUsers() {
        List<User> users = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_USER)) {
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                String surname = rs.getString("surname");
                int age = rs.getInt("age");
                int id = rs.getInt("id");
                String name = rs.getString("name");
                users.add(new User(id, name, surname, age));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return users;
    }
}












