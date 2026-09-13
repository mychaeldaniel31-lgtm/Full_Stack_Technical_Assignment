# Jawaban pertanyaan teknis

## 1. Timezone conflicts

Appointment disimpan sebagai `Instant` (UTC). `BusinessHoursPolicy` mengubah start/end ke `ZoneId` setiap peserta, termasuk pembuat, lalu menolak interval yang melewati tanggal lokal atau keluar dari 08:00–17:00. Pemeriksaan tanggal penting: membandingkan jam saja bisa meloloskan appointment 16:00 hari ini hingga 10:00 besok.

Saya mengikuti jam 08:00 pada bagian 4.4 brief, meskipun bagian 2 menyebut 09:00. Formulir memakai input UTC secara eksplisit; daftar memakai zona pilihan pengguna. Ini menghindari input lokal ambigu saat DST berakhir. Aturan DST pada zona IANA tetap dipakai ketika memvalidasi dan menampilkan waktu; tes Auckland Januari/Juli memeriksa perbedaan offset tersebut.

Jakarta dan New York tidak mempunyai irisan jam kerja 08:00–17:00 pada offset yang diuji. Sistem menolak slot dan menyebut peserta yang bermasalah, bukan memaksakan konversi. Pencarian irisan otomatis merupakan pengembangan berikutnya; saat ini pengguna memilih slot sendiri. Kalender libur belum dimodelkan.

## 2. Database optimization

`AppointmentRepository.upcomingIds` memfilter `start >= now` dan keanggotaan melalui creator atau `exists` pada invitee. Query ID memakai pagination database dengan urutan `start, id`, sehingga join undangan tidak menggandakan baris atau memotong peserta. Query kedua memakai entity graph untuk mengambil creator dan semua invitee; service mengembalikan urutan ID awal. Tidak ada fetch collection bersamaan dengan pagination.

Index tersedia pada start, creator_id, username unik, dan user_id tabel undangan. Untuk halaman kosong cukup satu query appointment; halaman berisi memakai dua query appointment, ditambah lookup sesi/user. Pembuatan appointment masih mencari setiap username undangan satu per satu; jika jumlah peserta membesar, lookup ini dapat dibatch.

Sebelum menambah cache atau index gabungan, saya akan melihat EXPLAIN ANALYZE pada PostgreSQL dan distribusi data sebenarnya. Untuk halaman yang sangat dalam, keyset pagination berdasarkan pasangan start/id menghindari biaya OFFSET. Cache bukan kebutuhan awal karena query dan invalidasi harus tetap sederhana.

## 3. Additional features

Prioritas pertama adalah RSVP dan pembatalan/reschedule, karena undangan saat ini hanya relasi ke peserta tanpa status penerimaan. Berikutnya pencarian slot beririsan serta reminder agar pengguna tidak perlu menghitung offset atau mengingat jadwal sendiri. Untuk produk nyata, login perlu bukti kepemilikan akun (misalnya magic link/SSO); username saja memang mengikuti brief, tetapi tidak membuktikan identitas.

Setelah kebutuhan itu jelas, barulah recurring appointments dan integrasi kalender. Recurrence harus menyimpan zona dan aturan lokal, bukan sekadar menambah 24 jam UTC, agar jam lokal tetap konsisten ketika DST berubah.

## 4. Session management

Token berupa UUID acak, tidak mengandung profil atau daftar undangan. Server menyimpan user ID serta expiry absolut satu jam. Setiap request memeriksa `now >= expiresAt`; logout menghapus token. Tes memakai waktu terkontrol untuk membuktikan token diterima pada detik 3599 dan ditolak pada detik 3600.

Store in-memory ringan untuk satu instance dan semua sesi hilang saat restart. Token kedaluwarsa dihapus ketika dipakai lagi; token yang ditinggalkan belum dibersihkan terjadwal. Deployment yang lebih lama atau multi-instance sebaiknya memakai store dengan TTL seperti Redis. UI saat ini mengirim Authorization header dan menyimpan token di localStorage; rendering teks pengguna di-escape untuk menutup injeksi HTML yang ditemukan saat review. Untuk produk nyata saya memilih HTTPS dan cookie HttpOnly/SameSite beserta proteksi CSRF, sehingga token tidak dapat dibaca JavaScript.
