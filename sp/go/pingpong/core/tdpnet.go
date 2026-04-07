package tdpnet

import (
	"encoding/json"
	"fmt"
	"log"
	"strings"
	"time"

	"github.com/gorilla/websocket"
)

type TDPnet struct {
	Conn *websocket.Conn
}

func NewTDPnet() *TDPnet {
	return &TDPnet{}
}

func (t *TDPnet) Connect(host string) error {
	dialer := websocket.DefaultDialer
	conn, _, err := dialer.Dial(host, nil)
	if err != nil {
		t.OnConnectionError(err.Error())
		return err
	}

	t.Conn = conn
	t.OnConnected(time.Now().UnixNano() / int64(time.Millisecond))

	// Start read loop in a background goroutine
	go func() {
		defer func() {
			t.Conn.Close()
			t.OnConnectionError("Connection closed")
		}()

		for {
			_, message, err := t.Conn.ReadMessage()
			if err != nil {
				t.OnError(err)
				break
			}

			// Try to parse as JSON first (matches handleEvent logic)
			var m struct {
				ID   interface{} `json:"id"`
				Data string      `json:"data"`
			}

			if err := json.Unmarshal(message, &m); err == nil && m.Data != "" {
				t.HandleEvent(m.ID, m.Data)
			} else {
				// If not valid JSON event, treat as raw packet data
				t.OnData(string(message))
			}
		}
	}()

	return nil
}

func (t *TDPnet) HandleEvent(id interface{}, data string) {
	packet, err := FromSP(data)
	if err != nil {
		return
	}

	subject := packet.Peering.Subject
	parts := strings.Split(subject, ":")
	category := parts[0]

	msgID := ""
	if len(parts) > 1 {
		msgID = parts[1]
	} else if id != nil {
		msgID = fmt.Sprintf("%v", id)
	}

	switch category {
	case "request":
		t.OnRequest(msgID, *packet)
	case "response":
		t.OnResponse(msgID, *packet)
	case "event":
		t.OnEvent(msgID, *packet)
	}
}

func (t *TDPnet) OnData(e string) {
	packet := FromString(e)
	if packet == nil {
		return
	}

	parts := strings.Split(packet.Peering.Subject, ":")
	category := parts[0]
	id := ""
	if len(parts) > 1 {
		id = parts[1]
	}

	switch category {
	case "request":
		t.OnRequest(id, *packet)
	case "response":
		t.OnResponse(id, *packet)
	case "event":
		t.OnEvent(id, *packet)
	}
}

// Lifecycle Hooks (Mocking JS override behavior)
func (t *TDPnet) OnConnected(timestamp int64)    {}
func (t *TDPnet) OnError(err error)              { log.Println("Error:", err) }
func (t *TDPnet) OnConnectionError(msg string)   { log.Println("Conn Error:", msg) }
func (t *TDPnet) OnRequest(id string, p Packet)  {}
func (t *TDPnet) OnResponse(id string, p Packet) {}
func (t *TDPnet) OnEvent(id string, p Packet)    {}

func (t *TDPnet) SendSignal(data string) error {
	if t.Conn != nil {
		return t.Conn.WriteMessage(websocket.TextMessage, []byte(data))
	}
	return fmt.Errorf("socket not connected")
}

func (t *TDPnet) SendData(data string) error {
	return t.SendSignal(data)
}

func (t *TDPnet) Request(from, to, id string, data interface{}) {
	jsonData, _ := json.Marshal(data)
	packetStr := CreateSignal(
		from,
		to,
		string(jsonData),
		"request:"+id,
		nil,
		0,
	)
	t.SendSignal(packetStr)
}

func (t *TDPnet) Response(packet Packet, id string, data interface{}) {
	jsonData, _ := json.Marshal(data)

	// Swap peering
	packet.Peering = Peering{
		Timestamp: uint64(time.Now().UnixNano() / 1000),
		From:      packet.Peering.To,
		To:        packet.Peering.From,
		Subject:   "response:" + id,
	}
	packet.Transformer.Data = string(jsonData)

	t.SendData(packet.ToString())
}
