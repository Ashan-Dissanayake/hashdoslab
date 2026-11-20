# Threat Model

## 1. Overview

HashDoS-Lab is a security research and demonstration project designed to investigate Hash-based Denial of Service (HashDoS) vulnerabilities.

The project demonstrates how specially crafted hash-collision inputs can degrade the performance of hash-based data structures and potentially affect application availability.

It follows a controlled security testing approach:

1. Identify the hash-collision vulnerability.
2. Generate controlled collision inputs.
3. Reproduce the performance degradation.
4. Measure the impact.
5. Implement mitigation strategies.
6. Verify the effectiveness of the mitigations.

## 2. Security Objective

The primary security objective is to understand and demonstrate how an attacker can exploit predictable hash collisions to increase computational complexity and consume excessive CPU resources.

The project aims to:

- Demonstrate the performance impact of hash collisions.
- Compare vulnerable and hardened hash table implementations.
- Evaluate the effectiveness of request-level protections.
- Identify residual risks that remain after mitigation.

## 3. System Under Test

The application contains three hash table implementations:

| Component | Purpose |
|---|---|
| VulnerableHashTable | Demonstrates inefficient collision handling using separate chaining. |
| JavaHashMapAdapter | Provides a baseline using Java's built-in HashMap implementation. |
| HardenedHashTable | Demonstrates collision-resistant bucket handling using TreeMap. |

The application exposes REST endpoints for collision analysis and performance comparison.

The request-level protection layer includes:

- IP-based request rate limiting.
- Request body size validation.

## 4. Assets to Protect

The primary assets considered in this threat model are:

- **CPU resources:** Prevent excessive computation caused by collision-heavy inputs.
- **Application availability:** Maintain the application's ability to process legitimate requests.
- **Request-processing capacity:** Prevent expensive requests from overwhelming application resources.
- **Application responsiveness:** Reduce excessive processing delays and request backlogs.

## 5. Threat Actor

The assumed threat actor is an unauthenticated external client who can send HTTP requests to publicly accessible analysis endpoints.

The attacker is assumed to:

- Understand or discover the application's request structure.
- Submit crafted input values.
- Send repeated requests to the application.
- Attempt to consume excessive server-side processing resources.

No privileged application access is assumed.

## 6. Attack Surface

The primary attack surface is the REST API endpoint:

`POST /api/v1/analysis/compare-engines`

This endpoint accepts a key count and performs insertion operations using generated keys across multiple hash table implementations.

The endpoint is relevant because collision-heavy inputs can cause