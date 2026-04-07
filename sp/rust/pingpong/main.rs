mod config;
mod system;
mod tdpnet;
mod packet;

use config::SYSTEM_CONFIG;
use system::System;
use std::time::{SystemTime, UNIX_EPOCH};

#[tokio::main]
async fn main() {
    let sys = System::new(&SYSTEM_CONFIG, true).await;

    // This mimics the event handlers in index.js
    // In Rust, you would typically pass a closure or implement a trait
    println!("System started. Connecting to {}", SYSTEM_CONFIG.host);

    // Simulated event loop logic based on index.js requirements:
    
    /* on_granted: 
    sys.tdp.request(
        SYSTEM_CONFIG.credential.address, 
        SYSTEM_CONFIG.pingpong_peer, 
        "pong", 
        json!(0)
    ).await;
    */

    /*
    on_event/on_request for "ping":
    let now = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_millis();
    println!("event: ping {}", now);
    sys.tdp.response(packet, "pong", json!(0)).await;
    */

    // Keep the async runtime alive
    loop {
        tokio::time::sleep(tokio::time::Duration::from_secs(1)).await;
    }
}