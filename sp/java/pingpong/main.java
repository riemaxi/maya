import org.json.JSONObject;

public class MainApplication extends SystemSession {
    public MainApplication() {
        super(Config.getSystemConfig(), true);
    }

    @Override
    public void onDenied(Object data) {
        System.out.println("denied: " + data);
    }

    @Override
    public void onGranted(Object data) {
        System.out.println("granted: " + data);
        this.request(this.getAddress(), this.getPeers().getString("pingpong"), "pong", 0);
    }

    @Override
    public void onEvent(String id, Packet packet) {
        System.out.println("event: " + id + " " + System.currentTimeMillis());
        if ("ping".equals(id)) {
            this.response(packet, "pong", 0);
        }
    }

    @Override
    public void onResponse(String id, Packet packet) {
        System.out.println("response: " + id + " " + System.currentTimeMillis());
        if ("ping".equals(id)) {
            this.response(packet, "pong", 0);
        }
    }

    @Override
    public void onRequest(String id, Packet packet) {
        System.out.println("request: " + id + " " + packet.toString());
        if ("ping".equals(id)) {
            this.response(packet, "pong", 0);
        }
    }

    public static void main(String[] args) {
        new MainApplication();
    }
}