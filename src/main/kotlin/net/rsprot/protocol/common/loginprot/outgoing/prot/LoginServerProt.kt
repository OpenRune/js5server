package net.rsprot.protocol.common.loginprot.outgoing.prot

import net.rsprot.protocol.ServerProt

public enum class LoginServerProt(
    override val opcode: Int,
    override val size: Int,
) : ServerProt {
    SUCCESSFUL(LoginServerProtId.SUCCESSFUL, 0),
    CLIENT_OUT_OF_DATE(LoginServerProtId.CLIENT_OUT_OF_DATE, 0),
    IP_LIMIT(LoginServerProtId.IP_LIMIT, 0),
}
