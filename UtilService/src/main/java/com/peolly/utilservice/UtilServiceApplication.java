package com.peolly.utilservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
<<<<<<<< HEAD:NotificationMicroservice/src/main/java/com/peolly/NotificationMicroserviceApplication.java
public class NotificationMicroserviceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationMicroserviceApplication.class);
========
public class UtilServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UtilServiceApplication.class, args);
>>>>>>>> origin/avro-integration:UtilService/src/main/java/com/peolly/utilservice/UtilServiceApplication.java
    }
}