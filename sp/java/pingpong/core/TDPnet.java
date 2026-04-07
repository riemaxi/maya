package pingpong.core;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import java.net.URI;
import java.net.URISyntaxException;

public class TDPnet {
    private InternalClient socket;

    public void connect(String host) throws URISyntaxException {
        this.socket = new InternalClient(new URI(host));
        this.socket.connect();
    }

    // Inner class to handle WebSocket events mapping to the JS logic
    private class InternalClient extends WebSocketClient {
        public InternalClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            onConnected(System.currentTimeMillis());
        }

        @Override
        public void onMessage(String message) {
            try {
                // Attempt to parse as JSON first
                JSONObject m = new JSONObject(message);
                handleEvent(m.opt("id"), m.optString("data"));
            } catch (Exception err) {
                // If not JSON, treat as raw packet data
                onData(message);
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            onConnectionError("Connection closed: " + reason);
        }

        @Override
        public void onError(Exception ex) {
            TDPnet.this.onError(ex);
            onConnectionError(ex.getMessage());
        }
    }

    public void handleEvent(Object id, String data) {
        if (data == null || data.isEmpty()) return;

        Packet packet = Packet.fromSP(data);
        String subject = packet.peering.subject;
        String[] parts = subject.split(":");
        String category = parts[0];
        String msgId = parts.length > 1 ? parts[1] : (id != null ? id.toString() : "");

        switch (category) {
            case "request": onRequest(msgId, packet); break;
            case "response": onResponse(msgId, packet); break;
            case "event": onEvent(msgId, packet); break;
        }
    }

    public void onData(String e) {
        Packet packet = Packet.fromString(e);
        String[] parts = packet.peering.subject.split(":");
        String category = parts[0];
        String id = parts.length > 1 ? parts[1] : "";

        switch (category) {
            case "request": onRequest(id, packet); break;
            case "response": onResponse(id, packet); break;
            case "event": onEvent(id, packet); break;
        }
    }

    // Lifecycle methods to be overridden by the user
    public void onConnected(long timestamp) {}
    public void onError(Exception err) {}
    public void onConnectionError(String msg) {}
    public void onRequest(String id, Packet p) {}
    public void onResponse(String id, Packet p) {}
    public void onEvent(String id, Packet p) {}

    public void sendSignal(String data) {
        if (socket != null && socket.isOpen()) {
            socket.send(data);
        }
    }

    public void sendData(String data) {
        if (socket != null && socket.isOpen()) {
            socket.send(data);
        }
    }

    public void request(String from, String to, String id, JSONObject data) {
        String packetStr = Packeter.createSignal(
            from,
            to,
            data.toString(),
            "request:" + id,
            null, // default operators
            0     // default pointer
        );
        sendSignal(packetStr);
    }

    public void response(Packet packet, String id, JSONObject data) {
        // Swap peering for response
        packet.peering = new Peering(
            packet.peering.to, 
            packet.peering.from, 
            "response:" + id
        );
        packet.transformer.data = data.toString();

        sendData(packet.toString());
    }
}