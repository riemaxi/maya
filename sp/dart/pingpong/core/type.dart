import 'dart:convert';

class Entity {
  Map<dynamic, dynamic> data;
  Entity({required this.data});
  dynamic operator [](dynamic id) => data[id];
  void operator []=(dynamic id, dynamic value) => data[id] = value;
  String get json => jsonEncode(data);
  set json(String data) => this.data = jsonDecode(data);
}
