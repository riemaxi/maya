#ifndef PACKET_HPP
#define PACKET_HPP

#include <string>
#include <vector>
#include <sstream>
#include <iostream>
#include <nlohmann/json.hpp>

using json = nlohmann::json;

struct Channel {
    int path;
    int layer;
    int signal;

    bool isSignal() const { return signal == 1; }
    void setSignal(bool value) { signal = value ? 1 : 0; }
    bool above(int l) const { return layer > l; }

    std::string toString() const {
        return std::to_string(path) + "|" + std::to_string(layer) + "|" + std::to_string(signal);
    }

    static Channel fromString(const std::string& s) {
        std::stringstream ss(s);
        std::string item;
        std::vector<std::string> parts;
        while (std::getline(ss, item, '|')) parts.push_back(item);
        return { std::stoi(parts[0]), std::stoi(parts[1]), std::stoi(parts[2]) };
    }
};

struct Status {
    int age;
    std::string health;

    bool tooOld(int max) const { return age > max; }
    bool ill(const std::string& pattern) const { return health != pattern; }
    void getOlder(int inc = 1) { age += inc; }

    std::string toString() const { return std::to_string(age) + "|" + health; }

    static Status fromString(const std::string& s) {
        auto pos = s.find('|');
        return { std::stoi(s.substr(0, pos)), s.substr(pos + 1) };
    }
};

struct Peering {
    long long timestamp;
    std::string from;
    std::string to;
    std::string subject;

    std::string toString() const {
        return std::to_string(timestamp) + " " + from + " " + to + " " + subject;
    }

    static Peering fromString(const std::string& s) {
        std::stringstream ss(s);
        std::string t, f, o, sub;
        ss >> t >> f >> o >> sub;
        return { std::stoll(t), f, o, sub };
    }
};

struct Transformer {
    int pointer;
    std::vector<int8_t> operators;
    std::string data;

    std::string toString() const {
        std::string ops;
        for (size_t i = 0; i < operators.size(); ++i) {
            ops += std::to_string((int)operators[i]) + (i == operators.size() - 1 ? "" : ",");
        }
        return std::to_string(pointer) + "|" + ops + "\n" + data;
    }

    static Transformer fromString(const std::string& s) {
        auto nl = s.find('\n');
        std::string meta = s.substr(0, nl);
        std::string data = (nl == std::string::npos) ? "" : s.substr(nl + 1);
        
        auto pipe = meta.find('|');
        int ptr = std::stoi(meta.substr(0, pipe));
        std::string opsStr = meta.substr(pipe + 1);
        
        std::vector<int8_t> ops;
        std::stringstream ss(opsStr);
        std::string val;
        while (std::getline(ss, val, ',')) ops.push_back((int8_t)std::stoi(val));
        
        return { ptr, ops, data };
    }
};

class Packet {
public:
    Channel channel;
    Status status;
    Peering peering;
    Transformer transformer;

    Packet(Channel c, Status s, Peering p, Transformer t) 
        : channel(c), status(s), peering(p), transformer(t) {}

    std::string toString() const {
        return channel.toString() + "\n" + status.toString() + "\n" + 
               peering.toString() + "\n" + transformer.toString();
    }

    static Packet fromString(const std::string& data) {
        std::stringstream ss(data);
        std::string line;
        std::vector<std::string> lines;
        while (std::getline(ss, line, '\n')) lines.push_back(line);

        Channel c = Channel::fromString(lines[0]);
        Status s = Status::fromString(lines[1]);
        Peering p = Peering::fromString(lines[2]);
        
        std::string transPart;
        for(size_t i=3; i<lines.size(); ++i) transPart += lines[i] + (i == lines.size()-1 ? "" : "\n");
        
        return Packet(c, s, p, Transformer::fromString(transPart));
    }
};

#endif