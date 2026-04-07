// main.dart
import 'dart:convert';
import 'config.dart';
import 'system.dart';
import 'packet.dart';

class Application extends System {
  Application() : super(Config.system);

  @override
  void onDenied(dynamic data) {
    print('denied: $data');
  }

  @override
  void onGranted(dynamic data) {
    print('granted: $data');
    request(address, peers['pingpong'], 'pong', 0);
  }

  @override
  void onEvent(String id, Packet packet) {
    print('event: $id at ${DateTime.now().millisecondsSinceEpoch}');
    if (id == 'ping') {
      response(packet, 'pong', 0);
    }
  }

  @override
  void onResponse(String id, Packet packet) {
    print('response: $id at ${DateTime.now().millisecondsSinceEpoch}');
    if (id == 'ping') {
      response(packet, 'pong', 0);
    }
  }

  @override
  void onRequest(String id, Packet packet) {
    print('request: $id payload: $packet');
    if (id == 'ping') {
      response(packet, 'pong', 0);
    }
  }
}

void main() {
  // Initialize the application
  Application();
}