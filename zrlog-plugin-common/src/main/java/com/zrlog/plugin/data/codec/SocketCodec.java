package com.zrlog.plugin.data.codec;

public class SocketCodec {
    private SocketDecode socketDecode;
    private SocketEncode socketEncode;

    public SocketCodec(SocketEncode encode, SocketDecode decode) {
        this.socketDecode = decode;
        this.socketEncode = encode;
    }

    public SocketDecode getSocketDecode() {
        return socketDecode;
    }

    public void setSocketDecode(SocketDecode socketDecode) {
        this.socketDecode = socketDecode;
    }

    public SocketEncode getSocketEncode() {
        return socketEncode;
    }

    public void setSocketEncode(SocketEncode socketEncode) {
        this.socketEncode = socketEncode;
    }
}
