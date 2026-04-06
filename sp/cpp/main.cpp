#include "./core/tdpnet.hpp"

class MyApp : public StandardSession {
public:
    void onConnected() override {
        json cred = {{"address", "name.maya.4da"}, {"accesskey", "ACSKEY"}};
        signin(cred);
    }

    void onGranted(const json& data) override {
        std::cout << "Access Granted!" << std::endl;
        request("name.maya.4da", "pingpong.maya.4da", "pong", 0);
    }

    void onRequest(std::string id, Packet& p) override {
        if (id == "ping") {
            std::cout << "Received Ping Request" << std::endl;
            response(p, "pong", 0);
        }
    }
};

int main() {
    MyApp app;
    app.connect("wss://103.177.249.118:30050", "...");
    
    // Keep the main thread alive
    while (true) {
        std::this_thread::sleep_for(std::chrono::seconds(1));
    }
    return 0;
}