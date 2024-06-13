package dev.openrune

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class Js5Encoder : MessageToByteEncoder<Js5ContainerResponse>() {
    override fun encode(ctx: ChannelHandlerContext, msg: Js5ContainerResponse, out: ByteBuf) {
        out.writeByte(msg.indexFileId)
        out.writeShort(msg.containerId)
        out.writeByte(msg.compressionType)
        out.writeInt(msg.compressedSize)
        var dataSize = msg.data.readableBytes()
        if (dataSize > BYTES_AFTER_HEADER) {
            dataSize = BYTES_AFTER_HEADER
        }
        out.writeBytes(msg.data.slice(msg.data.readerIndex(), dataSize))
        msg.data.readerIndex(msg.data.readerIndex() + dataSize)
        while (msg.data.readableBytes() > 0) {
            dataSize = msg.data.readableBytes()
            if (dataSize > BYTES_AFTER_BLOCK) {
                dataSize = BYTES_AFTER_BLOCK
            }
            out.writeByte(255)
            out.writeBytes(msg.data.slice(msg.data.readerIndex(), dataSize))
            msg.data.readerIndex(msg.data.readerIndex() + dataSize)
        }
    }

    companion object {
        private const val SECTOR_DATA_SIZE = 512
        private const val BYTES_AFTER_HEADER = SECTOR_DATA_SIZE - 8
        private const val BYTES_AFTER_BLOCK = SECTOR_DATA_SIZE - 1
    }
}