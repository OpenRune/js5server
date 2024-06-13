package org.jire.js5server

enum class ServiceType(val opcode: Int) {
    GAME(14), JS5(15);
}

interface ConnectionRequest

class GameConnectionRequest : ConnectionRequest

class Js5ConnectionRequest(val revision: Int) : ConnectionRequest