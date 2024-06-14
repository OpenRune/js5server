package org.jire.js5server.codec.js5

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.DecoderException
import org.jire.js5server.Js5Type

class Js5Decoder : ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, out: MutableList<Any>) {
        if (!input.isReadable(TOTAL_LENGTH)) return

        when (val opcode = input.readUnsignedByte().toInt()) {
            Js5Type.NORMAL_CONTAINER_REQUEST.opcode,Js5Type.URGENT_CONTAINER_REQUEST.opcode -> {
                val archive = input.readUnsignedByte().toInt()
                val group = input.readUnsignedShort()
                out += Js5Request.Group.Request(archive, group, opcode == Js5Type.URGENT_CONTAINER_REQUEST.opcode)
            }

            Js5Type.CLIENT_LOGGED_IN.opcode -> Js5Request.LoggedIn.skipped(input, out)
            Js5Type.CLIENT_LOGGED_OUT.opcode -> Js5Request.LoggedOut.skipped(input, out)

            Js5Type.ENCRYPTION_KEY_UPDATE.opcode -> {
                val key = input.readUnsignedByte().toInt()
                input.skipBytes(2)
                out += Js5Request.Rekey(key)
            }

            Js5Type.CONNECTED.opcode -> Js5Request.Connected.skipped(input, out)
            Js5Type.DISCONNECTED.opcode -> Js5Request.Disconnected.skipped(input, out)

            else -> throw DecoderException("Unsupported JS5 opcode: $opcode")
        }
    }

    private companion object {
        private const val HEADER_LENGTH = 1
        private const val BODY_LENGTH = 3
        private const val TOTAL_LENGTH = HEADER_LENGTH + BODY_LENGTH

        private fun Js5Request.skipped(
            input: ByteBuf,
            out: MutableList<Any>
        ) {
            input.skipBytes(BODY_LENGTH)
            out += this
        }
    }

}