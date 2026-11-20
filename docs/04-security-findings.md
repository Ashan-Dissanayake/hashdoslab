# Security Findings

## 1. Executive Summary

HashDoS-Lab was developed to investigate the impact of hash collision attacks on hash-based data structures and demonstrate practical mitigation strategies.

The assessment identified that the deliberately vulnerable hash table exhibits quadratic insertion behaviour when processing keys with identical hash codes.

Controlled experiments confirmed the expected traversal complexity and demonstrated a significant performance difference between the vulnerable and alternative implementations.

Additional request-level protections were implemented to reduce repeated-request abuse and oversized request payloads.

## 2. Findings Overview

| ID | Finding | Severity | Status |
|---|---|---|---|
| HD-01 | Quadratic insertion complexity under hash collisions | High | Demonstrated and mitigated in hardened implementation |
| HD-02 | Repeated expensive request abuse | Medium | Rate limiting implemented |
| HD-03 | Oversized request payload processing | Medium | Request body size limit implemented |
| HD-04 | Resource exhaustion from computationally expensive valid requests | Medium | Partially mitigated; residual risk remains |

Severity ratings represent the project's controlled lab assessment, not a formal production risk classification.

## 3. HD-01: Quadratic Insertion Complexity

### Description

The vulnerable hash table uses separate chaining with singly linked lists and tail insertion.

When multiple distinct keys produce the same hash code, they are assigned to the same bucket.

Each insertion traverses the existing linked list to locate the insertion position.

As a result, inserting `n` colliding keys requires:

`n(n - 1) / 2`

traversal operations.

### Evidence

The collision analysis experiment used 1,000 generated collision keys.

| Metric | Observed value |
|---|---:|
| Key count | 1,000 |
| Normal input traversal count | 31,842 |
| Collision input traversal count | 499,500 |
| Traversal difference | 467,658 |
| Traversal ratio | 15.69x |

The observed collision traversal count exactly matches the theoretical calculation:

`1000 × 999 / 2 = 499,500`

### Impact

An attacker who can influence the keys processed by a vulnerable hash table may cause excessive CPU consumption and processing delays.

Under repeated or sufficiently expensive requests, this behaviour can contribute to application-level Denial of Service.

### Mitigation

The `HardenedHashTable` uses `TreeMap` buckets instead of linked-list buckets.

This changes collision-bucket insertion behaviour from linear traversal to balanced-tree operations, with expected `O(log b)` complexity per operation for a bucket containing `b` entries.

## 4. HD-02: Repeated Expensive Request Abuse

### Description

The engine comparison endpoint performs computationally expensive operations.

Repeated requests can increase server workload even when each individual request is valid.

### Mitigation

An IP-based token bucket rate limiter was implemented using Bucket4j and Caffeine.

The configured policy allows an initial burst of 5 tokens and replenishes 10 tokens every 60 seconds.

Requests exceeding the available token count receive HTTP 429 Too Many Requests.

### Verification

Automated tests verify:

- Requests within the configured limit are accepted.
- Requests exceeding the limit are rejected.
- Different client IP addresses receive independent buckets.
- Unprotected endpoints and HTTP methods are not unintentionally rate-limited.

## 5. HD-03: Oversized Request Payloads

### Description

Large request bodies can consume memory and processing resources before application-level validation.

### Mitigation

A custom request body size filter was implemented for:

`POST /api/v1/analysis/compare-engines`

The configured maximum body size is 1,024 bytes.

Requests exceeding this limit receive HTTP 413 Payload Too Large.

### Verification

Automated tests verify that:

- Request bodies within the limit are accepted.
- Request bodies exceeding the limit are rejected.
- The rejection response contains the expected Problem Details-style fields.

## 6. HD-04: Expensive Valid Requests

### Description

A request may satisfy input validation and still require substantial computational resources.

The application currently accepts a maximum key count of 10,000 for the analysis operations.

However, an input-size limit alone does not guarantee that processing will finish within an acceptable time.

### Evidence

During testing, a comparison request using 10,000 collision keys remained pending for an extended period and was manually cancelled.

For 1,000 collision keys, the vulnerable implementation performed 499,500 traversal operations per insertion run.

The comparison endpoint executes multiple warm-up and measurement iterations, multiplying the computational workload.

### Mitigation Status

The hardened data structure and request-level rate limiter reduce specific sources of resource consumption.

However, execution time limits and request cancellation are not currently implemented.

This finding therefore remains partially mitigated.

## 7. Performance Comparison

The following results were observed using 1,000 collision keys, 5 warm-up iterations, and 15 measurement iterations.

| Metric | VulnerableHashTable | JavaHashMapAdapter | HardenedHashTable |
|---|---:|---:|---:|
| Average insertion time | 556.74 ms | 63.99 ms | 47.61 ms |
| Median insertion time | 498.70 ms | 46.82 ms | 44.94 ms |
| Entry count | 1,000 | 1,000 | 1,000 |

Based on the observed average insertion times:

- The vulnerable implementation was approximately 11.7 times slower than the hardened implementation.
- The vulnerable implementation was approximately 8.7 times slower than the Java HashMap adapter.
- All three implementations successfully inserted 1,000 entries.

These ratios are specific to the recorded experiment and should not be interpreted as universal performance guarantees.

## 8. Overall Assessment

The experiments successfully demonstrated the impact of predictable hash collisions on the deliberately vulnerable implementation.

The hardened implementation significantly reduced the observed insertion time for the tested collision input.

Request-level rate limiting and request body size validation provide additional protection against repeated requests and oversized payloads.

However, the project does not claim complete protection against all forms of algorithmic complexity attacks or resource exhaustion.

Remaining limitations are documented in `residual-risks.md`.

## 9. Conclusion

HashDoS-Lab demonstrates a complete security testing workflow:

1. Identify a potential algorithmic complexity vulnerability.
2. Reproduce the behaviour using controlled collision inputs.
3. Measure the computational impact.
4. Implement data-structure-level and request-level mitigations.
5. Verify the implementations through automated tests and performance comparisons.
6. Document remaining risks and limitations.

The findings show why input validation alone is insufficient when an application processes attacker-influenced data using computationally expensive algorithms.