package com.bluewave;


import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.bluewave")
public class CatalogServiceApplication {

    public static void main(String[] args) {

        Dotenv dotenv=Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(dotenvEntry -> System.setProperty(dotenvEntry.getKey(),dotenvEntry.getValue()));
        SpringApplication.run(CatalogServiceApplication.class,args);


    }
}
