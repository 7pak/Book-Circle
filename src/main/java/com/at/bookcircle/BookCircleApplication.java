package com.at.bookcircle;

import com.at.bookcircle.role.Role;
import com.at.bookcircle.role.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class BookCircleApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookCircleApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx, RoleRepository roleRepository ) {
        return args -> {
          if (roleRepository.findByName("USER").isEmpty()) {
              roleRepository.save(Role.builder().name("USER").build());
          }
        };
    }
}
