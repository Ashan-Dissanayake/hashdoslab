# Mitigation Strategy

## 1. Overview

HashDoS-Lab implements mitigation strategies at two levels:

1. **Data-structure-level protection:** Reduces the computational complexity caused by hash collisions.
2. **HTTP request-level protection:** Limits excessive or oversized requests before they reach the analysis logic.

The objective is to reduce the impact of collision-heavy inputs while maintaining the ability to demonstrate the original vulnerability.

## 2. Data Structure Hardening

### 2.1 Vulnerable Implementation

The `VulnerableHashTable` uses:

- A fixed-size bucket array.
- Separate chaining with singly linked lists.
- Tail insertion requiring traversal of existing nodes.
- No resizing or treeification.

When multiple keys map to the same bucket, insertion requires traversing the existing linked list.

For `n` colliding keys, the total traversal count is:

`n(n - 1) / 2`

This produces quadratic insertion complexity: `O(n²)`.

### 2.2 Hardened Implementation

The `HardenedHashTable` replaces linked-list buckets with `TreeMap` buckets.

Each bucket stores its entries in a balanced search tree.

**Key implementation details:**

- Maintains a fixed-size bucket array.
- Uses `Math.floorMod(key.hashCode(), buckets.length)` to select a bucket.
- Stores entries inside a `TreeMap` for each bucket.
- Uses natural String ordering to organize entries.
- Enforces a maximum entry count of 10,000.
- Rejects null keys and values.
- Updates existing entries without increasing the total entry count.

**Expected complexity:**

For a bucket containing `b` entries, insertion and lookup take `O(log b)` under normal balanced-tree behaviour.

This prevents the vulnerable implementation's linear bucket traversal from accumulating into quadratic insertion behaviour for the tested collision pattern.

## 3. Request Rate Limiting

### 3.1 Objective

Repeated expensive requests can consume significant application resources even after the underlying data structure has been hardened.

To reduce this risk, the application implements IP-based request rate limiting.

### 3.2 Implementation

The rate limiter uses:

- Bucket4j for token bucket rate limiting.
- Caffeine for in-memory bucket storage.
- A Spring `OncePerRequestFilter` to intercept requests.

The filter protects:

`POST /api/v1/analysis/compare-engines`

### 3.3 Rate Limit Policy

The configured policy is:

| Setting | Value |
|---|---:|
| Initial bucket capacity | 5 tokens |
| Refill amount | 10 tokens |
| Refill period | 60 seconds |
| Bucket expiration after access | 10 minutes |
| Maximum Caffeine cache size | 10,000 entries |

The token bucket allows a limited burst of requests and gradually replenishes tokens over time.

When a client exceeds the available token count, the application returns:

`HTTP 429 Too Many Requests`

The response includes a `Retry-After` header indicating when the client can retry.

The rate-limit response uses the `application/problem+json` media type and a Problem Details-style response body.

### 3.4 Client Identification

The rate limiter identifies clients using:

`HttpServletRequest.getRemoteAddr()`

This uses the remote address visible to the application.

When deployed behind a reverse proxy, the remote address may represent the proxy rather than the original client. Forwarded client IP information should only be trusted when supplied by a controlled and trusted proxy.

## 4. Request Body Size Protection

### 4.1 Objective

Large request bodies can consume memory and processing resources before application-level validation occurs.

To reduce this risk, the application enforces a request body size limit on the engine comparison endpoint.

### 4.2 Implementation

The protection is implemented using:

`RequestBodySizeLimitFilter`

The filter:

- Intercepts POST requests to `/api/v1/analysis/compare-engines`.
- Reads at most 1,025 bytes to determine whether the body exceeds the configured limit.
- Allows request bodies up to 1,024 bytes.
- Rejects bodies larger than 1,024 bytes.
- Wraps accepted request bodies so downstream Spring MVC components can read them.
- Returns `HTTP 413 Payload Too Large` for oversized requests.

The rejection response uses `application/problem+json` and provides a Problem Details-style error response.

## 5. Protection Layer Summary

| Protection | Security objective | Expected result |
|---|---|---|
| TreeMap-based buckets | Reduce collision-related insertion complexity | Avoids linear linked-list traversal within collision-heavy buckets |
| Maximum entry count | Limit the number of stored entries | Rejects inputs exceeding the configured capacity |
| Token bucket rate limiter | Control repeated requests | Rejects requests exceeding the configured rate |
| Request body size limit | Restrict oversized request payloads | Rejects bodies larger than 1 KB |

## 6. Verification

The implemented protections are verified through automated tests.

The tests cover:

- Correct insertion and retrieval behaviour in the hardened hash table.
- Maximum entry count enforcement.
- Rate-limit acceptance and rejection behaviour.
- Independent rate-limit buckets for different client IP addresses.
- Correct HTTP 429 response handling.
- Acceptance of request bodies within the configured size limit.
- Rejection of oversized request bodies with HTTP 413.

Performance comparisons are also performed using the same collision input across the vulnerable and hardened implementations.

## 7. Design Considerations

The project deliberately retains the vulnerable implementation so that the original weakness can be reproduced and compared against the hardened implementation.

The hardened implementation is a demonstration of one mitigation strategy, not a claim that all algorithmic complexity attacks are eliminated.

Likewise, rate limiting and request body size validation reduce specific forms of request abuse but do not guarantee complete protection against resource exhaustion.

Further limitations are documented in `residual-risks.md`.