This `README.md` is tailored for the Node.js implementation of the **TDPnet** protocol. It emphasizes the event-driven nature of the library and how to use the `StandardSession` to interact with the network.

Usage:
node index.js

---

# TDPnet Node.js API

The **TDPnet** Node.js library provides a high-level, asynchronous interface for interacting with the TDPnet network. It handles packet serialization, WebSocket management, and the request/response lifecycle natively.

## 🚀 Quick Start

### Installation
Ensure you have Node.js installed. This library depends on the `ws` (WebSocket) package.

```bash
npm install ws
```

### Basic Usage
To use the API, extend the `StandardSession` class. This allows you to handle network events like signals, data transfers, and peer requests.

```javascript
const { StandardSession } = require('./tdpnet');
const config = require('./config');

class MySession extends StandardSession {
    constructor() {
        super(config.system);
    }

    // Triggered once the connection is granted by the server
    onGranted(data) {
        console.log('Access Granted:', data);
        
        // Send a request to a peer
        this.request(
            this.address, 
            this.peers.pingpong, 
            'ping', 
            { message: 'Hello TDPnet' }
        );
    }

    // Handle incoming requests from other peers
    onRequest(id, packet) {
        console.log(`Received request: ${id}`);
        if (id === 'ping') {
            this.response(packet, 'pong', { status: 'online' });
        }
    }

    // Handle events/notifications
    onEvent(id, packet) {
        console.log(`Notification received: ${id}`);
    }
}

new MySession();
```

---

## 🏗 Architecture

The library is divided into three core modules:

### 1. Packet Logic (`packet.js`)
Handles the low-level serialization of the TDPnet protocol. A packet consists of four newline-separated segments:
* **Channel:** Routing and signal flags.
* **Status:** Age and health monitoring.
* **Peering:** Source, destination, and subject metadata.
* **Transformer:** Execution pointers and the JSON data payload.

### 2. Helper (`helper.js`)
Contains the `Packeter` static class, which simplifies the creation of Ads (Advertisements) and Signals.

### 3. Networking (`tdpnet.js`)
Manages the WebSocket lifecycle.
* **TDPnet:** Base class for raw WebSocket communication.
* **Session:** Adds sign-in/sign-off logic.
* **StandardSession:** Provides the Category-based event system (Request, Response, Event).

---

## 📂 Configuration
The network requires a configuration object typically structured as follows:

```javascript
{
    host: 'wss://...',
    credential: {
        address: 'name.maya.4da',
        accesskey: '...',
        password: '...'
    },
    security: {
        ca: '-----BEGIN CERTIFICATE----- ...'
    }
}
```

---

## 📝 Protocol Patterns

### Signals vs. Data
* **Signals:** Used for protocol-level negotiation, requests, and small notifications. They are high-priority.
* **Data:** Used for heavier payloads and response cycles.

### Subject Categories
The `StandardSession` automatically parses the `peering.subject` field using a `category:id` format:
* `request:myAction` -> Triggers `onRequest('myAction', packet)`
* `response:myAction` -> Triggers `onResponse('myAction', packet)`
* `event:myAction` -> Triggers `onEvent('myAction', packet)`

---

## 🔒 Security
Connections are established over **WSS (WebSocket Secure)**. When connecting to private or self-signed nodes, ensure the `ca` certificate is provided in the configuration to prevent handshake failures.