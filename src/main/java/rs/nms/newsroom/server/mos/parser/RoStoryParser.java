package rs.nms.newsroom.server.mos.parser;

import rs.nms.newsroom.server.dto.mos.*;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RoStoryParser
 *
 * Utility class for extracting and parsing MOS protocol Rundown Story XML blocks (roStoryInsert, roStoryReplace, etc.)
 * from incoming MOS XML messages. Converts XML fragments into strongly typed DTOs used by the backend for further
 * processing. Demonstrates practical use of regular expressions and robust parsing for newsroom automation workflows.
 *
 * This class is part of a modular backend architecture designed for integration with broadcast automation and newsroom systems.
 */
public class RoStoryParser {

    private static final Pattern RO_STORY_INSERT_PATTERN = Pattern.compile("<roStoryInsert>(.*?)</roStoryInsert>", Pattern.DOTALL);
    private static final Pattern RO_STORY_REPLACE_PATTERN = Pattern.compile("<roStoryReplace>(.*?)</roStoryReplace>", Pattern.DOTALL);
    private static final Pattern RO_STORY_DELETE_PATTERN = Pattern.compile("<roStoryDelete>(.*?)</roStoryDelete>", Pattern.DOTALL);
    private static final Pattern RO_STORY_MOVE_PATTERN = Pattern.compile("<roStoryMove>(.*?)</roStoryMove>", Pattern.DOTALL);
    private static final Pattern RO_STORY_SWAP_PATTERN = Pattern.compile("<roStorySwap>(.*?)</roStorySwap>", Pattern.DOTALL);
    private static final Pattern RO_STORY_STATUS_PATTERN = Pattern.compile("<roStoryStatus>(.*?)</roStoryStatus>", Pattern.DOTALL);

    public static Optional<String> extractRoStoryInsert(String xml) {
        Matcher matcher = RO_STORY_INSERT_PATTERN.matcher(xml);
        if (matcher.find()) {
            return Optional.of("<roStoryInsert>" + matcher.group(1) + "</roStoryInsert>");
        }
        return Optional.empty();
    }

    public static RoStoryInsertMessage parseRoStoryInsert(String xml) {
        RoStoryInsertMessage message = new RoStoryInsertMessage();

        Matcher roIdMatcher = Pattern.compile("<roID>(.*?)</roID>").matcher(xml);
        if (roIdMatcher.find()) {
            message.setRoID(roIdMatcher.group(1));
        }

        Matcher storyIdMatcher = Pattern.compile("<storyID>(.*?)</storyID>").matcher(xml);
        if (storyIdMatcher.find()) {
            message.setStoryID(storyIdMatcher.group(1));
        }

        Matcher slugMatcher = Pattern.compile("<storySlug>(.*?)</storySlug>").matcher(xml);
        if (slugMatcher.find()) {
            message.setStorySlug(slugMatcher.group(1));
        }

        return message;
    }

    public static Optional<String> extractRoStoryReplace(String xml) {
        Matcher matcher = RO_STORY_REPLACE_PATTERN.matcher(xml);
        if (matcher.find()) {
            return Optional.of("<roStoryReplace>" + matcher.group(1) + "</roStoryReplace>");
        }
        return Optional.empty();
    }

    public static RoStoryReplaceMessage parseRoStoryReplace(String xml) {
        RoStoryReplaceMessage message = new RoStoryReplaceMessage();

        Matcher roIdMatcher = Pattern.compile("<roID>(.*?)</roID>").matcher(xml);
        if (roIdMatcher.find()) {
            message.setRoID(roIdMatcher.group(1));
        }

        Matcher storyIdMatcher = Pattern.compile("<storyID>(.*?)</storyID>").matcher(xml);
        if (storyIdMatcher.find()) {
            message.setStoryID(storyIdMatcher.group(1));
        }

        Matcher slugMatcher = Pattern.compile("<storySlug>(.*?)</storySlug>").matcher(xml);
        if (slugMatcher.find()) {
            message.setStorySlug(slugMatcher.group(1));
        }

        return message;
    }
    
    /**
     * Extracts the <roStoryMove> XML block from the given string, if present.
     */
    public static Optional<String> extractRoStoryMove(String xml) {
        Matcher matcher = RO_STORY_MOVE_PATTERN.matcher(xml);
        if (matcher.find()) {
            return Optional.of("<roStoryMove>" + matcher.group(1) + "</roStoryMove>");
        }
        return Optional.empty();
    }

    /**
     * Parses a <roStoryMove> XML block into a RoStoryMoveMessage DTO.
     */
    public static RoStoryMoveMessage parseRoStoryMove(String xml) {
        RoStoryMoveMessage message = new RoStoryMoveMessage();

        Matcher roIdMatcher = Pattern.compile("<roID>(.*?)</roID>").matcher(xml);
        if (roIdMatcher.find()) {
            message.setRoID(roIdMatcher.group(1));
        }

        Matcher storyIdMatcher = Pattern.compile("<storyID>(.*?)</storyID>").matcher(xml);
        if (storyIdMatcher.find()) {
            message.setStoryID(storyIdMatcher.group(1));
        }

        Matcher beforeMatcher = Pattern.compile("<storyIDBefore>(.*?)</storyIDBefore>").matcher(xml);
        if (beforeMatcher.find()) {
            message.setStoryIDBefore(beforeMatcher.group(1));
        }

        Matcher afterMatcher = Pattern.compile("<storyIDAfter>(.*?)</storyIDAfter>").matcher(xml);
        if (afterMatcher.find()) {
            message.setStoryIDAfter(afterMatcher.group(1));
        }

        return message;
    }
    
    /**
     * Extracts the &lt;roStoryInsert&gt; XML block from the given string, if present.
     * @param xml incoming MOS XML message as String
     * @return Optional containing the matched XML block, or empty if not found
     */
    public static Optional<String> extractRoStorySwap(String xml) {
        Matcher matcher = RO_STORY_SWAP_PATTERN.matcher(xml);
        if (matcher.find()) {
            return Optional.of("<roStorySwap>" + matcher.group(1) + "</roStorySwap>");
        }
        return Optional.empty();
    }

    public static RoStorySwapMessage parseRoStorySwap(String xml) {
        RoStorySwapMessage message = new RoStorySwapMessage();

        Matcher roIdMatcher = Pattern.compile("<roID>(.*?)</roID>").matcher(xml);
        if (roIdMatcher.find()) {
            message.setRoID(roIdMatcher.group(1));
        }
        Matcher storyId1Matcher = Pattern.compile("<storyID1>(.*?)</storyID1>").matcher(xml);
        if (storyId1Matcher.find()) {
            message.setStoryID1(storyId1Matcher.group(1));
        }
        Matcher storyId2Matcher = Pattern.compile("<storyID2>(.*?)</storyID2>").matcher(xml);
        if (storyId2Matcher.find()) {
            message.setStoryID2(storyId2Matcher.group(1));
        }
        return message;
    }
    
    public static Optional<String> extractRoStoryStatus(String xml) {
        Matcher matcher = RO_STORY_STATUS_PATTERN.matcher(xml);
        if (matcher.find()) {
            return Optional.of("<roStoryStatus>" + matcher.group(1) + "</roStoryStatus>");
        }
        return Optional.empty();
    }

    public static RoStoryStatusMessage parseRoStoryStatus(String xml) {
        RoStoryStatusMessage message = new RoStoryStatusMessage();

        Matcher roIdMatcher = Pattern.compile("<roID>(.*?)</roID>").matcher(xml);
        if (roIdMatcher.find()) {
            message.setRoID(roIdMatcher.group(1));
        }
        Matcher storyIdMatcher = Pattern.compile("<storyID>(.*?)</storyID>").matcher(xml);
        if (storyIdMatcher.find()) {
            message.setStoryID(storyIdMatcher.group(1));
        }
        Matcher statusMatcher = Pattern.compile("<status>(.*?)</status>").matcher(xml);
        if (statusMatcher.find()) {
            message.setStatus(statusMatcher.group(1));
        }
        return message;
    }

    public static Optional<String> extractRoStoryDelete(String xml) {
        Matcher matcher = RO_STORY_DELETE_PATTERN.matcher(xml);
        if (matcher.find()) {
            return Optional.of("<roStoryDelete>" + matcher.group(1) + "</roStoryDelete>");
        }
        return Optional.empty();
    }

    public static RoStoryDeleteMessage parseRoStoryDelete(String xml) {
        RoStoryDeleteMessage message = new RoStoryDeleteMessage();

        Matcher roIdMatcher = Pattern.compile("<roID>(.*?)</roID>").matcher(xml);
        if (roIdMatcher.find()) {
            message.setRoID(roIdMatcher.group(1));
        }

        Matcher storyIdMatcher = Pattern.compile("<storyID>(.*?)</storyID>").matcher(xml);
        if (storyIdMatcher.find()) {
            message.setStoryID(storyIdMatcher.group(1));
        }

        return message;
    }
}