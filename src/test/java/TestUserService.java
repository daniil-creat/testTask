import dev.simple.demo.model.Result;
import dev.simple.demo.model.User;
import dev.simple.demo.service.UserService;
import dev.simple.demo.userDAO.UserDao;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import junit.framework.TestCase;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.junit.*;
import org.mockito.Mockito;

import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TestUserService extends TestCase {

    UserDao userDao = Mockito.mock(UserDao.class);
    Server server;
    Client client;



    public void createServer() {
        final JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setAddress("http://localhost:8081/");
        factory.setFeatures(List.of(new LoggingFeature()));
        factory.setResourceClasses(UserService.class);
        factory.setResourceClasses(User.class);
        factory.setResourceClasses(Result.class);
        factory.setResourceProvider(UserService.class,
                new SingletonResourceProvider(new UserService(userDao), true));

        Map<Object, Object> extensionMappings = new HashMap<Object, Object>();
        extensionMappings.put("json", MediaType.APPLICATION_JSON);
        factory.setExtensionMappings(extensionMappings);

        List<Object> providers = new ArrayList<Object>();
        providers.add(new JacksonJsonProvider());
        factory.setProviders(providers);
        client = ClientBuilder.newClient();
        server = factory.create();


    }


    public void closeServer() {
        server.destroy();
    }


    @Test
    public void testUpdateUserApi() throws SQLException {
        createServer();
        User user = new User(1, "Миша", "Конов", 22);
        Mockito.when(userDao.updateUser(user)).thenReturn(user);
        assertEquals(200, client.target("http://localhost:8081/api/update")
                .request()
                .put(Entity.json(user)).getStatus());
        assertEquals(user, client.target("http://localhost:8081/api/update")
                .request()
                .put(Entity.json(user)).readEntity(User.class));
        closeServer();
    }

    @Test
    public void testInserDb() {
        createServer();
        Mockito.when(userDao.insertIntoUser()).thenReturn(true);
        assertEquals(200, client.target("http://localhost:8081/api/insert")
                .request()
                .get().getStatus());
        assertEquals("true", client.target("http://localhost:8081/api/insert")
                .request()
                .get().readEntity(Boolean.class).toString());
        closeServer();
    }

    @Test
    public void testGetUserByName() {
        createServer();
        User user = new User(1, "Никита", "Конов", 22);
        Mockito.when(userDao.searchUserByName("Никита")).thenReturn(user);
        assertEquals(200, client.target("http://localhost:8081/api/user/Никита")
                .request()
                .get().getStatus());
        assertEquals(user, client.target("http://localhost:8081/api/user/Никита")
                .request()
                .get().readEntity(User.class));
        server.destroy();
        closeServer();
    }

    @Test
    public void testGetUserByParametrs() {
        createServer();
        List<User> users = new ArrayList<>();
        users.add(new User(1, "Никита", "Конов", 22));
        users.add(new User(1, "Дима", "Кон", 19));

        Mockito.when(userDao.searchAllUsers()).thenReturn(users);

        List<String> resultList = new ArrayList<>();
        resultList.add(users.get(1).getName());

        Result result = new Result(resultList, 1);


        assertEquals(200, client.target("http://localhost:8081/api/usersbyparametrs")
                .request()
                .get().getStatus());
        assertEquals(result, client.target("http://localhost:8081/api/usersbyparametrs")
                .request()
                .get().readEntity(Result.class));
        closeServer();
    }


}
