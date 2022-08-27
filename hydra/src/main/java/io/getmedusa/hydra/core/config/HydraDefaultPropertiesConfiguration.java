package io.getmedusa.hydra.core.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

public class HydraDefaultPropertiesConfiguration implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        Properties props = new Properties();

        props.put("spring.main.allow-bean-definition-overriding", true);
        props.put("logging.level.root", "INFO");

        environment.getPropertySources().addLast(new PropertiesPropertySource("default-hydra-properties", props));
    }

}