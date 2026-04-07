# system.py
import asyncio
from tdpnet import StandardSession

class System(StandardSession):
    def __init__(self, config, connect=True):
        super().__init__()
        self.config = config
        if connect:
            asyncio.create_task(self.connect(self.host, self.config.get('security', {}).get('ca')))

    @property
    def host(self):
        return self.config.get('host')

    @property
    def credential(self):
        return self.config.get('credential')

    @property
    def address(self):
        return self.credential.get('address')

    @property
    def peers(self):
        return self.config.get('peers')

    def on_connected(self, timestamp):
        self.signin(self.credential)

    def on_granted(self, data):
        pass

    def on_denied(self, data):
        pass