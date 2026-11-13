package com.example.data.seed

import com.example.data.local.LocalCharacterEntity

object RyujinCharacterSeeder {

    fun buildAll400Characters(novelId: String = RyujinNovelMeta.RYUJIN_NOVEL_ID): List<LocalCharacterEntity> {
        val core = listOf(
            LocalCharacterEntity(
                id = "${novelId}_char_001",
                novelId = novelId,
                characterNumber = 1,
                name = "Kenjiro Tatsumoto",
                alias = "K / Sang Naga Hitam",
                country = "Jepang",
                faction = "Ryujin Company (Pendiri & Ketua Tertinggi)",
                role = "Protagonis Utama",
                traits = "Tenang, Taktis, Tegas, Tanpa Ampun pada Pengkhianat, Pelindung Keluarga",
                arcPresence = "Arc 1, Arc 2, Arc 3",
                storyline = "Korban perundungan di SMA Shinjuku yang memalsukan kematiannya usai pembantaian keluarganya, berlatih 5 tahun di Hutan Kiso bersama Genzo, menaklukkan dunia bawah tanah sebagai 'K', mendirikan Ryujin, dan mengorbankan nyawanya di perairan Vietnam demi generasi penerus.",
                deepProfile = "Lahir dari keluarga Tatsumoto yang memiliki ikatan sejarah mendalam dengan Ryuzaki Taniki. Tato tebori Naga Hitam di punggungnya melambangkan sumpah balas dendam sekaligus perlindungan. Meski tampak sedingin es di medan tempur Italia dan Tokyo, hatinya luluh pada kelembutan Sakura Miya dan tekad untuk melindungi putranya, Saiken.",
                portraitKey = "kenjiro"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_002",
                novelId = novelId,
                characterNumber = 2,
                name = "Aoi Takashi",
                alias = "Sang Malaikat Berpita Biru",
                country = "Jepang",
                faction = "Syndicate of Angels (Pemimpin Tertinggi)",
                role = "Antagonis Utama",
                traits = "Manipulatif, Psikopat Murni, Posesif Ekstrem, Bermuka Dua, Kejam",
                arcPresence = "Arc 1, Arc 2, Arc 3",
                storyline = "Siswi berwajah malaikat di Shinjuku yang memicu perundungan Kenjiro lewat kecemburuan Ryusuke, membantai keluarga Tatsumoto dan keluarganya sendiri, membunuh Ryusuke, melarikan diri ke Vietnam bersama Xie Ping, dan akhirnya gugur setelah menusuk Kenjiro dari belakang.",
                deepProfile = "Mengidap obsesi patologis sejak masa kecil yang dingin. Pita birunya menjadi simbol kontras antara wajah manis yang memikat dengan jiwa yang kehabisan empati. Bahkan saat mengasuh putranya Ao Ping di pedalaman Vietnam, ia memproyeksikan halusinasinya tentang Kenjiro.",
                portraitKey = "aoi"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_003",
                novelId = novelId,
                characterNumber = 3,
                name = "Sakura Miya",
                alias = "Bunga Penenang Sang Naga",
                country = "Jepang",
                faction = "Ryujin Company (Ibu Negara / Keluarga Utama)",
                role = "Deuteragonis & Istri Kenjiro",
                traits = "Lembut, Berani, Setia, Empatik, Teguh Memegang Prinsip",
                arcPresence = "Arc 1, Arc 2, Arc 3",
                storyline = "Adik dari Masashi Miya yang bertemu 'K' di arena Italia dan menjadi jangkar kemanusiaan Kenjiro hingga berdirinya Ryujin Company. Melahirkan Saiken sebelum gugur tragis akibat penyusupan Aoi Takashi di kediaman Tokyo.",
                deepProfile = "Meskipun awalnya ngeri melihat brutalnya ring tinju global di Italia, Sakura memilih memahami luka di balik topeng Kenjiro. Pelukannya di Lantai 300 menjadi penutup Arc 1 dan motivasinya menjaga keluarga tetap hangat meski di tengah badai sindikat.",
                portraitKey = "sakura"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_004",
                novelId = novelId,
                characterNumber = 4,
                name = "Ryusuke Taniki",
                alias = "Putra Mahkota Sindikat",
                country = "Jepang",
                faction = "Faksi Ryuzaki (Mantan Pewaris) → Sekutu Keluarga",
                role = "Rival Masa Lalu & Penebus Dosa",
                traits = "Emosional, Mudah Terpancing, Setia pada Cinta, Rela Berkorban di Akhir",
                arcPresence = "Arc 1, Arc 2",
                storyline = "Mantan sahabat kecil Kenjiro yang dibutakan obsesi pada Aoi sehingga menjadi perundung utama di Shinjuku. Setelah ditaklukkan Kenjiro di akhir Arc 1, ia bertobat dan gugur secara heroik di Arc 2 saat menahan tusukan Aoi demi menyelamatkan Sakura dan mengamankan hardisk bukti.",
                deepProfile = "Sebenarnya Ryusuke dan ayahnya berniat melindungi Kenjiro, namun terhalang waktu dan adu domba Aoi. Pengorbanannya mempertahankan hardisk bukti di Arc 2 membuktikan bahwa hati nuraninya tidak pernah benar-benar mati.",
                portraitKey = "ryusuke"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_005",
                novelId = novelId,
                characterNumber = 5,
                name = "Ryuzaki Taniki",
                alias = "Bos Besar Sindikat Lama",
                country = "Jepang",
                faction = "Sindikat Ryuzaki",
                role = "Mentor Tersembunyi & Mantan Tangan Kanan Ayah Kenjiro",
                traits = "Berwibawa, Penuh Rahasia, Bijaksana, Menyesali Keterlambatan",
                arcPresence = "Arc 1",
                storyline = "Penguasa dunia bawah Tokyo yang bertarung habis-habisan melawan Ryujin sebelum akhirnya mengungkap fakta di ranjang kematiannya bahwa ia adalah mantan tangan kanan ayah Kenjiro yang terlambat menyelamatkan keluarga Tatsumoto.",
                deepProfile = "Wasiat terakhirnya agar Kenjiro mengampuni Ryusuke dan sang Bibi mengubah arah moral Ryujin Company dari faksi pembalas dendam menjadi penjaga stabilitas.",
                portraitKey = "ryuzaki"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_006",
                novelId = novelId,
                characterNumber = 6,
                name = "Genzo",
                alias = "Pertapa Kuil Sunyi Kiso",
                country = "Jepang",
                faction = "Independen (Guru Besar Kenjiro)",
                role = "Guru Bela Diri & Seniman Tebori",
                traits = "Keras, Disiplin, Penuh Kebijaksanaan, Loyal pada Ayah Kenjiro",
                arcPresence = "Arc 1",
                storyline = "Sosok tua di pedalaman Hutan Kiso yang tangannya cacat akibat beruang hitam masa lalu. Melatih Kenjiro selama 5 tahun di bawah air terjun dan badai salju serta menorehkan tato Naga Hitam pertama dengan jarum tebori.",
                deepProfile = "Pernah mengenal Kenjiro saat masih balita di hadapan Sang Bos dan Ryuzaki. Genzo menempa tubuh dan mental Kenjiro hingga mencapai puncak kekuatan fisik manusia tanpa sihir.",
                portraitKey = "genzo"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_007",
                novelId = novelId,
                characterNumber = 7,
                name = "Saiken Tatsumoto",
                alias = "Pewaris Terakhir Naga Hitam",
                country = "Jepang",
                faction = "Ryujin Generasi Akhir",
                role = "Protagonis Arc 3",
                traits = "Keras Kepala, Berapi-api, Tangguh, Protektif, Penyintas",
                arcPresence = "Arc 3",
                storyline = "Putra Kenjiro dan Sakura yang tumbuh membenci kesibukan ayahnya, namun bangkit memimpin garis depan bersama Ryumei setelah kematian ibu dan bibinya, menumbangkan Ao Ping di pantai Vietnam, dan menutup era dunia bawah bersama Ryumei.",
                deepProfile = "Mewarisi ketahanan mental dan fisik sang Naga Hitam sekaligus kehangatan hati Sakura. Bersama Ryumei, ia memilih membubarkan divisi kekerasan Ryujin dan membangun yayasan damai di Tokyo.",
                portraitKey = "saiken"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_008",
                novelId = novelId,
                characterNumber = 8,
                name = "Ryumei Taniki-Tatsuya",
                alias = "Bayangan Dua Klan",
                country = "Jepang",
                faction = "Ryujin Generasi Akhir",
                role = "Deuteragonis Arc 3",
                traits = "Cerdas Taktis, Lincah, Tenang, Penjaga Keseimbangan",
                arcPresence = "Arc 3",
                storyline = "Putri dari Ryusuke Taniki dan Mey Tatsuya yang mewarisi kecerdasan infiltrasi serta kelincahan tempur dua keluarga. Mendesak Aoi Takashi hingga tepi tebing dalam Duel 3 dan mendampingi Saiken membangun masa depan damai.",
                deepProfile = "Menjadi jembatan rekonsiliasi permanen antara darah Tatsumoto dan Taniki. Senyuman tulusnya bersama Saiken di akhir Bab 900 menandai tamatnya sejarah kelam sindikat.",
                portraitKey = "ryumei"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_009",
                novelId = novelId,
                characterNumber = 9,
                name = "Takaharu Miya",
                alias = "Arsitek 4 Pilar",
                country = "Jepang-Amerika",
                faction = "Ryujin Company (Pilar Pertama & Ahli Strategi)",
                role = "Jenderal Strategi Utama",
                traits = "Brilian, Visioner, Disiplin, Ahli Logistik Global",
                arcPresence = "Arc 1, Arc 2",
                storyline = "Petarung keturunan Jepang-Amerika yang awalnya menantang 'K', lalu menjadi arsitek pelatihan 4 Pilar dan menyarankan ekspansi global menuju markas mafia Florentino di Italia.",
                deepProfile = "Berperan vital mengubah jaringan petarung jalanan menjadi organisasi multinasional terstruktur dengan rantai logistik dan komunikasi sandi.",
                portraitKey = "takaharu"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_010",
                novelId = novelId,
                characterNumber = 10,
                name = "Masashi Miya",
                alias = "Kakak Ipar Sang Naga",
                country = "Jepang-Amerika",
                faction = "Ryujin Company (Panglima Penyerbu)",
                role = "Komandan Lapangan & Kakak Sakura",
                traits = "Ksatria, Terhormat, Analitis, Pelindung Adik",
                arcPresence = "Arc 1, Arc 2, Arc 3",
                storyline = "Kakak kandung Sakura yang menilai turnamen 200 Triliun di Italia, bertarung dalam duel kehormatan melawan 'K', lalu menjadi komandan inti yang membuka pintu rahasia benteng Ryuzaki dan armada Pasifik.",
                deepProfile = "Mengakui kekuatan mutlak K bukan karena takut, melainkan karena kehormatan seorang petarung sejati.",
                portraitKey = "masashi"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_011",
                novelId = novelId,
                characterNumber = 11,
                name = "Hinata Kawasaki",
                alias = "Mata Elang Ryujin",
                country = "Jepang",
                faction = "Ryujin Company (Divisi Intelijen & Spionase)",
                role = "Kepala Intelijen & Mata-Mata Handal",
                traits = "Observatif, Dingin, Akurat, Tak Terdeteksi",
                arcPresence = "Arc 1, Arc 2, Arc 3",
                storyline = "Pengamat tajam di arena Italia yang menawarkan diri menjadi mata-mata utama Ryujin, memetakan celah keamanan benteng Ryuzaki dan membongkar jaringan sandi Syndicate of Angels.",
                deepProfile = "Mampu membaca refleks petarung dari jarak jauh dan memutus sistem komunikasi musuh beberapa detik sebelum penyerbuan total dimulai.",
                portraitKey = "hinata"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_012",
                novelId = novelId,
                characterNumber = 12,
                name = "Ren Tatsuya",
                alias = "Kembar Pertama — Badai Besi",
                country = "Jepang",
                faction = "Ryujin Company (Komandan Barisan Depan)",
                role = "Trio Kembar Penyerbu",
                traits = "Agresif, Loyal, Berani, Kekuatan Penghancur",
                arcPresence = "Arc 1, Arc 2, Arc 3",
                storyline = "Salah satu dari Tiga Kembar Tatsuya dari Oby's Arena yang berduel sengit melawan K sebelum bersumpah setia dan merobohkan gerbang luar Ryuzaki serta gudang senjata Syndicate.",
                deepProfile = "Serangan beruntunnya bersama Ten menjadi pembuka jalan bagi pasukan utama Ryujin di setiap front pertempuran.",
                portraitKey = "ren"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_013",
                novelId = novelId,
                characterNumber = 13,
                name = "Ten Tatsuya",
                alias = "Kembar Kedua — Tinju Halilintar",
                country = "Jepang",
                faction = "Ryujin Company (Komandan Barisan Depan)",
                role = "Trio Kembar Penyerbu",
                traits = "Brutal, Pantang Mundur, Kompak dengan Ren",
                arcPresence = "Arc 1, Arc 2, Arc 3",
                storyline = "Bertarung brutal melawan K di arena bawah tanah lalu menaklukkan sektor pertahanan terberat di Tokyo dan Eropa bersama Ren.",
                deepProfile = "Memiliki sinkronisasi tempur tanpa kata dengan kembarannya Ren dan Mey.",
                portraitKey = "ten"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_014",
                novelId = novelId,
                characterNumber = 14,
                name = "Mey Tatsuya",
                alias = "Kembar Ketiga — Kilat Sektor Kanan",
                country = "Jepang",
                faction = "Ryujin Company (Komandan Serangan Kilat)",
                role = "Trio Kembar & Ibu Ryumei",
                traits = "Lincah, Tajam, Tangguh, Penyayang Keluarga",
                arcPresence = "Arc 1, Arc 2, Arc 3",
                storyline = "Ahli kelincahan di Oby's Arena yang menjadi pimpinan serangan kilat sektor kanan dan ibu dari Ryumei yang mewariskan kecepatan luar biasa kepada putrinya.",
                deepProfile = "Pesan terakhirnya kepada Ryumei sebelum perang akhir di Vietnam menjadi pegangan moral generasi muda.",
                portraitKey = "mey"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_015",
                novelId = novelId,
                characterNumber = 15,
                name = "Obys",
                alias = "Raja Arena Bawah Tanah Shinjuku",
                country = "Jepang",
                faction = "Oby's Arena → Sekutu Ryujin",
                role = "Bos Arena Tinju Gelap",
                traits = "Ambisius, Pemarah, Menghormati Kekuatan Sejati",
                arcPresence = "Arc 1",
                storyline = "Pemilik arena tinju bawah tanah Shinjuku yang murka melihat dominasi K, lalu berduel hidup-mati di puncak arena sebelum mengakui kepemimpinan sang Naga Hitam.",
                deepProfile = "Arena gelap miliknya menjadi panggung awal kembalinya Kenjiro ke Shinjuku dan kelak direvitalisasi di era damai.",
                portraitKey = "obys"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_016",
                novelId = novelId,
                characterNumber = 16,
                name = "Florentino",
                alias = "Il Padrino Italia",
                country = "Italia",
                faction = "Mafia Italia → Cabang Eropa Ryujin",
                role = "Mantan Bos Besar Mafia Italia",
                traits = "Aristokrat, Bangga, Pragmatis setelah Takluk",
                arcPresence = "Arc 1, Arc 2",
                storyline = "Penguasa mafia Italia yang tumbang dalam satu pukulan telak oleh K di ruang pribadinya dan bersujud mendukung penyelenggaraan Turnamen Tinju Dunia berhadiah 200 Triliun.",
                deepProfile = "Menyediakan infrastruktur megah di Italia bagi seleksi internasional pasukan Ryujin.",
                portraitKey = "florentino"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_017",
                novelId = novelId,
                characterNumber = 17,
                name = "Otsumu",
                alias = "Perisai Terakhir Ryuzaki",
                country = "Jepang",
                faction = "Sindikat Ryuzaki (Komandan Pasukan Elit)",
                role = "Antagonis Sektor Puncak Arc 1",
                traits = "Fanatik Setia, Tangguh, Keras, Rela Mati untuk Bos",
                arcPresence = "Arc 1",
                storyline = "Pemimpin pasukan elit Ryuzaki yang menghadang langkah K menuju lantai puncak gedung utama sebelum tumbang dalam duel penuh luka.",
                deepProfile = "Melambangkan kesetiaan mutlak pengawal lama sebelum era baru Ryujin dimulai.",
                portraitKey = "otsumu"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_018",
                novelId = novelId,
                characterNumber = 18,
                name = "Takeshi Takahashi",
                alias = "Pelampiasan yang Bangkit",
                country = "Jepang",
                faction = "Syndicate of Angels → Divisi Logistik Ryujin",
                role = "Agen Ganda & Pelatih Saiken",
                traits = "Traumatis, Cerdik, Berani Menebus Kesalahan",
                arcPresence = "Arc 2, Arc 3",
                storyline = "Satu-satunya penyintas pembantaian keluarga Takahashi oleh Aoi yang awalnya diperalat sebagai penyusup, namun kemudian berbalik menjadi informan kunci Kenjiro, melatih Saiken, dan memimpin dobrakan laut di Vietnam.",
                deepProfile = "Perjalanannya dari alat pelampiasan Aoi menjadi komandan infiltrasi laut Ryujin adalah kisah penebusan paling menyentuh di Arc 3.",
                portraitKey = "takeshi"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_019",
                novelId = novelId,
                characterNumber = 19,
                name = "Xie Ping",
                alias = "Panglima Armada Perbatasan",
                country = "Vietnam",
                faction = "Aliansi Militer Pesisir Vietnam",
                role = "Antagonis Militer Arc 2 & Arc 3",
                traits = "Militeristik, Dingin, Pragmatis, Pelindung Aoi",
                arcPresence = "Arc 2, Arc 3",
                storyline = "Pemimpin kelompok militer Vietnam yang mengevakuasi Aoi di tengah lautan, membangun benteng pantai di pedalaman Vietnam, memiliki anak bernama Ao Ping bersama Aoi, dan gugur di tangan Kenjiro dalam Duel 1 di geladak kapal komando.",
                deepProfile = "Mengabaikan kegilaan psikopat Aoi demi ambisi kekuatan armada laut bersenjata berat.",
                portraitKey = "xieping"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_020",
                novelId = novelId,
                characterNumber = 20,
                name = "Ao Ping",
                alias = "Putra Kegelapan Vietnam",
                country = "Vietnam-Jepang",
                faction = "Aliansi Vietnam & Syndicate of Angels",
                role = "Rival Generasi Muda Arc 3",
                traits = "Berdarah Dingin, Sadis, Korban Didikan Psikopat",
                arcPresence = "Arc 3",
                storyline = "Putra Aoi Takashi dan Xie Ping yang ditempa sejak kecil di arena berdarah untuk membenci Kenjiro, hingga akhirnya berhadapan dengan Saiken dalam Duel 2 di atas pasir pantai Vietnam.",
                deepProfile = "Cerminan tragis dari seorang anak yang dijadikan senjata balas dendam oleh ibunya sendiri.",
                portraitKey = "aoping"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_021",
                novelId = novelId,
                characterNumber = 21,
                name = "Bibi Tatsumoto",
                alias = "Adik Ayah Kenjiro",
                country = "Jepang",
                faction = "Keluarga Besar Tatsumoto-Taniki",
                role = "Penjaga Silsilah Keluarga",
                traits = "Penyayang, Penuh Penyesalan, Bijaksana, Tabah",
                arcPresence = "Arc 1, Arc 2, Arc 3",
                storyline = "Adik kandung ayah Kenjiro yang mengungkap silsilah keluarga di akhir Arc 1, meratapi kepergian Ryusuke di Arc 2, dan akhirnya gugur melindungi rumah Tokyo saat penyusupan Aoi di Arc 3.",
                deepProfile = "Menyimpan seluruh catatan kebenaran masa lalu antara keluarga Tatsumoto dan Ryuzaki.",
                portraitKey = "bibi"
            ),
            LocalCharacterEntity(
                id = "${novelId}_char_022",
                novelId = novelId,
                characterNumber = 22,
                name = "Ayah & Ibu Tatsumoto",
                alias = "Akar Kehormatan Kenjiro",
                country = "Jepang",
                faction = "Keluarga Tatsumoto",
                role = "Orang Tua Kenjiro",
                traits = "Hangat, Jujur, Terhormat, Penuh Kasih",
                arcPresence = "Arc 1 (Kilas Balik & Bab Awal)",
                storyline = "Pasangan hangat di ruang makan keluarga Tatsumoto sebelum malam bencana eksekusi senyap oleh Aoi Takashi yang memicu lahirnya legenda Ryujin.",
                deepProfile = "Pelukan terakhir mereka menjadi alasan Kenjiro mempertahankan kemanusiaannya hingga akhir hayat.",
                portraitKey = "tatsumoto"
            )
        )

        val countries = listOf(
            "Jepang", "Italia", "Vietnam", "Amerika Serikat", "Korea Selatan",
            "Prancis", "Jerman", "Spanyol", "Meksiko", "Inggris",
            "Brasil", "Thailand", "Malaysia", "Polandia", "Rusia",
            "Argentina", "Kanada", "Filipina", "Singapura", "Turki"
        )

        val factions = listOf(
            "Divisi Tempur Internasional Ryujin",
            "Brigade Arena Italia Ryujin",
            "Garda Bayangan Shinjuku",
            "Syndicate of Angels (Sektor Eropa)",
            "Armada Pesisir Aliansi Vietnam",
            "Mantan Pasukan Elit Sindikat Ryuzaki",
            "Kontingen Turnamen Dunia 200 Triliun",
            "Jaringan Intelijen Asia Tenggara"
        )

        val traitsPool = listOf(
            "Disiplin Baja, Ahli Counter-Attack, Tenang di Bawah Tekanan",
            "Kecepatan Kilat, Refleks Tajam, Tak Kenal Takut",
            "Spesialis Jiu-Jitsu, Strategis, Pandai Membaca Kelemahan Lawan",
            "Kekuatan Fisik Brutal, Ketahanan Tinggi, Loyal Mutlak",
            "Pengintai Senyap, Cerdik, Ahli Sandi Lapangan",
            "Petarung Jalanan Agresif, Insting Tajam, Pantang Menyerah",
            "Taktis Jarak Jauh, Analitis, Pengatur Jalur Logistik",
            "Ahli Kunci Leher & Bantingan, Fokus Tinggi, Dingin"
        )

        val firstNamesByCountry = mapOf(
            "Jepang" to listOf("Haruto", "Kaito", "Sora", "Daiki", "Riku", "Yuto", "Shun", "Akira", "Naoki", "Tsubasa", "Ayaka", "Rei"),
            "Italia" to listOf("Marco", "Lorenzo", "Matteo", "Luca", "Alessandro", "Dante", "Giulio", "Enzo", "Chiara", "Sofia"),
            "Vietnam" to listOf("Minh", "Quang", "Hoang", "Duc", "Tuan", "Hung", "Khang", "Linh", "Thao", "Dat"),
            "Amerika Serikat" to listOf("Marcus", "Cole", "Ethan", "Logan", "Victor", "Mason", "Tyler", "Grant", "Elena", "Maya"),
            "Korea Selatan" to listOf("Ji-Hoon", "Min-Jun", "Hyun", "Tae-Yang", "Seo-Jun", "Kang", "Dae-Sung", "Ye-Jin"),
            "Prancis" to listOf("Luc", "Antoine", "Julien", "Bastien", "Hugo", "Camille", "Claire", "Remi"),
            "Jerman" to listOf("Lukas", "Felix", "Maximilian", "Jonas", "Stefan", "Erik", "Tobias", "Hannah"),
            "Spanyol" to listOf("Carlos", "Mateo", "Javier", "Diego", "Alejandro", "Sergio", "Lucia", "Pablo"),
            "Meksiko" to listOf("Emiliano", "Santiago", "Rafael", "Hector", "Cruz", "Eduardo", "Andres", "Valeria"),
            "Inggris" to listOf("Oliver", "Arthur", "Harry", "Callum", "Liam", "Declan", "Freya", "alistair")
        )

        val lastNamesPool = listOf(
            "Kurosawa", "Nakamura", "Moretti", "Conti", "Nguyen", "Tran", "Vance", "Mercer",
            "Park", "Kim", "Laurent", "Dupont", "Weber", "Richter", "Navarro", "Mendoza",
            "Sterling", "Hawke", "Silva", "Tanaka", "Sato", "Watanabe", "Rossi", "Pham",
            "Kowalski", "Volkov", "Alvarez", "Cruz", "Chaiyaporn", "Rahman"
        )

        val remaining = (23..400).map { num ->
            val country = countries[(num * 7) % countries.size]
            val faction = factions[(num * 3) % factions.size]
            val firstList = firstNamesByCountry[country] ?: listOf("Ken", "Leo", "Kai", "Aria", "Zane", "Niko", "Rayan", "Sasha")
            val firstName = firstList[num % firstList.size].replaceFirstChar { it.uppercase() }
            val lastName = lastNamesPool[(num * 5 + 3) % lastNamesPool.size]
            val fullName = "$firstName $lastName"
            val traits = traitsPool[num % traitsPool.size]
            val arc = when {
                num <= 160 -> "Arc 1 (Turnamen Italia & Perang Tokyo)"
                num <= 285 -> "Arc 2 (Perang Global Syndicate of Angels)"
                else -> "Arc 3 (Ekspedisi Vietnam & Generasi Akhir)"
            }
            val role = when {
                num % 11 == 0 -> "Komandan Sektor & Ahli Taktik"
                num % 7 == 0 -> "Petarung Unggulan Turnamen 200 Triliun"
                num % 5 == 0 -> "Anggota Elite Divisi Penyerbu"
                else -> "Petarung Lapangan & Pengawal Skuadron"
            }
            val alias = "Unit-${num.toString().padStart(3, '0')} • ${country.take(3).uppercase()}"

            val storyline = when {
                num <= 160 -> "Petarung asal $country yang ikut dalam Turnamen Tinju Dunia berhadiah 200 Triliun di Italia. Setelah menyaksikan dominasi mutlak K menumbangkan ratusan kandidat sekaligus di tengah ring, $fullName mengucapkan sumpah tunduk dan bergabung ke $faction."
                num <= 285 -> "Direkrut ke dalam konflik lintas negara antara Ryujin dan Syndicate of Angels. $fullName bertarung di garis depan sektor pelabuhan serta benteng logistik demi mempertahankan supremasi faksinya."
                else -> "Terlibat langsung dalam mobilisasi armada laut menuju perairan Vietnam dan pertempuran pantai penutup, sebelum akhirnya memilih jalan hidup damai pasca-pembubaran divisi kekerasan."
            }

            val deepProfile = "$fullName adalah representasi ketangguhan wilayah $country di dalam semesta Ryujin. " +
                "Dikenal dengan karakter '$traits', ia berperan krusial pada $arc. " +
                "Dalam catatan arsip Ryujin Company, kontribusinya tercatat dalam formasi $faction sebagai $role yang ikut membentuk keseimbangan dunia bawah."

            LocalCharacterEntity(
                id = "${novelId}_char_${num.toString().padStart(3, '0')}",
                novelId = novelId,
                characterNumber = num,
                name = fullName,
                alias = alias,
                country = country,
                faction = faction,
                role = role,
                traits = traits,
                arcPresence = arc,
                storyline = storyline,
                deepProfile = deepProfile,
                portraitKey = "badge_${num % 6}"
            )
        }

        return core + remaining
    }
}
