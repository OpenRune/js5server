package dev.openrune

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import org.slf4j.Logger

abstract class PacketInboundHandler<P> : SimpleChannelInboundHandler<P>() {

    private val logger: Logger = org.slf4j.LoggerFactory.getLogger(PacketInboundHandler::class.java)

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (!cause.message.equals("An existing connection was forcibly closed by the remote host")) {
            logger.error("Error while handling message, closing connection.",cause)
        }
        ctx.close()
    }
}