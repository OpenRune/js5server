package dev.advo.js5

import dev.openrune.filesystem.Cache
import net.rsprot.protocol.api.NetworkService
import java.nio.file.Path

@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
object Js5Server {

    var REVISION: Int = 229
    lateinit var network: NetworkService

    fun init(cache: Path, revision: Int = 230, port: Int = 43594) {
        REVISION = revision
        run(Cache.load(cache, false), port)
    }

    fun init(cache: Cache, revision: Int = 230, port: Int = 43594) {
        REVISION = revision
        run(cache, port)
    }

    fun run(cache: Cache, port: Int) {
        val groupProvider = CacheJs5GroupProvider().apply {
            load(cache)
        }

        network = NetworkServiceFactory(groupProvider, listOf(port)).build()
        network.start()
    }
}
