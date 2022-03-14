package dev.simple.demo.service;


import dev.simple.demo.model.Result;
import dev.simple.demo.model.User;
import dev.simple.demo.userDAO.UserDao;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserService {

    UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }


    @GET
    @Path("/insert")
    public boolean insertDb() throws IOException, URISyntaxException {
        return userDao.insertIntoUser();
    }

    @Path("/update")
    @PUT
    public User updateUser(User user) throws SQLException {
        User userUpdate = userDao.updateUser(user);
        ;
        return userUpdate;
    }

    @Path("/user/{name}")
    @GET
    public User getUserByName(@PathParam("name") String name) {
        return userDao.searchUserByName(name);
    }

    @Path("/usersbyparametrs")
    @GET
    public Result getUserByParametrs() {
        List<User> users = userDao.searchAllUsers();
        List<String> names = new ArrayList<>();
        users.stream().filter(x -> x.getAge() < 20).forEach(x -> names.add(x.getName()));
        Long count = users.stream().filter(x -> x.getSurname().matches(".*ов$")).count();
        return new Result(names, count);

    }

}
