package com.plusmobileapps.chefmate.recipe.importer.impl

/** A single decompressed file entry from a zip archive. */
internal class ZipEntry(val name: String, val bytes: ByteArray)

/**
 * Minimal, dependency-free zip reader. Walks the End Of Central Directory record to enumerate
 * entries, then inflates each one via the platform [inflateRaw]. Only the two compression methods
 * that recipe-export archives use are supported: stored (0) and deflate (8). ZIP64 is not handled —
 * recipe archives are small enough to stay within the 32-bit size fields.
 */
internal object ZipReader {

    private const val EOCD_SIGNATURE = 0x06054b50
    private const val CENTRAL_DIR_SIGNATURE = 0x02014b50
    private const val METHOD_STORED = 0
    private const val METHOD_DEFLATE = 8

    fun read(archive: ByteArray): List<ZipEntry> {
        val eocdOffset = findEocd(archive) ?: error("Not a valid zip archive (no EOCD record)")
        val centralDirOffset = archive.readIntLE(eocdOffset + 16)
        val totalEntries = archive.readShortLE(eocdOffset + 10)

        val entries = ArrayList<ZipEntry>(totalEntries)
        var offset = centralDirOffset
        repeat(totalEntries) {
            if (archive.readIntLE(offset) != CENTRAL_DIR_SIGNATURE) return@repeat
            val method = archive.readShortLE(offset + 10)
            val compressedSize = archive.readIntLE(offset + 20)
            val uncompressedSize = archive.readIntLE(offset + 24)
            val nameLength = archive.readShortLE(offset + 28)
            val extraLength = archive.readShortLE(offset + 30)
            val commentLength = archive.readShortLE(offset + 32)
            val localHeaderOffset = archive.readIntLE(offset + 42)
            val name = archive.decodeToString(offset + 46, offset + 46 + nameLength)

            if (!name.endsWith("/")) {
                val data =
                    readEntryData(
                        archive,
                        localHeaderOffset,
                        method,
                        compressedSize,
                        uncompressedSize,
                    )
                if (data != null) entries.add(ZipEntry(name, data))
            }

            offset += 46 + nameLength + extraLength + commentLength
        }
        return entries
    }

    private fun readEntryData(
        archive: ByteArray,
        localHeaderOffset: Int,
        method: Int,
        compressedSize: Int,
        uncompressedSize: Int,
    ): ByteArray? {
        // Local-header name/extra lengths can differ from the central-directory copies, so read
        // them
        // from the local header to locate the start of the file data.
        val localNameLength = archive.readShortLE(localHeaderOffset + 26)
        val localExtraLength = archive.readShortLE(localHeaderOffset + 28)
        val dataStart = localHeaderOffset + 30 + localNameLength + localExtraLength
        val compressed = archive.copyOfRange(dataStart, dataStart + compressedSize)
        return when (method) {
            METHOD_STORED -> compressed
            METHOD_DEFLATE -> inflateRaw(compressed, uncompressedSize)
            else -> null
        }
    }

    private fun findEocd(archive: ByteArray): Int? {
        // The EOCD record is 22 bytes plus an optional trailing comment, so scan backwards from the
        // end of the archive for the signature.
        val minOffset = maxOf(0, archive.size - 22 - 0xFFFF)
        var offset = archive.size - 22
        while (offset >= minOffset) {
            if (archive.readIntLE(offset) == EOCD_SIGNATURE) return offset
            offset--
        }
        return null
    }

    private fun ByteArray.readShortLE(index: Int): Int =
        (this[index].toInt() and 0xFF) or ((this[index + 1].toInt() and 0xFF) shl 8)

    private fun ByteArray.readIntLE(index: Int): Int =
        (this[index].toInt() and 0xFF) or
            ((this[index + 1].toInt() and 0xFF) shl 8) or
            ((this[index + 2].toInt() and 0xFF) shl 16) or
            ((this[index + 3].toInt() and 0xFF) shl 24)
}

/** Inflates a raw DEFLATE stream (no zlib header) of known [expectedSize]. */
internal expect fun inflateRaw(data: ByteArray, expectedSize: Int): ByteArray
