package org.jire.js5server

import io.netty.channel.ChannelHandlerContext

class Js5Handler(private val repository: Js5GroupRepository) : PacketInboundHandler<Js5ContainerRequest>() {
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Js5ContainerRequest) {
        ctx.writeAndFlush(repository[msg.indexFileId, msg.containerId]?.retainedDuplicate(), ctx.voidPromise())
    }
}