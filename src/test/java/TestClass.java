import com.google.gson.Gson;
import dev.simpleapi.demo.ServletUser;
import dev.simpleapi.demo.model.User;
import dev.simpleapi.demo.userDAO.UserDao;
import junit.framework.TestCase;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestClass extends TestCase {

    private String jdbcUrl = "jdbc:h2:file:./testdb";
    private String jdbcName = "sa";
    private String jdbcPassword = "";

    private static final String ADD_USER = "CREATE TABLE users(id INT AUTO_INCREMENT, name VARCHAR(20), " +
            "surname VARCHAR(20), age INT,PRIMARY KEY (name));" +
            "INSERT INTO users(name,surname,age) VALUES('Полина','Яшкина',15);" +
            "INSERT INTO users(name,surname,age) VALUES('Яков','Полищук',40);" +
            "INSERT INTO users(name,surname,age) VALUES('Михаил','Задоров',30);";
    private static final String DELETE_DB = "DROP TABLE users;";
    private static final String SEARCH_USER = "SELECT * FROM users WHERE name = ?;";


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


    public void createUser() {

        try (Connection connection = getConnection();
             Statement preparedStatement = connection.createStatement()) {
            preparedStatement.executeUpdate(ADD_USER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void deleteDb() {

        try (Connection connection = getConnection();
             Statement preparedStatement = connection.createStatement()) {
            preparedStatement.executeUpdate(DELETE_DB);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int searchId() {
        int id = 0;
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SEARCH_USER)) {
            preparedStatement.setString(1, "Полина");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                id = resultSet.getInt("id");
            }
            return id;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return id;
    }

    HttpServletRequest stubRequest = mock(HttpServletRequest.class);
    HttpServletResponse stubResponse = mock(HttpServletResponse.class);
    UserDao userDao = new UserDao();


    @Test
    public void testGetUserByName() throws IOException {
        createUser();

        when(stubRequest.getParameter("name")).thenReturn("Полина");
        when(stubRequest.getServletPath()).thenReturn("/user");


        userDao.setJdbcName(jdbcName);
        userDao.setJdbcPassword(jdbcPassword);
        userDao.setJdbcUrl(jdbcUrl);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(stubResponse.getWriter()).thenReturn(pw);

        new ServletUser(userDao).doGet(stubRequest, stubResponse);

        String result = sw.getBuffer().toString();
        User user = new Gson().fromJson(result, User.class);
        assertEquals(user.getName(), "Полина");

        deleteDb();
    }

    @Test
    public void testUpdateUser() throws IOException {
        createUser();

        when(stubRequest.getServletPath()).thenReturn("/update");

        String jsonData = "{\"name\":\"Полина\",\"id\":" + "\"" + searchId() + "\"" + ",\"surname\":\"Кошкина\",\"age\":\"10\"}";
        ;
        InputStream targetStream = new ByteArrayInputStream(jsonData.getBytes());
        InputStreamReader inputStreamReader = new InputStreamReader(targetStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        when(stubRequest.getReader()).thenReturn(bufferedReader);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(stubResponse.getWriter()).thenReturn(pw);

        userDao.setJdbcName(jdbcName);
        userDao.setJdbcPassword(jdbcPassword);
        userDao.setJdbcUrl(jdbcUrl);

        new ServletUser(userDao).doPut(stubRequest, stubResponse);

        String result = sw.getBuffer().toString();
        User user = new Gson().fromJson(result, User.class);
        assertEquals(user.getAge(), 10);

        deleteDb();
    }

    @Test
    public void testAllUser() throws IOException {

        createUser();

        when(stubRequest.getServletPath()).thenReturn("/allusers");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(stubResponse.getWriter()).thenReturn(pw);

        userDao.setJdbcName(jdbcName);
        userDao.setJdbcPassword(jdbcPassword);
        userDao.setJdbcUrl(jdbcUrl);
        new ServletUser(userDao).doGet(stubRequest, stubResponse);

        String result = sw.getBuffer().toString();
        String[] strings = result.split("\"|:|}");
        String name = Arrays.stream(strings).filter(x -> x.equals("Полина")).collect(Collectors.joining());
        assertEquals(name, "Полина");
        String str = Arrays.stream(strings).filter(x -> x.equals("1")).findFirst().get();
        assertEquals(str, "1");
        deleteDb();
    }

    @Test
    public void testInsertDb() throws SQLException {

        when(stubRequest.getParameter("fileName")).thenReturn("sqlTest.txt");
        when(stubRequest.getServletPath()).thenReturn("/insert");

        userDao.setJdbcName(jdbcName);
        userDao.setJdbcPassword(jdbcPassword);
        userDao.setJdbcUrl(jdbcUrl);
        new ServletUser(userDao).doGet(stubRequest, stubResponse);

        List<User> users = new ArrayList<>();
        Connection connection = getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT*FROM users");
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            String surname = rs.getString("surname");
            int age = rs.getInt("age");
            int id = rs.getInt("id");
            String name = rs.getString("name");
            users.add(new User(id, name, surname, age));
        }
        users.stream().forEach(x -> {
            assertEquals(x.getSurname(), "Кох");
            assertEquals(x.getName(), "Даниил");
            assertEquals(x.getAge(), 21);
        });
        preparedStatement = connection.prepareStatement("DROP TABLE users");
        preparedStatement.executeUpdate();

    }

}
