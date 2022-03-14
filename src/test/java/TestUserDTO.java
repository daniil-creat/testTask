import dev.simple.demo.model.User;
import dev.simple.demo.userDAO.UserDao;
import junit.framework.TestCase;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.spy;


public class TestUserDTO extends TestCase {

    public Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:file:./testdb", "sa", "");
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return connection;
    }


    UserDao userDao = spy(new UserDao());

    private static final String SQL_SQRIPT = "CREATE TABLE IF NOT EXISTS users(id INT AUTO_INCREMENT, name VARCHAR(20), surname VARCHAR(20), age INT,PRIMARY KEY (name));\n" +
            "INSERT INTO users(name,surname,age) VALUES('Даниил','Кох',21);\n" +
            "INSERT INTO users(name,surname,age) VALUES('Дмитрий','Потолочных',22);\n" +
            "INSERT INTO users(name,surname,age) VALUES('Петр','Яковлев',25);";

    @Test
    public void testInsertIntoUser() throws Exception {


        Mockito.when(userDao.getContent()).thenReturn(SQL_SQRIPT);

        Mockito.when(userDao.getConnection()).thenReturn(getConnection());

        userDao.insertIntoUser();

        List<User> users = new ArrayList<>();
        try (Connection connection = getConnection();
             Statement preparedStatement = connection.createStatement()) {
            ResultSet rs = preparedStatement.executeQuery("SELECT * FROM users");
            while (rs.next()) {
                String surname = rs.getString("surname");
                int age = rs.getInt("age");
                int id = rs.getInt("id");
                String name = rs.getString("name");
                users.add(new User(id, name, surname, age));
            }
            assertEquals(3, users.size());
            assertEquals(1, users.stream().filter(x -> x.getName().equals("Даниил")).count());
            assertEquals(1, users.stream().filter(x -> x.getName().equals("Дмитрий")).count());
            assertEquals(1, users.stream().filter(x -> x.getName().equals("Петр")).count());
            preparedStatement.executeUpdate("DROP TABLE users");
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }


    @Test
    public void testUpdateUser() throws IOException, URISyntaxException, SQLException {
        Mockito.when(userDao.getConnection()).thenReturn(getConnection());

        try (Connection connection = getConnection();
             Statement preparedStatement = connection.createStatement()) {
            preparedStatement.executeUpdate(SQL_SQRIPT);

            User user = new User(3, "Миша", "Куров", 22);
            List<User> users = new ArrayList<>();

            User userUpdated = userDao.updateUser(user);

            assertEquals("Миша", userUpdated.getName());
            preparedStatement.executeUpdate("DROP TABLE users");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Test
    public void testSearchUserByName() {
        Mockito.when(userDao.getConnection()).thenReturn(getConnection());

        try (Connection connection = getConnection();
             Statement preparedStatement = connection.createStatement()) {
            preparedStatement.executeUpdate(SQL_SQRIPT);

            User user = userDao.searchUserByName("Даниил");

            assertEquals("Даниил", user.getName());
            preparedStatement.executeUpdate("DROP TABLE users");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Test
    public void testSearchAllUsers() {
        Mockito.when(userDao.getConnection()).thenReturn(getConnection());

        try (Connection connection = getConnection();
             Statement preparedStatement = connection.createStatement()) {
            preparedStatement.executeUpdate(SQL_SQRIPT);

            List<User> user = userDao.searchAllUsers();

            assertEquals(3, user.size());
            assertEquals("Даниил", user.stream().filter(x -> x.getName().equals("Даниил")).findFirst().get().getName());
            assertEquals("Дмитрий", user.stream().filter(x -> x.getName().equals("Дмитрий")).findFirst().get().getName());
            assertEquals("Петр", user.stream().filter(x -> x.getName().equals("Петр")).findFirst().get().getName());
            preparedStatement.executeUpdate("DROP TABLE users");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}






