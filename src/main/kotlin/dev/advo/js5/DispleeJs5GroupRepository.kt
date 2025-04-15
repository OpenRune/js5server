package dev.advo.js5

import com.displee.cache.CacheLibrary
import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.util.ReferenceCounted
import net.rsprot.protocol.api.js5.Js5GroupProvider
import kotlin.math.min
import kotlin.math.pow

class DispleeJs5GroupRepository : Js5GroupProvider {

    val groups: MutableMap<Int, ByteBuf> = HashMap(2.toDouble().pow(17).toInt())

    override fun provide(
        archive: Int,
        group: Int,
    ): ByteBuf? {
        return groups[bitpack(archive, group)]
    }

    fun load(path: String) {
        val cache = CacheLibrary(path)

        encodeMasterIndex(cache)

        for(test in cache.indices()) {
            encodeArchive(cache, test.id)
        }
        encodeArchiveMasterIndex(cache, 255)

        logger.info("Loaded {} JS5 responses", groups.size)
        cache.close()
    }

    private fun encodeMasterIndex(cache: CacheLibrary) {
        Unpooled.directBuffer().use { output ->
            output.writeByte(0)

            val highestIndex = cache.indices().maxOf { it.id }
            val indices = cache.indices()

            output.writeInt(8 + highestIndex * 8)

            for (id in 0..highestIndex) {
                val index = indices.find { it.id == id }

                if (index != null) {
                    output.writeInt(index.crc).writeInt(index.revision)
                } else {
                    output.writeLong(0)
                }
            }

            encodeGroup(255, 255, output)
        }
    }

    private fun encodeArchiveMasterIndex(cache: CacheLibrary, index: Int) {
        for (archive in cache.indices()) {
            if (archive.id == 255) continue// this is the prebuilt versiontable.
            try {
                val data = cache.store.read(255, archive.id)

                Unpooled.directBuffer().use { uncompressed ->
                    uncompressed.writeBytes(data)
                    encodeGroup(index, archive.id, uncompressed)
                }
            } catch (e: Exception) {
                continue
            }
        }
    }

    private fun encodeArchive(cache: CacheLibrary, index: Int) {
        for (archive in cache.index(index).archives()) {
            try {
                val data = cache.store.read(index, archive.id)
                Unpooled.directBuffer().use { uncompressed ->
                    uncompressed.writeBytes(data)
                    strip(uncompressed)
                    encodeGroup(index, archive.id, uncompressed)
                }
            } catch (e: Exception) {
                continue
            }
        }
    }

    private fun encodeGroup(archive: Int, group: Int, data: ByteBuf) {
        val response = Unpooled.directBuffer()
            .writeByte(archive)
            .writeShort(group)
            .writeByte(data.readUnsignedByte().toInt()) // compression
            .writeBytes(data, min(data.readableBytes(), BYTES_BEFORE_BLOCK))
        while (data.isReadable) {
            response.writeByte(0xFF)
            response.writeBytes(data, min(data.readableBytes(), BYTES_AFTER_BLOCK))
        }

        val bitpack = bitpack(archive, group)
        groups[bitpack] = response
    }

    private fun strip(buf: ByteBuf): Int? {
        return if (buf.readableBytes() >= 2) {
            val index = buf.writerIndex() - 2
            val version = buf.getUnsignedShort(index)
            buf.writerIndex(index)
            version
        } else {
            null
        }
    }

    private inline fun <T : ReferenceCounted?, R> T.use(block: (T) -> R): R {
        try {
            return block(this)
        } finally {
            this?.release()
        }
    }

    private companion object {

        private const val BLOCK_SIZE = 512
        private const val BLOCK_HEADER_SIZE = 1 + 2 + 1
        private const val BLOCK_DELIMITER_SIZE = 1
        private const val BYTES_BEFORE_BLOCK = BLOCK_SIZE - BLOCK_HEADER_SIZE
        private const val BYTES_AFTER_BLOCK = BLOCK_SIZE - BLOCK_DELIMITER_SIZE

        private val logger = InlineLogger()

        fun bitpack(
            archive: Int,
            group: Int,
        ): Int {
            require(archive and 0xFF.inv() == 0) { "invalid archive $archive:$group" }
            require(group and 0xFFFF.inv() == 0) { "invalid group $archive:$group" }

            return ((archive and 0xFF) shl 16) or (group and 0xFFFF)
        }
    }

}