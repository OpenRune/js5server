package org.jire.js5server

import org.jire.js5server.codec.js5.Js5Handler
import java.nio.file.Path

object Js5Server {

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

        val service = Js5Service(version, groupRepository)

        for (port in listenPorts) {
            service.listen(port)
        }

        if (supportPrefetch) {
            Js5Handler.startPrefetching()
        }
    }
}