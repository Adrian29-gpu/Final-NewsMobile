News App - Android News Reader
A modern Android application for reading the latest news from various sources using News API. This app supports both online and offline experiences with robust network connectivity handling and a responsive UI.

✨ Fitur Utama
Berita Terkini
Tampilkan headline berita terbaru dari berbagai kategori.

Pencarian Artikel
Cari artikel berita berdasarkan kata kunci tertentu.

Bookmark Artikel
Simpan artikel favorit untuk dibaca nanti, tersedia secara offline.

Mode Offline
Menampilkan peringatan saat tidak ada koneksi internet dan menggunakan data lokal secara otomatis.

UI Responsif
Navigasi intuitif dengan Bottom Navigation dan desain antarmuka yang bersih serta modern.

🧠 Teknologi & Arsitektur
REST API: Mengambil data berita melalui NewsAPI.org.

Retrofit: HTTP client untuk komunikasi data yang efisien.

Room Database: Menyimpan data lokal termasuk artikel yang dibookmark.

LiveData & ViewModel: Arsitektur MVVM untuk data yang reaktif dan mudah dipantau.

Network Monitoring: Pemantauan jaringan secara real-time menggunakan NetworkConnectionLiveData.

Offline-First Design: Aplikasi tetap berfungsi meski tanpa koneksi internet.

Database Migration: Menangani perubahan skema database dengan migrasi yang aman.

🔁 Alur Kerja Aplikasi
Deteksi Koneksi
Aplikasi memantau status koneksi internet menggunakan komponen khusus (NetworkConnectionLiveData).

Pemrosesan Data

Jika online, data berita dimuat dari API.

Jika offline, data diambil dari cache lokal menggunakan Room.

Interaksi Pengguna
Pengguna dapat:

Menjelajahi artikel terbaru.

Mencari artikel berdasarkan keyword.

Menyimpan artikel sebagai bookmark.

Persistensi Bookmark
Bookmark dipertahankan antar sesi dan tetap tersedia meski tanpa koneksi.

Peringatan Koneksi
Saat koneksi terputus, layout peringatan akan menutupi UI hingga koneksi pulih kembali.

🗂️ Struktur Proyek
📁 models
  └─ Article.kt, Source.kt (Definisi data model)

📁 database
  └─ ArticleDao.kt, AppDatabase.kt (Room & migrasi database)

📁 network
  └─ NewsApiService.kt, RetrofitClient.kt (Komunikasi ke API)

📁 fragments
  └─ HomeFragment.kt, SearchFragment.kt, BookmarkFragment.kt

📁 utils
  └─ NetworkConnectionLiveData.kt, Constants.kt, Helpers.kt

📦 Integrasi & Dependensi
Retrofit

Room Database

LiveData & ViewModel (Jetpack)

Coroutine

Material Components

Glide / Coil (untuk loading gambar berita)

Internet Permission & ConnectivityManager

🔧 Konfigurasi API Key
Pastikan untuk menambahkan API key milikmu dari newsapi.org di file konfigurasi yang sesuai (misalnya di local.properties atau melalui BuildConfig).
