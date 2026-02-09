# Distributed Rate Limiter

A robust, distributed rate limiting solution built with Spring Boot and Redis. This application provides flexible rate limiting capabilities with support for multiple strategies and dynamic configuration.

## Features

- **Distributed Rate Limiting**: Leverages Redis for shared state across multiple application instances
- **Multiple Strategies**: 
  - Fixed Window: Simple time-window based rate limiting
  - Sliding Window: More accurate rate limiting with overlapping windows
- **Annotation-Based Configuration**: Use `@RateLimit` and `@RateLimiterDynamic` annotations to easily apply rate limiting to endpoints
- **Dynamic Configuration**: Update rate limit settings at runtime through the admin API without redeploying
- **Spring Security Integration**: Built-in security configuration for protecting admin endpoints
- **AOP-Based Implementation**: Clean separation of concerns using aspect-oriented programming

## Requirements

- Java 21+
- Maven 3.6+
- Redis 7.0+
- Docker & Docker Compose (for containerized deployment)

## Project Structure

```
rate-limiter/
├── src/main/java/com/example/rate_limiter/
│   ├── Annotations/           # Rate limiting annotations
│   ├── Aspect/               # AOP aspects for rate limit enforcement
│   ├── Config/               # Spring configuration classes
│   ├── Controller/           # REST API endpoints
│   ├── dto/                  # Data transfer objects
│   ├── exception/            # Custom exceptions
│   ├── Filter/               # Request filters
│   ├── Service/              # Business logic
│   └── strategy/             # Rate limiting strategy implementations
├── src/main/resources/       # Configuration files
├── docker-compose.yml        # Docker Compose configuration
└── pom.xml                   # Maven dependencies
```

## Quick Start

### Using Docker Compose (Recommended)

```bash
# Start Redis and the application
docker-compose up -d

# Application runs on http://localhost:8080
```

### Local Development

```bash
# Start Redis (ensure it's running on port 6379)
redis-server

# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/rate-limiter-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Test Endpoints

- **GET `/api/fixed`** - Fixed Window strategy (5 requests per 120 seconds)
  ```bash
  curl http://localhost:8080/api/fixed
  ```
  Response: `Free tier - 5 requests per minute (Fixed Window)`

- **GET `/api/sliding`** - Sliding Window strategy (20 requests per 60 seconds)
  ```bash
  curl http://localhost:8080/api/sliding
  ```
  Response: `Premium tier - 20 requests per minute (Sliding Window)`

- **GET `/api/unlimited`** - No annotation (default global filter limit)
  ```bash
  curl http://localhost:8080/api/unlimited
  ```

### Dynamic Rate Limit Endpoints

- **GET `/api/free`** - Free tier with dynamic configuration
  ```bash
  curl http://localhost:8080/api/free
  ```

- **GET `/api/premium`** - Premium tier with dynamic configuration
  ```bash
  curl http://localhost:8080/api/premium
  ```

### Admin Configuration Endpoints

- **GET `/api/admin/config/{endpoint}`** - Get rate limit config
  ```bash
  curl http://localhost:8080/api/admin/config/free
  ```

- **PUT `/api/admin/config/{endpoint}`** - Update rate limit config
  ```bash
  curl -X PUT http://localhost:8080/api/admin/config/free \
    -H "Content-Type: application/json" \
    -d '{"requests": 10, "windowSeconds": 60, "strategy": "SLIDING_WINDOW"}'
  ```

- **DELETE `/api/admin/config/{endpoint}`** - Delete rate limit config
  ```bash
  curl -X DELETE http://localhost:8080/api/admin/config/free
  ```

### Health Check

- **GET `/api/health`** - Application health status
  ```bash
  curl http://localhost:8080/api/health
  ```

## Usage

### Static Rate Limiting with Annotations

Apply the `@RateLimit` annotation to any controller method:

```java
@RestController
@RequestMapping("/api")
public class MyController {
    
    @RateLimit(requests = 10, windowSeconds = 60, strategy = StrategyType.FIXED_WINDOW)
    @GetMapping("/my-endpoint")
    public String myEndpoint() {
        return "Limited to 10 requests per 60 seconds";
    }
}
```

#### Annotation Parameters

- **requests**: Number of allowed requests within the time window
- **windowSeconds**: Time window duration in seconds
- **strategy**: Rate limiting strategy (`FIXED_WINDOW` or `SLIDING_WINDOW`)

### Dynamic Rate Limiting

Use the `@RateLimiterDynamic` annotation for endpoints with runtime-configurable limits:

```java
@RateLimiterDynamic(requests = 5, windowSeconds = 120, strategy = StrategyType.SLIDING_WINDOW)
@GetMapping("/api/premium")
public String premiumEndpoint() {
    return "Premium tier with dynamic limits";
}
```

Update limits via the admin API without restarting the application.

## Configuration

### application.properties

```properties
spring.application.name=rate-limiter
spring.redis.host=localhost
spring.redis.port=6379
spring.profiles.active=dev
```

### Environment-Specific Profiles

- **application-dev.properties**: Development configuration
- **application-prod.properties**: Production configuration

Activate profiles using: `SPRING_PROFILES_ACTIVE=prod`

## Rate Limiting Strategies

### Fixed Window
- Simple time-based windowing
- Resets at fixed intervals
- Suitable for basic rate limiting needs

### Sliding Window
- Uses overlapping windows for more accurate limiting
- Prevents burst requests at window boundaries
- Recommended for strict rate limiting requirements

## Architecture

### Components

1. **Annotations**: Define rate limit rules on controller methods
2. **Aspects**: AOP interceptors that enforce rate limiting logic
3. **Strategies**: Pluggable implementations for different rate limiting algorithms
4. **Redis Config**: Manages Redis connection and serialization
5. **Admin Service**: Handles dynamic configuration CRUD operations
6. **Security Config**: Protects admin endpoints

### Flow

```
Request
  ↓
Spring Security Filter
  ↓
AOP Aspect (RateLimitAspect)
  ↓
Strategy Check (Redis)
  ↓
Allow/Reject + Response
```

## Development

### Building

```bash
mvn clean package
```

### Running Tests

```bash
mvn test
```

### Building Docker Image

```bash
docker build -f dockerfile.dev -t rate-limiter:latest .
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| SPRING_PROFILES_ACTIVE | dev | Active Spring profile |
| SPRING_REDIS_HOST | localhost | Redis hostname |
| SPRING_REDIS_PORT | 6379 | Redis port |
| SERVER_PORT | 8080 | Application port |

## Troubleshooting

### Redis Connection Error
- Ensure Redis is running: `redis-cli ping`
- Check Redis host and port in `application.properties`
- Verify network connectivity if using Docker

### Rate Limit Not Working
- Confirm the `@RateLimit` annotation is present on the method
- Check that Redis is properly configured and accessible
- Review application logs for errors

### Dynamic Configuration Not Updating
- Verify the admin endpoint is accessible
- Ensure the endpoint name in the request matches the configuration key
- Check Redis for stored configuration values: `redis-cli`

## Performance Considerations

- Redis operations are non-blocking and optimized for high throughput
- Use sliding window strategy for more accurate but slightly higher CPU usage
- Fixed window strategy offers faster performance with lower overhead

## Security

- Admin endpoints are protected by Spring Security
- Configure credentials in your application properties
- Use HTTPS in production environments
- Restrict admin API access to trusted network ranges

## Monitoring

### Health Endpoint
Check application status: `/api/health`

### Redis Monitoring
```bash
redis-cli
> INFO stats
> MONITOR
```

## Contributing

1. Create a feature branch
2. Make your changes
3. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For issues, questions, or contributions, please open an issue on the repository.
