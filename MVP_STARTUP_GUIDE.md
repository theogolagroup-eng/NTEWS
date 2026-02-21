# NTEWS MVP Startup Guide

This guide provides step-by-step instructions to run the complete NTEWS (National Threat Early Warning System) MVP using **real Kenya security datasets** from ACLED, GTD, and other authoritative sources.

---

## Prerequisites

- **Python 3.8+** (tested on 3.12)
- **Java 17+** (for Spring Boot backend)
- **Node.js 18+** (for Next.js frontend)
- **Docker & Docker Compose** (optional, for containerized deployment)

---

## Quick Start (Minimal - Without Docker)

### Step 1: Download Real Kenya Security Data & Train Models

```powershell
# Navigate to ai-engine directory
cd ai-engine

# Create virtual environment
python -m venv .venv

# Activate virtual environment (Windows PowerShell)
.\.venv\Scripts\Activate.ps1

# Install dependencies
pip install -r requirements_full.txt

# Download NLTK data
python -c "import nltk; nltk.download('punkt'); nltk.download('stopwords'); nltk.download('wordnet'); nltk.download('averaged_perceptron_tagger')"

# Download real Kenya security datasets (ACLED, GTD)
cd data
python download_kenya_security_data.py
cd ..

# Train ML models on real data
python train_models.py
```

After this step, you should see:

**Raw Data** (`data/raw/`):
- `acled_kenya_raw.csv` or `acled_kenya_sample.csv` - ACLED conflict events
- `gtd_kenya_sample.csv` - Global Terrorism Database events

**Processed Data** (`data/processed/`):
- `crisis_data.csv` (~2800 records) - Combined threat text data
- `geospatial_data.csv` (~2800 records) - Location-based security data

**Trained Models** (`models/trained/`):
- `random_forest.pkl`, `gradient_boosting.pkl`, `xgboost.pkl`, etc.
- `tfidf_vectorizer.pkl`, `label_encoder.pkl`
- `metadata.json` (training metadata)

### Step 2: Start the AI Engine API

```powershell
# Still in ai-engine directory with venv activated
python model_integration.py
```

The AI engine will start on **http://localhost:8000**. Test it:
```powershell
# In a new terminal
curl http://localhost:8000/health
```

Expected response:
```json
{"status": "healthy", "models_loaded": 8, "timestamp": "..."}
```

### Step 3: Start Backend Services (Requires Docker for Kafka/DBs)

```powershell
# From project root
docker-compose up -d postgres mongodb redis zookeeper kafka
```

Wait ~30 seconds for services to initialize, then build and run Spring Boot services:

```powershell
# Build all backend services
cd backend
.\mvnw clean package -DskipTests

# Start each service in separate terminals:
# Terminal 1 - API Gateway
cd api-gateway
.\mvnw spring-boot:run

# Terminal 2 - Auth Service
cd auth-service
.\mvnw spring-boot:run

# Terminal 3 - Intelligence Service
cd intelligence-service
.\mvnw spring-boot:run

# Terminal 4 - Prediction Service
cd prediction-service
.\mvnw spring-boot:run

# Terminal 5 - Alert Service
cd alert-service
.\mvnw spring-boot:run

# Terminal 6 - Ingestion Service
cd ingestion-service
.\mvnw spring-boot:run
```

### Step 4: Start Frontend Dashboard

```powershell
cd frontend-dashboard
npm install
npm run dev
```

Frontend runs on **http://localhost:3000**

---

## Full Docker Deployment

```powershell
# From project root - builds and starts everything
docker-compose up --build
```

Services will be available at:
| Service | URL |
|---------|-----|
| Frontend Dashboard | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| AI Engine | http://localhost:8000 |
| Auth Service | http://localhost:8081 |
| Intelligence Service | http://localhost:8082 |
| Prediction Service | http://localhost:8083 |
| Alert Service | http://localhost:8084 |

---

## Architecture Data Flow

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Data Sources   │────▶│ Ingestion Service│────▶│     Kafka       │
│ (Social, CCTV,  │     │    (port 8085)   │     │  (port 9092)    │
│  Cyber feeds)   │     └──────────────────┘     └────────┬────────┘
└─────────────────┘                                       │
                                                          ▼
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   AI Engine     │◀────│  Intelligence    │◀────│ Kafka Topics:   │
│  (port 8000)    │     │    Service       │     │ social-media-   │
│  /predict/text  │     │  (port 8082)     │     │ data, cctv-data │
│  /predict/geo   │     └────────┬─────────┘     │ cyber-data      │
└─────────────────┘              │               └─────────────────┘
                                 ▼
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   AI Engine     │◀────│  Prediction      │◀────│ Kafka Topic:    │
│  /predict/      │     │    Service       │     │ intelligence-   │
│  forecast/*     │     │  (port 8083)     │     │ reports         │
└─────────────────┘     └────────┬─────────┘     └─────────────────┘
                                 │
                                 ▼
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│    Frontend     │◀────│  Alert Service   │◀────│ Kafka Topics:   │
│   Dashboard     │     │  (port 8084)     │     │ risk-forecasts  │
│  (port 3000)    │     │  + WebSocket     │     │ hotspot-forecasts│
└─────────────────┘     └──────────────────┘     └─────────────────┘
```

---

## API Endpoints Reference

### AI Engine (port 8000)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/health` | GET | Health check |
| `/models` | GET | List loaded models |
| `/predict/text` | POST | Text threat classification |
| `/predict/geospatial` | POST | Location-based risk prediction |
| `/predict/batch` | POST | Batch text prediction |
| `/predict/forecast/trend` | POST | Risk trend forecasting |
| `/predict/forecast/hotspots` | POST | Hotspot prediction |
| `/predict/forecast/immediate` | POST | Immediate threat forecast |

### Intelligence Service (port 8082)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/intelligence/reports` | GET | List intelligence reports |
| `/api/intelligence/dashboard/summary` | GET | Dashboard summary data |
| `/api/intelligence/threat-trends` | GET | Threat trend analysis |
| `/api/intelligence/threat-map` | GET | Threat map data |

### Prediction Service (port 8083)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/predictions/forecasts` | GET | List forecasts |
| `/api/predictions/hotspots` | GET | Hotspot predictions |
| `/api/predictions/risk-trends` | GET | Risk trend data |
| `/api/predictions/dashboard/summary` | GET | Dashboard summary |
| `/api/predictions/location-risk` | GET | Location risk data |

### Alert Service (port 8084)
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/alerts` | GET | List all alerts |
| `/api/alerts/{id}` | GET | Get specific alert |
| `/api/alerts/dashboard/summary` | GET | Alert summary stats |
| `/api/alerts/active` | GET | Active alerts only |
| `/api/alerts/{id}/acknowledge` | PUT | Acknowledge alert |
| `/api/alerts/{id}/resolve` | PUT | Resolve alert |
| `/ws/alerts` | WebSocket | Real-time alert stream |

---

## Testing the Pipeline

### 1. Test AI Engine directly
```powershell
# Text prediction
curl -X POST http://localhost:8000/predict/text `
  -H "Content-Type: application/json" `
  -d '{"text": "BREAKING: Bomb threat reported in Nairobi CBD, evacuations underway", "model": "random_forest"}'

# Geospatial prediction
curl -X POST http://localhost:8000/predict/geospatial `
  -H "Content-Type: application/json" `
  -d '{"latitude": -1.2864, "longitude": 36.8172}'

# Trend forecast
curl -X POST http://localhost:8000/predict/forecast/trend `
  -H "Content-Type: application/json" `
  -d '{"historical_data": [], "forecast_hours": 24}'
```

### 2. Test Backend Services
```powershell
# Intelligence summary
curl http://localhost:8082/api/intelligence/dashboard/summary

# Predictions
curl http://localhost:8083/api/predictions/dashboard/summary

# Alerts
curl http://localhost:8084/api/alerts/dashboard/summary
```

---

## Troubleshooting

### Models not loading
- Ensure `train_models.py` completed successfully
- Check `models/trained/` directory has `.pkl` files
- Run `generate_synthetic_data.py` first if data is missing

### AI Engine connection refused
- Verify AI engine is running on port 8000
- Check `AI_ENGINE_URL` environment variable in backend services

### Kafka connection errors
- Ensure Kafka and Zookeeper are running
- Wait 30+ seconds after starting for initialization
- Check `docker-compose logs kafka`

### Frontend shows no data
- Backend services may not be running
- Check browser console for API errors
- Verify CORS settings allow localhost:3000

---

## Environment Variables

Create `.env` file in project root:
```env
# AI Engine
AI_ENGINE_URL=http://localhost:8000

# Database
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
MONGODB_URI=mongodb://localhost:27017

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
```

---

## Next Steps

1. **Add real data sources**: Integrate actual social media APIs, CCTV feeds
2. **Improve models**: Train on larger/real datasets from Kaggle
3. **Add authentication**: Configure JWT tokens in auth-service
4. **Deploy to cloud**: Use Docker Compose with cloud providers
