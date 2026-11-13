package com.example.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.provider.DocumentsContract
import com.example.data.local.LocalChapterEntity
import com.example.data.local.LocalChapterSegmentEntity
import com.example.data.local.LocalCharacterEntity
import com.example.data.local.LocalNovelEntity
import com.example.data.model.DriveSyncLog
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ReportExporter {

    private val dateFmt = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    /**
     * Generates a styled multi-page PDF analytical & manuscript summary report.
     */
    suspend fun buildPdfReportBytes(
        novels: List<LocalNovelEntity>,
        chapters: List<LocalChapterEntity>,
        characters: List<LocalCharacterEntity>,
        users: List<UserProfile>,
        syncLogs: List<DriveSyncLog>
    ): ByteArray = withContext(Dispatchers.Default) {
        val pdf = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val titlePaint = Paint().apply {
            color = Color.rgb(180, 30, 48)
            textSize = 20f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        val headerPaint = Paint().apply {
            color = Color.rgb(25, 28, 38)
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(45, 50, 62)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val mutedPaint = Paint().apply {
            color = Color.rgb(110, 118, 134)
            textSize = 9.5f
        }
        val linePaint = Paint().apply {
            color = Color.rgb(212, 175, 55)
            strokeWidth = 1.5f
        }

        // Page 1: Executive Overview & Novels
        val page1 = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
        var c = page1.canvas
        var y = 52f

        c.drawText("RYUJIN STUDIO — LAPORAN ANALISIS EKSEKUTIF", 40f, y, titlePaint)
        y += 18f
        c.drawText("Dicetak pada: ${dateFmt.format(Date())} | Kontrol Eksklusif Kreator", 40f, y, mutedPaint)
        y += 12f
        c.drawLine(40f, y, pageWidth - 40f, y, linePaint)
        y += 26f

        c.drawText("1. RINGKASAN KATALOG & SKALA KONTEN", 40f, y, headerPaint)
        y += 20f
        val totalWords = chapters.sumOf { it.wordCount.toLong() }
        c.drawText("• Total Novel Terdaftar: ${novels.size}", 48f, y, bodyPaint); y += 16f
        c.drawText("• Total Bab Ryujin: ${chapters.size} Bab (3 Arc Besar)", 48f, y, bodyPaint); y += 16f
        c.drawText("• Estimasi Total Kata: $totalWords kata (~52.500 kata/bab)", 48f, y, bodyPaint); y += 16f
        c.drawText("• Total Karakter Terdokumentasi: ${characters.size} Tokoh Lintas Negara", 48f, y, bodyPaint); y += 16f
        c.drawText("• Pengguna Basis Data Cloud: ${users.size} Akun Terverifikasi", 48f, y, bodyPaint); y += 16f
        c.drawText("• Aktivitas Sinkronisasi Drive: ${syncLogs.size} Log Tercatat", 48f, y, bodyPaint); y += 28f

        c.drawText("2. DISTRIBUSI 3 ARC NOVEL RYUJIN", 40f, y, headerPaint)
        y += 20f
        for (arc in 1..3) {
            val arcChapters = chapters.filter { it.arcNumber == arc }
            val arcWords = arcChapters.sumOf { it.wordCount.toLong() }
            val arcTitle = arcChapters.firstOrNull()?.arcTitle ?: "ARC $arc"
            c.drawText("$arcTitle — ${arcChapters.size} Bab • $arcWords kata", 48f, y, bodyPaint)
            y += 18f
        }
        y += 16f

        c.drawText("3. SAMPEL BASIS DATA PENGGUNA (EKSKLUSIF KREATOR)", 40f, y, headerPaint)
        y += 20f
        users.take(14).forEach { user ->
            c.drawText(
                "• ${user.displayName} [${user.role.uppercase()}] | Bab ${user.lastChapterRead} | ${user.wordsRead} kata | Drive: ${if (user.driveLinked) "Terhubung" else "Belum"}",
                48f, y, bodyPaint
            )
            y += 15f
        }
        if (users.isEmpty()) {
            c.drawText("• Belum ada akun pembaca tambahan yang tersinkronisasi.", 48f, y, mutedPaint)
        }
        pdf.finishPage(page1)

        // Page 2: Characters & Drive Sync Logs
        val page2 = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create())
        c = page2.canvas
        y = 52f
        c.drawText("4. PROFIL TOKOH UTAMA & LOG SINKRONISASI", 40f, y, headerPaint)
        y += 14f
        c.drawLine(40f, y, pageWidth - 40f, y, linePaint)
        y += 22f

        characters.take(22).forEach { ch ->
            if (y < 600f) {
                c.drawText("${ch.characterNumber}. ${ch.name} (${ch.alias}) — ${ch.country} | ${ch.traits.take(48)}", 44f, y, bodyPaint)
                y += 15f
            }
        }
        y += 14f
        c.drawText("5. RIWAYAT SINKRONISASI GOOGLE DRIVE & EKSPOR", 40f, y, headerPaint)
        y += 18f
        syncLogs.take(10).forEach { log ->
            if (y < pageHeight - 40f) {
                c.drawText("• [${log.syncType}] ${log.fileName} -> ${log.destination} (${log.status})", 44f, y, bodyPaint)
                y += 15f
            }
        }
        pdf.finishPage(page2)

        val out = ByteArrayOutputStream()
        pdf.writeTo(out)
        pdf.close()
        out.toByteArray()
    }

    /**
     * Generates a genuine multi-sheet OOXML Excel (.xlsx) workbook using ZipOutputStream.
     */
    suspend fun buildExcelXlsxBytes(
        novels: List<LocalNovelEntity>,
        chapters: List<LocalChapterEntity>,
        characters: List<LocalCharacterEntity>,
        users: List<UserProfile>
    ): ByteArray = withContext(Dispatchers.Default) {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            fun addEntry(name: String, content: String) {
                zos.putNextEntry(ZipEntry(name))
                zos.write(content.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }

            addEntry(
                "[Content_Types].xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                  <Default Extension="xml" ContentType="application/xml"/>
                  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
                  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                  <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                  <Override PartName="/xl/worksheets/sheet3.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                  <Override PartName="/xl/worksheets/sheet4.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                </Types>""".trimIndent()
            )

            addEntry(
                "_rels/.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                </Relationships>""".trimIndent()
            )

            addEntry(
                "xl/_rels/workbook.xml.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
                  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet3.xml"/>
                  <Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet4.xml"/>
                  <Relationship Id="rId5" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
                </Relationships>""".trimIndent()
            )

            addEntry(
                "xl/workbook.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                  <sheets>
                    <sheet name="Ringkasan_Novel" sheetId="1" r:id="rId1"/>
                    <sheet name="Daftar_Bab_900" sheetId="2" r:id="rId2"/>
                    <sheet name="Karakter_400" sheetId="3" r:id="rId3"/>
                    <sheet name="Basis_Data_Pengguna" sheetId="4" r:id="rId4"/>
                  </sheets>
                </workbook>""".trimIndent()
            )

            addEntry(
                "xl/styles.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                  <fonts count="1"><font><sz val="11"/><name val="Calibri"/></font></fonts>
                  <fills count="1"><fill><patternFill patternType="none"/></fill></fills>
                  <borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
                  <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
                  <cellXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/></cellXfs>
                </styleSheet>""".trimIndent()
            )

            val sheet1Rows = buildList {
                add(listOf("ID Novel", "Judul", "Penulis", "Genre", "Total Bab", "Total Arc", "Kata/Bab"))
                novels.forEach { n ->
                    add(listOf(n.id, n.title, n.authorName, n.genre, n.totalChapters.toString(), n.totalArcs.toString(), n.wordsPerChapter.toString()))
                }
            }
            addEntry("xl/worksheets/sheet1.xml", buildSheetXml(sheet1Rows))

            val sheet2Rows = buildList {
                add(listOf("Bab Global", "Arc", "Bab Arc", "Judul Bab", "Jumlah Kata", "Tokoh Terlibat"))
                chapters.forEach { ch ->
                    add(listOf(ch.chapterNumber.toString(), ch.arcNumber.toString(), ch.arcChapterNumber.toString(), ch.title, ch.wordCount.toString(), ch.featuredCharacters))
                }
            }
            addEntry("xl/worksheets/sheet2.xml", buildSheetXml(sheet2Rows))

            val sheet3Rows = buildList {
                add(listOf("No", "Nama", "Alias", "Negara", "Faksi", "Sifat", "Kemunculan Arc"))
                characters.forEach { ch ->
                    add(listOf(ch.characterNumber.toString(), ch.name, ch.alias, ch.country, ch.faction, ch.traits, ch.arcPresence))
                }
            }
            addEntry("xl/worksheets/sheet3.xml", buildSheetXml(sheet3Rows))

            val sheet4Rows = buildList {
                add(listOf("UID", "Nama", "Email", "Peran", "Bab Terakhir", "Kata Dibaca", "Drive Terhubung"))
                users.forEach { u ->
                    add(listOf(u.userId, u.displayName, u.email, u.role, u.lastChapterRead.toString(), u.wordsRead.toString(), if (u.driveLinked) "Ya" else "Tidak"))
                }
            }
            addEntry("xl/worksheets/sheet4.xml", buildSheetXml(sheet4Rows))
        }
        baos.toByteArray()
    }

    suspend fun buildChapterManuscriptText(
        novel: LocalNovelEntity,
        chapter: LocalChapterEntity,
        segments: List<LocalChapterSegmentEntity>
    ): ByteArray = withContext(Dispatchers.Default) {
        val sb = StringBuilder()
        sb.append("==========================================================\n")
        sb.append("${novel.title.uppercase()} — ${chapter.arcTitle}\n")
        sb.append("Bab ${chapter.chapterNumber} (Arc Bab ${chapter.arcChapterNumber}): ${chapter.title}\n")
        sb.append("Penulis: ${novel.authorName}\n")
        sb.append("Total Kata: ${chapter.wordCount} kata\n")
        sb.append("==========================================================\n\n")
        segments.forEach { seg ->
            sb.append("### ${seg.sceneTitle}\n\n")
            sb.append(seg.content).append("\n\n")
        }
        sb.toString().toByteArray(Charsets.UTF_8)
    }

    suspend fun writeBytesToUri(context: Context, uri: Uri, bytes: ByteArray): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out: OutputStream ->
                    out.write(bytes)
                    out.flush()
                } ?: return@withContext Result.failure(IllegalStateException("Gagal membuka stream file"))
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Auto-syncs a file directly into the creator's connected Google Drive (or SAF) folder Tree URI.
     */
    suspend fun saveFileToConnectedDriveTree(
        context: Context,
        treeUriString: String,
        mimeType: String,
        fileName: String,
        bytes: ByteArray
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val treeUri = Uri.parse(treeUriString)
            val docId = DocumentsContract.getTreeDocumentId(treeUri)
            val dirUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
            val createdUri = DocumentsContract.createDocument(
                context.contentResolver,
                dirUri,
                mimeType,
                fileName
            ) ?: return@withContext Result.failure(IllegalStateException("Tidak dapat membuat file di folder Drive"))
            context.contentResolver.openOutputStream(createdUri)?.use { out ->
                out.write(bytes)
                out.flush()
            } ?: return@withContext Result.failure(IllegalStateException("OutputStream Drive null"))
            Result.success(createdUri.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildSheetXml(rows: List<List<String>>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")
        rows.forEachIndexed { rowIdx, cols ->
            val r = rowIdx + 1
            sb.append("<row r=\"").append(r).append("\">")
            cols.forEachIndexed { colIdx, value ->
                val ref = colLetter(colIdx) + r
                sb.append("<c r=\"").append(ref).append("\" t=\"inlineStr\"><is><t>")
                    .append(escapeXml(value))
                    .append("</t></is></c>")
            }
            sb.append("</row>")
        }
        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun colLetter(index: Int): String {
        var i = index
        val sb = StringBuilder()
        while (i >= 0) {
            sb.insert(0, ('A' + (i % 26)))
            i = (i / 26) - 1
        }
        return sb.toString()
    }

    private fun escapeXml(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                '&' -> sb.append("&amp;")
                '<' -> sb.append("&lt;")
                '>' -> sb.append("&gt;")
                '"' -> sb.append("&quot;")
                '\'' -> sb.append("&apos;")
                else -> if (ch.code >= 0x20 || ch == '\t' || ch == '\n' || ch == '\r') sb.append(ch)
            }
        }
        return sb.toString()
    }
}
