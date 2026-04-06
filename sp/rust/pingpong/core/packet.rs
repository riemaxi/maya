use std::fmt;
use std::str::FromStr;
use serde_json::{Value, json}; // Requires serde_json crate

pub struct Packet {
    pub channel: Channel,
    pub status: Status,
    pub peering: Peering,
    pub transformer: Transformer,
    pub separator: String,
}

impl Packet {
    pub fn new(channel: Channel, status: Status, peering: Peering, transformer: Transformer) -> Self {
        Self {
            channel,
            status,
            peering,
            transformer,
            separator: "\n".to_string(),
        }
    }

    /// Equivalent to JS fromSP(json)
    pub fn from_sp(json_str: &str) -> Result<Self, Box<dyn std::error::Error>> {
        let v: Value = serde_json::from_str(json_str)?;
        
        let channel = Channel::from_str(v["channel"].as_str().unwrap_or("0|0|0"))?;
        let status = Status::from_str("0|0000")?;
        let peering = Peering::from_value(&v["peering"])?;
        let transformer = Transformer::from_str(v["transformer"].as_str().unwrap_or("0|0\n"))?;

        Ok(Packet::new(channel, status, peering, transformer))
    }
}

impl fmt::Display for Packet {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(
            f,
            "{}{}{}{}{}{}{}",
            self.channel, self.separator, 
            self.status, self.separator, 
            self.peering, self.separator, 
            self.transformer
        )
    }
}

impl FromStr for Packet {
    type Err = Box<dyn std::error::Error>;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let parts: Vec<&str> = s.split('\n').collect();
        if parts.len() < 4 {
            return Err("Invalid packet format".into());
        }

        Ok(Packet::new(
            Channel::from_str(parts[0])?,
            Status::from_str(parts[1])?,
            Peering::from_str(parts[2])?,
            Transformer::from_strings(&parts[3..].iter().map(|&x| x.to_string()).collect())?,
        ))
    }
}

pub struct Channel {
    pub path: i32,
    pub layer: i32,
    pub signal: i32,
}

impl Channel {
    pub fn is_signal(&self) -> bool {
        self.signal == 1
    }

    pub fn set_is_signal(&mut self, value: bool) {
        self.signal = if value { 1 } else { 0 };
    }

    pub fn above(&self, layer: i32) -> bool {
        self.layer > layer
    }
}

impl fmt::Display for Channel {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}|{}|{}", self.path, self.layer, self.signal)
    }
}

impl FromStr for Channel {
    type Err = Box<dyn std::error::Error>;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let parts: Vec<&str> = s.split('|').collect();
        Ok(Channel {
            path: parts[0].parse()?,
            layer: parts[1].parse()?,
            signal: parts[2].parse()?,
        })
    }
}

pub struct Status {
    pub age: i32,
    pub health: String,
}

impl Status {
    pub fn too_old(&self, max: i32) -> bool {
        self.age > max
    }

    pub fn ill(&self, pattern: &str) -> bool {
        self.health != pattern
    }

    pub fn get_older(&mut self, inc: i32) {
        self.age += inc;
    }
}

impl fmt::Display for Status {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}|{}", self.age, self.health)
    }
}

impl FromStr for Status {
    type Err = Box<dyn std::error::Error>;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let parts: Vec<&str> = s.split('|').collect();
        Ok(Status {
            age: parts[0].parse()?,
            health: parts[1].to_string(),
        })
    }
}

pub struct Peering {
    pub timestamp: u64,
    pub from: String,
    pub to: String,
    pub subject: String,
}

impl Peering {
    pub fn from_value(v: &Value) -> Result<Self, Box<dyn std::error::Error>> {
        Ok(Peering {
            timestamp: std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)?
                .as_micros() as u64,
            from: v["from"].as_str().unwrap_or("").to_string(),
            to: v["to"].as_str().unwrap_or("").to_string(),
            subject: v["subject"].as_str().unwrap_or("").to_string(),
        })
    }
}

impl fmt::Display for Peering {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{} {} {} {}", self.timestamp, self.from, self.to, self.subject)
    }
}

impl FromStr for Peering {
    type Err = Box<dyn std::error::Error>;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let parts: Vec<&str> = s.split(' ').collect();
        Ok(Peering {
            timestamp: parts[0].parse()?,
            from: parts[1].to_string(),
            to: parts[2].to_string(),
            subject: parts[3].to_string(),
        })
    }
}

pub struct Transformer {
    pub pointer: usize,
    pub operators: Vec<u8>,
    pub data: String,
}

impl Transformer {
    pub fn shift(&mut self, delta: isize) -> usize {
        let p = self.pointer as isize + delta;
        if p >= 0 && (p as usize) < self.operators.len() {
            self.pointer = p as usize;
        }
        self.pointer
    }

    pub fn apply(&mut self) {
        self.shift(1);
    }

    pub fn from_strings(list: &Vec<String>) -> Result<Self, Box<dyn std::error::Error>> {
        let meta = &list[0];
        let meta_parts: Vec<&str> = meta.split('|').collect();
        let pointer = meta_parts[0].parse()?;
        let operators = meta_parts[1]
            .split(',')
            .map(|s| s.parse::<u8>().unwrap_or(0))
            .collect();
        let data = list[1..].join("\n");

        Ok(Transformer { pointer, operators, data })
    }
}

impl fmt::Display for Transformer {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        let ops: Vec<String> = self.operators.iter().map(|b| b.to_string()).collect();
        write!(f, "{}|{}\n{}", self.pointer, ops.join(","), self.data)
    }
}

impl FromStr for Transformer {
    type Err = Box<dyn std::error::Error>;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let lines: Vec<String> = s.split('\n').map(|l| l.to_string()).collect();
        Self::from_strings(&lines)
    }
}