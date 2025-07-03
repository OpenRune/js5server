package net.rsprot.protocol.common.js5.incoming.codec

import dev.advo.js5.Js5GroupId
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.common.js5.incoming.prot.Js5ClientProt
import net.rsprot.protocol.js5.incoming.PrefetchRequest
import net.rsprot.protocol.message.codec.MessageDecoder

public class PrefetchRequestExtDecoder : MessageDecoder<PrefetchRequest> {
    override val prot: ClientProt = Js5ClientProt.PREFETCH_REQUEST_EXT

    override fun decode(buffer: JagByteBuf): PrefetchRequest {
        val archiveId = buffer.g1()
        val groupId = buffer.g3()
        return PrefetchRequest(
            Js5GroupId.from(
                archiveId,
                groupId
            )
        )
    }
}
