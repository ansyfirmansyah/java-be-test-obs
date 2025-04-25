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
- Docker containerization

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
├── Dockerfile             # Docker build config
├── docker-compose.yml     # Docker compose config
├── pom.xml                # Maven build config
└── README.md              # Project documentation
```