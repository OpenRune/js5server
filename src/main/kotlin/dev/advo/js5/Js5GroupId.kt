package dev.advo.js5

@JvmInline
value class Js5GroupId(private val packed: Int) {

    val archiveId: Int
        get() = (packed ushr 24) and 0xFF

    val groupId: Int
        get() = packed and 0xFFFFFF

    val bitpacked: Int
        get() = packed

    companion object {
        fun from(archive: Int, group: Int): Js5GroupId {
            require(archive and 0xFF.inv() == 0) { "Invalid archive: $archive" }
            require(group and 0xFFFFFF.inv() == 0) { "Invalid group: $group" }
            val packed = (archive shl 24) or (group and 0xFFFFFF)
            return Js5GroupId(packed)
        }
    }

    override fun toString(): String = "Js5GroupId(archiveId=$archiveId, groupId=$groupId)"
}