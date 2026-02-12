#!/bin/bash

echo "🚀 Setting up NTEWS MVP Environment..."

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Create necessary directories
echo "📁 Creating directories..."
mkdir -p logs
mkdir -p data/postgres
mkdir -p data/mongodb
mkdir -p data/redis

# Set permissions
chmod 755 logs data

# Build and start services
echo "🔨 Building and starting services..."
docker-compose down
docker-compose build --no-cache
docker-compose up -d

# Wait for services to start
echo "⏳ Waiting for services to start..."
sleep 30

# Check service health
echo "🔍 Checking service health..."
docker-compose ps

# Show URLs
echo ""
echo "✅ NTEWS MVP is ready!"
echo ""
echo "🌐 Frontend Dashboard: http://localhost:3000"
echo "🔧 API Gateway: http://localhost:8080"
echo "📊 Intelligence Service: http://localhost:8082"
echo "🚨 Alert Service: http://localhost:8084"
echo "🔮 Prediction Service: http://localhost:8083"
echo "📥 Ingestion Service: http://localhost:8085"
echo ""
echo "🗃️  Database URLs:"
echo "   PostgreSQL: localhost:5432"
echo "   MongoDB: localhost:27017"
echo "   Redis: localhost:6379"
echo "   Kafka: localhost:9092"
echo ""
echo "📋 To view logs: docker-compose logs -f [service-name]"
echo "🛑 To stop: docker-compose down"
echo "🔄 To restart: docker-compose restart"
