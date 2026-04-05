const config = require('./config')

new class extends require('./system'){
    constructor(){
        super(config.system)
    }

    onDenied(data){
        console.log('denied', data)
    }

    onGranted(data){
        console.log('granted', data)
        this.request(this.address, this.peers.pingpong, 'pong', 0);
    }

    onEvent(id, packet){
        console.log('event', id, Date.now());
        switch(id){
            case 'ping': this.response(packet,'pong', 0); break;
        }
    }

    onResponse(id, packet){
        console.log('response', id, Date.now());
        switch(id){
            case 'ping': this.response(packet,'pong', 0); break;
        }
    }

    onRequest(id, packet){
        console.log('request', id, packet);
        switch(id){
            case 'ping': this.response(packet,'pong', 0); break;
        }
    }
}

