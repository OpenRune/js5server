package net.rsprot.protocol.common.loginprot.outgoing.prot

import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.common.loginprot.outgoing.codec.EmptyLoginResponseEncoder
import net.rsprot.protocol.common.loginprot.outgoing.codec.SuccessfulLoginResponseEncoder
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepositoryBuilder

private typealias Encoder<T> = EmptyLoginResponseEncoder<T>

public object LoginMessageEncoderRepository {
    @ExperimentalStdlibApi
    public fun build(): MessageEncoderRepository<LoginServerProt> {
        val protRepository = ProtRepository.of<LoginServerProt>()
        val builder =
            MessageEncoderRepositoryBuilder(
                protRepository,
            ).apply {
                bind(SuccessfulLoginResponseEncoder())
                bind(Encoder<LoginResponse.ClientOutOfDate>(LoginServerProt.CLIENT_OUT_OF_DATE))
                bind(Encoder<LoginResponse.IPLimit>(LoginServerProt.IP_LIMIT))
            }
        return builder.build()
    }
}
