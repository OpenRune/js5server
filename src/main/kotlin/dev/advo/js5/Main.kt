package dev.advo.js5

public object Main {
    @OptIn(ExperimentalStdlibApi::class)
    @JvmStatic
    public fun main(args: Array<String>) {
        val path = "data/js5"
        val groupProvider = CacheJs5GroupProvider()
        groupProvider.load(path)
        val port = 43594
        val network2 = NetworkServiceFactory(groupProvider, listOf(port))
        val network = network2.build()

        /*
         * Bind the game port.
         */
        network.start()
    }
}
