import 'dart:io';
import 'dart:convert';
import 'dart:async';
import 'packet.dart'; // Assumes the previous Packet/Entity code is here
import 'helper.dart'; // Assumes the previous Packeter code is here

class TDPnet {
  WebSocket? _socket;
  StreamSubscription? _subscription;

  Future<void> connect(String host) async {
    try {
      _socket = await WebSocket.connect(host);
      
      onConnected(DateTime.now().millisecondsSinceEpoch);

      _subscription = _socket!.listen(
        (dynamic message) {
          try {
            // Attempt to parse as JSON first
            final m = jsonDecode(message.toString());
            handleEvent(m['id'], m['data']);
          } catch (err) {
            // If not JSON, treat as raw packet data
            onData(message.toString());
          }
        },
        onError: (err) {
          onError(err);
          onConnectionError(err.toString());
        },
        onDone: () {
          onConnectionError('Connection closed');
        },
        cancelOnError: true,
      );
    } catch (e) {
      onConnectionError(e.toString());
      rethrow;
    }
  }

  void handleEvent(dynamic id, dynamic data) {
    if (data == null) return;
    
    // Using fromSP as defined in previous step
    final packet = Packet.fromSP(data.toString());
    final subject = packet.peering.subject;
    final parts = subject.split(':');
    final category = parts[0];
    final msgId = parts.length > 1 ? parts[1] : id.toString();

    switch (category) {
      case 'request':
        onRequest(msgId, packet);
        break;
      case 'response':
        onResponse(msgId, packet);
        break;
      case 'event':
        onEvent(msgId, packet);
        break;
    }
  }

  void onData(String e) {
    final packet = Packet.fromString(e);
    final subject = packet.peering.subject;
    final parts = subject.split(':');
    final category = parts[0];
    final id = parts.length > 1 ? parts[1] : '';

    switch (category) {
      case 'request':
        onRequest(id, packet);
        break;
      case 'response':
        onResponse(id, packet);
        break;
      case 'event':
        onEvent(id, packet);
        break;
    }
  }

  // Lifecycle hooks (to be overridden)
  void onConnected(int timestamp) {}
  void onError(dynamic err) {}
  void onConnectionError(String msg) {}
  void onRequest(String id, Packet p) {}
  void onResponse(String id, Packet p) {}
  void onEvent(String id, Packet p) {}

  void sendSignal(String data) {
    _socket?.add(data);
  }

  void sendData(String data) {
    _socket?.add(data);
  }

  void request(String from, String to, String id, Map<String, dynamic> data) {
    final packetStr = Packeter.createSignal(
      from: from,
      to: to,
      subject: 'request:$id',
      data: jsonEncode(data),
    );
    sendSignal(packetStr);
  }

  void response(Packet packet, String id, Map<String, dynamic> data) {
    // Swap peering logic
    packet.peering = Peering(
      packet.peering.to, 
      packet.peering.from, 
      'response:$id'
    );
    packet.transformer.data = jsonEncode(data);

    sendData(packet.toString());
  }

  Future<void> close() async {
    await _subscription?.cancel();
    await _socket?.close();
  }
}