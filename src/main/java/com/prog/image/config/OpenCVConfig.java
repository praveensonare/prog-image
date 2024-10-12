package com.prog.image.config;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Data
@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "opencv")
public class OpenCVConfig {
    private final String fileName = "opencv_java451";
}
