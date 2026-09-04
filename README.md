# FRESCROW — Spring Boot Core Application Backend 🛡️💼

[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot 4.0.5](https://img.shields.io/badge/Spring_Boot-4.0.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security-JWT_Auth-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Algorand SDK](https://img.shields.io/badge/Algorand-Java_SDK_2.10.1-000000?style=for-the-badge&logo=algorand&logoColor=white)](https://github.com/algorand/java-algorand-sdk)
[![Swagger / OpenAPI](https://img.shields.io/badge/OpenAPI-SpringDoc-85EA2D?style=for-the-badge&logo=openapi-initiative&logoColor=black)](https://swagger.io/)

The **FRESCROW Spring Boot Backend** serves as the central business application service and database persistence layer for the **FRESCROW Milestone Escrow Platform**. It manages nonced Algorand wallet authentication, user profiles, project lifecycles, milestone tracking, freelancer applications, work deliverable submissions, dispute resolutions, payment records, and orchestrates calls to the FastAPI microservice for AI milestone breakdown and Algorand smart contract transaction generation.

---

## 🏗️ System Architecture & Integration

```
               +----------------------------------+
               | Next.js Frontend (Pera Wallet)   |
               +----------------+-----------------+
                                |
                                | REST API + JWT
                                v
      +----------------------------------------------------+
      |            Spring Boot Core Application            |
      |                                                    |
      | - Nonce-based Wallet Verification (Algorand SDK)   |
      | - JWT Auth & Security Filter                       |
      | - Business Logic (Projects, Milestones, Payments)  |
      | - PostgreSQL Database via Spring Data JPA          |
      | - OpenAPI 3 / Swagger Documentation               |
      +-------------------------+--------------------------+
                                |
                                | HTTP REST Client
                                v
               +----------------------------------+
               |  FastAPI AI & Blockchain Engine  |
               |  (PyTeal, Gemini AI, LangChain)  |
               +----------------------------------+
```

---

## 🔑 Core Features & Workflow

### 1. 🔐 Cryptographic Algorand Wallet Auth (Nonce Challenge Response)
1. **Request Nonce**: Client requests a time-bound server-generated nonce for their Algorand address via `GET /auth/nonce?wallet={address}`.
2. **Wallet Signature**: The user signs the nonce using **Pera Wallet** or an Algorand SDK compatible wallet.
3. **Verification**: Frontend sends the public key, signature, and nonce to `POST /auth/verify`. Spring Boot uses `com.algorand.algosdk` to verify signature authenticity and issues a signed **JWT token**.
4. **Protected Routes**: All subsequent project, milestone, submission, and payment requests authenticate via HTTP `Bearer` tokens.

### 2. 📋 Project & AI Milestone Workflow
1. **Post Project Preview**: Client submits project details (`POST /api/projects/generate-milestones-preview`). Spring Boot forwards this to the FastAPI AI service, which uses **Google Gemini 3 Flash** to generate optimal milestone title, budget split, and description.
2. **Confirm Project & Milestones**: Client confirms creation (`POST /api/projects/confirm-create`), saving the project and milestones into PostgreSQL in an un-funded status.
3. **Deploy PyTeal Contract**:
   - `POST /api/projects/{id}/deploy-contract/prepare`: Requests un-signed contract deployment transaction bytes from FastAPI.
   - Client signs transaction via Pera Wallet and broadcasts to Algorand Testnet.
   - `POST /api/projects/deploy-contract/confirm`: Confirms deployment transaction hash, fetches the allocated Algorand `app_id` via FastAPI, and attaches it to the project.
4. **Fund Project Escrow**:
   - `POST /api/projects/{id}/fund/prepare`: Builds transaction group to deposit total project funds + MBR buffer into the contract address.
   - Client signs and submits group transaction.
   - `POST /api/projects/fund/confirm`: Validates funding transaction and marks project status as `FUNDED`.

### 3. 🎯 Milestone Execution & AI Deliverable Review
1. **Submission**: Freelancer submits work evidence (GitHub repo link, IPFS demo, text notes) via `POST /api/submissions`.
2. **AI Deliverable Review**: Client or system triggers AI review (`POST /api/milestones/{id}/ai-evaluate`). Spring Boot delegates to FastAPI's LangChain MCP engine to evaluate the GitHub repo and return a quality score (0-100) with approval recommendations.
3. **Milestone Release**:
   - Client triggers milestone release (`POST /api/payments/release/prepare`).
   - Client signs `approve_and_release` smart contract transaction via Pera Wallet.
   - `POST /api/payments/release/confirm`: Confirms release on-chain, marks milestone `RELEASED`, and logs payment details.

---

## 📁 Repository Structure

```
escrowsystem-springboot-backend/
├── src/main/java/com/highkernel/milestonebackend/
│   ├── ai/          # DTOs & HTTP Client for FastAPI AI service
│   ├── application/ # Project applications from freelancers
│   ├── auth/        # Nonce challenge, wallet verification & JWT security
│   ├── blockchain/  # DTOs & HTTP Client for FastAPI Blockchain service
│   ├── common/      # System health & base utilities
│   ├── config/      # RestTemplate, CORS, Security & Swagger configs
│   ├── dispute/     # Dispute resolution domain logic & controller
│   ├── exception/   # Global exception handling & custom exceptions
│   ├── milestone/   # Milestone entities, DTOs & management
│   ├── payment/     # Payment transaction records & escrow triggers
│   ├── project/     # Project lifecycle, state machine & deployment handlers
│   ├── submission/  # Deliverable submission management
│   └── user/        # User entity, role assignments & profile endpoints
├── src/main/resources/
│   └── application.yml
├── .env.example     # Environment template file
├── Dockerfile       # Container build instructions
├── pom.xml          # Maven dependencies & plugins
└── README.md
```

---

## 📑 API Endpoint Summary

### 🔐 Authentication (`/auth`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/auth/nonce?wallet={address}` | Generate time-bound nonce challenge for wallet signature |
| `POST` | `/auth/verify` | Verify wallet signature & issue JWT Bearer token |

---

### 📂 Projects (`/api/projects`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/projects` | Create a new project (draft/unfunded) |
| `POST` | `/api/projects/generate-milestones-preview` | Request AI-generated milestone preview breakdown |
| `POST` | `/api/projects/confirm-create` | Create project with confirmed milestones |
| `POST` | `/api/projects/{id}/deploy-contract/prepare` | Request PyTeal deployment transaction bytes |
| `POST` | `/api/projects/deploy-contract/confirm` | Confirm deployment transaction & store `app_id` |
| `POST` | `/api/projects/{id}/fund/prepare` | Build escrow funding group transaction |
| `POST` | `/api/projects/fund/confirm` | Confirm project escrow funding |
| `GET` | `/api/projects/me/owned` | Get projects created by logged-in client |
| `GET` | `/api/projects/me/workspace` | Get projects assigned to logged-in freelancer |
| `GET` | `/api/projects/with-milestones` | Fetch all active marketplace projects with milestones |

---

### 🚩 Milestones (`/api/milestones`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/milestones/project/{projectId}` | List all milestones for a specific project |
| `POST` | `/api/milestones/{id}/ai-evaluate` | Trigger AI review on milestone deliverable |
| `PATCH` | `/api/milestones/{id}/status` | Update milestone status (PENDING, SUBMITTED, APPROVED, DISPUTED) |

---

### 📤 Submissions (`/api/submissions`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/submissions` | Freelancer submits milestone work deliverable |
| `GET` | `/api/submissions/milestone/{milestoneId}` | View deliverable submission for a milestone |

---

### 💳 Payments (`/api/payments`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/payments/release/prepare` | Build milestone release transaction for smart contract |
| `POST` | `/api/payments/release/confirm` | Confirm milestone release payment execution |
| `GET` | `/api/payments/project/{projectId}` | Retrieve transaction payment log for a project |

---

### ⚖️ Disputes (`/api/disputes`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/disputes` | Raise a dispute on a milestone |
| `GET` | `/api/disputes/project/{projectId}` | List dispute tickets for a project |

---

## 🛠️ Prerequisites & Configuration

### Prerequisites
- **Java JDK**: `17` or higher
- **Maven**: `3.8+` (or use bundled `./mvnw`)
- **PostgreSQL Database**: Version 13+

### Environment Setup (`.env`)
Copy `.env.example` to `.env` and fill in configuration parameters:

```env
# Server
PORT=8080

# Database Connection
DB_URL=jdbc:postgresql://localhost:5432/frescrow_db
DB_USER=postgres
DB_PASS=postgres

# Security JWT Key (Base64 Encoded Secret Key)
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION_MS=86400000
NONCE_EXPIRY_SECONDS=300

# AI & Blockchain FastAPI Service
AI_VALIDATOR_BASE_URL=http://localhost:8000
AI_GENERATE_MILESTONES_PATH=/api/v1/ai/generate-milestones
AI_EVALUATE_PATH=/api/v1/ai/evaluate

BLOCKCHAIN_BASE_URL=http://localhost:8000
BLOCKCHAIN_DEPLOY_CONTRACT_PATH=/api/v1/blockchain/deploy-contract
BLOCKCHAIN_GET_APP_ID_PATH=/api/v1/blockchain/get-app-id
BLOCKCHAIN_FUND_PROJECT_PATH=/api/v1/blockchain/fund-project
BLOCKCHAIN_RELEASE_MILESTONE_PATH=/api/v1/blockchain/release-milestone
```

---

## 🏃 Local Development & Execution

### Running locally with Maven:
```bash
# Clean and compile
./mvnw clean install

# Run application
./mvnw spring-boot:run
```

### Access Documentation:
- **OpenAPI UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Docs**: `http://localhost:8080/v3/api-docs`
- **Health Check**: `http://localhost:8080/api/v1/health`

---

## 🐳 Docker Deployment

Build and run using Docker:

```bash
# Build image
docker build -t frescrow-springboot-backend .

# Run container
docker run -p 8080:8080 --env-file .env frescrow-springboot-backend
```
