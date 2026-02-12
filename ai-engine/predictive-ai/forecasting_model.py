"""
Predictive AI Forecasting Model
Handles short-term risk forecasting and trend analysis
"""

import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import mean_absolute_error, mean_squared_error
from typing import Dict, List, Tuple, Optional
import logging
from datetime import datetime, timedelta
import joblib

logger = logging.getLogger(__name__)

class RiskForecastingModel:
    def __init__(self):
        """Initialize the risk forecasting model"""
        self.model = RandomForestRegressor(
            n_estimators=100,
            random_state=42,
            max_depth=10
        )
        self.scaler = StandardScaler()
        self.is_trained = False
        self.feature_columns = []
        
    def prepare_features(self, data: pd.DataFrame) -> pd.DataFrame:
        """
        Prepare features for forecasting model
        
        Args:
            data: Historical threat data
            
        Returns:
            DataFrame with engineered features
        """
        features = data.copy()
        
        # Time-based features
        features['hour'] = pd.to_datetime(features['timestamp']).dt.hour
        features['day_of_week'] = pd.to_datetime(features['timestamp']).dt.dayofweek
        features['month'] = pd.to_datetime(features['timestamp']).dt.month
        
        # Lag features
        features['risk_lag_1h'] = features['risk_score'].shift(1)
        features['risk_lag_6h'] = features['risk_score'].shift(6)
        features['risk_lag_24h'] = features['risk_score'].shift(24)
        
        # Rolling statistics
        features['risk_mean_6h'] = features['risk_score'].rolling(window=6).mean()
        features['risk_std_6h'] = features['risk_score'].rolling(window=6).std()
        features['risk_mean_24h'] = features['risk_score'].rolling(window=24).mean()
        
        # Threat type features
        threat_dummies = pd.get_dummies(features['threat_type'], prefix='threat')
        features = pd.concat([features, threat_dummies], axis=1)
        
        # Location-based features (if available)
        if 'location_id' in features.columns:
            location_dummies = pd.get_dummies(features['location_id'], prefix='loc')
            features = pd.concat([features, location_dummies], axis=1)
        
        # Drop original categorical columns and NaN values
        features = features.drop(['timestamp', 'threat_type'], axis=1, errors='ignore')
        if 'location_id' in features.columns:
            features = features.drop(['location_id'], axis=1)
        
        features = features.fillna(features.mean())
        
        self.feature_columns = features.columns.tolist()
        return features
    
    def train(self, historical_data: pd.DataFrame) -> Dict[str, float]:
        """
        Train the forecasting model
        
        Args:
            historical_data: Historical threat data with columns:
                           timestamp, risk_score, threat_type, location_id
            
        Returns:
            Dictionary with training metrics
        """
        try:
            # Prepare features
            features = self.prepare_features(historical_data)
            
            # Target variable (next hour risk score)
            target = features['risk_score'].shift(-1)
            
            # Remove last row (no target)
            features = features[:-1]
            target = target[:-1]
            
            # Split data (80% train, 20% test)
            split_idx = int(len(features) * 0.8)
            X_train, X_test = features[:split_idx], features[split_idx:]
            y_train, y_test = target[:split_idx], target[split_idx:]
            
            # Scale features
            X_train_scaled = self.scaler.fit_transform(X_train)
            X_test_scaled = self.scaler.transform(X_test)
            
            # Train model
            self.model.fit(X_train_scaled, y_train)
            
            # Evaluate
            y_pred = self.model.predict(X_test_scaled)
            mae = mean_absolute_error(y_test, y_pred)
            rmse = np.sqrt(mean_squared_error(y_test, y_pred))
            
            self.is_trained = True
            
            metrics = {
                "mae": float(mae),
                "rmse": float(rmse),
                "training_samples": len(X_train),
                "test_samples": len(X_test)
            }
            
            logger.info(f"Model trained successfully. MAE: {mae:.3f}, RMSE: {rmse:.3f}")
            return metrics
            
        except Exception as e:
            logger.error(f"Error training forecasting model: {e}")
            raise
    
    def predict_risk_trend(self, current_data: pd.DataFrame, hours_ahead: int = 24) -> List[Dict]:
        """
        Predict risk trend for the next N hours
        
        Args:
            current_data: Current/historical data
            hours_ahead: Number of hours to forecast
            
        Returns:
            List of predictions with timestamps and risk scores
        """
        if not self.is_trained:
            raise ValueError("Model must be trained before making predictions")
        
        try:
            predictions = []
            current_features = self.prepare_features(current_data)
            
            # Get the latest data point
            latest_data = current_features.iloc[-1:].copy()
            
            for hour in range(1, hours_ahead + 1):
                # Scale features
                features_scaled = self.scaler.transform(latest_data)
                
                # Predict
                risk_score = self.model.predict(features_scaled)[0]
                
                # Create prediction entry
                future_time = datetime.now() + timedelta(hours=hour)
                prediction = {
                    "timestamp": future_time.isoformat(),
                    "predicted_risk_score": float(risk_score),
                    "confidence": self._calculate_confidence(risk_score),
                    "hour_ahead": hour
                }
                predictions.append(prediction)
                
                # Update features for next prediction
                latest_data['risk_lag_1h'] = risk_score
                latest_data['hour'] = future_time.hour
                latest_data['day_of_week'] = future_time.weekday()
                latest_data['month'] = future_time.month
            
            return predictions
            
        except Exception as e:
            logger.error(f"Error in risk trend prediction: {e}")
            raise
    
    def predict_hotspots(self, current_data: pd.DataFrame) -> List[Dict]:
        """
        Predict high-risk locations for the next 24 hours
        
        Args:
            current_data: Current/historical data with location information
            
        Returns:
            List of high-risk location predictions
        """
        if not self.is_trained:
            raise ValueError("Model must be trained before making predictions")
        
        try:
            # Group by location and predict for each
            location_predictions = []
            
            if 'location_id' in current_data.columns:
                locations = current_data['location_id'].unique()
                
                for location in locations:
                    location_data = current_data[current_data['location_id'] == location]
                    
                    # Predict risk trend for this location
                    predictions = self.predict_risk_trend(location_data, hours_ahead=24)
                    
                    # Calculate average risk for next 24 hours
                    avg_risk = np.mean([p['predicted_risk_score'] for p in predictions])
                    max_risk = np.max([p['predicted_risk_score'] for p in predictions])
                    
                    location_predictions.append({
                        "location_id": location,
                        "avg_predicted_risk": float(avg_risk),
                        "max_predicted_risk": float(max_risk),
                        "risk_trend": "increasing" if predictions[-1]['predicted_risk_score'] > predictions[0]['predicted_risk_score'] else "decreasing",
                        "confidence": float(np.mean([p['confidence'] for p in predictions]))
                    })
            
            # Sort by risk level
            location_predictions.sort(key=lambda x: x['max_predicted_risk'], reverse=True)
            
            return location_predictions[:10]  # Return top 10 hotspots
            
        except Exception as e:
            logger.error(f"Error in hotspot prediction: {e}")
            raise
    
    def _calculate_confidence(self, risk_score: float) -> float:
        """
        Calculate prediction confidence based on risk score
        
        Args:
            risk_score: Predicted risk score
            
        Returns:
            Confidence value between 0 and 1
        """
        # Higher confidence for extreme values (very low or very high risk)
        if risk_score < 0.2 or risk_score > 0.8:
            return 0.85
        elif risk_score < 0.4 or risk_score > 0.6:
            return 0.75
        else:
            return 0.65
    
    def save_model(self, filepath: str) -> None:
        """Save the trained model"""
        if not self.is_trained:
            raise ValueError("No trained model to save")
        
        model_data = {
            'model': self.model,
            'scaler': self.scaler,
            'feature_columns': self.feature_columns,
            'is_trained': self.is_trained
        }
        joblib.dump(model_data, filepath)
        logger.info(f"Model saved to {filepath}")
    
    def load_model(self, filepath: str) -> None:
        """Load a trained model"""
        try:
            model_data = joblib.load(filepath)
            self.model = model_data['model']
            self.scaler = model_data['scaler']
            self.feature_columns = model_data['feature_columns']
            self.is_trained = model_data['is_trained']
            logger.info(f"Model loaded from {filepath}")
        except Exception as e:
            logger.error(f"Error loading model: {e}")
            raise
