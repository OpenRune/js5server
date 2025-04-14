package net.rsprot.protocol.common.loginprot.incoming.prot

import net.rsprot.protocol.ClientProt

public enum class LoginClientProt(
    override val opcode: Int,
    override val size: Int,
) : ClientProt {
    INIT_JS5REMOTE_CONNECTION(LoginClientProtId.INIT_JS5REMOTE_CONNECTION, 20),
}
