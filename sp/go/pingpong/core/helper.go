package tdpnet

import (
	"encoding/json"
)

type Packeter struct{}

var OneOperator = []int8{0}

func (p Packeter) CreateAd(from string, to string, name *string, description string, status *string, subject string) string {
	displayName := from
	if name != nil {
		displayName = *name
	}

	payload := map[string]interface{}{
		"name":        displayName,
		"description": description,
	}
	if status != nil {
		payload["status"] = *status
	}

	data, _ := json.Marshal(payload)
	if subject == "" {
		subject = "signin"
	}

	return p.CreateSignal(from, to, string(data), subject, nil, 0)
}

func (p Packeter) CreateSignal(from, to, data, subject string, operators []int8, pointer int) string {
	ops := operators
	if ops == nil {
		ops = OneOperator
	}

	packet := NewPacket(
		Channel{Path: "0", Layer: 0, Signal: 1},
		Status{Age: 0, Health: "0000"},
		Peering{From: from, To: to, Subject: subject},
		Transformer{Pointer: pointer, Operators: ops, Data: data},
	)
	return packet.ToString()
}
