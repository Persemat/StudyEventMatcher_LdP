package com.example.backend.integration;
// package com.example.backend.integration;

// import org.junit.jupiter.api.Tag;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.context.DynamicPropertyRegistry;
// import org.springframework.test.context.DynamicPropertySource;
// import org.testcontainers.containers.MySQLContainer;
// import org.testcontainers.junit.jupiter.Container;
// import org.testcontainers.junit.jupiter.Testcontainers;

// @Tag("integration")
// @Testcontainers
// @SpringBootTest
// @AutoConfigureMockMvc
// @ActiveProfiles("test")
// public abstract class BaseIntegrationIT {

//     @Container
//     static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
//             .withDatabaseName("testdb")
//             .withUsername("test")
//             .withPassword("test");

//     @DynamicPropertySource
//     static void configure(DynamicPropertyRegistry registry) {
//         registry.add("spring.datasource.url", mysql::getJdbcUrl);
//         registry.add("spring.datasource.username", mysql::getUsername);
//         registry.add("spring.datasource.password", mysql::getPassword);
//     }
// }