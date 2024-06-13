package org.jire.js5server

import com.displee.cache.CacheLibrary
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
import java.io.IOException

class ServiceHandler(
    private val path : String,
    private val currentRevision: Int,
) : PacketInboundHandler<ConnectionRequest>() {
    override fun channelRead0(ctx: ChannelHandlerContext, msg: ConnectionRequest) {
        ctx.pipeline().addStatusEncoder()
        when (msg) {
            is GameConnectionRequest ->  throw IOException(
                "GameConnectionRequest Not Supported."
            )
            is Js5ConnectionRequest -> {
                if (msg.revision != currentRevision) {
                    ctx.writeAndFlush(StatusResponse.OUT_OF_DATE)
                    throw IOException(
                        "Revision handshake failed, expected revision $currentRevision but got ${msg.revision}."
                    )
                }
                ctx.writeAndFlush(StatusResponse.SUCCESSFUL)
                ctx.pipeline().swapToJs5()
            }
        }
    }

    private fun ChannelPipeline.addStatusEncoder() {
        addAfter(ServiceDecoder::class.qualifiedName, StatusEncoder::class.qualifiedName, StatusEncoder())
    }

    private fun ChannelPipeline.swapToJs5() {
        replace(ServiceDecoder::class.qualifiedName, Js5Decoder::class.qualifiedName, Js5Decoder())
        replace(ServiceHandler::class.qualifiedName, Js5Handler::class.qualifiedName, Js5Handler(CacheLibrary.create(path)))
        replace(StatusEncoder::class.qualifiedName, Js5Encoder::class.qualifiedName, Js5Encoder())
    }
}