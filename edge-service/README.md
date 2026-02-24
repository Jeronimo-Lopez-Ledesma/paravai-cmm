# Edge Service – API Gateway for Shared Services

This project implements a centralized API Gateway for DEKRA's Shared Services ecosystem, using **Spring Cloud Gateway** with a reactive, resilient, and secure architecture.

## Key Features

- **Reactive API Gateway** using Spring WebFlux
- **Resilience** with Circuit Breaker and Retry policies (Resilience4j)
- **Observability** via Actuator, Prometheus, and Grafana dashboards
- **Security** via JWT validation (internal tokens derived from Azure AD)
- **Route-based** forwarding with fallback mechanisms

---

## Technologies

- **Java 17**
- **Spring Boot 3.4.4**
- **Spring Cloud Gateway**
- **Resilience4j**
- **Micrometer + Prometheus**
- **Grafana Dashboards**
- **JWT (custom token derived from Azure AD)**

---
