package net.rsprot.protocol.api.handlers

import net.rsprot.protocol.api.ChannelExceptionHandler

/**
 * A wrapper class for all the exception handlers necessary to make this library function safely.
 * @property channelExceptionHandler the exception handler for any exceptions caught by netty handlers
 * via any message consumers, in order to allow the message processing to take place safely without
 * the server needing to wrap each payload with its own exception handler
 */
public class ExceptionHandlers
    public constructor(
        public val channelExceptionHandler: ChannelExceptionHandler,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ExceptionHandlers

            if (channelExceptionHandler != other.channelExceptionHandler) return false

            return true
        }

        override fun hashCode(): Int {
            var result = channelExceptionHandler.hashCode()
            return result
        }

        override fun toString(): String =
            "ExceptionHandlers(" +
                "channelExceptionHandler=$channelExceptionHandler, " +
                ")"
    }
