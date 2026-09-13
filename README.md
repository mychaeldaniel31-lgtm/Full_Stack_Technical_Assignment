# Appointment System

Aplikasi Spring Boot dengan login username tanpa password, session server-side satu jam, appointment dengan undangan, dan tampilan sesuai zona waktu pengguna.

## Menjalankan

Prasyarat: Java 17+; wrapper Maven membutuhkan akses internet pada build pertama.
export DB_URL=jdbc:mysql://localhost:3306/appointments
export DB_USERNAME=root
export DB_PASSWORD=

./mvnw spring-boot:run

Buka http://localhost:8080..

### Demo cepat tanpa mySQL

Pada shell tanpa `SPRING_PROFILES_ACTIVE=mys`:

```bash
./mvnw spring-boot:run
```

Mode default memakai H2 file `./data/appointments` untuk demo lokal. Untuk penilaian persyaratan database, gunakan profil mySQL. Konsol H2 dinonaktifkan.

User demo: `siti` (Asia/Jakarta), `alex` (America/New_York), `maya` (Pacific/Auckland). Satu appointment demo dibuat jika tabel appointment kosong.

## Aturan waktu

- Jam kerja **08:00–17:00**, mengikuti bagian 4.4; bagian 2 brief menyebut 09:00, sehingga asumsi ini perlu dikonfirmasi saat penilaian.
- Seluruh interval harus berada pada satu tanggal lokal dan jam kerja **setiap peserta**, termasuk pembuat. Waktu selesai harus setelah mulai.
- Input formulir berlabel **UTC** dan dibaca sebagai UTC, terlepas dari zona browser. Daftar dikonversi ke `preferredTimezone` pengguna dan menampilkan label zona.
- Contoh: 1 Januari 2040, 01:00–02:00 UTC = 08:00–09:00 Jakarta = 14:00–15:00 Auckland. Undang `maya` saat login sebagai `siti`.
- DST dihitung menggunakan zona IANA oleh Java/browser. Peserta dengan jam kerja tanpa irisan akan mendapat penolakan; pencarian slot otomatis belum tersedia. Weekend/libur belum dibedakan karena brief hanya menentukan jam kerja.


### Jam kerja pengguna demo dalam UTC (per 13 Sep 2026)

Referensi cepat untuk menentukan slot appointment yang valid. Jam kerja lokal
semua pengguna diasumsikan 08:00–17:00. Kolom UTC dihitung untuk tanggal
berjalan dan bisa bergeser saat DST berubah (lihat catatan di bawah).

| Nama                  | Username    | Zona (IANA)                    | Jam kerja (UTC)      |
|-----------------------|-------------|---------------------------------|-----------------------|
| Siti Admin            | siti        | Asia/Jakarta                    | 01:00–10:00           |
| Alex Product          | alex        | America/New_York                | 12:00–21:00           |
| Maya Auckland         | maya        | Pacific/Auckland                | 20:00 (H-1)–05:00     |
| Budi Bandung          | budi        | Asia/Jakarta                    | 01:00–10:00           |
| Wayan Denpasar        | wayan       | Asia/Makassar                   | 00:00–09:00           |
| Rizky Papua           | rizky       | Asia/Jayapura                   | 23:00 (H-1)–08:00     |
| Aiko Tokyo            | aiko        | Asia/Tokyo                      | 23:00 (H-1)–08:00     |
| Wei Beijing           | wei         | Asia/Shanghai                   | 00:00–09:00           |
| Minjun Seoul          | minjun      | Asia/Seoul                      | 23:00 (H-1)–08:00     |
| Anjali Mumbai         | anjali      | Asia/Kolkata                    | 02:30–11:30           |
| Farid Dubai           | farid       | Asia/Dubai                      | 04:00–13:00           |
| Elif Istanbul         | elif        | Europe/Istanbul                 | 05:00–14:00           |
| Hans Berlin           | hans        | Europe/Berlin (CEST)            | 06:00–15:00           |
| Sophie Paris          | sophie      | Europe/Paris (CEST)             | 06:00–15:00           |
| Oliver London         | oliver      | Europe/London (BST)             | 07:00–16:00           |
| Ivan Moscow           | ivan        | Europe/Moscow                   | 05:00–14:00           |
| Kwame Lagos           | kwame       | Africa/Lagos                    | 07:00–16:00           |
| Amara Nairobi         | amara       | Africa/Nairobi                  | 05:00–14:00           |
| Layla Cairo           | layla       | Africa/Cairo (DST aktif)        | 05:00–14:00           |
| Thabo Johannesburg    | thabo       | Africa/Johannesburg             | 06:00–15:00           |
| Carlos Sao Paulo      | carlos      | America/Sao_Paulo               | 11:00–20:00           |
| Valentina Buenos Aires| valentina   | America/Argentina/Buenos_Aires  | 11:00–20:00           |
| Diego Mexico City     | diego       | America/Mexico_City             | 14:00–23:00           |
| Emily Los Angeles     | emily       | America/Los_Angeles (PDT)       | 15:00–00:00 (H+1)     |
| James Chicago         | james       | America/Chicago (CDT)           | 13:00–22:00           |
| Liam Toronto          | liam        | America/Toronto (EDT)           | 12:00–21:00           |
| Noah Honolulu         | noah        | Pacific/Honolulu                | 18:00–03:00 (H+1)     |
| Grace Sydney          | grace       | Australia/Sydney (AEST)         | 22:00 (H-1)–07:00     |
| Jack Perth            | jack        | Australia/Perth                 | 00:00–09:00           |
| Malia Fiji            | malia       | Pacific/Fiji                    | 20:00 (H-1)–05:00     |
| Nils Reykjavik        | nils        | Atlantic/Reykjavik              | 08:00–17:00           |
| Priya Singapore       | priya       | Asia/Singapore                  | 00:00–09:00           |
| Somchai Bangkok       | somchai     | Asia/Bangkok                    | 01:00–10:00           |
| Linh Hanoi            | linh        | Asia/Ho_Chi_Minh                | 01:00–10:00           |
| Juan Manila           | juan        | Asia/Manila                     | 00:00–09:00           |

**Catatan:**
- "(H-1)" berarti jam kerja dimulai pada hari UTC sebelumnya; "(H+1)" berarti berakhir pada hari UTC berikutnya.
- Kolom UTC hanya valid untuk periode DST saat ini. Beberapa zona akan bergeser dalam waktu dekat: **NZDT** mulai 27 Sep 2026 (Auckland/Fiji-area geser 1 jam), **DST Eropa** berakhir 25 Okt 2026 (Berlin/Paris/London/Istanbul-area kembali ke offset musim dingin), **DST AS/Kanada** berakhir 1 Nov 2026 (New York/Chicago/LA/Toronto mundur 1 jam), **AEDT Australia** mulai 4 Okt 2026 (Sydney maju 1 jam), dan **DST Mesir** berakhir sekitar akhir Okt 2026.
- Untuk mencari irisan jam kerja dua atau lebih pengguna, cari overlap rentang UTC pada tabel di atas. Jika tidak ada irisan sama sekali dalam jam kerja normal, appointment akan ditolak (400) karena aplikasi belum mendukung pencarian slot otomatis.
## API

Login: `POST /api/auth/login`, body `{"username":"siti"}`. Respons berisi token opaque, user, dan `expiresInSeconds: 3600`.

Endpoint berikut memakai `Authorization: Bearer <token>`:

- `GET /api/me`: profil pengguna login.
- `GET /api/users`: daftar pengguna.
- `GET /api/appointments?page=0&size=50`: appointment mendatang milik pembuat atau yang mengundangnya; ukuran halaman 1–100. UI menyediakan navigasi halaman.
- `POST /api/appointments`: membuat appointment, status 201.
- `POST /api/auth/logout`: mencabut token di server, status 200.

Contoh body appointment:

```json
{
  "title": "Product sync",
  "start": "2040-01-01T01:00:00Z",
  "end": "2040-01-01T02:00:00Z",
  "invitees": ["maya"]
}
```

`POST /api/users` tersedia tanpa login untuk membuat pengguna, status 201. Username baru disimpan lowercase; login tidak membedakan kapitalisasi. Gunakan nama zona terdaftar seperti `Asia/Jakarta` atau `UTC`, bukan `UTC+07:00`:

```json
{"name":"Budi","username":"budi","preferredTimezone":"Asia/Jakarta"}
```

Status utama: 400 untuk input/jam kerja tidak valid, 401 untuk sesi/login tidak valid, 409 untuk username yang sudah digunakan. Error validasi mengandung `message` untuk feedback UI. Sesi memiliki expiry absolut, tidak diperpanjang oleh aktivitas, dan hilang saat server restart.

## Pengujian

```bash
./mvnw test
node src/test/js/app.test.cjs
./mvnw package
```

Node.js 18+ hanya diperlukan untuk tes JavaScript. Tes Java memakai database H2 in-memory terpisah, bukan file data demo. Tes mencakup login, akses tanpa token, expiry tepat satu jam, logout, visibilitas undangan, pagination, batas input, jam kerja lintas hari, perbedaan offset DST, konversi UTC, rendering aman, dan pemulihan UI setelah 401.

## Struktur

Controller/DTO menangani HTTP dan validasi input; service menangani sesi, pengguna dan aturan appointment; repository menangani persistence. Appointment menyimpan `Instant` dan relasi pembuat/undangan. Daftar mengambil ID dengan pagination lalu memuat peserta dalam satu query detail agar tidak melakukan lazy loading per baris.

## Dokumentasi

- [Jawaban teknis](answers.md)
- [Hasil pemeriksaan dan batasan](docs/functional-review.md)
- [Video demo](docs/demo.mp4)
- [Login](docs/login.png), [appointment Jakarta](docs/appointments.png), [undangan Auckland](docs/invitee.png), [validasi jam kerja](docs/validation.png)

Screenshot dan video direkam dari aplikasi yang berjalan dengan mySQL; browser memakai zona America/New_York untuk memeriksa independensi dari zona browser.
