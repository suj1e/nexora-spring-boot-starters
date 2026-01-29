# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a multi-module Gradle project providing Spring Boot starters for microservice infrastructure. Each starter is an auto-configuration library that provides zero-configuration functionality for common concerns (caching, messaging, resilience, security, etc.).

**Group ID:** `com.nexora`
**Version:** `1.0.0`
**Java Version:** 21
**Project:** nexora-spring-boot-starters

## Module Architecture

The project follows a clear separation pattern:

- **nexora-dependencies** - BOM for unified version management (imports Spring Boot dependencies)
- **nexora-starter** - Convenience module that aggregates all other starters
- Individual feature starters (each is independent and auto-configures via Spring Boot)

### Auto-Configuration Pattern

Each starter uses Spring Boot's `@AutoConfiguration` mechanism:

1. Auto-configuration classes are in `.../autoconfigure/` packages
2. Registrations are in `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
3. Conditional activation uses:
   - `@ConditionalOnClass` - only activate if dependencies are present
   - `@ConditionalOnMissingBean` - allow user override
   - `@ConditionalOnProperty` - enable/disable via configuration

### Starter Responsibilities

| Starter | Key Components |
|---------|----------------|
| **nexora-id-starter** | `SnowflakeIdGenerator` - distributed ID generation with configurable worker/datacenter IDs |
| **nexora-web-starter** | `ResponseWrapperAspect` - wraps controller responses in `Result<T>`, `GlobalExceptionHandler` - unified error handling |
| **nexora-redis-starter** | Multi-level caching (Caffeine L1 + Redis L2), JSON serialization, per-cache TTL |
| **nexora-kafka-starter** | `EventPublisher` for messaging, DLQ error handler, `OutboxEvent` entity for transactional outbox pattern |
| **nexora-resilience-starter** | CircuitBreaker, Retry, TimeLimiter registries using Resilience4j with event logging |
| **nexora-security-starter** | `JwtTokenProvider` for JWT creation/validation, `Encryptor` using Jasypt for config encryption |

## Development Commands

### Build

```bash
./gradlew build
```

### Build specific module

```bash
./gradlew :nexora-id-starter:build
```

### Clean build

```bash
./gradlew clean build
```

### Publish to local Maven repository

```bash
./gradlew publishToMavenLocal
```

## Key Architectural Patterns

### Configuration Properties

Each starter defines its own `@ConfigurationProperties` class (e.g., `SnowflakeProperties`, `KafkaProperties`, `RedisProperties`). Properties follow the naming pattern:

```yaml
snowflake:          # ID generation
common:
  kafka:            # Kafka features
  redis:            # Cache configuration
  resilience:       # Resilience4j settings
  security:         # JWT/encryption settings
```

### Component Scanning

Auto-configurations use `@ComponentScan` with `basePackageClasses` to discover components:
- Kafka: `EventPublisher` in `com.nexora.kafka.publisher`
- Outbox: `OutboxEvent` entity scanned via `@EntityScan`

### Event Listeners

Resilience4j event listeners (`CircuitBreakerEventLogger`, `RetryEventLogger`) are registered in a separate `EventListenerAutoConfiguration` to avoid circular dependencies.

## Adding a New Starter

1. Create module directory with `build.gradle.kts`
2. Add to `settings.gradle.kts` include list
3. Create auto-configuration class in `.../autoconfigure/` package
4. Create `@ConfigurationProperties` class if needed
5. Register in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
6. Add to `nexora-starter/build.gradle.kts` dependencies if it should be included in the aggregate
