#include <iostream>
#include "config.hpp"   // Same level as main.cpp
#include "core/tdpnet.hpp"  // Ensure this is also in your include path or same folder

class MyApp : public StandardSession {
public:
    // Pass the config from main.cpp via the constructor
    MyApp(Config::SystemConfig cfg) : StandardSession(cfg) {}

    void onConnected() override {
        std::cout << "Connected. Sending signin for: " << sessionConfig.credential.address << std::endl;
        signin();
    }

    void onGranted(const json& data) override {
        std::cout << "Access Granted!" << std::endl;
        // Accessing peers from the passed config
        request(sessionConfig.credential.address, sessionConfig.peers.pingpong, "ping", 0);
    }

    void onRequest(std::string id, Packet& p) override {
        if (id == "ping") {
            std::cout << "Received Ping Request" << std::endl;
            // Response logic...
        }
    }
};

int main() {
    // 1. We take the global 'system' config from config.hpp
    // 2. Pass it into our MyApp instance
    MyApp app(Config::system);
    
    // 3. Connect using the values inside that config
    app.connect(Config::system.host, Config::system.security.ca);
    
    std::cout << "Client started. Host: " << Config::system.host << std::endl;

    while (true) {
        std::this_thread::sleep_for(std::chrono::seconds(1));
    }
    return 0;
}