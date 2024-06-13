package org.jire.js5server

enum class StatusResponse(val opcode: Int) {
    SUCCESSFUL(0),
    OUT_OF_DATE(6);
}