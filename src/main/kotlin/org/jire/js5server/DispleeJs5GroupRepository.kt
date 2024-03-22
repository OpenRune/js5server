package org.jire.js5server

import com.displee.cache.CacheLibrary
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.util.ReferenceCounted
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.jire.js5server.Js5GroupRepository.Companion.bitpack
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.math.min
import kotlin.math.pow

class DispleeJs5GroupRepository : Js5GroupRepository {

    private val map: Int2ObjectMap<ByteBuf> = Int2ObjectOpenHashMap(2.toDouble().pow(17).toInt())

    override fun get(bitpack: Int): ByteBuf? = map[bitpack]

    override fun load(path: Path) {
        val cache = CacheLibrary(path.toString())
         val versionTable = cache.generateUkeys(false)
            encodeMasterIndex(versionTable)

        for(test in cache.indices()) {
            encodeArchive(cache, test.id)
        }
        encodeArchiveMasterIndex(cache, 255)

        logger.info("Loaded {} JS5 responses", map.size)
    }

    private fun encodeMasterIndex(masterIndex: ByteArray) {
        Unpooled.directBuffer().use { uncompressed ->
            uncompressed.writeBytes(masterIndex)

            val data = compress(uncompressed)
            encodeGroup(255, 255, data)
        }
    }

    private fun encodeArchiveMasterIndex(cache: CacheLibrary, index: Int) {
        for (archive in cache.indices()) {
            if (archive.id == 255) continue// this is the prebuilt versiontable.

            Unpooled.directBuffer().use { uncompressed ->

                val data = cache.index255?.readArchiveSector(archive.id)?.data

                uncompressed.writeBytes(data)
                encodeGroup(index, archive.id, uncompressed)
            }
        }
    }

    private fun encodeArchive(cache: CacheLibrary, index: Int) {
        for (archive in cache.index(index).archives()) {
            Unpooled.directBuffer().use { uncompressed ->

                val data = cache.index(index).readArchiveSector(archive.id)?.data

                uncompressed.writeBytes(data)
                strip(uncompressed)
                encodeGroup(index, archive.id, uncompressed)
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
        map[bitpack] = response
    }

    private fun compress(input: ByteBuf): ByteBuf {
        input.alloc().buffer().use { output ->
            output.writeByte(0)
            val len = input.readableBytes()
            output.writeInt(len)
            output.writeBytes(input)
            return output.retain()
        }
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

        private val logger: Logger = LoggerFactory.getLogger(Js5GroupRepository::class.java)

    }

}