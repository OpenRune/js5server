package dev.advo.js5

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.DecoderException
import net.rsprot.protocol.api.AbstractNetworkServiceFactory
import net.rsprot.protocol.api.ChannelExceptionHandler
import net.rsprot.protocol.api.handlers.ExceptionHandlers
import net.rsprot.protocol.api.js5.Js5GroupProvider
import java.io.IOException

public class NetworkServiceFactory(
    public val groupProvider: Js5GroupProvider,
    override val ports: List<Int>,
) : AbstractNetworkServiceFactory() {

    override fun getExceptionHandlers(): ExceptionHandlers {
        return ExceptionHandlers(channelExceptionHandler())
    }

    /**
     * @author Kris
     */
    private fun channelExceptionHandler(): ChannelExceptionHandler {
        return ChannelExceptionHandler { ctx: ChannelHandlerContext, cause: Throwable ->
            val channel = ctx.channel()
            if (channel.isOpen) {
                channel.close()
            }

            // IOExceptions basically all kinds of "connection dropped" type errors,
            // e.g. Closing the client, losing connection and so on...
            if (cause is IOException) return@ChannelExceptionHandler
            // Limit the decoder exceptions, so it doesn't just spam the error a thousand times if a thousand connections get rejected
            // Instead allow up to 100 to log, then turn it off. After that, only allow one more to log per tick
            if (cause is DecoderException) {
                return@ChannelExceptionHandler
            }

            logger.error{"Exception in netty handlers: $cause."}
        }
    }

    override fun getJs5GroupProvider(): Js5GroupProvider {
        return groupProvider
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
