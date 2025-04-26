# Stationery Store Microservice

Backend microservice sederhana untuk mengelola inventory dan order toko alat tulis.

## Tech Stack

- **Bahasa**: Java 21 dengan Spring Boot 3
- **Database**: H2 Database (in-memory) dengan initial data
- **Auth**: Basic Authentication

## Fitur

- CRUD operasi untuk Item, Inventory, dan Order
- Validasi stock saat membuat order baru
- Transaction management
- Error dan exception handling
- Validasi input
- Logging
- Unit testing dengan JUnit

## Struktur Project

```
stationery-store/
├── src/
│   ├── main/java/com/stationery/
│   │   ├── config/        # Konfigurasi aplikasi
│   │   ├── controller/    # REST controllers
│   │   ├── dto/           # Data Transfer Objects
│   │   ├── entity/        # Entity classes
│   │   ├── enums/         # Enum types
│   │   ├── exception/     # Exception handling
│   │   ├── repository/    # Repository interfaces
│   │   ├── service/       # Business logic
│   │   └── util/          # Utility classes
│   ├── resources/         # Properties dan configurations
│   └── test/              # Unit tests
├── pom.xml                # Maven build config
└── README.md              # Project documentation
```

## Model Data

1. **Item**
    - `id` (int, primary key)
    - `name` (string)
    - `price` (double)

2. **Order**
    - `id` (UUID, primary key)
    - `order_no` (string, auto-generated dengan format Oxxx)
    - `item_id` (foreign key ke tabel Item)
    - `qty` (int)
    - `price` (double)

3. **Inventory**
    - `id` (int, primary key)
    - `item_id` (foreign key ke tabel Item)
    - `qty` (int)
    - `type` (Enum: T / W) - Top-up atau Withdrawal
    - `order_id` (nullable, foreign key ke tabel Order)

## REST API Endpoints

### Items API
- `GET /api/items` - Daftar semua Item dengan pagination
- `GET /api/items/{id}` - Detail Item dengan stock tersisa
- `POST /api/items` - Buat Item baru
- `PUT /api/items/{id}` - Update Item
- `DELETE /api/items/{id}` - Hapus Item (jika tidak memiliki inventory)

### Inventory API
- `GET /api/inventories` - Daftar semua Inventory dengan pagination
- `GET /api/inventories/{id}` - Detail Inventory
- `GET /api/inventories/item/{itemId}` - Daftar Inventory untuk Item tertentu
- `GET /api/inventories/stock/{itemId}` - Cek stock untuk Item tertentu
- `POST /api/inventories` - Buat Inventory baru (Top-up atau Withdrawal)
- `PUT /api/inventories/{id}` - Update Inventory (jika tidak terkait order)
- `DELETE /api/inventories/{id}` - Hapus Inventory (jika tidak terkait order)

### Orders API
- `GET /api/orders` - Daftar semua Order dengan pagination
- `GET /api/orders/{id}` - Detail Order
- `GET /api/orders/item/{itemId}` - Daftar Order untuk Item tertentu
- `POST /api/orders` - Buat Order baru (dengan validasi stock)
- `PUT /api/orders/{id}` - Update Order
- `DELETE /api/orders/{id}` - Hapus Order

## Authentication

Aplikasi ini menggunakan Basic Authentication untuk semua endpoints.
Default credentials:
- Username: `admin`
- Password: `admin123`

## Running Locally

### Dengan Maven

```bash
# Compile dan run tests
mvn clean package

# Run aplikasi
mvn spring-boot:run
```

## H2 Console

H2 Database console tersedia di:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:stationerydb`
- Username: `sa`
- Password: `password`

### Catatan
1. Pagination di Spring Boot dimulai dari 0 (zero-based) 
karena mengikuti konvensi dari Java dan banyak framework pengembangan software.
Spring Data menggunakan Pageable dengan parameter page yang merepresentasikan nomor halaman (dimulai dari 0)
Jadi page=0 adalah halaman pertama, page=1 adalah halaman kedua, dan seterusnya
