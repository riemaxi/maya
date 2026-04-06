package pingpong.core;

import java.util.*;
import java.util.stream.Collectors;
import org.json.JSONObject; // Assumes org.json library

public class Packet {
    public Channel channel;
    public Status status;
    public Peering peering;
    public Transformer transformer;
    public String separator = "\n";

    public Packet(Channel channel, Status status, Peering peering, Transformer transformer) {
        this.channel = channel;
        this.status = status;
        this.peering = peering;
        this.transformer = transformer;
    }

    @Override
    public String toString() {
        return String.join(separator, 
            channel.toString(), 
            status.toString(), 
            peering.toString(), 
            transformer.toString()
        );
    }

    public static Packet fromString(String data) {
        return fromString(data, "\n");
    }

    public static Packet fromString(String data, String separator) {
        String[] parts = data.split(separator);
        // Transformer takes the remainder of the parts
        List<String> transformerParts = Arrays.asList(parts).subutList(3, parts.length);
        
        return new Packet(
            Channel.fromString(parts[0]),
            Status.fromString(parts[1]),
            Peering.fromString(parts[2]),
            Transformer.fromStrings(transformerParts)
        );
    }

    public static Packet fromSP(String json) {
        JSONObject data = new JSONObject(json);

        Channel channel = Channel.fromString(data.getString("channel"));
        Status status = Status.fromString("0|0000");
        Peering peering = Peering.fromMap(data.getJSONObject("peering").toMap());
        Transformer transformer = Transformer.fromString(data.getString("transformer"));

        return new Packet(channel, status, peering, transformer);
    }
}

class Channel {
    public int path;
    public int layer;
    public int signal;

    public Channel(int path, int layer, int signal) {
        this.path = path;
        this.layer = layer;
        this.signal = signal;
    }

    public boolean isSignal() { return this.signal == 1; }
    public void setSignal(boolean value) { this.signal = value ? 1 : 0; }

    public boolean above(int layer) { return this.layer > layer; }

    @Override
    public String toString() {
        return path + "|" + layer + "|" + signal;
    }

    public static Channel fromString(String data) {
        String[] parts = data.split("\\|");
        return new Channel(
            Integer.parseInt(parts[0]), 
            Integer.parseInt(parts[1]), 
            Integer.parseInt(parts[2])
        );
    }
}

class Status {
    public int age;
    public String health;

    public Status(int age, String health) {
        this.age = age;
        this.health = health;
    }

    public boolean tooOld(int max) { return this.age > max; }
    public boolean ill(String pattern) { return !this.health.equals(pattern); }
    public void getOlder(int inc) { this.age += inc; }

    @Override
    public String toString() {
        return age + "|" + health;
    }

    public static Status fromString(String data) {
        String[] parts = data.split("\\|");
        return new Status(Integer.parseInt(parts[0]), parts[1]);
    }
}

class Peering {
    public long timestamp;
    public String from;
    public String to;
    public String subject;

    public Peering(String from, String to, String subject) {
        this(from, to, subject, System.currentTimeMillis() * 1000);
    }

    public Peering(String from, String to, String subject, long timestamp) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return timestamp + " " + from + " " + to + " " + subject;
    }

    public static Peering fromString(String data) {
        String[] parts = data.split(" ");
        return new Peering(parts[1], parts[2], parts[3], Long.parseLong(parts[0]));
    }

    public static Peering fromMap(Map<String, Object> data) {
        return new Peering(
            (String) data.get("from"), 
            (String) data.get("to"), 
            (String) data.get("subject")
        );
    }
}

class Transformer {
    public int pointer;
    public byte[] operators;
    public String data;

    public Transformer(int pointer, byte[] operators, String data) {
        this.pointer = pointer;
        this.operators = operators;
        this.data = data;
    }

    public int shift(int delta) {
        int p = this.pointer + delta;
        if (p >= 0 && p < this.operators.length) {
            this.pointer = p;
        }
        return this.pointer;
    }

    public void apply() { shift(1); }

    @Override
    public String toString() {
        String ops = "";
        for (int i = 0; i < operators.length; i++) {
            ops += operators[i] + (i == operators.length - 1 ? "" : ",");
        }
        return pointer + "|" + ops + "\n" + data;
    }

    public static Transformer fromStrings(List<String> list) {
        String meta = list.get(0);
        String data = list.stream().skip(1).collect(Collectors.joining("\n"));
        
        String[] metaParts = meta.split("\\|");
        int pointer = Integer.parseInt(metaParts[0]);
        String[] opsParts = metaParts[1].split(",");
        byte[] operators = new byte[opsParts.length];
        for (int i = 0; i < opsParts.length; i++) {
            operators[i] = Byte.parseByte(opsParts[i]);
        }
        
        return new Transformer(pointer, operators, data);
    }

    public static Transformer fromString(String str) {
        return fromStrings(Arrays.asList(str.split("\n")));
    }
}