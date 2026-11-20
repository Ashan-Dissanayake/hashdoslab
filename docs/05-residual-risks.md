# Residual Risks

## 1. Overview

HashDoS-Lab implements data-structure-level and HTTP request-level protections to reduce the impact of hash collision attacks and excessive request processing.

However, these mitigations do not eliminate every possible resource exhaustion or algorithmic complexity risk.

This document identifies the limitations that remain after implementing the current protections.

## 2. Execution Time and Resource Consumption

### Risk

The application does not currently implement an execution timeout or cancellation mechanism for expensive analysis operations.

Although the API validates the maximum key count, a valid request can still require substantial CPU resources.

During testing, a comparison request using 10,000 collision keys remained pending for an extended period and was manually cancelled.

### Impact

- Excessive CPU consumption.
- Increased request processing latency.
- Request backlog.
- Reduced application responsiveness.
- Potential application-level Denial of Service.

### Current Mitigation

- Hardened hash table implementation.
- Request rate limiting.
- Maximum accepted key count.

### Remaining Risk

Input-size validation does not guarantee a bounded execution time.

Expensive requests can still consume significant resources within the configured input limit.

Execution timeout and cancellation are not currently implemented.

## 3. In-Memory Rate Limiting

### Risk

The rate limiter stores client token buckets in a local Caffeine cache.

Its state is maintained within a single application instance.

### Impact

- Restarting the application resets rate-limit state.
- Multiple application instances maintain independent rate-limit states.
- A client may receive separate token buckets when requests reach different instances.

### Current Mitigation

The Caffeine cache uses:

- Maximum cache size of 10,000 entries.
- Expiration after 10 minutes of inactivity.

### Remaining Risk

Caffeine cache eviction is approximate. If a client's bucket is evicted, its previous rate-limit state is lost and a new bucket may be created.

A distributed rate limiter or shared storage mechanism would be needed for consistent enforcement across multiple application instances.

## 4. Client IP Identification

### Risk

The rate limiter identifies clients using `HttpServletRequest.getRemoteAddr()`.

When the application runs behind a reverse proxy or load balancer, this address may represent the proxy instead of the original client.

### Impact

Multiple clients may share the same rate-limit bucket.

Alternatively, incorrectly trusting client-supplied forwarded headers could allow clients to manipulate their apparent IP addresses.

### Recommended Consideration

Only trust forwarded client IP headers when they are supplied by a controlled and trusted reverse proxy.

## 5. Endpoint-Specific Protection

### Risk

The current rate limiter and request body size filter specifically protect:

`POST /api/v1/analysis/compare-engines`

The protection should not be interpreted as a global request-level security policy covering every endpoint.

### Impact

Other endpoints may have different resource consumption characteristics and may require their own protection policies.

### Recommended Consideration

Evaluate endpoint-specific resource requirements before extending these protections to additional APIs.

## 6. Request Body Size Limitations

### Risk

The custom request body size filter enforces a 1,024-byte limit on the protected endpoint.

This is a lab-specific limit rather than a universally appropriate production value.

The filter reads up to 1,025 bytes to detect an oversized request and rejects the request when the configured limit is exceeded.

### Impact

- Legitimate requests with larger payloads may be rejected.
- The configured limit may need adjustment when request structures change.
- The filter does not replace broader server and infrastructure-level request protections.

### Recommended Consideration

Review the body size limit against legitimate application requirements and consider additional protections at the web server or reverse proxy layer.

## 7. Java HashMap Behaviour

### Risk

The JavaHashMapAdapter is used as a baseline implementation.

Java HashMap implementations may use tree-based collision handling under certain conditions, depending on the JDK implementation and bucket characteristics.

### Impact

Performance behaviour can vary across JDK versions, input distributions, and runtime conditions.

### Recommended Consideration

Do not assume that every hash-based data structure has identical collision behaviour.

Evaluate the actual implementation and runtime environment when assessing collision-related risks.

## 8. Benchmarking Limitations

### Risk

The project uses an in-application timing harness rather than a dedicated benchmarking framework.

### Impact

Measured execution times may be influenced by:

- JVM warm-up and JIT compilation.
- Garbage collection.
- Operating system scheduling.
- Background processes.
- Hardware differences.

### Recommended Consideration

Treat the recorded timing values as experimental observations rather than universal performance guarantees.

For more rigorous benchmarking, a dedicated framework such as JMH could be used.

## 9. Scope Limitations

The current project does not attempt to provide complete protection against:

- Distributed Denial of Service attacks.
- Network-layer resource exhaustion.
- Every possible algorithmic complexity attack.
- Distributed rate-limit bypass across multiple instances.
- All forms of memory and CPU exhaustion.

The project focuses specifically on demonstrating HashDoS behaviour and selected mitigation strategies.

## 10. Final Risk Assessment

The implemented mitigations significantly reduce the impact of the demonstrated collision scenario and provide additional controls against repeated and oversized requests.

However, the application still has limitations around execution time, distributed deployment, client identification, and broader resource protection.

These risks are documented intentionally to distinguish implemented protections from future improvements.

The project demonstrates that effective application security requires not only identifying and mitigating vulnerabilities but also understanding and documenting the risks that remain.