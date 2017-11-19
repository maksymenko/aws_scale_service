package com.sm.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class Application {
  private static final String  id = UUID.randomUUID().toString();
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }


  @RequestMapping("/")
  public String getVersion() {
    return " Version: 'DEV' \r\n id: " + id + "\r\n current time: " + LocalDateTime.now();
  }
}