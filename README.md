# HashDoS-Lab

**A Java Spring Boot security research lab for demonstrating Hash-based Denial of Service (HashDoS) vulnerabilities, measuring their performance impact, and evaluating practical mitigation strategies.**

---

## Overview

HashDoS-Lab is a security research and demonstration project that explores how specially crafted hash-collision inputs can degrade the performance of hash-based data structures.

The project implements a deliberately vulnerable hash table to reproduce collision-related performance degradation. It compares this implementation against Java's built-in `HashMap` and a hardened hash table implementation.

In addition to data-structure-level mitigation, the project implements HTTP request-level protections to reduce repeated and oversized request abuse.

The project follows a practical application security workflow:

1. Identify the vulnerability.
2. Reproduce the attack using controlled collision inputs.
3. Measure the performance impact.
4. Implement mitigation strategies.
5. Verify the effectiveness of the mitigations.
6. Document findings and residual risks.

## Problem Statement

Hash-based data structures are commonly used to provide efficient data storage and retrieval.

However, when multiple distinct keys produce the same hash value, they may be assigned to the same bucket.

If collision handling is inefficient, specially crafted inputs can cause operations to require significantly more computation than expected.

An attacker may exploit this behaviour to consume excessive CPU resources, increase request processing time, and potentially degrade application availability.

This project demonstrates how such behaviour can occur and evaluates practical techniques to reduce its impact.

## Project Objectives

- Understand the underlying mechanism of hash collisions in Java.
- Reproduce HashDoS-style behaviour using controlled collision inputs.
- Measure the computational impact of collision-heavy inputs.
- Compare vulnerable, standard, and hardened hash table implementations.
- Implement data-structure-level collision mitigation.
- Implement HTTP request-level protections.
- Verify mitigations through automated tests and controlled experiments.
- Identify and document residual security risks.

## Architecture & Components

The application follows a layered architecture that separates REST API handling, analysis logic, hash table implementations, payload generation, and request-level security protections.

### Core Components

| Component | Responsibility |
|---|---|
| REST API Layer | Exposes endpoints for collision analysis and engine comparison. |
| Analysis Layer | Coordinates experiments and collects performance measurements. |
| VulnerableHashTable | Demonstrates inefficient collision handling using separate chaining. |
| JavaHashMapAdapter | Provides a baseline using Java's built-in HashMap. |
| HardenedHashTable | Uses TreeMap-based buckets to reduce collision-related insertion complexity. |
| CollisionKeyGenerator | Generates distinct Java String keys with identical hash codes. |
| RateLimitFilter | Applies IP-based token bucket rate limiting. |
| RequestBodySizeLimitFilter | Rejects oversized request bodies on the protected endpoint. |

### Request Processing Flow

1. A client sends an HTTP request to an analysis endpoint.
2. Request-level security filters apply the configured protections where applicable.
3. The REST API layer validates and processes the request.
4. The analysis layer generates or obtains the required input keys.
5. The selected hash table implementations process the input.
6. Performance measurements and analysis results are returned as JSON.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Primary programming language |
| Spring Boot 4.1.2 | Application framework |
| Spring Web | REST API development |
| Maven | Dependency management and build automation |
| Bucket4j 8.22.0 | Token bucket rate limiting |
| Caffeine | In-memory rate-limit bucket storage |
| Java Collections Framework | HashMap and TreeMap implementations |
| JUnit | Automated testing |
| Spring MockMvc | REST endpoint and filter testing |

## Security Features

### 1. Hash Collision Demonstration

- Generates controlled Java String hash collisions using the `Aa` and `BB` pattern.
- Demonstrates quadratic insertion behaviour in a deliberately vulnerable hash table.
- Tracks traversal counts to measure collision impact.

### 2. Hash Table Implementations

The project compares three implementations:

| Implementation | Description |
|---|---|
| VulnerableHashTable | Fixed-size bucket array with separate chaining and tail insertion. |
| JavaHashMapAdapter | Adapter around Java's built-in HashMap. |
| HardenedHashTable | Uses TreeMap-based buckets to reduce collision-related traversal costs. |

### 3. Performance Analysis

- Executes warm-up iterations before measurements.
- Performs repeated insertion measurements.
- Calculates average and median insertion times.
- Compares traversal counts and stored entry counts.
- Rotates engine execution order to reduce fixed execution-order bias.

### 4. Request Rate Limiting

- Uses Bucket4j token bucket rate limiting.
- Identifies clients using their remote IP address.
- Configures an initial bucket capacity of 5 tokens.
- Replenishes 10 tokens every 60 seconds.
- Returns HTTP 429 when the configured limit is exceeded.
- Uses Caffeine for in-memory bucket storage.

### 5. Request Body Size Protection

- Enforces a maximum request body size of 1,024 bytes.
- Applies to `POST /api/v1/analysis/compare-engines`.
- Returns HTTP 413 for oversized request bodies.
- Wraps accepted request bodies so downstream Spring MVC components can read them.

### 6. Automated Testing

Automated tests verify:

- Hash table insertion and retrieval behaviour.
- Collision handling and traversal counts.
- Maximum entry count enforcement.
- Rate-limit acceptance and rejection behaviour.
- Independent rate-limit buckets for different client IP addresses.
- Request body size enforcement.
- API response behaviour.

## API Endpoints

The application exposes two analysis endpoints.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/analysis/compare` | Compares traversal behaviour between normal and collision-heavy inputs. |
| POST | `/api/v1/analysis/compare-engines` | Compares insertion performance across the three hash table implementations. |

### 1. Collision Analysis

**Endpoint**

```http
POST /api/v1/analysis/compare
Content-Type: application/json
```

**Request body**

```json
{
  "keyCount": 1000
}
```

This endpoint compares traversal counts for normal and collision-heavy inputs.

**Example response**

```json
{
  "keyCount": 1000,
  "normal": {
    "traversalCount": 31842
  },
  "collision": {
    "traversalCount": 499500
  },
  "traversalDifference": 467658,
  "traversalRatio": 15.686828716789146
}
```

### 2. Engine Performance Comparison

**Endpoint**

```http
POST /api/v1/analysis/compare-engines
Content-Type: application/json
```

**Request body**

```json
{
  "keyCount": 1000
}
```

This endpoint measures insertion performance across:

- VulnerableHashTable
- JavaHashMapAdapter
- HardenedHashTable

**Example response**

```json
{
  "keyCount": 1000,
  "warmupIterations": 5,
  "measurementIterations": 15,
  "vulnerableAverageInsertionTimeNanos": 556739333,
  "vulnerableMedianInsertionTimeNanos": 498703400,
  "javaHashMapAverageInsertionTimeNanos": 63987800,
  "javaHashMapMedianInsertionTimeNanos": 46821900,
  "hardenedAverageInsertionTimeNanos": 47607720,
  "hardenedMedianInsertionTimeNanos": 44940000,
  "vulnerableTraversalCount": 499500,
  "vulnerableEntryCount": 1000,
  "javaHashMapEntryCount": 1000,
  "hardenedEntryCount": 1000
}
```

**Note:** These are observed experimental results for 1,000 collision keys. Execution times depend on the runtime environment and are not universal performance guarantees.

The engine comparison endpoint is protected by the configured rate limiter and request body size filter.


## Getting Started

### Prerequisites

Make sure the following are installed:

- Java Development Kit (JDK) 17
- Git

The project includes Maven Wrapper, so a separate Maven installation is not required.

### Clone the Repository

```bash
git clone https://github.com/Ashan-Dissanayake/hashdoslab.git
cd hashdoslab
```

### Run the Application

On Windows PowerShell:

```powershell
.\mvnw spring-boot:run
```

On Linux or macOS:

```bash
./mvnw spring-boot:run
```

The application starts on:

`http://localhost:8080`

### Run with Docker

#### Prerequisites

Make sure Docker and Docker Compose are installed.

#### Build and Start the Application

Build the Docker image and start the application container:

```bash
docker compose up --build -d
```

The application will be available at:

`http://localhost:8080`

#### Check Container Status

```bash
docker compose ps
```

#### View Application Logs

```bash
docker compose logs -f
```

#### Stop the Application

```bash
docker compose down
```

### Docker Configuration

The project includes the following Docker configuration files:

- `Dockerfile` - Uses a multi-stage build with Maven and Eclipse Temurin JRE 17 to create the application image.
- `.dockerignore` - Excludes unnecessary files from the Docker build context.
- `compose.yaml` - Defines the application container, image build configuration, port mapping, and restart policy.


### Test the API

Use Postman, IntelliJ HTTP Client, or curl to send requests to the endpoints described in the API section.

For the initial performance comparison, use a key count of `1000`.

**Important:** Large collision-heavy inputs can require substantial CPU processing time in the deliberately vulnerable implementation. Avoid using large key counts during initial testing.

## Running Tests

Execute the complete test suite using Maven Wrapper.

**Windows PowerShell**

```powershell
.\mvnw clean test
```

**Linux or macOS**

```bash
./mvnw clean test
```

The test suite covers the core hash table implementations, analysis services, REST endpoints, and request-level security protections.

## Documentation

Detailed technical documentation is available in the `docs/` directory.

| Document | Description |
|---|---|
| [01 - Threat Model](docs/01-threat-model.md) | Security objectives, threat actors, attack surfaces, and threat scenarios. |
| [02 - Attack Reproduction](docs/02-attack-reproduction.md) | Collision generation, attack reproduction steps, and experimental results. |
| [03 - Mitigation Strategy](docs/03-mitigation-strategy.md) | Data-structure hardening and HTTP request-level protections. |
| [04 - Security Findings](docs/04-security-findings.md) | Identified vulnerabilities, evidence, impact, and mitigation results. |
| [05 - Residual Risks](docs/05-residual-risks.md) | Remaining limitations and security considerations. |

## Limitations & Future Improvements

The current implementation has several known limitations:

- No execution timeout or cancellation mechanism for expensive analysis operations.
- Rate-limit state is maintained in an in-memory cache within a single application instance.
- Rate-limit state is not shared across multiple application instances.
- Caffeine cache eviction may reset an individual client's rate-limit history.
- Client IP identification requires appropriate trusted-proxy configuration when deployed behind a reverse proxy.
- Request-level protections are currently scoped to the engine comparison endpoint.
- Benchmark results are environment-dependent and are not produced using a dedicated benchmarking framework such as JMH.

These limitations are intentionally documented as residual risks rather than being presented as solved problems.

## Responsible Use

HashDoS-Lab is intended for educational purposes, controlled security research, and authorized testing.

The vulnerable implementation is deliberately designed to demonstrate inefficient collision handling. Experiments should be performed in a controlled local environment and not against systems without authorization.

## License

License information will be added when the project license is defined.