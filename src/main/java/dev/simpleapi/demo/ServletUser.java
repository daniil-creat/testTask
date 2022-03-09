package dev.simpleapi.demo;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.simpleapi.demo.model.User;
import dev.simpleapi.demo.userDAO.UserDao;
import lombok.SneakyThrows;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@WebServlet(name = "myServlet", urlPatterns = "/")
public class ServletUser extends HttpServlet {
    private UserDao userDao;
    private Gson gson;

    public ServletUser() {
        this.userDao = new UserDao();
        this.gson = new Gson();

    }

    public ServletUser(UserDao userDao) {
        this.userDao = userDao;
        this.gson = new Gson();

    }

    @SneakyThrows
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String action = req.getServletPath();

        switch (action) {
            case "/allusers":
                getAllUsers(resp);
                break;
            case "/user":
                getUserByName(req, resp);
                break;
            case "/insert":
                insertData(req);
                break;

        }
    }

    @SneakyThrows
    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) {
        String action = req.getServletPath();

        switch (action) {
            case "/update":
                updateUser(req, resp);
                break;

        }
    }


    public void insertData(HttpServletRequest req) throws IOException {
        String fileName = req.getParameter("fileName");
        String content = Files.lines(Paths.get(fileName)).reduce("", String::concat);
        userDao.insertIntoUser(content);
    }

    public void getAllUsers(HttpServletResponse resp) throws IOException {
        List<User> users = userDao.searchAllUsers();
        List<String> namesUsers = new ArrayList<>();
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        Pattern pattern = Pattern.compile(".*ов$");
        long count = users.stream()
                .map(x -> pattern.matcher(x.getSurname()))
                .filter(Matcher::find)
                .map(x -> x.group())
                .count();
        List<User> userSorted = users.stream().filter(user -> user.getAge() < 20).collect(Collectors.toList());
        userSorted.stream().forEach(user -> namesUsers.add(user.getName()));
        JsonObject jsonObjects = new JsonObject();
        jsonObjects.add("namesUsersWhoHightTwenty", gson.toJsonTree(namesUsers));
        jsonObjects.add("countUsersLastNameWhoEndingOv", gson.toJsonTree(count));
        out.print(jsonObjects);
        out.flush();
    }

    public void getUserByName(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        String name = req.getParameter("name");
        User user = userDao.searchUserByName(name);
        PrintWriter out = resp.getWriter();
        String userJson = gson.toJson(user);
        out.print(userJson);
        out.flush();
    }

    public void updateUser(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        req.setCharacterEncoding("UTF-8");
        StringBuilder body = new StringBuilder();
        char[] buffer = new char[1024];
        int readChars;
        try (Reader reader = req.getReader()) {
            while ((readChars = reader.read(buffer)) != -1) {
                body.append(buffer, 0, readChars);
            }
        }
        User user = gson.fromJson(body.toString(), User.class);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        User userUpdate = userDao.updateUser(user);
        PrintWriter out = resp.getWriter();
        String userJson = gson.toJson(userUpdate);
        out.print(userJson);
        out.flush();
    }
}




