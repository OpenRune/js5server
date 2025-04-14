package net.rsprot.protocol.api

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.PooledByteBufAllocator
import net.rsprot.protocol.api.bootstrap.BootstrapBuilder
import net.rsprot.protocol.api.handlers.ExceptionHandlers
import net.rsprot.protocol.api.handlers.INetAddressHandlers
import net.rsprot.protocol.api.js5.Js5Configuration
import net.rsprot.protocol.api.js5.Js5DisconnectionReason
import net.rsprot.protocol.api.js5.Js5GroupProvider
import net.rsprot.protocol.api.login.LoginDisconnectionReason
import net.rsprot.protocol.common.js5.incoming.prot.Js5ClientProt
import net.rsprot.protocol.common.js5.outgoing.prot.Js5ServerProt
import net.rsprot.protocol.common.loginprot.incoming.prot.LoginClientProt
import net.rsprot.protocol.common.loginprot.outgoing.prot.LoginServerProt
import net.rsprot.protocol.metrics.NetworkTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.ConcurrentChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.GameChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.Js5ChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.LoginChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.NoopChannelTrafficMonitor
import net.rsprot.protocol.metrics.impl.ConcurrentNetworkTrafficMonitor
import net.rsprot.protocol.metrics.impl.NoopNetworkTrafficMonitor
import net.rsprot.protocol.metrics.lock.TrafficMonitorLock

/**
 * The abstract network service factory is used to build the network service that is used
 * as the entry point to this library, allowing one to bind the network and supply everything
 * necessary network-wise from one spot.
 */
@Suppress("MemberVisibilityCanBePrivate")
@ExperimentalUnsignedTypes
public abstract class AbstractNetworkServiceFactory {
    /**
     * The allocator that will be used for everything in this networking library.
     * This will primarily be passed onto NPC and Player info objects, which will utilize
     * this to precompute extended info blocks and the overall main buffer.
     * It is HIGHLY recommended to use a pooled direct byte buffer if possible.
     * Pooling in particular allows us to avoid allocating new expensive 40kb buffers
     * per each player, for both player and npc infos, and direct buffers allow
     * the Netty layer to skip one copy operation to move the data off of the heap.
     */
    public open val allocator: ByteBufAllocator
        get() = PooledByteBufAllocator.DEFAULT

    /**
     * The host to which to bind to, defaulting to null.
     */
    public open val host: String?
        get() = null

    /**
     * The list of ports to listen to. Typically, the server should listen to ports
     * 43594 and 443 in this section, with the 43594 port being the primary one,
     * and 443 being the fallback. 443 is additionally used for HTTP requests, should
     * those be supported.
     */
    public abstract val ports: List<Int>

    /**
     * Gets the bootstrap factory builder to register the network service.
     * The bootstrap builder offers the initial socket and Netty configurations
     * to be used within this library. These configurations are by default
     * made to mirror the client as much as possible.
     */
    public open fun getBootstrapBuilder(): BootstrapBuilder = BootstrapBuilder()

    /**
     * Gets the JS5 configuration settings to be used within the JS5 service.
     * These settings allow a server to modify the frequency at which
     * groups are served to the client, as well as the ratio between
     * high priority logged in players and the low priority logged out ones,
     * allowing the JS5 protocol to send more data to those logged in
     * Furthermore, this allows defining the block size that is written
     * per client per iteration.
     * The default configuration is set to be fast enough to not show any
     * client speed reduction via localhost.
     */
    public open fun getJs5Configuration(): Js5Configuration = Js5Configuration()

    /**
     * Gets the JS5 group provider, used to return the respective byte buffers
     * or file regions from the server based on the incoming request.
     * It is fine to use lazy-loading for development, but it is highly
     * recommended to pre-compute the JS5 groups in the final form when
     * used in development, to avoid instant no-delay responses.
     */
    public abstract fun getJs5GroupProvider(): Js5GroupProvider

    /**
     * Gets the exception handlers for channel exceptions as well as any incoming
     * game message consumers that get processed in the library.
     * Further implementations may be introduced in the future if the need arises.
     */
    public open fun getExceptionHandlers(): ExceptionHandlers =
        ExceptionHandlers(
            { ctx, cause ->
                if (ctx.channel().isActive) {
                    ctx.close()
                }
                logger.error(cause) {
                    "Exception in channel ${ctx.channel()}"
                }
            },
        )

    /**
     * Gets the handlers for anything related to INetAddresses.
     * The default implementation will keep track of the number of concurrent
     * game connections and JS5 connections separately.
     * There is furthermore a validation implementation that will by default
     * reject any connection to either service if there are 10 concurrent connections
     * to that service from the same address already.
     * The initial check is performed after the login connection, when either
     * the game login or the JS5 connection is established. For game logins
     * a secondary validation is performed right before the login block
     * is passed onto the server to handle.
     * It should be noted that the tracking mechanism is fairly straightforward
     * and doesn't cost much in performance.
     */
    public open fun getINetAddressHandlers(): INetAddressHandlers = INetAddressHandlers()

    /**
     * Gets the network traffic monitor which is responsible for tracking any incoming
     * and outgoing packets, connections and more.
     * The default is a [NoopNetworkTrafficMonitor] which does not store or track anything,
     * but a [net.rsprot.protocol.metrics.impl.ConcurrentNetworkTrafficMonitor] can be used
     * to enable tracking. Custom implementations are additionally also possible.
     */
    public open fun getNetworkTrafficMonitor(): NetworkTrafficMonitor<*> = NoopNetworkTrafficMonitor

    /**
     * Builds a network service through this factoring, using all
     * the information provided in here.
     */
    public fun build(): NetworkService {
        val allocator = this.allocator
        val host = this.host
        val ports = this.ports

        return NetworkService(
            allocator,
            host,
            ports,
            getBootstrapBuilder(),
            getExceptionHandlers(),
            getINetAddressHandlers(),
            getNetworkTrafficMonitor(),
            getJs5Configuration(),
            getJs5GroupProvider(),
        )
    }

    public fun buildNetworkTrafficMonitor(): ConcurrentNetworkTrafficMonitor<Any> {
        val loginClientProts = enumValues<LoginClientProt>()
        val loginServerProts = enumValues<LoginServerProt>()
        val loginDisconnectionReasons = enumValues<LoginDisconnectionReason>()
        val js5ClientProts = enumValues<Js5ClientProt>()
        val js5ServerProts = enumValues<Js5ServerProt>()
        val js5DisconnectionReasons = enumValues<Js5DisconnectionReason>()

        val lock = TrafficMonitorLock()
        val loginChannelTrafficHandler =
            LoginChannelTrafficMonitor(
                ConcurrentChannelTrafficMonitor(
                    lock,
                    loginClientProts,
                    loginServerProts,
                    loginDisconnectionReasons,
                ),
            )
        val js5ChannelTrafficHandler =
            Js5ChannelTrafficMonitor(
                ConcurrentChannelTrafficMonitor(
                    lock,
                    js5ClientProts,
                    js5ServerProts,
                    js5DisconnectionReasons,
                ),
            )
        val gameChannelTrafficHandler =
            GameChannelTrafficMonitor(
                NoopChannelTrafficMonitor,
            )
        return ConcurrentNetworkTrafficMonitor(
            lock,
            loginChannelTrafficHandler,
            js5ChannelTrafficHandler,
            gameChannelTrafficHandler,
        )
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
