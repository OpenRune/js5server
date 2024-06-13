package org.jire.js5server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.WriteBufferWaterMark
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import org.slf4j.Logger
import java.nio.file.Path

object Js5Server {

    private val logger: Logger = org.slf4j.LoggerFactory.getLogger(Js5Server::class.java)

    fun init(
        cachePath: String,
        listenPort: Int = 43595,
        version: Int = -1,
        supportPrefetch: Boolean = true
    ) {
        init(cachePath, arrayOf(listenPort).toIntArray(), version, supportPrefetch)
    }

    fun init(
        cachePath: String,
        listenPorts: IntArray,
        version: Int = -1,
        supportPrefetch: Boolean = true
    ) {

        val groupRepository = DispleeJs5GroupRepository().apply {
            load(Path.of(cachePath))
        }

        val bossGroup = NioEventLoopGroup()
        val loopGroup = NioEventLoopGroup()
        try {
            val bootstrap = ServerBootstrap().apply {
                group(bossGroup, loopGroup)
                channel(NioServerSocketChannel::class.java)
                childOption(ChannelOption.SO_KEEPALIVE, true)
                childOption(ChannelOption.TCP_NODELAY, true)
                childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark(8192, 131072))
                childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(channel: SocketChannel) {
                        channel.pipeline().addLast(ServiceDecoder::class.qualifiedName, ServiceDecoder())
                        channel.pipeline().addLast(
                            ServiceHandler::class.qualifiedName,
                            ServiceHandler(groupRepository,version)
                        )
                    }
                })
            }
            val port = listenPorts.first()
            val bind = bootstrap.bind(listenPorts.first()).sync().addListener {
                if (it.isSuccess) {
                    logger.info("Server now listening to port $port")
                    if (version == -1) {
                        logger.info("JS5 Revision is -1 Ignoring version check")
                    }
                } else {
                    logger.error("Server failed to connect to port $port",it.cause())
                }
            }
            bind.channel().closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            loopGroup.shutdownGracefully()
            logger.info("Server has now shutdown")
        }
    }
}