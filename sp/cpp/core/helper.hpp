#include "packet.hpp"

class Packeter {
public:
    static std::string createSignal(std::string from, std::string to, std::string data, std::string subject) {
        Channel c{0, 0, 1};
        Status s{0, "0000"};
        Peering p{0, from, to, subject}; // Timestamp simplified
        Transformer t{0, {0}, data};
        
        return Packet(c, s, p, t).toString();
    }
};