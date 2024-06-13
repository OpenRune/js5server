package dev.openrune

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import java.io.IOException

class ServiceDecoder : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, inc: ByteBuf, out: MutableList<Any>) {
        if (!inc.isReadable) return
        inc.markReaderIndex()
        when (val opcode = inc.readUnsignedByte().toInt()) {
            ServiceType.GAME.opcode -> throw IOException("Not supported")
            ServiceType.JS5.opcode -> {
                if (!inc.isReadable(4)) {
                    inc.resetReaderIndex()
                    return
                }
                out.add(Js5ConnectionRequest(inc.readInt()))
            }
            else -> throw IOException("Could not identify service with opcode $opcode.")
        }
    }
}