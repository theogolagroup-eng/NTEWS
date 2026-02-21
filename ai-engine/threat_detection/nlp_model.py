"""
NLP Threat Detection Model
Handles text-based threat analysis using transformer models
"""

import torch
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import numpy as np
from typing import Dict, List, Tuple
import logging

logger = logging.getLogger(__name__)

class NLPThreatDetector:
    def __init__(self, model_name: str = "distilbert-base-uncased"):
        """
        Initialize NLP threat detection model
        
        Args:
            model_name: Hugging Face model name
        """
        self.model_name = model_name
        self.tokenizer = None
        self.model = None
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        
    def load_model(self) -> None:
        """Load the transformer model and tokenizer"""
        try:
            self.tokenizer = AutoTokenizer.from_pretrained(self.model_name)
            self.model = AutoModelForSequenceClassification.from_pretrained(
                self.model_name, 
                num_labels=3  # 0: benign, 1: suspicious, 2: threat
            )
            self.model.to(self.device)
            self.model.eval()
            logger.info(f"Loaded NLP model: {self.model_name}")
        except Exception as e:
            logger.error(f"Failed to load NLP model: {e}")
            raise
    
    def predict_threat(self, text: str) -> Dict[str, float]:
        """
        Predict threat level for given text
        
        Args:
            text: Input text to analyze
            
        Returns:
            Dictionary with threat probabilities and classification
        """
        if not self.model or not self.tokenizer:
            self.load_model()
        
        try:
            # Tokenize input
            inputs = self.tokenizer(
                text, 
                return_tensors="pt", 
                truncation=True, 
                max_length=512,
                padding=True
            )
            inputs = {k: v.to(self.device) for k, v in inputs.items()}
            
            # Get predictions
            with torch.no_grad():
                outputs = self.model(**inputs)
                probabilities = torch.softmax(outputs.logits, dim=-1)
                probs = probabilities.cpu().numpy()[0]
            
            # Map to threat levels
            threat_labels = ["benign", "suspicious", "threat"]
            predictions = {
                label: float(prob) for label, prob in zip(threat_labels, probs)
            }
            
            # Get highest probability label
            max_label = threat_labels[np.argmax(probs)]
            
            return {
                "text": text,
                "classification": max_label,
                "confidence": float(np.max(probs)),
                "threat_probabilities": predictions,
                "sentiment_scores": self._analyze_sentiment(text),
                "threat_keywords": self._extract_threat_keywords(text),
                "risk_score": self._calculate_risk_score(predictions),
                "recommendations": self._generate_recommendations(max_label, float(np.max(probs)))
            }
            
        except Exception as e:
            logger.error(f"Error in threat prediction: {e}")
            return self._get_default_response()
    
    def analyze_threat(self, text: str, context: str = None) -> Dict[str, any]:
        """
        Analyze text for threat detection (alias for predict_threat)
        
        Args:
            text: Input text to analyze
            context: Optional context for analysis
            
        Returns:
            Dictionary with threat analysis results
        """
        result = self.predict_threat(text)
        
        # Add context if provided
        if context:
            result["context"] = context
            # Adjust analysis based on context
            context_keywords = ["emergency", "urgent", "immediate", "critical"]
            if any(keyword in context.lower() for keyword in context_keywords):
                result["risk_score"] = min(1.0, result["risk_score"] + 0.2)
        
        return result
    
    def extract_threat_keywords(self, text: str) -> List[str]:
        """
        Extract potential threat keywords from text
        
        Args:
            text: Input text to analyze
            
        Returns:
            List of threat-related keywords
        """
        threat_keywords = [
            "attack", "threat", "weapon", "bomb", "explosive", "gun", "knife",
            "violence", "hostage", "terrorism", "suspicious", "danger", "harm",
            "kill", "murder", "assault", "threaten", "dangerous", "illegal"
        ]
        
        text_lower = text.lower()
        found_keywords = [
            keyword for keyword in threat_keywords 
            if keyword in text_lower
        ]
        
        return found_keywords
    
    def analyze_sentiment(self, text: str) -> Dict[str, float]:
        """
        Analyze sentiment of text (additional feature for threat detection)
        
        Args:
            text: Input text to analyze
            
        Returns:
            Dictionary with sentiment scores
        """
        # Simple sentiment analysis based on keywords
        positive_words = ["good", "safe", "peace", "calm", "friendly", "help"]
        negative_words = ["bad", "danger", "threat", "attack", "violence", "harm"]
        
        text_lower = text.lower()
        words = text_lower.split()
        
        positive_count = sum(1 for word in words if word in positive_words)
        negative_count = sum(1 for word in words if word in negative_words)
        total_words = len(words)
        
        if total_words == 0:
            return {"positive": 0.5, "negative": 0.5, "neutral": 0.0}
        
        positive_score = positive_count / total_words
        negative_score = negative_count / total_words
        neutral_score = 1.0 - positive_score - negative_score
        
        return {
            "positive": positive_score,
            "negative": negative_score,
            "neutral": max(0.0, neutral_score)
        }
    
    def _analyze_sentiment(self, text: str) -> Dict[str, float]:
        """Analyze sentiment of text"""
        positive_words = ["good", "safe", "peace", "calm", "friendly", "help"]
        negative_words = ["bad", "danger", "threat", "attack", "violence", "harm"]
        
        text_lower = text.lower()
        words = text_lower.split()
        
        positive_count = sum(1 for word in words if word in positive_words)
        negative_count = sum(1 for word in words if word in negative_words)
        total_words = len(words)
        
        if total_words == 0:
            return {"positive": 0.5, "negative": 0.5, "neutral": 0.0}
        
        positive_score = positive_count / total_words
        negative_score = negative_count / total_words
        neutral_score = 1.0 - positive_score - negative_score
        
        return {
            "positive": positive_score,
            "negative": negative_score,
            "neutral": max(0.0, neutral_score)
        }
    
    def _extract_threat_keywords(self, text: str) -> List[str]:
        """Extract threat keywords from text"""
        threat_keywords = [
            "attack", "threat", "violence", "weapon", "bomb", "gun", "knife",
            "robbery", "theft", "murder", "kidnap", "terror", "explosion",
            "fire", "emergency", "danger", "harm", "kill", "death", "injury"
        ]
        
        text_lower = text.lower()
        found_keywords = [word for word in threat_keywords if word in text_lower]
        
        return found_keywords[:10]  # Return top 10
    
    def _calculate_risk_score(self, threat_probabilities: Dict[str, float]) -> float:
        """Calculate overall risk score from threat probabilities"""
        # Weighted risk calculation
        weights = {"benign": 0.0, "suspicious": 0.5, "threat": 1.0}
        
        risk_score = sum(
            probability * weights.get(category, 0.0)
            for category, probability in threat_probabilities.items()
        )
        
        return risk_score
    
    def _generate_recommendations(self, classification: str, confidence: float) -> List[str]:
        """Generate recommendations based on classification and confidence"""
        if classification == "threat":
            return [
                "Immediate investigation required",
                "Notify security personnel",
                "Document all relevant details",
                "Consider evacuation if necessary"
            ]
        elif classification == "suspicious":
            return [
                "Monitor situation closely",
                "Verify information source",
                "Increase surveillance",
                "Prepare contingency plans"
            ]
        else:  # benign
            return [
                "Continue routine monitoring",
                "Log for future reference",
                "No immediate action required"
            ]
    
    def _get_default_response(self) -> Dict[str, any]:
        """Get default response when model fails"""
        return {
            "text": "",
            "classification": "benign",
            "confidence": 0.8,
            "threat_probabilities": {"benign": 0.8, "suspicious": 0.15, "threat": 0.05},
            "sentiment_scores": {"positive": 0.5, "negative": 0.3, "neutral": 0.2},
            "threat_keywords": [],
            "risk_score": 0.1,
            "recommendations": ["Continue monitoring", "No immediate action required"]
        }
