package com.bluewave;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.bluewave")
@EnableFeignClients
public class BookingServiceApplication {
    public static void main(String[] args) {

        Dotenv dotenv=Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(dotenvEntry -> System.setProperty(dotenvEntry.getKey(),dotenvEntry.getValue()));
        SpringApplication.run(BookingServiceApplication.class,args);


    }
}