package com.bluewave;


import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;


@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {
    public static void main(String[] args) {

        Dotenv dotenv=Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(dotenvEntry -> System.setProperty(dotenvEntry.getKey(),dotenvEntry.getValue()));
        SpringApplication.run(DiscoveryServerApplication.class,args);


    }
}