package net.rsprot.protocol.common.js5.incoming.codec

import dev.advo.js5.Js5GroupId
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.common.js5.incoming.prot.Js5ClientProt
import net.rsprot.protocol.js5.incoming.UrgentRequest
import net.rsprot.protocol.message.codec.MessageDecoder

public class UrgentRequestExtDecoder : MessageDecoder<UrgentRequest> {
    override val prot: ClientProt = Js5ClientProt.URGENT_REQUEST_EXT

    override fun decode(buffer: JagByteBuf): UrgentRequest {
        val archiveId = buffer.g1()
        val groupId = buffer.g3()
        return UrgentRequest(
            Js5GroupId.from(
                archiveId,
                groupId
            )
        )
    }
}
