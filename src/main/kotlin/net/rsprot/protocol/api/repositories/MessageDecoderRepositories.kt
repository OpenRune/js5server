package net.rsprot.protocol.api.repositories

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.common.js5.incoming.prot.Js5MessageDecoderRepository
import net.rsprot.protocol.common.loginprot.incoming.codec.InitJs5RemoteConnectionDecoder
import net.rsprot.protocol.common.loginprot.incoming.prot.LoginClientProt
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepositoryBuilder

/**
 * The message decoder repositories for login, JS5 and game, all held in the same place.
 */
@OptIn(ExperimentalStdlibApi::class)
public class MessageDecoderRepositories private constructor(
    public val loginMessageDecoderRepository: MessageDecoderRepository<ClientProt>,
    public val js5MessageDecoderRepository: MessageDecoderRepository<ClientProt>,
) {
    internal companion object {
        fun initialize(): MessageDecoderRepositories {
            return MessageDecoderRepositories(
                build(),
                Js5MessageDecoderRepository.build(),
            )
        }

        @ExperimentalStdlibApi
        public fun build(): MessageDecoderRepository<LoginClientProt> {
            val protRepository = ProtRepository.of<LoginClientProt>()
            val builder =
                MessageDecoderRepositoryBuilder(
                    protRepository,
                ).apply {
                    bind(InitJs5RemoteConnectionDecoder())
                }
            return builder.build()
        }
    }
}
