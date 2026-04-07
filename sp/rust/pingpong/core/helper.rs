use crate::packet::{Packet, Channel, Status, Peering, Transformer};
use serde_json::json;

pub struct Packeter;

impl Packeter {
    pub const ONE_OPERATOR: [u8; 1] = [0];

    pub fn create_ad(
        from_id: &str,
        to: &str,
        name: Option<&str>,
        description: &str,
        status: Option<&str>,
        subject: &str
    ) -> String {
        let display_name = name.unwrap_or(from_id);
        
        let mut payload = json!({
            "name": display_name,
            "description": description
        });

        if let Some(s) = status {
            payload["status"] = json!(s);
        }

        Self::create_signal(
            from_id,
            to,
            &payload.to_string(),
            subject,
            None,
            0
        )
    }

    pub fn create_signal(
        from_id: &str,
        to: &str,
        data: &str,
        subject: &str,
        operators: Option<Vec<u8>>,
        pointer: usize
    ) -> String {
        let ops = operators.unwrap_or_else(|| Self::ONE_OPERATOR.to_vec());
        Packet::new(
            Channel { path: 0, layer: 0, signal: 1 },
            Status { age: 0, health: "0000".to_string() },
            Peering { timestamp: 0, from: from_id.to_string(), to: to.to_string(), subject: subject.to_string() },
            Transformer { pointer, operators: ops, data: data.to_string() }
        ).to_string()
    }
}