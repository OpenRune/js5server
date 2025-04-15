package dev.advo.js5

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil

public object Main {
    @OptIn(ExperimentalStdlibApi::class)
    @JvmStatic
    public fun main(args: Array<String>) {
        val path = "C:\\Users\\Home\\Downloads\\cache-oldschool-live-en-b230-2025-04-09-10-45-05-openrs2#2168\\cacheO\\"
        val groupProvider = CacheJs5GroupProvider()
        val groupProviderDs = DispleeJs5GroupRepository()
        Thread.sleep(500)
        val stopwatch = Stopwatch()
        stopwatch.start()
        groupProvider.load(path)
        groupProviderDs.load(path)
        stopwatch.stop()
        println(mapsEqualByContent(groupProvider.groups, groupProviderDs.groups))
        System.err.println("Loaded ${groupProvider.groups.size} JS5 responses in ${stopwatch.elapsedMillis()}ms")
        val port = 43594
        val network2 = NetworkServiceFactory(groupProvider, listOf(port))
        val network = network2.build()

        /*
         * Bind the game port.
         */
        network.start()
    }

    private fun mapsEqualByContent(a: Map<Int, ByteBuf>, b: Map<Int, ByteBuf>): Boolean {
        if (a.size != b.size) return false
        for ((key, bufA) in a) {
            val bufB = b[key] ?: return false
            if (!ByteBufUtil.equals(bufA, bufB)) return false
        }
        return true
    }
}
