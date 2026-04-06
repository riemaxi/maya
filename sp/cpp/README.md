This `README.md` is designed to help a C++ developer transition from the Node.js implementation to a stable, high-performance C++ client. It highlights the architectural hurdles and provides the solution for the **TDPnet** protocol.

---

# TDPnet C++ API 

This repository contains the C++ implementation of the **TDPnet** protocol. Moving from a Node.js environment to C++ introduces specific challenges regarding asynchronous networking and data serialization.

## ⚠️ Architectural Challenges

### 1. JSON Serialization (Native vs. Library)
In Node.js, JSON is a first-class citizen (`JSON.stringify`). In C++, JSON requires an external library. We use **nlohmann/json** because:
* **Type Safety:** C++ is strictly typed. You cannot simply throw a "dynamic object" into a string.
* **Performance:** Native C++ serialization is significantly faster than V8's JSON parser, but requires explicit mapping of structures.

### 2. WebSockets & Event Loop
Node.js uses a single-threaded non-blocking event loop (`ws` library). C++ (via **IXWebSocket**) typically handles networking in a separate background thread.
* **Concurrency:** You must ensure that shared data between the UI/Main thread and the Network thread is thread-safe.
* **Manual Heartbeats:** Unlike some Node.js wrappers, you may need to handle PING/PONG frames manually if the connection passes through strict firewalls.

### 3. SSL/TLS Implementation
Node.js bundles OpenSSL. On Ubuntu, C++ requires you to link against the system `libssl-dev` and explicitly point the client to the CA bundle (usually located at `/etc/ssl/certs/ca-certificates.crt`) to verify the `wss://` handshake.

---

## 🚀 Getting Started (Ubuntu)

### Prerequisites
Install the core build tools and the SSL development headers:
```bash
sudo apt update
sudo apt install build-essential cmake libssl-dev nlohmann-json3-dev
```

### Library Setup
1. **nlohmann-json**: Installed via `apt` above.
2. **IXWebSocket**: 
   ```bash
   git clone https://github.com/machinezone/IXWebSocket.git
   cd IXWebSocket && mkdir build && cd build
   cmake -DUSE_TLS=1 ..
   make && sudo make install
   ```

---

## 🛠 Usage

### Defining a Session
To interact with TDPnet, inherit from `StandardSession` and override the event handlers:

```cpp
#include "tdpnet.hpp"

class MyTDPClient : public StandardSession {
    void onGranted(const json& data) override {
        // Logic after successful sign-in
        this->request("my.address", "peer.address", "action", {{"key", "value"}});
    }

    void onEvent(std::string id, Packet& p) override {
        if (id == "ping") {
            this->response(p, "pong", 0);
        }
    }
};
```

### Connecting to the Network
The connection requires the host address and the CA certificate for secure communication.

```cpp
int main() {
    MyTDPClient client;
    // Host from config.js
    std::string host = "wss://103.177.249.118:30050";
    
    // Connect (Non-blocking)
    client.connect(host, "/etc/ssl/certs/ca-certificates.crt");

    // Keep process alive
    while(true) { 
        std::this_thread::sleep_for(std::chrono::seconds(1)); 
    }
}
```

---

## 📝 Protocol Specification

The **TDPnet** packet follows a strict 4-part newline-separated structure:

| Component | Format | Example |
| :--- | :--- | :--- |
| **Channel** | `path\|layer\|signal` | `0\|0\|1` |
| **Status** | `age\|health` | `0\|0000` |
| **Peering** | `ts from to subject` | `1712416000 me you request:ping` |
| **Transformer**| `ptr\|ops\ndata` | `0\|0\n{"json":true}` |

### Important Note on Transformers
The `Transformer` data segment can contain multiple lines. The C++ parser is designed to treat everything after the third newline as the data payload to ensure JSON integrity.

---

## ⚖️ License
This API is provided for access to the TDPnet network. Ensure your `accesskey` and `password` are stored securely and not hardcoded in public repositories.