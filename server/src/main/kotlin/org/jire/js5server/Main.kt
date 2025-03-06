package org.jire.js5server

import org.jire.js5server.codec.js5.Js5Handler
import java.io.FileInputStream
import java.nio.file.Path
import java.util.*

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        val cachePath = args[0]

        val ports = args[1].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val listenPorts = IntArray(ports.size)
        for (i in ports.indices) {
            listenPorts[i] = ports[i].trim { it <= ' ' }.toInt()
        }
        val version = args[2].toInt()
        val supportPrefetch = args[3].toBoolean()

        Js5Server.init(cachePath, listenPorts, version, supportPrefetch)
    }
}