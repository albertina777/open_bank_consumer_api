package com.openbank.client.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "com.openbank.apiclient")
public class UserProperties {

    private String user;
    private String password;
}
