package org.jire.js5server

import io.netty.buffer.ByteBuf
import io.netty.buffer.DefaultByteBufHolder

enum class Js5Type(val opcode: Int) {
    NORMAL_CONTAINER_REQUEST(0),
    URGENT_CONTAINER_REQUEST(1),
    CLIENT_LOGGED_IN(2),
    CLIENT_LOGGED_OUT(3),
    ENCRYPTION_KEY_UPDATE(4);
}

data class Js5ContainerRequest(val isUrgent: Boolean, val indexFileId: Int, val containerId: Int)

class Js5ContainerResponse(
    val indexFileId: Int,
    val containerId: Int,
    val compressionType: Int,
    val compressedSize: Int,
    val data: ByteBuf
) : DefaultByteBufHolder(data)