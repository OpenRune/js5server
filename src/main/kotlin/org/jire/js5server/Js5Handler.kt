package org.jire.js5server

import com.displee.cache.CacheLibrary
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext

class Js5Handler(private val store: CacheLibrary) : PacketInboundHandler<Js5ContainerRequest>() {
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Js5ContainerRequest) {
        val data = Unpooled.wrappedBuffer(store.index255!!.readArchiveSector(msg.indexFileId)?.data)
        val compressionType = data.readUnsignedByte().toInt()
        val compressedSize = data.readInt()
        val response = Js5ContainerResponse(
            msg.indexFileId,
            msg.containerId,
            compressionType,
            compressedSize,
            data.copy()
        )
        ctx.writeAndFlush(response)
    }
}