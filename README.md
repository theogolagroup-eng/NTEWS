# NTEWS - Threat Intelligence Command Dashboard

Real-time threat intelligence platform providing situational awareness and predictive analytics.

## Architecture

- **Backend**: Java Spring Boot microservices
- **AI Engine**: Python with FastAPI
- **Frontend**: React with TypeScript and Ant Design
- **Data Layer**: PostgreSQL, Redis
- **Streaming**: Apache Kafka

## Services

- **API Gateway** (Port 8080): Central routing and load balancing
- **Auth Service** (Port 8081): Authentication and authorization
- **Intelligence Service** (Port 8082): Threat intelligence processing
- **Prediction Service** (Port 8083): Risk forecasting and analytics
- **Alert Service** (Port 8084): Alert management and notifications
- **Ingestion Service** (Port 8085): Data ingestion and processing
- **AI Engine** (Port 8000): Machine learning predictions
- **Frontend Dashboard** (Port 3000): Web interface

## Quick Start

```bash
# Clone the repository
git clone https://github.com/NTEWS2026/NTEWS2026.git
cd NTEWS2026

# Start all services
./start-local.bat

# Access the dashboard
http://localhost:3000
```

## Project Structure

```
├── backend/                 # Spring Boot microservices
│   ├── api-gateway/
│   ├── auth-service/
│   ├── intelligence-service/
│   ├── prediction-service/
│   ├── alert-service/
│   └── ingestion-service/
├── ai-engine/              # Python FastAPI service
├── frontend-dashboard/      # React TypeScript frontend
├── infra/                  # Infrastructure configurations
└── scripts/               # Utility scripts
```

## Development

Each service can be started independently:

```bash
# Backend services
cd backend/[service-name]
./gradlew bootRun

# AI Engine
cd ai-engine
.venv\Scripts\Activate.ps1
uvicorn model_integration:app --host 0.0.0.0 --port 8000

# Frontend
cd frontend-dashboard
npm run dev
```

## License

MIT License
