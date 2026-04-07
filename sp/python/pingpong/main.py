# main.py
import asyncio
import time
from system import System
from config import SYSTEM

class Application(System):
    def __init__(self):
        super().__init__(SYSTEM)

    def on_denied(self, data):
        print('denied', data)

    def on_granted(self, data):
        print('granted', data)
        self.request(self.address, self.peers.get('pingpong'), 'pong', 0)

    def on_event(self, id, packet):
        print('event', id, int(time.time() * 1000))
        if id == 'ping':
            self.response(packet, 'pong', 0)

    def on_response(self, id, packet):
        print('response', id, int(time.time() * 1000))
        if id == 'ping':
            self.response(packet, 'pong', 0)

    def on_request(self, id, packet):
        print('request', id, packet)
        if id == 'ping':
            self.response(packet, 'pong', 0)

if __name__ == "__main__":
    app = Application()
    asyncio.get_event_loop().run_forever()