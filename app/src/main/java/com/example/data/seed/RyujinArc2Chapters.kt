package com.example.data.seed

object RyujinArc2Chapters {
    private val explicitFirst40 = listOf(
        "Masa Kecil yang Dingin dan Lahirnya Jiwa Psikopat",
        "Kepindahan Sekolah dan Pertemuan Pertama dengan Kenjiro",
        "Senyum Manis yang Memikat Hati Ryusuke Taniki",
        "Persahabatan Erat Kenjiro dan Ryusuke di Tengah Ketegangan",
        "Benih Obsesi: Ryusuke yang Cinta Mati pada Aoi",
        "Kecemburuan Buta Ryusuke pada Kenjiro",
        "Awal Mula Perundungan Kenjiro atas Permainan Aoi",
        "Rencana Tersembunyi Aoi Memanfaatkan Ryusuke",
        "Pengetahuan Aoi tentang Hubungan Ryuzaki dan Ayah Kenjiro",
        "Keputusan Aoi Memutus Rantai Pasokan Sindikat Ryuzaki",
        "Menyusupkan Takeshi Takahashi ke Lingkar Dalam",
        "Undangan Makan Malam di Rumah Keluarga Kenjiro",
        "Aoi Menginjakkan Kaki di Rumah Tatsumoto",
        "Kemarahan Posesif: Aoi Menganggap Kenjiro Miliknya Sepenuhnya",
        "Keputusan Berdarah: Merencanakan Pembunuhan dengan Tangan Sendiri",
        "Eksekusi Senyap di Malam Kelam Rumah Tatsumoto",
        "Pembantaian Keji Tanpa Ampun oleh Sang Psikopat Murni",
        "Keterlambatan Pasukan Ryuzaki Menyelamatkan Keadaan",
        "Kesalahpahaman Besar dan Kambing Hitam yang Diciptakan",
        "Senyum Kemenangan Aoi di Atas Puing Tragedi",
        "Kabar Mengejutkan: Kenjiro Dinyatakan Telah Mati",
        "Rasa Bersalah Semu yang Menyusup di Hati Aoi",
        "Amukan Darah: Kemarahan Membabi Buta Aoi pada Ayah dan Ibu Sendiri",
        "Pembantaian Keluarga Takahashi: Kecuali Takeshi sang Pelampiasan",
        "Kamar Sunyi dan Sisi Gelap: Sisi Psikopat Aoi",
        "Topeng Poster Kenjiro dan Sentuhan Halusinasi di Malam Hari",
        "Bisikan Lembut: 'Sayang, Ayo Lagi...' di Kegelapan Kamar",
        "Titik Kebosanan Melanda Jiwa Kosong Aoi",
        "Keluar dari Sangkar dan Menemukan Kenjiro Masih Hidup",
        "Kenyataan Pahit: Kenjiro Telah Memiliki Istri Bernama Sakura",
        "Niat Jahat Tumbuh Kembali: Rencana Pembunuhan Kedua",
        "Mengintai Kediaman Kenjiro di Saat Sang Suami Pergi",
        "Serangan Senyap Aoi dan Takeshi ke Rumah Sakura",
        "Kehadiran Tak Terduga Ryusuke yang Mencoba Melindungi",
        "Pertarungan Sengit Antara Ryusuke dan Aoi di Dalam Rumah",
        "Pengorbanan Terakhir Ryusuke Menahan Bilah Pisau Demi Sakura",
        "Kegagalan Total Misi Pembunuhan Akibat Perlawanan Ryusuke",
        "Jejak yang Tertinggal: Hardisk Bukti Kejahatan yang Tak Sengaja Jatuh",
        "Pelarian Cepat Aoi dan Takeshi dari Tempat Kejadian",
        "Menyadari Bahwa Bukti Vital Telah Hilang di Lokasi"
    )

    private val mid109 = (41..149).map { idx ->
        when ((idx - 41) % 10) {
            0 -> "Jejak Manipulasi Aoi di Lingkaran Syndicate of Angels (Tahap ${idx - 40})"
            1 -> "Penggalangan Pasukan Bayaran Lintas Negara oleh Aoi Takashi"
            2 -> "Takeshi Takahashi dalam Bayang-Bayang Pengendalian Psikologis"
            3 -> "Retakan Pikiran Sang Malaikat di Tengah Malam Sepi"
            4 -> "Jaringan Perdagangan Gelap dan Pendanaan Syndicate of Angels"
            5 -> "Analisis Aoi Terhadap Gerak-Gerik Kenjiro dan Sakura"
            6 -> "Eksekusi Senyap Para Pengkhianat di Markas Bawah Tanah"
            7 -> "Latihan Kejam Pasukan Elit Syndicate of Angels"
            8 -> "Rencana Pengepungan Jalur Distribusi Ryujin Company"
            else -> "Detik-Detik Menjelang Sobeknya Topeng Malaikat Aoi"
        }
    }

    private val explicit150to169 = listOf(
        "Puncak Kesadaran Aoi Bahwa Topeng Malaikatnya Telah Sobek Total",
        "Pertemuan Tak Sengaja Kenjiro dan Aoi di Kafe Sudut Kota",
        "Aoi Berpura-pura Manis dan Memperkenalkan Diri di Hadapan Sakura",
        "Kepulangan Kenjiro dengan Tanda Tanya Besar di Kepala",
        "Kesibukan Kenjiro Mencari Bukti dan Melacak Dalang Sebenarnya",
        "Kebuntuan Penyelidikan di Tengah Jaringan Sindikat",
        "Dering Telepon Darurat dari Sakura di Tengah Malam",
        "Kenjiro Bergegas Pulang Menemui Istrinya",
        "Pemandangan Mengerikan: Ryusuke Sekarat Bersimbah Darah",
        "Pertarungan Terakhir Ryusuke Mempertahankan Hardisk/Flashdisk di Tangan Aoi",
        "Hembusan Napas Terakhir Ryusuke di Samping Hardisk Bukti",
        "Tangisan Histeris Sang Bibi Pecah di Samping Jenazah Ryusuke",
        "Proses Pemakaman Khidmat dan Air Mata Penyesalan",
        "Perintah Kenjiro untuk Memeriksa Rekaman CCTV Tersembunyi di Gelas Rumah",
        "Layar Menyala: Terungkapnya Wajah Asli Aoi sebagai Dalang Segala Tragedi",
        "Kemarahan Dingin Sang Naga Hitam: Perburuan Total Dimulai",
        "Nasib Takeshi Takahashi: Memilih Pulang Usai Merasa Kalah di Arc 1",
        "Kegagalan Semalam Membuat Aoi Mengibarkan Bendera Perang Terbuka",
        "Persiapan Pasukan Penuh dari Pihak Ryujin dan Syndicate of Angels",
        "Mobilisasi Pasukan Global Menuju Titik Bentrok Akhir"
    )

    private val war170to250 = (170..250).map { idx ->
        val phase = idx - 169
        when (phase % 9) {
            1 -> "Perang Global Ryujin vs Syndicate of Angels: Front Eropa #$phase"
            2 -> "Serangan Kilat Ren dan Ten Menghancurkan Gudang Senjata Syndicate"
            3 -> "Mey Tatsuya dan Hinata Kawasaki Memutus Jalur Sandi Musuh"
            4 -> "Masashi Miya Memimpin Armada Sektor Pasifik Menggempur Pelabuhan"
            5 -> "Serangan Balik Brutal Komandan Bayaran Syndicate of Angels"
            6 -> "K Memimpin Barisan Depan Merebut Kembali Zona Merah"
            7 -> "Duel Jarak Dekat Para Petinggi Divisi di Medan Kota Pelabuhan"
            8 -> "Keruntuhan Basis Pertahanan Laut Syndicate of Angels"
            else -> "Pengepungan Akhir Menuju Garis Pantai Pelarian Aoi"
        }
    }

    private val explicit251to269 = listOf(
        "Pertempuran Puncak Berpindah ke Garis Pantai dan Pelabuhan Internasional",
        "Pengepungan Kapal Induk Militer Tempat Aoi Melarikan Diri",
        "Kenjiro Menembus Pertahanan Pasukan Bayaran di Atas Dek Kapal",
        "Pertemuan Tatap Muka Terakhir Antara Kenjiro dan Aoi",
        "Tawa Nyaring Sang Psikopat di Tengah Dentuman Meriam Militer",
        "Kehadiran Xie Ping, Pemimpin Kelompok Vietnam yang Melindungi Aoi",
        "Pertempuran Brutal di Geladak Kapal Perang yang Membara",
        "Aoi Berhasil Melarikan Diri ke Kapal Militer Perbatasan Bersama Xie Ping",
        "Kenjiro Mengejar Tanpa Ragu hingga ke Ujung Haluan Kapal",
        "Detik-Detik Kritis di Tengah Lautan Lepas",
        "Aksi Nekat: Terjun Bebas ke Laut Lepas Tanpa Kepastian Hidup atau Mati",
        "Ombak Besar Menelan Sosok di Dalam Gelapnya Samudra",
        "Ketenangan Semu di Sisa Puing Perang Global",
        "Pasukan Ryujin Menyatakan Kemenangan Mutlak di Daratan",
        "Nasib Pelarian Aoi: Menetap Bertahun-tahun di Pedalaman Vietnam",
        "Hubungan Aoi dan Xie Ping serta Lahirnya Anak Mereka: Ao Ping",
        "Sisi Lain Aoi yang Mengasuh Ao Ping dengan Jiwa Psikopatnya",
        "Misteri Keberadaan Kenjiro yang Belum Ditemukan di Dasar Laut",
        "Pencarian Tanpa Henti oleh Pasukan Setia Ryujin"
    )

    private val shadow270to299 = (270..299).map { idx ->
        val yearStep = idx - 269
        when (yearStep % 6) {
            1 -> "Bayangan Masa Depan: Pertumbuhan Awal Ao Ping di Benteng Vietnam (#$yearStep)"
            2 -> "Kabar Tersembunyi Mengenai Penyelamatan Kenjiro di Pesisir Selatan"
            3 -> "Xie Ping Memperkuat Armada Perbatasan Menghadapi Ryujin"
            4 -> "Sakura dan Saiken Menanti Kembalinya Sang Naga Hitam di Tokyo"
            5 -> "Bisikan Obsesi Aoi yang Menanamkan Kebencian pada Ao Ping"
            else -> "Konsolidasi Diam-Diam Dua Kekuatan Sebelum Era Generasi Terakhir"
        }
    }

    val titles: List<String> = buildList {
        addAll(explicitFirst40)
        addAll(mid109)
        addAll(explicit150to169)
        addAll(war170to250)
        addAll(explicit251to269)
        addAll(shadow270to299)
        add("Penutupan Arc 2: Hilangnya Sang Psikopat di Perbatasan Vietnam dan Misteri Abadi Sang Naga Hitam (Tamat Arc 2)")
    }
}
