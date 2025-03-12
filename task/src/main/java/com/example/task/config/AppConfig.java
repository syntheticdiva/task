package com.example.task.config;

import com.example.task.exception.ConfigFileNotFoundException;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.File;

@Configuration
public class AppConfig implements EnvironmentAware {
    @Override
    public void setEnvironment(Environment environment) {
        File configFile = new File("./config.txt");
        if (!configFile.exists()) {
            throw new ConfigFileNotFoundException("Configuration file config.txt not found in root directory");
        }
    }
}
