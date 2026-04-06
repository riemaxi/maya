#include <ixwebsocket/IXWebSocket.h>
#include <nlohmann/json.hpp>
#include "packet.hpp"
#include "helper.hpp"

class TDPnet {
protected:
    ix::WebSocket webSocket;

public:
    virtual ~TDPnet() = default;

    void connect(const std::string& host, const std::string& caContent) {
        ix::WebSocketHttpHeaders headers;
        webSocket.setUrl(host);

        // Configure TLS/SSL
        ix::SocketTLSOptions tlsOptions;
        tlsOptions.caFile = "ca.pem"; // IXWebSocket usually requires a file path or buffer config
        webSocket.setTLSOptions(tlsOptions);

        webSocket.setOnMessageCallback([this](const ix::WebSocketMessagePtr& msg) {
            if (msg->type == ix::WebSocketMessageType::Message) {
                auto j = json::parse(msg->str);
                this->handleEvent(j["id"], j["data"]);
            } else if (msg->type == ix::WebSocketMessageType::Open) {
                this->onConnected();
            } else if (msg->type == ix::WebSocketMessageType::Error) {
                this->onError(msg->errorInfo.reason);
            }
        });

        webSocket.start();
    }

    void send(const std::string& id, const json& data) {
        json j;
        j["id"] = id;
        j["data"] = data;
        webSocket.send(j.dump());
    }

    virtual void onConnected() {}
    virtual void onError(const std::string& err) { std::cerr << "Error: " << err << std::endl; }
    virtual void handleEvent(const std::string& id, const json& data) = 0;
};

class StandardSession : public TDPnet {
public:
    void signin(json credentials) { send("signin", credentials); }
    void sendSignal(const std::string& data) { send("signal", data); }
    void sendData(const std::string& data) { send("data", data); }

    void handleEvent(const std::string& id, const json& data) override {
        if (id == "granted") onGranted(data);
        else if (id == "signal") onSignal(data.get<std::string>());
        else if (id == "data") onData(data.get<std::string>());
    }

    virtual void onGranted(const json& data) {}
    
    void onSignal(const std::string& e) {
        Packet p = Packet::fromString(e);
        processCategorizedPacket(p);
    }

    void onData(const std::string& e) {
        Packet p = Packet::fromString(e);
        processCategorizedPacket(p);
    }

    void processCategorizedPacket(Packet& p) {
        auto sub = p.peering.subject;
        size_t sep = sub.find(':');
        std::string cat = sub.substr(0, sep);
        std::string id = sub.substr(sep + 1);

        if (cat == "request") onRequest(id, p);
        else if (cat == "response") onResponse(id, p);
        else if (cat == "event") onEvent(id, p);
    }

    void request(std::string from, std::string to, std::string id, json data) {
        sendSignal(Packeter::createSignal(from, to, data.dump(), "request:" + id));
    }

    void response(Packet& p, std::string id, json data) {
        p.peering = {0, p.peering.to, p.peering.from, "response:" + id};
        p.transformer.data = data.dump();
        sendData(p.toString());
    }

    virtual void onRequest(std::string id, Packet& p) {}
    virtual void onResponse(std::string id, Packet& p) {}
    virtual void onEvent(std::string id, Packet& p) {}
};