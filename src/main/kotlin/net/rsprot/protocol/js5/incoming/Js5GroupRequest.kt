package net.rsprot.protocol.js5.incoming

import dev.advo.js5.Js5GroupId

public sealed interface Js5GroupRequest {
    public val request: Js5GroupId
}
