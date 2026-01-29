# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Multi-module Gradle project providing Spring Boot starters for microservice infrastructure. Each starter is an auto-configuration library with zero-configuration functionality.

**Group ID:** `com.nexora`
**Version:** `1.0.0`
**Java Version:** 21
**Project:** nexora-spring-boot-starters

## Module Architecture

### Auto-Configuration Pattern

Each starter uses Spring Boot's `@AutoConfiguration` mechanism:

1. Auto-configuration classes in `.../autoconfigure/` packages
2. Registrations in `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
3. Conditional activation:
   - `@ConditionalOnClass` - only activate if dependencies present
   - `@ConditionalOnMissingBean` - allow user override
   - `@ConditionalOnProperty` - enable/disable via configuration

### Starter Responsibilities

| Starter | Auto-Configuration | Key Components | Properties Prefix |
|---------|-------------------|----------------|-------------------|
| **nexora-spring-boot-starter-web** | `CommonWebAutoConfiguration` | `ResponseWrapperAspect`, `GlobalExceptionHandler`, `Result<T>`, `BusinessException` | None (uses defaults) |
| **nexora-spring-boot-starter-redis** | `RedisCacheAutoConfiguration`, `CaffeineAutoConfiguration` | `RedisCacheManager`, `CaffeineCacheManager`, `CacheHelper` | `nexora.redis` |
| **nexora-spring-boot-starter-kafka** | `KafkaAutoConfiguration`, `KafkaDlqAutoConfiguration` | `EventPublisher`, `OutboxEvent`, DLQ handler | `nexora.kafka` |
| **nexora-spring-boot-starter-resilience** | `ResilienceAutoConfiguration`, `EventListenerAutoConfiguration` | `CircuitBreakerRegistry`, `RetryRegistry`, `TimeLimiterRegistry`, Event loggers | `nexora.resilience` |
| **nexora-spring-boot-starter-security** | `SecurityAutoConfiguration`, `JasyptAutoConfiguration` | `JwtTokenProvider`, `Encryptor` | `nexora.security` |

## Development Commands

### Build

```bash
./gradlew build
```

### Build specific module

```bash
./gradlew :nexora-spring-boot-starter-web:build
```

### Clean build

```bash
./gradlew clean build
```

### Publish to local Maven repository

```bash
./gradlew publishToMavenLocal
```

## Configuration Properties

All properties use the `nexora.*` prefix pattern:

```yaml
nexora:
  redis:
    enabled: true
    cache-default-ttl: 30m
    cache-ttl-mappings: {}
    use-cache-prefix: true
    key-prefix: ""
    cache-null-values: true
    enable-caffeine: true
    caffeine-spec: "maximumSize=1000,expireAfterWrite=5m"

  kafka:
    dlq:
      enabled: true
      retry-attempts: 3
    outbox:
      enabled: false

  resilience:
    circuit-breaker:
      enabled: true
      failure-rate-threshold: 50
      sliding-window-size: 10
      minimum-number-of-calls: 5
      wait-duration-in-open-state: 10s
      permitted-number-of-calls-in-half-open-state: 3
      instance-configs: {}
    retry:
      enabled: true
      max-attempts: 3
      wait-duration: 1s
      enable-exponential-backoff: false
      exponential-backoff-multiplier: 2.0
    time-limiter:
      enabled: false
      timeout-duration: 5s
    rate-limiter:
      enabled: false
      limit-for-period: 10
      limit-refresh-period: 1s
      timeout-duration: 5s

  security:
    jasypt:
      enabled: false
      password: ${JASYPT_PASSWORD}
      algorithm: PBEWITHHMACSHA512ANDAES_256
      key-obtention-iterations: 1000
      pool-size: 1
    jwt:
      enabled: false
      secret: ${JWT_SECRET}
      expiration: 1h
      refresh-expiration: 7d
      issuer: nexora-auth
      audience: nexora-api
```

## Key Architectural Patterns

### Web Starter

- `@ConditionalOnWebApplication` - only activates in web apps
- `@EnableAspectJAutoProxy` - enables AOP for response wrapping
- `ResponseWrapperAspect` - wraps all `@RestController` responses in `Result<T>`
- `GlobalExceptionHandler` - `@RestControllerAdvice` for unified error handling

Exception mappings:
- `BusinessException` → 400
- `MethodArgumentNotValidException` → 400
- `ConstraintViolationException` → 400
- `IllegalArgumentException` → 400
- `IllegalStateException` → 400
- `EntityNotFoundException` → 404
- `Exception` → 500

### Redis Starter

- **Multi-level caching**: Caffeine L1 (local) + Redis L2 (distributed)
- **JSON serialization** with Jackson `JavaTimeModule`
- **Per-cache TTL** via `cache-ttl-mappings`
- **Transaction-aware** cache manager
- **Key prefix** support for multi-environment isolation

Auto-configurations:
- `RedisCacheAutoConfiguration` - activates when `RedisConnectionFactory` present, `nexora.redis.enabled=true`
- `CaffeineAutoConfiguration` - activates when `Caffeine` present, `nexora.redis.enable-caffeine=true`

### Kafka Starter

- **Transactional publishing** via `EventPublisher`
- **DLQ** with fixed backoff (1s, 3 attempts)
- **Outbox pattern** for reliable event publishing (requires JPA)

Auto-configurations:
- `KafkaAutoConfiguration` - `@EnableKafka`, component scan for `EventPublisher`
- `KafkaDlqAutoConfiguration` - DLQ error handler when `nexora.kafka.dlq.enabled=true`

Non-retryable exceptions: `IllegalArgumentException`, `DeserializationException`

DLQ topic naming: `{original-topic}.dlq`

### Resilience Starter

- **CircuitBreaker** - prevents cascading failures
- **Retry** - with exponential backoff support
- **TimeLimiter** - timeout protection
- **RateLimiter** - rate limiting (disabled by default)

Auto-configurations:
- `ResilienceAutoConfiguration` - creates registry beans
- `EventListenerAutoConfiguration` - registers event listeners via `@PostConstruct` to avoid circular dependencies

Event listeners: `CircuitBreakerEventLogger`, `RetryEventLogger`

### Security Starter

- **JWT** - generation, validation, refresh token support
- **Jasypt** - property encryption with `ENC()` wrapper

Auto-configurations:
- `SecurityAutoConfiguration` - nested `JwtTokenProviderConfiguration`, `EncryptorConfiguration`
- `JasyptAutoConfiguration` - `StandardPBEStringEncryptor` when `nexora.security.jasypt.enabled=true`

## Component Scanning

Auto-configurations use `@ComponentScan` with `basePackageClasses`:
- Kafka: `EventPublisher` in `com.nexora.kafka.publisher`
- Outbox: `OutboxEvent` entity scanned via `@EntityScan` when JPA available

## Adding a New Starter

1. Create module directory with `build.gradle.kts`
2. Add to `settings.gradle.kts` include list
3. Create auto-configuration class in `.../autoconfigure/` package
4. Create `@ConfigurationProperties` class if needed
5. Register in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## Common Patterns

- All use `lombok` for reducing boilerplate (`compileOnly` + `annotationProcessor`)
- All use `spring-boot-configuration-processor` for metadata generation
- All use `jackson-databind` for JSON serialization
- All use `slf4j` for logging
- All have comprehensive JavaDoc with usage examples
- All conditional on class presence and property enable/disable
