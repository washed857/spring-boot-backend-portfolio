package rs.nms.newsroom.server.util;

import org.apache.poi.xwpf.usermodel.*;
import rs.nms.newsroom.server.dto.export.StoryExportWordDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WordGeneratorUtil {

    public static byte[] generateStoryDocx(StoryExportWordDTO story) throws IOException {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Title
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText(safe(story.getTitle()));
            titleRun.setBold(true);
            titleRun.setFontSize(16);

            addLine(document, "Status: " + safe(story.getStatus()));
            addLine(document, "Author: " + safe(story.getAuthorName()));
            addLine(document, "Story Type: " + safe(story.getStoryType()));
            addLine(document, "Rundown: " + safe(story.getRundownTitle()));
            addLine(document, "Approved by: " + safe(story.getApprovedBy()));
            addLine(document, "Created at: " + safe(story.getCreatedAt()));
            document.createParagraph();

            // Story Items
            XWPFParagraph itemHeader = document.createParagraph();
            XWPFRun itemHeaderRun = itemHeader.createRun();
            itemHeaderRun.setText("Story Items:");
            itemHeaderRun.setBold(true);

            for (StoryExportWordDTO.StoryItemExportDTO item : story.getItems()) {
                addLine(document, "- " + safe(item.getNum()) + ". " + safe(item.getStoryName()));
                if (item.getTextDescription() != null) {
                    addLine(document, "   Text: " + item.getTextDescription());
                }
                if (item.getVideoName() != null) {
                    addLine(document, "   Video: " + item.getVideoName());
                }
                if (item.getCgMainTitle() != null) {
                    addLine(document, "   CG Naslov: " + item.getCgMainTitle());
                }
                if (item.getCgSubtitle() != null) {
                    addLine(document, "   CG Podnaslov: " + item.getCgSubtitle());
                }
                if (item.getCgSpeakerName() != null) {
                    addLine(document, "   CG Govornik: " + item.getCgSpeakerName());
                }
                document.createParagraph();
            }

            // Comments
            if (story.getComments() != null && !story.getComments().isEmpty()) {
                XWPFParagraph commentsHeader = document.createParagraph();
                XWPFRun commentsRun = commentsHeader.createRun();
                commentsRun.setText("Comments:");
                commentsRun.setBold(true);

                for (String comment : story.getComments()) {
                    addLine(document, "- " + comment);
                }
            }

            document.write(out);
            return out.toByteArray();
        }
    }

    private static void addLine(XWPFDocument document, String text) {
        XWPFParagraph para = document.createParagraph();
        XWPFRun run = para.createRun();
        run.setText(text);
    }

    private static String safe(Object obj) {
        return obj == null ? "" : obj.toString();
    }
}
