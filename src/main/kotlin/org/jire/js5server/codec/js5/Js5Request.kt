package org.jire.js5server.codec.js5

import org.jire.js5server.codec.Request

enum class LoginState {
    LOGGED_IN,
    LOGGED_OUT
}


sealed interface Js5Request : Request {

    sealed interface Group : Js5Request {
        val archive: Int
        val group: Int
        val highPriority : Boolean

        data class Request(
            override val archive: Int,
            override val group: Int,
            override val highPriority: Boolean
        ) : Group

    }

    object LoggedIn : Js5Request
    object LoggedOut : Js5Request

    data class Rekey(val key: Int) : Js5Request

    object Connected : Js5Request
    object Disconnected : Js5Request

}