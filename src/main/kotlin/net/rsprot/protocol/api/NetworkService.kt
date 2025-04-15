package net.rsprot.protocol.api

import com.github.michaelbull.logging.InlineLogger
import dev.advo.js5.Js5Server
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelFuture
import io.netty.channel.EventLoopGroup
import net.rsprot.protocol.api.bootstrap.BootstrapBuilder
import net.rsprot.protocol.api.handlers.ExceptionHandlers
import net.rsprot.protocol.api.handlers.INetAddressHandlers
import net.rsprot.protocol.api.handlers.OutgoingMessageSizeEstimator
import net.rsprot.protocol.api.js5.Js5Configuration
import net.rsprot.protocol.api.js5.Js5GroupProvider
import net.rsprot.protocol.api.js5.Js5Service
import net.rsprot.protocol.api.repositories.MessageDecoderRepositories
import net.rsprot.protocol.api.repositories.MessageEncoderRepositories
import net.rsprot.protocol.api.util.asCompletableFuture
import net.rsprot.protocol.common.RSProtConstants
import net.rsprot.protocol.metrics.NetworkTrafficMonitor
import net.rsprot.protocol.threads.IllegalThreadAccessException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ScheduledFuture
import kotlin.time.measureTime
import net.rsprot.protocol.internal.setCommunicationThread as setInternalCommunicationThread

/**
 * The primary network service implementation that brings all the necessary components together
 * in a single "god" object.
 * @param R the receiver type for the incoming game message consumers, typically a player
 * @property allocator the byte buffer allocator used throughout the library
 * @property host the host to which to bind to, defaulting to null.
 * @property ports the list of ports that the service will connect to
 * @property bootstrapBuilder the bootstrap builder used to configure the socket and Netty
 * @property exceptionHandlers the wrapper object for any exception handlers that the server must provide
 * @property iNetAddressHandlers the wrapper object to handle anything to do with tracking and rejecting
 * network addresses trying to establish connections
 * @param js5Configuration the configuration used by the JS5 service to determine the exact conditions
 * for serving any connected clients
 * @param js5GroupProvider the provider for any JS5 requests that the client makes
 * @property encoderRepositories the encoder repositories for all the connection types
 * @property js5Service the service behind the JS5, serving all connected clients fairly
 * @property js5ServiceExecutor the thread executing the JS5 service. Since the main JS5
 * service is fairly lightweight and doesn't actually process much, a single thread
 * is more than sufficient here. Utilizing more threads makes implementing a fair JS5
 * service significantly more difficult.
 * @property decoderRepositories the repositories for decoding all the incoming client packets
 * @property trafficMonitor a monitor for tracking network traffic, by default a no-op
 * implementation that tracks nothing.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class NetworkService
    internal constructor(
        internal val allocator: ByteBufAllocator,
        internal val host: String?,
        internal val ports: List<Int>,
        internal val bootstrapBuilder: BootstrapBuilder,
        internal val exceptionHandlers: ExceptionHandlers,
        internal val iNetAddressHandlers: INetAddressHandlers,
        public val trafficMonitor: NetworkTrafficMonitor<*>,
        js5Configuration: Js5Configuration,
        js5GroupProvider: Js5GroupProvider,
    ) {
        internal val encoderRepositories: MessageEncoderRepositories = MessageEncoderRepositories()
        internal val js5Service: Js5Service =
            Js5Service(
                this,
                js5Configuration,
                js5GroupProvider,
            )
        private val js5ServiceExecutor = Thread(js5Service)
        internal val decoderRepositories: MessageDecoderRepositories =
            MessageDecoderRepositories.initialize()
        public val messageSizeEstimator: OutgoingMessageSizeEstimator =
            OutgoingMessageSizeEstimator(encoderRepositories)

        private lateinit var bossGroup: EventLoopGroup
        private lateinit var childGroup: EventLoopGroup
        private lateinit var js5PrefetchFuture: ScheduledFuture<*>

        /**
         * Starts the network service by binding the provided ports.
         * If any of them fail, the service is shut down and the exception is propagated forward.
         */
        @ExperimentalUnsignedTypes
        @ExperimentalStdlibApi
        public fun start() {
            val time =
                measureTime {
                    val bootstrap = bootstrapBuilder.build(messageSizeEstimator)
                    val initializer =
                        bootstrap.childHandler(
                            LoginChannelInitializer(this),
                        )
                    this.bossGroup = initializer.config().group()
                    this.childGroup = initializer.config().childGroup()
                    val host = this.host
                    val futures =
                        ports
                            .map { if (host != null) initializer.bind(host, it) else initializer.bind(it) }
                            .map<ChannelFuture, CompletableFuture<Void>>(ChannelFuture::asCompletableFuture)
                    val future =
                        CompletableFuture
                            .allOf(*futures.toTypedArray())
                            .handle { _, exception ->
                                if (exception != null) {
                                    bossGroup.shutdownGracefully()
                                    childGroup.shutdownGracefully()
                                    throw exception
                                }
                            }
                    js5ServiceExecutor.start()
                    js5PrefetchFuture = Js5Service.startPrefetching(js5Service)
                    try {
                        // join it, which will propagate any exceptions
                        future.join()
                    } catch (t: Throwable) {
                        js5Service.triggerShutdown()
                        throw t
                    }
                }
            logger.info { "Started in: $time" }
            logger.info { "Bound to ports: ${ports.joinToString(", ")}" }
            logger.info { "Revision: ${Js5Server.REVISION}" }
        }

        public fun shutdown() {
            logger.info { "Attempting to shut down network service." }
            js5Service.triggerShutdown()
            js5PrefetchFuture.cancel(true)
            bossGroup.shutdownGracefully()
            childGroup.shutdownGracefully()
            logger.info { "Network service successfully shut down." }
        }

        /**
         * Sets the thread which is permitted to communicate with RSProt's thread-unsafe
         * properties. If set to null, all threads are allowed to communicate again.
         * @param thread the thread permitted to communicate with RSProt's thread-unsafe functions.
         * @param warnOnError whether to warn on a thread violation error. If false, am
         * [IllegalThreadAccessException] is thrown instead.
         */
        @JvmOverloads
        public fun setCommunicationThread(
            thread: Thread?,
            warnOnError: Boolean = true,
        ) {
            setInternalCommunicationThread(thread, warnOnError)
        }

        public companion object {
            public const val INITIAL_TIMEOUT_SECONDS: Long = 30
            public const val LOGIN_TIMEOUT_SECONDS: Long = 40
            public const val GAME_TIMEOUT_SECONDS: Long = 15
            public const val JS5_TIMEOUT_SECONDS: Long = 30
            private val logger = InlineLogger()
        }
    }
