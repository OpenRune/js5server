package net.rsprot.protocol.loginprot.outgoing

import net.rsprot.protocol.message.OutgoingLoginMessage

public sealed interface LoginResponse : OutgoingLoginMessage {
    public data class Successful(
        public val sessionId: Long?,
    ) : LoginResponse

    public data object ClientOutOfDate : LoginResponse

    public data object IPLimit : LoginResponse
}
