package rs.nms.newsroom.server.mos.builder;

public class MosRoAckBuilder {

    public static String build(String roId, boolean success, String message) {
        return """
            <mos>
              <roAck>
                <roID>%s</roID>
                <status>%s</status>
                <message>%s</message>
              </roAck>
            </mos>
            """.formatted(roId, success ? "OK" : "ERROR", message);
    }
}