package org.bee.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"org.bee.orders", "org.bee.product", "org.bee.banking", "org.bee.events", "org.bee.restapi", "org.bee.sample"})
@EnableAsync
@ConfigurationPropertiesScan
@EnableJpaRepositories(basePackages = {"org.bee.orders", "org.bee.product", "org.bee.banking", "org.bee.events"})
@EntityScan(basePackages = {"org.bee.orders", "org.bee.product", "org.bee.banking", "org.bee.events"})
public class SpringBootProjectsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootProjectsApplication.class, args);
    }

}
