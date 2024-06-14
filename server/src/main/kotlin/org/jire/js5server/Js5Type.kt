package org.jire.js5server

enum class Js5Type(val opcode: Int) {
    NORMAL_CONTAINER_REQUEST(0),
    URGENT_CONTAINER_REQUEST(1),
    CLIENT_LOGGED_IN(2),
    CLIENT_LOGGED_OUT(3),
    ENCRYPTION_KEY_UPDATE(4),
    CONNECTED(5),
    DISCONNECTED(6);
}