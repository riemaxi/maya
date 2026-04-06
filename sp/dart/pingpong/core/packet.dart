import 'dart:convert';

class Entity {
  Map<dynamic, dynamic> data;
  Entity({required this.data});
  dynamic operator [](dynamic id) => data[id];
  void operator []=(dynamic id, dynamic value) => data[id] = value;
  String get json => jsonEncode(data);
  set json(String data) => this.data = jsonDecode(data);
}

class Packet {
  Channel channel;
  Status status;
  Peering peering;
  Transformer transformer;
  String separator = '\n';

  Packet(this.channel, this.status, this.peering, this.transformer);

  @override
  String toString() {
    return [channel, status, peering, transformer]
        .map((p) => p.toString())
        .join(separator);
  }

  static Packet fromString(String data, {String separator = '\n'}) {
    List<String> part = data.split(separator);
    return Packet(
      Channel.fromString(part[0]),
      Status.fromString(part[1]),
      Peering.fromString(part[2]),
      Transformer.fromStrings(part.sublist(3)),
    );
  }

  static Packet fromSP(String jsonStr) {
    // Using the Entity superclass for data access
    Entity entity = Entity(data: jsonDecode(jsonStr));

    Channel channel = Channel.fromString(entity['channel']);
    Status status = Status.fromString('0|0000');
    Peering peering = Peering.fromMap(Map<String, dynamic>.from(entity['peering']));
    Transformer transformer = Transformer.fromString(entity['transformer']);

    return Packet(channel, status, peering, transformer);
  }
}

class Channel {
  int path;
  int layer;
  int signal;

  Channel(this.path, this.layer, this.signal);

  bool get isSignal => signal == 1;
  set isSignal(bool value) => signal = value ? 1 : 0;

  bool above(int layer) => this.layer > layer;

  @override
  String toString() => '$path|$layer|$signal';

  static Channel create(int path, int layer, int signal) =>
      Channel(path, layer, signal);

  static Channel fromString(String data) {
    List<String> parts = data.split('|');
    return Channel(
        int.parse(parts[0]), int.parse(parts[1]), int.parse(parts[2]));
  }
}

class Status {
  int age;
  String health;

  Status(this.age, this.health);

  bool tooOld(int max) => age > max;
  bool ill(String pattern) => health != pattern;
  void getOlder({int inc = 1}) => age += inc;

  @override
  String toString() => '$age|$health';

  static Status fromString(String data) {
    List<String> parts = data.split('|');
    return Status(int.parse(parts[0]), parts[1]);
  }
}

class Peering {
  int timestamp;
  String from;
  String to;
  String subject;

  Peering(this.from, this.to, this.subject, {int? timestamp})
      : timestamp = timestamp ?? DateTime.now().microsecondsSinceEpoch;

  @override
  String toString() => '$timestamp $from $to $subject';

  static Peering fromString(String data) {
    List<String> parts = data.split(' ');
    return Peering(parts[1], parts[2], parts[3],
        timestamp: int.parse(parts[0]));
  }

  static Peering fromMap(Map<String, dynamic> data) {
    return Peering(data['from'], data['to'], data['subject']);
  }
}

class Transformer {
  int pointer;
  List<int> operators; // Using List<int> as equivalent to Buffer/Int8Array
  String data;

  Transformer(this.pointer, this.operators, this.data);

  int shift({int delta = 1}) {
    int p = pointer + delta;
    bool valid = p >= 0 && p < operators.length;
    if (valid) pointer = p;
    return pointer;
  }

  void apply() => shift();

  @override
  String toString() => '$pointer|${operators.join(',')}\n$data';

  static Transformer fromStrings(List<String> list) {
    String meta = list[0];
    List<String> dataLines = list.sublist(1);
    List<String> metaParts = meta.split('|');
    int pointer = int.parse(metaParts[0]);
    List<int> operators = metaParts[1].split(',').map(int.parse).toList();
    return Transformer(pointer, operators, dataLines.join('\n'));
  }

  static Transformer fromString(String str) {
    List<String> lines = str.split('\n');
    return fromStrings(lines);
  }
}