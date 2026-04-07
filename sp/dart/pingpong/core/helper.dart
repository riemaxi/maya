import 'dart:convert';
import 'packet.dart';

class Packeter {
  static List<int> oneOperator = [0];

  static String createAd({
    required String from,
    String to = '',
    String? name,
    String description = '',
    dynamic status,
    String subject = 'signin',
  }) {
    final Map<String, dynamic> payload = {
      'name': name ?? from,
      'description': description,
    };
    if (status != null) payload['status'] = status;

    return createSignal(
      from: from,
      to: to,
      data: jsonEncode(payload),
      subject: subject,
    );
  }

  static String createSignal({
    required String from,
    required String to,
    required String data,
    required String subject,
    List<int>? operators,
    int pointer = 0,
  }) {
    return Packet(
      Channel(0, 0, 1),
      Status(0, '0000'),
      Peering(from, to, subject),
      Transformer(pointer, operators ?? oneOperator, data),
    ).toString();
  }
}