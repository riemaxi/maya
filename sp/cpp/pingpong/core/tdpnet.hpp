#ifndef TDPNET_HPP
#define TDPNET_HPP

#include <ixwebsocket/IXWebSocket.h>
#include <nlohmann/json.hpp>
#include <iostream>
#include <thread>
#include "packet.hpp"

using json = nlohmann::json;

class TDPnet {
protected:
    ix::WebSocket webSocket;

public:
    virtual ~TDPnet() = default;

    // We pass the host and CA content directly here from main
    void connect(const std::string& host, const std::string& caContent) {
        webSocket.setUrl(host);

        ix::SocketTLSOptions tlsOptions;
        // On Ubuntu, point to the system cert store
        tlsOptions.caFile = "/etc/ssl/certs/ca-certificates.crt";
        webSocket.setTLSOptions(tlsOptions);

        webSocket.setOnMessageCallback([this](const ix::WebSocketMessagePtr& msg) {
            if (msg->type == ix::WebSocketMessageType::Message) {
                try {
                    auto j = json::parse(msg->str);
                    this->handleEvent(j["id"], j["data"]);
                } catch (...) {}
            } else if (msg->type == ix::WebSocketMessageType::Open) {
                this->onConnected();
            }
        });

        webSocket.start();
    }

    void send(const std::string& id, const json& data) {
        json j = {{"id", id}, {"data", data}};
        webSocket.send(j.dump());
    }

    virtual void onConnected() = 0;
    virtual void handleEvent(const std::string& id, const json& data) = 0;
};

class StandardSession : public TDPnet {
public:
    Config::SystemConfig sessionConfig;

    // Constructor accepts the config object
    StandardSession(Config::SystemConfig cfg) : sessionConfig(cfg) {}

    void signin() { 
        json cred = {
            {"address", sessionConfig.credential.address},
            {"accesskey", sessionConfig.credential.accesskey},
            {"password", sessionConfig.credential.password}
        };
        send("signin", cred); 
    }

    void request(std::string from, std::string to, std::string id, json data) {
        // Simplified raw packet string construction based on your protocol
        std::string pStr = "0|0|1\n0|0000\n0 " + from + " " + to + " request:" + id + "\n0|0\n" + data.dump();
        send("signal", pStr);
    }

    void handleEvent(const std::string& id, const json& data) override {
        if (id == "granted") onGranted(data);
        else if (id == "signal" || id == "data") {
            Packet p = Packet::fromString(data.get<std::string>());
            std::string sub = p.peering.subject;
            size_t sep = sub.find(':');
            if (sep != std::string::npos) {
                std::string cat = sub.substr(0, sep);
                std::string subId = sub.substr(sep + 1);
                if (cat == "request") onRequest(subId, p);
            }
        }
    }

    virtual void onGranted(const json& data) {}
    virtual void onRequest(std::string id, Packet& p) {}
};

#endif