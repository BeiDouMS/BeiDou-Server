package org.gms.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "gms.server")
@Component
@Data
public class ServerProperty {
    private String host;
    private int port;
}
