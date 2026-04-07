package main

import (
	"fmt"
	"time"
	"yourproject/tdpnet" // Adjust path as necessary
)

type Application struct {
	*tdpnet.System
}

func NewApplication() *Application {
	app := &Application{
		System: tdpnet.NewSystem(tdpnet.Config, true),
	}
	return app
}

func (a *Application) OnDenied(data interface{}) {
	fmt.Printf("denied: %v\n", data)
}

func (a *Application) OnGranted(data interface{}) {
	fmt.Printf("granted: %v\n", data)
	// Request a pong from the peer upon being granted access
	a.Request(a.Config.Credential.Address, a.Config.Peers["pingpong"], "pong", 0)
}

func (a *Application) OnEvent(id string, packet tdpnet.Packet) {
	fmt.Printf("event: %s %d\n", id, time.Now().UnixMilli())
	if id == "ping" {
		a.Response(packet, "pong", 0)
	}
}

func (a *Application) OnResponse(id string, packet tdpnet.Packet) {
	fmt.Printf("response: %s %d\n", id, time.Now().UnixMilli())
	if id == "ping" {
		a.Response(packet, "pong", 0)
	}
}

func (a *Application) OnRequest(id string, packet tdpnet.Packet) {
	fmt.Printf("request: %s %v\n", id, packet)
	if id == "ping" {
		a.Response(packet, "pong", 0)
	}
}

func main() {
	app := NewApplication()
	
	// Keep the application running
	select {} 
}