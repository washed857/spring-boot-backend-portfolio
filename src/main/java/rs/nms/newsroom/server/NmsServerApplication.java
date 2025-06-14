package rs.nms.newsroom.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import rs.nms.newsroom.server.config.storage.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class)
public class NmsServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NmsServerApplication.class, args);
    }
}