package net.rsprot.protocol.js5.incoming

import dev.advo.js5.Js5GroupId
import net.rsprot.protocol.message.IncomingJs5Message

public class PrefetchRequest(
    private val _js5GroupId: Js5GroupId,
) : IncomingJs5Message,
    Js5GroupRequest {

    constructor(archive: UByte, group: UShort) : this(Js5GroupId.from(archive.toInt(), group.toInt()))

    override val request: Js5GroupId
        get() = _js5GroupId

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PrefetchRequest) return false

        return _js5GroupId == other._js5GroupId
    }

    override fun hashCode(): Int {
        return _js5GroupId.bitpacked
    }

    override fun toString(): String =
        "PrefetchRequest(" +
            "archiveId=${_js5GroupId.archiveId}, " +
            "groupId=${_js5GroupId.groupId}" +
            ")"
}
