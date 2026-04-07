import org.json.JSONObject;

public class SystemSession extends StandardSession {
    protected JSONObject config;

    public SystemSession(JSONObject config, boolean shouldConnect) {
        this.config = config;
        if (shouldConnect) {
            try {
                this.connect(getHost());
            } catch (Exception e) {
                this.onConnectionError(e.getMessage());
            }
        }
    }

    public String getHost() { return config.getString("host"); }
    public JSONObject getCredential() { return config.getJSONObject("credential"); }
    public String getAddress() { return getCredential().getString("address"); }
    public JSONObject getPeers() { return config.getJSONObject("peers"); }

    @Override
    public void onConnected(long timestamp) {
        this.signin(getCredential());
    }

    public void onGranted(Object data) {}
    public void onDenied(Object data) {}
}