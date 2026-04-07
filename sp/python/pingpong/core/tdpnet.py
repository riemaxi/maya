import asyncio
import websockets
import json
import time
from packet import Packet, Peering
from helper import Packeter

class TDPnet:
    def __init__(self):
        self._socket = None

    async def connect(self, host, ca_file=None):
        """
        Connects to the websocket host. 
        ca_file can be used to pass a path to a certificate authority file for SSL.
        """
        try:
            # If host starts with wss:// and ca_file is provided, you would configure ssl_context here
            async with websockets.connect(host) as websocket:
                self._socket = websocket
                self.on_connected(int(time.time() * 1000))
                
                # Main listen loop
                async for message in websocket:
                    try:
                        # Attempt to parse as JSON first (matches JS logic)
                        try:
                            m = json.loads(message)
                            self.handle_event(m.get('id'), m.get('data'))
                        except json.JSONDecodeError:
                            # If not JSON, treat as raw packet string
                            self.on_data(message)
                    except Exception as e:
                        self.on_error(e)
                        
                self.on_connection_error("Connection closed")
        except Exception as e:
            self.on_connection_error(e)
            raise e

    def on_connected(self, timestamp):
        """Override this method"""
        pass

    def on_error(self, err):
        """Override this method"""
        print(f"Error: {err}")

    def on_connection_error(self, err):
        """Override this method"""
        print(f"Connection Error: {err}")

    def handle_event(self, id, data):
        if not data:
            return
            
        packet = Packet.from_sp(data)
        subject = packet.peering.subject
        parts = subject.split(':')
        category = parts[0]
        msg_id = parts[1] if len(parts) > 1 else id

        if category == 'request':
            self.on_request(msg_id, packet)
        elif category == 'response':
            self.on_response(msg_id, packet)
        elif category == 'event':
            self.on_event(msg_id, packet)

    def on_data(self, e):
        packet = Packet.from_string(e)
        subject = packet.peering.subject
        parts = subject.split(':')
        category = parts[0]
        id = parts[1] if len(parts) > 1 else None

        if category == 'request':
            self.on_request(id, packet)
        elif category == 'response':
            self.on_response(id, packet)
        elif category == 'event':
            self.on_event(id, packet)

    def on_request(self, id, p):
        """Override this method"""
        pass

    def on_response(self, id, p):
        """Override this method"""
        pass

    def on_event(self, id, p):
        """Override this method"""
        pass

    async def send_signal(self, data):
        if self._socket:
            await self._socket.send(data)

    async def send_data(self, data):
        if self._socket:
            await self._socket.send(data)

    async def request(self, from_id, to_id, id, data):
        packet_str = Packeter.create_signal(
            from_id=from_id,
            to_id=to_id,
            subject=f"request:{id}",
            data=json.dumps(data)
        )
        await self.send_signal(packet_str)

    async def response(self, packet, id, data):
        # Swap peering for response: to -> from, from -> to
        packet.peering = Peering(
            from_id=packet.peering.to_id,
            to_id=packet.peering.from_id,
            subject=f"response:{id}"
        )
        packet.transformer.data = json.dumps(data)
        await self.send_data(str(packet))