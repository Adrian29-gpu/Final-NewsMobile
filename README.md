Proyek News App
Proyek ini adalah aplikasi berita mobile untuk Android yang menyediakan akses ke berbagai sumber berita menggunakan News API dari newsapi.org. Aplikasi dirancang dengan memperhatikan pengalaman pengguna offline dan online, termasuk penanganan konektivitas jaringan yang robust.  
Fitur Utama
Berita Terkini: Menampilkan headline berita terbaru dari berbagai kategori
Pencarian Artikel: Kemampuan untuk mencari artikel berdasarkan kata kunci
Bookmark Artikel: Menyimpan artikel favorit untuk dibaca nanti, dapat diakses offline
Penanganan Offline Mode: Menampilkan peringatan tidak ada koneksi dan menyimpan data secara lokal
Antarmuka Responsif: Navigasi yang intuitif dengan bottom navigation dan UI yang bersih
Teknologi dan Arsitektur
REST API: Menggunakan News API (newsapi.org) untuk mengambil data berita terbaru
Room Database: Persistensi data lokal untuk menyimpan artikel dan bookmark
Retrofit: HTTP client untuk komunikasi dengan API
LiveData: Implementasi observer pattern untuk pemantauan data reaktif
Custom Network Monitoring: Sistem pemantauan jaringan real-time yang menangani perubahan konektivitas
Offline-First Design: Aplikasi dirancang untuk berfungsi bahkan tanpa koneksi internet
Database Migration: Penanganan upgrade database dengan migrasi yang cermat
Alur Kerja Aplikasi
Aplikasi memulai dengan memantau konektivitas jaringan menggunakan komponen custom NetworkConnectionLiveData
Berita ditampilkan dari API jika online, atau dari cache lokal jika offline
Pengguna dapat menjelajahi artikel, melakukan pencarian, dan menambahkan bookmark
Status bookmark dipertahankan antara sesi dan diintegrasikan secara mulus dengan berita terbaru
Saat koneksi terputus, layout peringatan menutupi UI hingga konektivitas pulih
Struktur Proyek
Models: Definisi struktur data seperti Article dan Source
Database: Komponen Room untuk persistensi data termasuk migrasi database
Network: Client Retrofit dan definisi API untuk komunikasi dengan News API
Fragments: UI untuk Home, Search, dan Bookmark
Utils: Utilitas termasuk pemantau koneksi dan helper lainnya
