# Nexora Spring Boot Starters

统一的微服务基础设施组件库，提供开箱即用的自动配置。

## 模块列表

| 模块 | 说明 | 核心组件 |
|------|------|----------|
| **nexora-spring-boot-starter-web** | Web 统一处理 | `ResponseWrapperAspect`, `GlobalExceptionHandler`, `Result<T>`, `BusinessException` |
| **nexora-spring-boot-starter-redis** | 多级缓存 | `RedisCacheManager`, `CaffeineCacheManager`, `CacheHelper` |
| **nexora-spring-boot-starter-kafka** | 消息队列 | `EventPublisher`, `OutboxEvent`, DLQ 错误处理器 |
| **nexora-spring-boot-starter-resilience** | 熔断降级 | `CircuitBreakerRegistry`, `RetryRegistry`, `TimeLimiterRegistry`, 事件监听器 |
| **nexora-spring-boot-starter-security** | 安全工具 | `JwtTokenProvider`, `Encryptor` (Jasypt) |

## 使用方式

### 添加依赖

```gradle
dependencies {
    implementation("com.nexora:nexora-spring-boot-starter-web")
    implementation("com.nexora:nexora-spring-boot-starter-redis")
    implementation("com.nexora:nexora-spring-boot-starter-kafka")
    implementation("com.nexora:nexora-spring-boot-starter-resilience")
    implementation("com.nexora:nexora-spring-boot-starter-security")
}
```

### 配置（可选）

大部分功能零配置开启，可通过配置调整行为：

```yaml
# Redis 多级缓存 (Caffeine L1 + Redis L2)
nexora:
  redis:
    enabled: true
    cache-default-ttl: 30m
    cache-ttl-mappings:
      user-cache: 1h
      product-cache: 10m
    use-cache-prefix: true
    key-prefix: "nexora:"
    cache-null-values: true
    enable-caffeine: true
    caffeine-spec: "maximumSize=1000,expireAfterWrite=5m"

# Kafka + DLQ + Outbox
spring:
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:localhost:9092}
nexora:
  kafka:
    dlq:
      enabled: true
      retry-attempts: 3
    outbox:
      enabled: false  # 开启需添加 JPA 依赖

# Resilience4j 熔断降级
nexora:
  resilience:
    circuit-breaker:
      enabled: true
      failure-rate-threshold: 50
      sliding-window-size: 10
      wait-duration-in-open-state: 10s
    retry:
      enabled: true
      max-attempts: 3
      enable-exponential-backoff: false
    time-limiter:
      enabled: false
      timeout-duration: 5s

# JWT + 配置加密
nexora:
  security:
    jwt:
      enabled: false
      secret: ${JWT_SECRET}
      expiration: 1h
      refresh-expiration: 7d
    jasypt:
      enabled: false
      password: ${JASYPT_PASSWORD}
```

## 模块详解

### Web Starter

- **自动包装 REST 响应**为统一格式 `Result<T>`
- **全局异常处理**，自动映射 HTTP 状态码
- **业务异常基类** `BusinessException`，便于业务错误定义

```java
// 自动包装响应
@GetMapping("/user/{id}")
public Result<User> getUser(@PathVariable Long id) {
    return Result.success(userService.findById(id));
}

// 抛出业务异常
throw new BusinessException("USER_NOT_FOUND", "用户不存在");
```

### Redis Starter

- **多级缓存**：Caffeine (本地 L1) + Redis (分布式 L2)
- **JSON 序列化**：支持任意对象缓存
- **TTL 配置**：支持全局默认和单个缓存配置
- **Key 前缀**：避免多环境 key 冲突

### Kafka Starter

- **事务性发布**：`EventPublisher` 支持事务
- **DLQ 支持**：失败消息自动发送到 `{topic}.dlq`
- **Outbox 模式**：可靠事件发布（需 JPA）

```java
// 发布事件
eventPublisher.publish("user-topic", new UserCreatedEvent(userId));
```

### Resilience Starter

- **熔断器**：防止级联故障
- **重试**：支持指数退避
- **时间限制器**：超时保护
- **事件监听器**：记录状态变化，便于监控

### Security Starter

- **JWT 工具**：生成、验证、刷新 token
- **配置加密**：使用 `ENC()` 加密敏感配置

```java
// JWT 操作
String token = jwtTokenProvider.generateToken(userId, claims);
Claims claims = jwtTokenProvider.getClaims(token);

// 配置加密
app:
  password: ENC(encryptedValueHere)
```

## 开发规范

- 每个 Starter 只解决一类问题
- 依赖 Spring Boot 自动配置机制
- 提供合理的默认值
- 支持外部配置覆盖
- 完整的单元测试
- 详细的文档和示例

## License

Apache License 2.0
