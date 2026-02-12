#!/bin/bash

echo "🚀 Starting NTEWS MVP..."

# Start all services
docker-compose up -d

# Wait for services to be ready
echo "⏳ Waiting for services to start..."
sleep 20

# Check if services are running
echo "🔍 Checking service status..."
docker-compose ps

echo ""
echo "✅ Services started successfully!"
echo ""
echo "🌐 Access the dashboard at: http://localhost:3000"
echo "📊 View API documentation: http://localhost:8080/swagger-ui.html"
echo ""
echo "📋 Useful commands:"
echo "   View logs: docker-compose logs -f"
echo "   Stop services: docker-compose down"
echo "   Restart service: docker-compose restart [service-name]"
