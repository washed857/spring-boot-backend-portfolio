package rs.nms.newsroom.server.config.mos;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rs.nms.newsroom.server.tcp.mos.MosTcpServerInitializer;

/**
 * Configuration class for starting and gracefully shutting down the MOS TCP Server using Netty.
 * This bean ensures the server is properly started at application bootstrap and stopped on shutdown.
 */
@Configuration
public class MosTcpServerConfig {

    private static final Logger logger = LoggerFactory.getLogger(MosTcpServerConfig.class);

    @Value("${mos.tcp.port:10540}")
    private int mosTcpPort;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    /**
     * Starts the MOS TCP server with the provided initializer.
     *
     * @param initializer Netty channel initializer for incoming connections.
     * @return The running server {@link Channel}.
     * @throws InterruptedException if server startup is interrupted.
     */
    @Bean
    public Channel startMosTcpServer(MosTcpServerInitializer initializer) throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                 .channel(NioServerSocketChannel.class)
                 .childHandler(initializer)
                 .option(ChannelOption.SO_BACKLOG, 128)
                 .option(ChannelOption.SO_REUSEADDR, true)
                 .childOption(ChannelOption.SO_KEEPALIVE, true);

        logger.info("Starting MOS TCP server on port {}", mosTcpPort);
        try {
            serverChannel = bootstrap.bind(mosTcpPort).sync().channel();
            logger.info("MOS TCP server started successfully on port {}", mosTcpPort);
            return serverChannel;
        } catch (InterruptedException e) {
            logger.error("Failed to start MOS TCP server", e);
            shutdownEventLoops(true);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while starting MOS TCP server", e);
            shutdownEventLoops(true);
            throw new RuntimeException(e);
        }
    }

    /**
     * Gracefully shuts down Netty EventLoopGroups.
     *
     * @param await if true, waits for full shutdown before returning.
     */
    private void shutdownEventLoops(boolean await) {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
            if (await) waitForShutdown(bossGroup);
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
            if (await) waitForShutdown(workerGroup);
        }
    }

    /**
     * Waits for the EventLoopGroup termination.
     *
     * @param group EventLoopGroup to wait for.
     */
    private void waitForShutdown(EventLoopGroup group) {
        try {
            group.terminationFuture().sync();
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting for Netty EventLoopGroup shutdown", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Shuts down the MOS TCP server and releases Netty resources.
     * This method is automatically called on Spring shutdown.
     */
    @PreDestroy
    public void stopMosTcpServer() {
        logger.info("Stopping MOS TCP server...");
        try {
            if (serverChannel != null) {
                serverChannel.close().sync();
            }
            shutdownEventLoops(true);
        } catch (InterruptedException e) {
            logger.error("Error while stopping MOS TCP server", e);
            Thread.currentThread().interrupt();
        }
        logger.info("MOS TCP server stopped.");
    }
}