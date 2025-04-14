package net.rsprot.protocol.api.repositories

import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.js5.outgoing.prot.Js5MessageEncoderRepository
import net.rsprot.protocol.common.loginprot.outgoing.prot.LoginMessageEncoderRepository
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository

/**
 * The message encoder repository for all outgoing messages, for JS5, login and game.
 */
@OptIn(ExperimentalStdlibApi::class)
public class MessageEncoderRepositories private constructor(
    public val loginMessageEncoderRepository: MessageEncoderRepository<ServerProt>,
    public val js5MessageEncoderRepository: MessageEncoderRepository<ServerProt>,
) {
    public constructor() : this(
        LoginMessageEncoderRepository.build(),
        Js5MessageEncoderRepository.build(),
    )
}
