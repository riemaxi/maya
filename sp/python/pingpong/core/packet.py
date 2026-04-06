import json
import time

class Packet:
    def __init__(self, channel, status, peering, transformer):
        self.channel = channel
        self.status = status
        self.peering = peering
        self.transformer = transformer
        self.separator = '\n'

    def __str__(self):
        """Equivalent to JS toString()"""
        return self.separator.join([
            str(self.channel), 
            str(self.status), 
            str(self.peering), 
            str(self.transformer)
        ])

    @staticmethod
    def from_string(data, separator='\n'):
        part = data.split(separator)
        return Packet(
            Channel.from_string(part[0]),
            Status.from_string(part[1]),
            Peering.from_string(part[2]),
            Transformer.from_strings(part[3:])
        )

    @staticmethod
    def from_sp(json_str):
        data = json.loads(json_str)
        channel = Channel.from_string(data['channel'])
        status = Status.from_string('0|0000')
        peering = Peering.from_map(data['peering'])
        transformer = Transformer.from_string(data['transformer'])
        return Packet(channel, status, peering, transformer)

class Channel:
    def __init__(self, path, layer, signal):
        self.path = int(path)
        self.layer = int(layer)
        self.signal = int(signal)

    @property
    def is_signal(self):
        return self.signal == 1

    @is_signal.setter
    def is_signal(self, value):
        self.signal = 1 if value else 0

    def above(self, layer):
        return self.layer > layer

    def __str__(self):
        return f"{self.path}|{self.layer}|{self.signal}"

    @staticmethod
    def from_string(data):
        path, layer, signal = data.split('|')
        return Channel(path, layer, signal)

class Status:
    def __init__(self, age, health):
        self.age = int(age)
        self.health = health

    def too_old(self, max_age):
        return self.age > max_age

    def ill(self, pattern):
        return self.health != pattern

    def get_older(self, inc=1):
        self.age += inc

    def __str__(self):
        return f"{self.age}|{self.health}"

    @staticmethod
    def from_string(data):
        age, health = data.split('|')
        return Status(age, health)

class Peering:
    def __init__(self, from_id, to_id, subject, timestamp=None):
        # JS uses milliseconds * 1000 for microseconds-like precision
        self.timestamp = int(timestamp) if timestamp else int(time.time() * 1000000)
        self.from_id = from_id
        self.to_id = to_id
        self.subject = subject

    def __str__(self):
        return f"{self.timestamp} {self.from_id} {self.to_id} {self.subject}"

    @staticmethod
    def from_string(data):
        timestamp, from_id, to_id, subject = data.split(' ')
        return Peering(from_id, to_id, subject, timestamp)

    @staticmethod
    def from_map(data):
        return Peering(data['from'], data['to'], data['subject'])

class Transformer:
    def __init__(self, pointer, operators, data):
        self.pointer = int(pointer)
        self.operators = operators  # Expected to be a list or bytearray
        self.data = data

    def shift(self, delta=1):
        p = self.pointer + delta
        if 0 <= p < len(self.operators):
            self.pointer = p
        return self.pointer

    def apply(self):
        self.shift()

    def __str__(self):
        ops = ",".join(map(str, self.operators))
        return f"{self.pointer}|{ops}\n{self.data}"

    @staticmethod
    def from_strings(lines):
        meta = lines[0]
        data = "\n".join(lines[1:])
        pointer, ops_str = meta.split('|')
        operators = [int(x) for x in ops_str.split(',')]
        return Transformer(pointer, operators, data)

    @staticmethod
    def from_string(string):
        lines = string.split('\n')
        return Transformer.from_strings(lines)