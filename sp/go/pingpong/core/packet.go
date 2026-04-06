package tdpnet

import (
	"encoding/json"
	"fmt"
	"strconv"
	"strings"
	"time"
)

type Packet struct {
	Channel     Channel
	Status      Status
	Peering     Peering
	Transformer Transformer
	Separator   string
}

func NewPacket(channel Channel, status Status, peering Peering, transformer Transformer) *Packet {
	return &Packet{
		Channel:     channel,
		Status:      status,
		Peering:     peering,
		Transformer: transformer,
		Separator:   "\n",
	}
}

func (p *Packet) ToString() string {
	return strings.Join([]string{
		p.Channel.ToString(),
		p.Status.ToString(),
		p.Peering.ToString(),
		p.Transformer.ToString(),
	}, p.Separator)
}

func FromString(data string) *Packet {
	parts := strings.Split(data, "\n")
	if len(parts) < 4 {
		return nil
	}

	return NewPacket(
		ChannelFromString(parts[0]),
		StatusFromString(parts[1]),
		PeeringFromString(parts[2]),
		TransformerFromStrings(parts[3:]),
	)
}

func FromSP(jsonStr string) (*Packet, error) {
	var raw map[string]interface{}
	if err := json.Unmarshal([]byte(jsonStr), &raw); err != nil {
		return nil, err
	}

	channel := ChannelFromString(raw["channel"].(string))
	status := StatusFromString("0|0000")

	peeringRaw := raw["peering"].(map[string]interface{})
	peering := Peering{
		Timestamp: uint64(time.Now().UnixNano() / 1000),
		From:      peeringRaw["from"].(string),
		To:        peeringRaw["to"].(string),
		Subject:   peeringRaw["subject"].(string),
	}

	transformer := TransformerFromString(raw["transformer"].(string))

	return NewPacket(channel, status, peering, *transformer), nil
}

type Channel struct {
	Path   string
	Layer  int
	Signal int
}

func (c *Channel) IsSignal() bool { return c.Signal == 1 }
func (c *Channel) SetIsSignal(val bool) {
	if val {
		c.Signal = 1
	} else {
		c.Signal = 0
	}
}
func (c *Channel) Above(layer int) bool { return c.Layer > layer }

func (c Channel) ToString() string {
	return fmt.Sprintf("%s|%d|%d", c.Path, c.Layer, c.Signal)
}

func ChannelFromString(data string) Channel {
	parts := strings.Split(data, "|")
	layer, _ := strconv.Atoi(parts[1])
	signal, _ := strconv.Atoi(parts[2])
	return Channel{Path: parts[0], Layer: layer, Signal: signal}
}

type Status struct {
	Age    int
	Health string
}

func (s *Status) TooOld(max int) bool     { return s.Age > max }
func (s *Status) Ill(pattern string) bool { return s.Health != pattern }
func (s *Status) GetOlder(inc int)        { s.Age += inc }

func (s Status) ToString() string {
	return fmt.Sprintf("%d|%s", s.Age, s.Health)
}

func StatusFromString(data string) Status {
	parts := strings.Split(data, "|")
	age, _ := strconv.Atoi(parts[0])
	return Status{Age: age, Health: parts[1]}
}

type Peering struct {
	Timestamp uint64
	From      string
	To        string
	Subject   string
}

func (p Peering) ToString() string {
	return fmt.Sprintf("%d %s %s %s", p.Timestamp, p.From, p.To, p.Subject)
}

func PeeringFromString(data string) Peering {
	parts := strings.Split(data, " ")
	ts, _ := strconv.ParseUint(parts[0], 10, 64)
	return Peering{Timestamp: ts, From: parts[1], To: parts[2], Subject: parts[3]}
}

type Transformer struct {
	Pointer   int
	Operators []int8
	Data      string
}

func (t *Transformer) Shift(delta int) int {
	p := t.Pointer + delta
	if p >= 0 && p < len(t.Operators) {
		t.Pointer = p
	}
	return t.Pointer
}

func (t *Transformer) Apply() { t.Shift(1) }

func (t Transformer) ToString() string {
	var ops []string
	for _, v := range t.Operators {
		ops = append(ops, strconv.Itoa(int(v)))
	}
	return fmt.Sprintf("%d|%s\n%s", t.Pointer, strings.Join(ops, ","), t.Data)
}

func TransformerFromStrings(list []string) Transformer {
	meta := list[0]
	data := strings.Join(list[1:], "\n")
	metaParts := strings.Split(meta, "|")
	pointer, _ := strconv.Atoi(metaParts[0])

	opsStr := strings.Split(metaParts[1], ",")
	operators := make([]int8, len(opsStr))
	for i, s := range opsStr {
		val, _ := strconv.Atoi(s)
		operators[i] = int8(val)
	}

	return Transformer{Pointer: pointer, Operators: operators, Data: data}
}

func TransformerFromString(str string) *Transformer {
	lines := strings.Split(str, "\n")
	t := TransformerFromStrings(lines)
	return &t
}
