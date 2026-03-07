"""
Sheng-Aware NLP Processor
Implements multilingual NLP strategy for Swahili, Sheng, and English
Uses Swahili-BERT and AfroXML models for better East African context understanding
"""

import torch
import numpy as np
from transformers import AutoTokenizer, AutoModelForSequenceClassification
from typing import Dict, List, Tuple
import logging
import re

logger = logging.getLogger(__name__)

class ShengNLPProcessor:
    def __init__(self):
        """
        Initialize Sheng-aware NLP processor with multilingual models
        """
        self.swahili_tokenizer = None
        self.swahili_model = None
        self.english_tokenizer = None
        self.english_model = None
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        
        # Sheng dictionary for normalization (high-frequency slang words)
        self.sheng_dictionary = {
            # Transportation slang
            "moti": "car", "gari": "car", "mat": "car",
            # People/authority
            "karao": "police", "mambas": "police", "askari": "police",
            # Common expressions
            "tuko": "is/are", "kuna": "there is", "ipo": "there is",
            # Security-related terms
            "noma": "steal", "mbaya": "bad/dangerous", "kasho": "money",
            "chill": "relax/calm down", "poa": "work/business",
            "vijana": "show off/display", "kushoto": "show off",
            # Social gathering
            "mambo": "talk/chat", "kongea": "plan/plot",
            "maandamano": "demonstration/protest"
        }
        
        # Security context keywords in multiple languages
        self.security_keywords = {
            "swahili": ["maandamano", "ghasia", "tatu", "mapinduzi", "hatari"],
            "english": ["protest", "demonstration", "unrest", "violence", "threat"],
            "sheng": ["risto", "mambo", "kongea", "maandamano"]
        }
        
        self.load_models()
    
    def load_models(self) -> None:
        """
        Load multilingual models for better context understanding
        """
        try:
            # Load distilbert-base-multilingual-cased for faster processing and lower memory usage
            logger.info("Loading distilbert-base-multilingual-cased model for Swahili context...")
            self.swahili_tokenizer = AutoTokenizer.from_pretrained("distilbert-base-multilingual-cased")
            self.swahili_model = AutoModelForSequenceClassification.from_pretrained(
                "distilbert-base-multilingual-cased",
                num_labels=4  # 0: benign, 1: suspicious, 2: threat, 3: civil_unrest
            )
            self.swahili_model.to(self.device)
            
            # Load English BERT as fallback
            logger.info("Loading English BERT model...")
            self.english_tokenizer = AutoTokenizer.from_pretrained("distilbert-base-uncased")
            self.english_model = AutoModelForSequenceClassification.from_pretrained(
                "distilbert-base-uncased",
                num_labels=3
            )
            self.english_model.to(self.device)
            
            logger.info("Optimized models loaded successfully")
            
        except Exception as e:
            logger.error(f"Failed to load models: {e}")
            self.load_fallback_models()
    
    def load_fallback_models(self) -> None:
        """Load fallback models when main models fail"""
        try:
            # Use multilingual BERT as fallback
            logger.info("Loading multilingual BERT as fallback...")
            self.swahili_tokenizer = AutoTokenizer.from_pretrained("bert-base-multilingual-cased")
            self.swahili_model = AutoModelForSequenceClassification.from_pretrained(
                "bert-base-multilingual-cased",
                num_labels=4
            )
            self.swahili_model.to(self.device)
            
        except Exception as e:
            logger.error(f"Failed to load fallback models: {e}")
            raise
    
    def normalize_sheng_text(self, text: str) -> str:
        """
        Normalize Sheng slang before NLP processing
        """
        normalized_text = text.lower()
        
        # Apply Sheng dictionary normalization
        for sheng_word, standard_word in self.sheng_dictionary.items():
            # Use word boundaries to avoid partial matches
            pattern = r'\b' + re.escape(sheng_word) + r'\b'
            normalized_text = re.sub(pattern, standard_word, normalized_text)
        
        logger.info(f"Sheng normalization: {text} -> {normalized_text}")
        return normalized_text
    
    def detect_language(self, text: str) -> str:
        """
        Detect if text is primarily Swahili, English, or mixed with Sheng
        """
        text_lower = text.lower()
        
        # Count Sheng words
        sheng_word_count = sum(1 for word in self.sheng_dictionary.keys() 
                                if word in text_lower.split())
        
        # Count Swahili words (simplified detection)
        swahili_indicators = ["ni", "na", "wa", "ya", "za", "la", "li", "me", "ko", "mo"]
        swahili_word_count = sum(1 for indicator in swahili_indicators 
                                   if indicator in text_lower)
        
        total_words = len(text_lower.split())
        
        if sheng_word_count > 0:
            return "sheng-mixed"  # Contains Sheng slang
        elif swahili_word_count > total_words * 0.3:
            return "swahili"  # Primarily Swahili
        else:
            return "english"  # Primarily English
    
    def analyze_text_threat(self, text: str, context: str = None) -> Dict[str, any]:
        """
        Analyze text for threat detection using multilingual strategy
        """
        try:
            # Normalize Sheng slang first
            normalized_text = self.normalize_sheng_text(text)
            
            # Detect language
            detected_language = self.detect_language(normalized_text)
            
            # Choose appropriate model based on language detection
            if detected_language in ["swahili", "sheng-mixed"]:
                return self._analyze_with_swahili_model(normalized_text, context)
            else:
                return self._analyze_with_english_model(normalized_text, context)
                
        except Exception as e:
            logger.error(f"Error in multilingual threat analysis: {e}")
            return self._get_default_response()
    
    def _analyze_with_swahili_model(self, text: str, context: str = None) -> Dict[str, any]:
        """
        Analyze using Swahili-BERT model for better East African context
        """
        if not self.swahili_model or not self.swahili_tokenizer:
            logger.warning("Swahili model not loaded, using English fallback")
            return self._analyze_with_english_model(text, context)
        
        try:
            # Tokenize input
            inputs = self.swahili_tokenizer(
                text, 
                return_tensors="pt", 
                truncation=True, 
                max_length=512,
                padding=True
            )
            inputs = {k: v.to(self.device) for k, v in inputs.items()}
            
            # Get predictions
            with torch.no_grad():
                outputs = self.swahili_model(**inputs)
                probabilities = torch.softmax(outputs.logits, dim=-1)
                probs = probabilities.cpu().numpy()[0]
            
            # Map to Swahili-aware threat levels
            threat_labels = ["benign", "suspicious", "threat", "civil_unrest"]
            predictions = {
                label: float(prob) for label, prob in zip(threat_labels, probs)
            }
            
            # Get highest probability label
            max_label = threat_labels[np.argmax(probs)]
            confidence = float(np.max(probs))
            
            # Enhanced context analysis for East African social context
            context_enhancement = self._analyze_east_african_context(text, context)
            
            result = {
                "text": text,
                "original_language": "swahili-mixed",
                "classification": max_label,
                "confidence": confidence,
                "threat_probabilities": predictions,
                "sentiment_scores": self._analyze_multilingual_sentiment(text),
                "threat_keywords": self._extract_multilingual_threat_keywords(text),
                "risk_score": self._calculate_contextual_risk_score(predictions, context_enhancement),
                "context_enhancement": context_enhancement,
                "recommendations": self._generate_culturally_aware_recommendations(max_label, confidence, text),
                "sheng_words_detected": self._extract_sheng_words(text)
            }
            
            logger.info(f"Swahili-BERT analysis: {max_label} (confidence: {confidence})")
            return result
            
        except Exception as e:
            logger.error(f"Error in Swahili model analysis: {e}")
            return self._get_default_response()
    
    def _analyze_with_english_model(self, text: str, context: str = None) -> Dict[str, any]:
        """
        Analyze using English BERT model
        """
        if not self.english_model or not self.english_tokenizer:
            logger.warning("English model not loaded")
            return self._get_default_response()
        
        try:
            # Tokenize input
            inputs = self.english_tokenizer(
                text, 
                return_tensors="pt", 
                truncation=True, 
                max_length=512,
                padding=True
            )
            inputs = {k: v.to(self.device) for k, v in inputs.items()}
            
            # Get predictions
            with torch.no_grad():
                outputs = self.english_model(**inputs)
                probabilities = torch.softmax(outputs.logits, dim=-1)
                probs = probabilities.cpu().numpy()[0]
            
            # Map to standard threat levels
            threat_labels = ["benign", "suspicious", "threat"]
            predictions = {
                label: float(prob) for label, prob in zip(threat_labels, probs)
            }
            
            # Get highest probability label
            max_label = threat_labels[np.argmax(probs)]
            confidence = float(np.max(probs))
            
            result = {
                "text": text,
                "original_language": "english",
                "classification": max_label,
                "confidence": confidence,
                "threat_probabilities": predictions,
                "sentiment_scores": self._analyze_multilingual_sentiment(text),
                "threat_keywords": self._extract_multilingual_threat_keywords(text),
                "risk_score": self._calculate_contextual_risk_score(predictions, context),
                "recommendations": self._generate_culturally_aware_recommendations(max_label, confidence, text),
                "sheng_words_detected": self._extract_sheng_words(text)
            }
            
            logger.info(f"English-BERT analysis: {max_label} (confidence: {confidence})")
            return result
            
        except Exception as e:
            logger.error(f"Error in English model analysis: {e}")
            return self._get_default_response_response()
    
    def _analyze_east_african_context(self, text: str, context: str = None) -> Dict[str, any]:
        """
        Analyze text for East African social and political context
        """
        context_score = 0.0
        
        # Check for East African context indicators
        east_african_indicators = [
            "kenya", "nairobi", "mombasa", "kisumu", "nakuru",
            "tanzania", "dar es salaam", "dodoma", "arusha",
            "uganda", "kampala", "entebbe", "jinja",
            "rwanda", "kigali", "burundi", "bujumbura"
        ]
        
        text_lower = text.lower()
        for indicator in east_african_indicators:
            if indicator in text_lower:
                context_score += 0.2
        
        # Check for political/social gathering terms
        political_indicators = ["election", "vote", "campaign", "rally", "protest", "demonstration"]
        for indicator in political_indicators:
            if indicator in text_lower:
                context_score += 0.3
        
        # Check for civil unrest indicators
        unrest_indicators = ["maandamano", "ghasia", "tatu", "mapinduzi"]
        for indicator in unrest_indicators:
            if indicator in text_lower:
                context_score += 0.4
        
        return {
            "east_african_relevance": min(1.0, context_score),
            "political_context": any(indicator in text_lower for indicator in political_indicators),
            "civil_unrest_indicators": [ind for ind in unrest_indicators if ind in text_lower]
        }
    
    def _analyze_multilingual_sentiment(self, text: str) -> Dict[str, float]:
        """
        Analyze sentiment across multiple languages
        """
        # Simple sentiment analysis that works across languages
        positive_words = {
            "swahili": ["nzuri", "salama", "heri", "jambo", "penda"],
            "english": ["good", "safe", "peace", "calm", "friendly", "help"],
            "sheng": ["poa", "freshi", "safi", "noma"]
        }
        
        negative_words = {
            "swahili": ["hatari", "dhiki", "hasara", "tatizo", "sumbu"],
            "english": ["bad", "danger", "threat", "attack", "violence", "harm"],
            "sheng": ["mbaya", "kasho", "noma", "chizi"]
        }
        
        text_lower = text.lower()
        words = text_lower.split()
        
        if len(words) == 0:
            return {"positive": 0.5, "negative": 0.5, "neutral": 0.0}
        
        positive_count = 0
        negative_count = 0
        
        for word in words:
            # Check all language dictionaries
            for lang_words in [positive_words, negative_words]:
                if word in lang_words.values():
                    if lang_words == positive_words:
                        positive_count += 1
                    elif lang_words == negative_words:
                        negative_count += 1
        
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
    
    def _extract_multilingual_threat_keywords(self, text: str) -> List[str]:
        """
        Extract threat keywords from multiple languages
        """
        text_lower = text.lower()
        found_keywords = []
        
        # Combine all language threat keywords
        all_threat_keywords = []
        for lang_keywords in self.security_keywords.values():
            all_threat_keywords.extend(lang_keywords)
        
        for keyword in all_threat_keywords:
            if keyword in text_lower:
                found_keywords.append(keyword)
        
        return found_keywords[:10]  # Return top 10
    
    def _extract_sheng_words(self, text: str) -> List[str]:
        """
        Extract Sheng slang words from text
        """
        text_lower = text.lower()
        sheng_words_found = []
        
        for sheng_word in self.sheng_dictionary.keys():
            if sheng_word in text_lower:
                sheng_words_found.append(f"{sheng_word} -> {self.sheng_dictionary[sheng_word]}")
        
        return sheng_words_found[:5]  # Return top 5 Sheng words
    
    def _calculate_contextual_risk_score(self, threat_probabilities: Dict[str, float], context: Dict[str, any] = None) -> float:
        """
        Calculate risk score with contextual enhancement
        """
        # Base risk calculation
        weights = {"benign": 0.0, "suspicious": 0.5, "threat": 1.0, "civil_unrest": 0.8}
        
        # Add civil_unrest if available
        if "civil_unrest" in threat_probabilities:
            weights["civil_unrest"] = 0.8
        
        risk_score = sum(
            probability * weights.get(category, 0.0)
            for category, probability in threat_probabilities.items()
        )
        
        # Apply context enhancement
        if context and context.get("east_african_relevance", 0.0) > 0.5:
            risk_score += 0.2  # Boost for East African relevance
        
        # Apply emergency context boost
        if context and any(keyword in str(context.get("emergency_keywords", "")).lower() 
                   for keyword in ["emergency", "urgent", "immediate", "critical"]):
            risk_score += 0.3
        
        return min(1.0, risk_score)
    
    def _generate_culturally_aware_recommendations(self, classification: str, confidence: float, text: str) -> List[str]:
        """
        Generate recommendations that are culturally and contextually aware
        """
        recommendations = []
        
        if classification == "threat":
            recommendations.extend([
                "Immediate investigation required",
                "Notify local authorities - " + self._get_relevant_authorities(),
                "Document all relevant details",
                "Consider security measures for the area"
            ])
        elif classification == "civil_unrest":
            recommendations.extend([
                "Monitor civil unrest situation closely",
                "Check for similar incidents in the area",
                "Prepare crowd control measures",
                "Alert community leaders and elders"
            ])
        elif classification == "suspicious":
            recommendations.extend([
                "Enhance surveillance in the area",
                "Verify information through multiple sources",
                "Increase security patrols",
                "Prepare contingency plans"
            ])
        
        # Add Sheng-specific recommendations
        sheng_words = self._extract_sheng_words(text)
        if sheng_words:
            recommendations.append("Sheng slang detected - consider local interpreter if needed")
        
        # Add confidence-based recommendations
        if confidence > 0.85:
            recommendations.append("HIGH CONFIDENCE - Immediate action recommended")
        elif confidence > 0.6:
            recommendations.append("MEDIUM CONFIDENCE - Enhanced monitoring recommended")
        
        return recommendations
    
    def _get_relevant_authorities(self) -> str:
        """
        Get relevant authorities based on context (simplified)
        """
        return "Kenya Police, County Commissioners, Local Administration"
    
    def _get_default_response(self) -> Dict[str, any]:
        """
        Get default response when models fail
        """
        return {
            "text": "",
            "classification": "benign",
            "confidence": 0.5,
            "threat_probabilities": {"benign": 0.7, "suspicious": 0.2, "threat": 0.1},
            "sentiment_scores": {"positive": 0.3, "negative": 0.3, "neutral": 0.4},
            "threat_keywords": [],
            "risk_score": 0.2,
            "recommendations": ["Continue monitoring - system in degraded mode"],
            "sheng_words_detected": [],
            "original_language": "unknown"
        }
