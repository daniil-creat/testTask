package dev.simple.demo;

import dev.simple.demo.service.UserService;
import dev.simple.demo.userDAO.UserDao;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {


        final JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setAddress("http://localhost:8080");
        factory.setFeatures(List.of(new LoggingFeature()));
        factory.setResourceClasses(UserService.class);
        factory.setResourceProvider(UserService.class,
                new SingletonResourceProvider(new UserService(new UserDao()), true));

        Map<Object, Object> extensionMappings = new HashMap<Object, Object>();
        extensionMappings.put("json", MediaType.APPLICATION_JSON);
        factory.setExtensionMappings(extensionMappings);

        List<Object> providers = new ArrayList<Object>();
        providers.add(new JacksonJsonProvider());
        factory.setProviders(providers);


        final Server server = factory.create();
        try {
            System.in.read();
        } finally {
            server.destroy();
            System.exit(0);
        }
    }
}
