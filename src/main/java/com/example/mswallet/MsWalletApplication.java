package com.example.mswallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class MsWalletApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsWalletApplication.class, args);
    }

}
