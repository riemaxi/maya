// system.dart
import 'core/packet.dart'; // Contains the Entity class
import 'core/tdpnet.dart';

class System extends StandardSession {
  final Entity config;

  System(Map<String, dynamic> configMap, {bool connect = true})
      : config = Entity(data: configMap) {
    if (connect) {
      this.connect(host);
      // Note: In a production Dart/Flutter app, you would pass the CA 
      // string from config['security']['ca'] to a SecurityContext.
    }
  }

  String get host => config['host'];
  
  Map<String, dynamic> get credential => 
      Map<String, dynamic>.from(config['credential']);
      
  String get address => credential['address'];
  
  Map<String, dynamic> get peers => 
      Map<String, dynamic>.from(config['peers']);

  @override
  void onConnected(int timestamp) {
    signin(credential);
  }

  void onGranted(dynamic data) {}
  void onDenied(dynamic data) {}
}