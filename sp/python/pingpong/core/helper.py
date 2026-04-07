import json
from packet import Packet, Channel, Status, Peering, Transformer

class Packeter:
    ONE_OPERATOR = [0] # List as equivalent to Int8Array(1)

    @staticmethod
    def create_ad(from_id, name=None, description="", status=None, to="", subject='signin'):
        # JS: name ?? from
        display_name = name if name is not None else from_id
        
        payload = {"name": display_name, "description": description}
        if status is not None:
            payload["status"] = status
            
        data = json.dumps(payload)

        return Packeter.create_signal(
            from_id=from_id,
            to=to,
            data=data,
            subject=subject
        )

    @staticmethod
    def create_signal(from_id, to, data, subject, operators=None, pointer=0):
        return str(Packet(
            Channel("", 0, 1),
            Status(0, '0000'),
            Peering(from_id, to, subject),
            Transformer(pointer, operators if operators is not None else Packeter.ONE_OPERATOR, data)
        ))

    @staticmethod
    def create_data(channel, status, peering, transformer):
        return str(Packet(channel, status, peering, transformer))