package rs.nms.newsroom.server.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import rs.nms.newsroom.server.dto.export.RundownExportPdfDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PdfGeneratorUtil {

    public static byte[] generateRundownPdf(RundownExportPdfDTO dto) throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.setFont(PDType1Font.HELVETICA_BOLD, 16);
                content.beginText();
                content.setLeading(18f);
                content.newLineAtOffset(50, 750);

                // Header
                content.showText("Rundown: " + safe(dto.getTitle()));
                content.newLine();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.showText("Show: " + safe(dto.getShowName()));
                content.newLine();
                content.showText("Broadcast Date: " + safe(dto.getBroadcastDate()));
                content.newLine();
                content.showText("Author: " + safe(dto.getAuthor()));
                content.newLine();
                content.showText("Created At: " + safe(String.valueOf(dto.getCreatedAt())));
                content.newLine();
                content.newLine();

                // Stories
                for (RundownExportPdfDTO.StoryDTO story : dto.getStories()) {
                    content.setFont(PDType1Font.HELVETICA_BOLD, 13);
                    content.showText("Story: " + safe(story.getTitle()) + " (" + safe(story.getStatus()) + ")");
                    content.newLine();
                    content.setFont(PDType1Font.HELVETICA, 12);
                    content.showText("Type: " + safe(story.getStoryType()) + ", Author: " + safe(story.getAuthorName()));
                    content.newLine();

                    for (RundownExportPdfDTO.StoryItemDTO item : story.getItems()) {
                        content.showText(" - " + safe(item.getNum()) + ". " + safe(item.getStoryName()));
                        content.newLine();
                        if (item.getTextDescription() != null) {
                            content.showText("     Text: " + truncate(item.getTextDescription(), 80));
                            content.newLine();
                        }
                        if (item.getVideoName() != null) {
                            content.showText("     Video: " + item.getVideoName());
                            content.newLine();
                        }
                        // Nova polja za grafiku
                        if (item.getCgMainTitle() != null) {
                            content.showText("     CG Naslov: " + item.getCgMainTitle());
                            content.newLine();
                        }
                        if (item.getCgSubtitle() != null) {
                            content.showText("     CG Podnaslov: " + item.getCgSubtitle());
                            content.newLine();
                        }
                        if (item.getCgSpeakerName() != null) {
                            content.showText("     CG Govornik: " + item.getCgSpeakerName());
                            content.newLine();
                        }
                    }

                    content.newLine();
                }

                content.endText();
            }

            document.save(out);
            return out.toByteArray();
        }
    }

    private static String truncate(String input, int maxLength) {
        if (input == null) return "";
        return input.length() > maxLength ? input.substring(0, maxLength - 3) + "..." : input;
    }

    private static String safe(Object obj) {
        return obj == null ? "" : obj.toString();
    }
}
