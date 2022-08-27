package io.getmedusa.hydra.core.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * This is autoconfiguration that gets called by /META-INF/spring.factories. <br/>
 * It simply ensures the beans defined in the hydra library are component scanned
 */
@Configuration
@ComponentScan("io.getmedusa.hydra.core")
@ConfigurationPropertiesScan("io.getmedusa.hydra.core")
@Order
public class HydraAutoConfiguration {

}
