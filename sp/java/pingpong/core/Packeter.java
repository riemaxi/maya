package pingpong.core;

import org.json.JSONObject;
import java.util.ArrayList;

public class Packeter {
    public static byte[] ONE_OPERATOR = new byte[]{0};

    public static String createAd(String from, String to, String name, String description, String status, String subject) {
        JSONObject payload = new JSONObject();
        payload.put("name", name != null ? name : from);
        payload.put("description", description);
        if (status != null) payload.put("status", status);

        return createSignal(from, to, payload.toString(), subject != null ? subject : "signin", null, 0);
    }

    public static String createSignal(String from, String to, String data, String subject, byte[] operators, int pointer) {
        return new Packet(
            new Channel(0, 0, 1),
            new Status(0, "0000"),
            new Peering(from, to, subject),
            new Transformer(pointer, operators != null ? operators : ONE_OPERATOR, data)
        ).toString();
    }
}