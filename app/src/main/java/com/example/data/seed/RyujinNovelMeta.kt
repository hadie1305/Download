package com.example.data.seed

import com.example.data.local.LocalNovelEntity

data class GenreHighlight(
    val id: String,
    val name: String,
    val tagline: String,
    val badge: String
)

data class ArcDefinition(
    val arcNumber: Int,
    val title: String,
    val subtitle: String,
    val rangeLabel: String,
    val synopsis: String
)

object RyujinNovelMeta {
    const val RYUJIN_NOVEL_ID = "ryujin"
    const val CREATOR_EMAIL = "nioyuzi@gmail.com"
    const val AUTHOR_NAME = "Nio Yuzi (Sang Arsitek Naga Hitam)"

    const val BLURB = """Di lorong-lorong dingin SMA Shinjuku, Kenjiro Tatsumoto hanyalah bayangan yang diinjak-injak oleh sang putra mahkota sindikat, Ryusuke Taniki. Di balik senyum manis berpita biru milik Aoi Takashi yang tampak seperti malaikat pelindung, bersembunyi obsesi gelap yang merenggut nyawa kedua orang tua Kenjiro dan memaksanya memalsukan kematiannya sendiri.

Lima tahun ditempa neraka alam liar Hutan Kiso di bawah bimbingan pendekar tua Genzo dan jarum tebori Naga Hitam, Kenjiro bangkit kembali sebagai 'K'—petarung tanpa wajah yang menaklukkan ring bawah tanah Tokyo, Tiga Kembar Tatsuya, Mafia Florentino di Italia, hingga mendirikan faksi global legendaris: RYUJIN.

Dari perang total melawan Sindikat Ryuzaki, pengkhianatan berdarah The Mask of Angels di perairan Vietnam, hingga perjuangan generasi terakhir Saiken dan Ryumei menuju fajar damai—inilah epik 900 bab penuh dialog tanpa batas, strategi dunia bawah, pengorbanan keluarga, dan 400 tokoh lintas negara yang saling bertaut dalam takdir darah."""

    const val PREFACE = """Kata Pengantar Penulis

Salam hangat untuk para pembaca setia Ryujin Studio.

Novel 'Ryujin' lahir dari keinginan saya untuk menghadirkan sebuah kisah drama aksi psikologis berskala kolosal yang disajikan secara utuh melalui kekuatan dialog penuh tanpa batas kata. Setiap bab di dalam 3 Arc besar (900 Bab keseluruhan) dirancang agar Anda tidak sekadar membaca ringkasan peristiwa, melainkan mendengar langsung tarikan napas, bisikan pengkhianatan, benturan fisik, hingga pergolakan batin 400 karakter yang terlibat di dalamnya.

Melalui arsitektur basis data lokal berkecepatan tinggi, seluruh 900 bab yang masing-masing memuat lebih dari 50.000 kata naskah dialog dapat dinavigasi dengan ringan, dicari seketika, dan dipantau pembaruannya secara langsung. Terima kasih telah memasuki dunia Shinjuku, Kiso, Italia, hingga perairan Vietnam bersama Kenjiro, Sakura, Saiken, Ryumei, dan bayang-bayang Aoi Takashi.

Selamat membaca,
Nio Yuzi — Creator & Developer Eksklusif Ryujin Studio"""

    val arcs = listOf(
        ArcDefinition(
            arcNumber = 1,
            title = "ARC 1: RYUJIN NO TANJOU",
            subtitle = "Lahirnya Naga Hitam",
            rangeLabel = "Bab 1 – 300",
            synopsis = "Perundungan di Shinjuku, tragedi malam keluarga Tatsumoto, 5 tahun latihan ekstrem di Hutan Kiso bersama Genzo, kebangkitan 'K', turnamen global Italia, hingga runtuhnya Dinasti Ryuzaki dan berdirinya Ryujin Company."
        ),
        ArcDefinition(
            arcNumber = 2,
            title = "ARC 2: THE MASK OF ANGELS",
            subtitle = "Topeng Sang Malaikat",
            rangeLabel = "Bab 301 – 600 (Arc 2: 1 – 300)",
            synopsis = "Sisi gelap psikopat Aoi Takashi, pengkhianatan berdarah dan pengorbanan terakhir Ryusuke demi melindungi Sakura, terungkapnya rekaman CCTV, perang global Ryujin melawan Syndicate of Angels, hingga pelarian Aoi dan Xie Ping ke pedalaman Vietnam."
        ),
        ArcDefinition(
            arcNumber = 3,
            title = "ARC 3: THE LAST GENERATION",
            subtitle = "Generasi Terakhir & Fajar Damai",
            rangeLabel = "Bab 601 – 900 (Arc 3: 1 – 300)",
            synopsis = "Tumbuhnya Saiken dan Ryumei di tengah bayangan kejayaan Ryujin, kelahiran Ao Ping, penyusupan mematikan Aoi ke Tokyo, perang akhir lintas samudra, gugurnya sang Naga Hitam, hingga pemulihan menuju matahari terbit Tokyo."
        )
    )

    val popularGenres = listOf(
        GenreHighlight("g1", "Dark Action Thriller", "Pertarungan bawah tanah, sindikat, dan strategi taktis.", "45M+ Kata"),
        GenreHighlight("g2", "Psychological Noir", "Manipulasi, topeng malaikat, dan obsesi kelam.", "Arc 2 Fokus"),
        GenreHighlight("g3", "Martial Arts & Mafia", "Tebori Naga Hitam, Hutan Kiso, hingga Arena Italia.", "400 Tokoh"),
        GenreHighlight("g4", "Family & Redemption", "Wasiat masa lalu, penyesalan, dan generasi baru.", "Arc 3 Epik"),
        GenreHighlight("g5", "Full-Dialogue Drama", "Naskah percakapan murni tanpa batas kata per bab.", "50K+/Bab")
    )

    fun createRyujinNovelEntity(): LocalNovelEntity = LocalNovelEntity(
        id = RYUJIN_NOVEL_ID,
        title = "Ryujin",
        authorName = AUTHOR_NAME,
        genre = "Dark Action Thriller • Psychological Noir",
        blurb = BLURB,
        preface = PREFACE,
        totalChapters = 900,
        totalArcs = 3,
        wordsPerChapter = 52500,
        isRyujinDefault = true,
        isPublished = true,
        coverTag = "RYUJIN_900",
        createdAtMillis = 1731500000000L
    )
}
