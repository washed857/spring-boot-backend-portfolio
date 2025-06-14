package rs.nms.newsroom.server.tcp.mos;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rs.nms.newsroom.server.dto.mos.*;
import rs.nms.newsroom.server.mos.builder.MosRoAckBuilder;
import rs.nms.newsroom.server.mos.parser.MosXmlParser;
import rs.nms.newsroom.server.service.MosRoMessageService;
import rs.nms.newsroom.server.service.MosStoryMessageService;

import java.util.Optional;

@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class MosTcpServerHandler extends SimpleChannelInboundHandler<String> {

    private final MosRoMessageService mosRoMessageService;
    private final MosStoryMessageService mosStoryMessageService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        log.info("MOS TCP received message:\n{}", msg);

        if (!msg.contains("<mos>")) {
            log.warn("Received message is not MOS XML.");
            return;
        }

        log.info("MOS XML detected.");

        Optional<String> roCreateBlock = MosXmlParser.extractRoCreate(msg);
        if (roCreateBlock.isPresent()) {
            log.info("roCreate block detected.");
            RoCreateMessage roCreate = MosXmlParser.parseRoCreate(roCreateBlock.get());
            log.info("Parsed roCreate:\nRO ID: {}\nSlug: {}\nMeta: {}",
                    roCreate.getRoID(), roCreate.getSlug(), roCreate.getMosExternalMeta());

            mosRoMessageService.saveFromRoCreate(roCreate);

            String ackXml = MosRoAckBuilder.build(roCreate.getRoID(), true, "roCreate received successfully");
            ctx.writeAndFlush(ackXml + "\r\n");
            return;
        }

        Optional<String> roUpdateBlock = MosXmlParser.extractRoUpdate(msg);
        if (roUpdateBlock.isPresent()) {
            log.info("roUpdate block detected.");
            RoUpdateMessage update = MosXmlParser.parseRoUpdate(roUpdateBlock.get());
            log.info("Parsed roUpdate:\nRO ID: {}\nNew Slug: {}\nNew Meta: {}",
                    update.getRoID(), update.getNewSlug(), update.getNewMosExternalMeta());

            mosRoMessageService.updateFromRoUpdate(update);

            String ackXml = MosRoAckBuilder.build(update.getRoID(), true, "roUpdate processed");
            ctx.writeAndFlush(ackXml + "\r\n");
            return;
        }

        Optional<String> roDeleteBlock = MosXmlParser.extractRoDelete(msg);
        if (roDeleteBlock.isPresent()) {
            log.info("roDelete block detected.");
            RoDeleteMessage roDelete = MosXmlParser.parseRoDelete(roDeleteBlock.get());
            log.info("Parsed roDelete: RO ID: {}", roDelete.getRoID());

            mosRoMessageService.deleteByRoId(roDelete.getRoID());

            String ackXml = MosRoAckBuilder.build(roDelete.getRoID(), true, "roDelete processed");
            ctx.writeAndFlush(ackXml + "\r\n");
            return;
        }

        Optional<String> roReplaceBlock = MosXmlParser.extractRoReplace(msg);
        if (roReplaceBlock.isPresent()) {
            log.info("roReplace block detected.");
            RoReplaceMessage roReplace = MosXmlParser.parseRoReplace(roReplaceBlock.get());
            log.info("Parsed roReplace:\nRO ID: {}\nSlug: {}\nMeta: {}",
                    roReplace.getRoID(), roReplace.getSlug(), roReplace.getMosExternalMeta());

            mosRoMessageService.replaceFromRoReplace(roReplace);

            String ackXml = MosRoAckBuilder.build(roReplace.getRoID(), true, "roReplace processed");
            ctx.writeAndFlush(ackXml + "\r\n");
            return;
        }

        Optional<String> roStoryInsertBlock = MosXmlParser.extractRoStoryInsert(msg);
        if (roStoryInsertBlock.isPresent()) {
            log.info("roStoryInsert block detected.");
            RoStoryInsertMessage insert = MosXmlParser.parseRoStoryInsert(roStoryInsertBlock.get());
            log.info("Parsed roStoryInsert:\nRO ID: {}\nStory ID: {}\nSlug: {}",
                    insert.getRoID(), insert.getStoryID(), insert.getStorySlug());

            mosStoryMessageService.processRoStoryInsert(insert);

            String ackXml = MosRoAckBuilder.build(insert.getRoID(), true, "roStoryInsert processed");
            ctx.writeAndFlush(ackXml + "\r\n");
            return;
        }

        Optional<String> roStoryReplaceBlock = MosXmlParser.extractRoStoryReplace(msg);
        if (roStoryReplaceBlock.isPresent()) {
            log.info("roStoryReplace block detected.");
            RoStoryReplaceMessage replace = MosXmlParser.parseRoStoryReplace(roStoryReplaceBlock.get());
            log.info("Parsed roStoryReplace:\nRO ID: {}\nStory ID: {}\nSlug: {}",
                    replace.getRoID(), replace.getStoryID(), replace.getStorySlug());

            mosStoryMessageService.processRoStoryReplace(replace);

            String ackXml = MosRoAckBuilder.build(replace.getRoID(), true, "roStoryReplace processed");
            ctx.writeAndFlush(ackXml + "\r\n");
            return;
        }

        Optional<String> roStoryDeleteBlock = MosXmlParser.extractRoStoryDelete(msg);
        if (roStoryDeleteBlock.isPresent()) {
            log.info("roStoryDelete block detected.");
            RoStoryDeleteMessage delete = MosXmlParser.parseRoStoryDelete(roStoryDeleteBlock.get());
            log.info("Parsed roStoryDelete:\nRO ID: {}\nStory ID: {}",
                    delete.getRoID(), delete.getStoryID());

            mosStoryMessageService.processRoStoryDelete(delete);

            String ackXml = MosRoAckBuilder.build(delete.getRoID(), true, "roStoryDelete processed");
            ctx.writeAndFlush(ackXml + "\r\n");
            return;
        }
        
        Optional<String> roStoryMoveBlock = MosXmlParser.extractRoStoryMove(msg);
        if (roStoryMoveBlock.isPresent()) {
            log.info("roStoryMove block detected.");
            RoStoryMoveMessage move = MosXmlParser.parseRoStoryMove(roStoryMoveBlock.get());
            log.info("Parsed roStoryMove:\nRO ID: {}\nStory ID: {}\nBefore: {}\nAfter: {}",
                    move.getRoID(), move.getStoryID(), move.getStoryIDBefore(), move.getStoryIDAfter());

            mosStoryMessageService.processRoStoryMove(move);

            String ackXml = MosRoAckBuilder.build(move.getRoID(), true, "roStoryMove processed");
            ctx.writeAndFlush(ackXml + "\r\n");
            return;
        }
        
        Optional<String> roStorySwapBlock = MosXmlParser.extractRoStorySwap(msg);
        if (roStorySwapBlock.isPresent()) {
            log.info("roStorySwap block detected.");
            RoStorySwapMessage swap = MosXmlParser.parseRoStorySwap(roStorySwapBlock.get());
            log.info("Parsed roStorySwap:\nRO ID: {}\nStory1: {}\nStory2: {}",
                    swap.getRoID(), swap.getStoryID1(), swap.getStoryID2());

            mosStoryMessageService.processRoStorySwap(swap);

            String ackXml = MosRoAckBuilder.build(swap.getRoID(), true, "roStorySwap processed");
            ctx.writeAndFlush(ackXml + "\r\n");
            return;
        }
        
        Optional<String> roStoryStatusBlock = MosXmlParser.extractRoStoryStatus(msg);
        if (roStoryStatusBlock.isPresent()) {
            log.info("roStoryStatus block detected.");
            RoStoryStatusMessage statusMsg = MosXmlParser.parseRoStoryStatus(roStoryStatusBlock.get());
            log.info("Parsed roStoryStatus: RO ID: {}, Story ID: {}, Status: {}", 
                     statusMsg.getRoID(), statusMsg.getStoryID(), statusMsg.getStatus());

            mosStoryMessageService.processRoStoryStatus(statusMsg);

            String ackXml = MosRoAckBuilder.build(statusMsg.getRoID(), true, "roStoryStatus processed");
            ctx.writeAndFlush(ackXml + "\r\n");
            return;
        }

        log.warn("Unsupported or unrecognized MOS message.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("MOS TCP server error", cause);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("MOS TCP client connected: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("MOS TCP client disconnected: {}", ctx.channel().remoteAddress());
    }
}