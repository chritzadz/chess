package com.project.chess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
public class ChessApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChessApplication.class, args);
    }
}

