package com.claudiodornelles.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class SpringWebfluxEssentialsApplication {

    @Bean
    public WebProperties.Resources resources() {
        return new WebProperties.Resources();
    }

    static {
        BlockHound.install(builder -> builder.allowBlockingCallsInside("java.util.UUID", "randomUUID"));
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringWebfluxEssentialsApplication.class, args);
    }

}
