package org.jire.js5server

import org.jire.js5server.codec.js5.Js5Handler
import java.io.FileInputStream
import java.nio.file.Path
import java.util.*

object Main {

    @JvmStatic
    fun main(args: Array<String>) {


        Js5Server.init("E:\\RSPS\\VarlamoreRSPS\\Varlamore-Server\\data\\cache\\")
    }
}