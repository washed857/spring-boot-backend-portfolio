package rs.nms.newsroom.server;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import rs.nms.newsroom.server.config.storage.FileStorageProperties;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class StartupLogger {

    private static final Logger logger = LoggerFactory.getLogger(StartupLogger.class);

    private final DataSource dataSource;
    private final FileStorageProperties fileStorageProperties;

    public StartupLogger(DataSource dataSource, FileStorageProperties fileStorageProperties) {
        this.dataSource = dataSource;
        this.fileStorageProperties = fileStorageProperties;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(fileStorageProperties.getUploadDir()));
            logger.info("Upload directory created: {}", fileStorageProperties.getUploadDir());
        } catch (IOException e) {
            logger.error("Could not create upload directory!", e);
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("\n\n" +
                "============================================\n" +
                "  Newsroom Management System Server Started  \n" +
                "============================================\n"
        );

        try (Connection connection = dataSource.getConnection()) {
            logger.info("Database connection successful!");
            logger.info("Connection URL: {}", connection.getMetaData().getURL());
            logger.info("Database user: {}", connection.getMetaData().getUserName());
            logger.info("Database product: {}", connection.getMetaData().getDatabaseProductName());
            logger.info("Upload directory: {}", fileStorageProperties.getUploadDir());
        } catch (SQLException e) {
            logger.error("Database connection failed!", e);
        }

        logger.info("\n" +
                "============================================\n" +
                "  Server is ready to accept requests         \n" +
                "============================================\n"
        );
    }
}