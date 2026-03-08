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
            "swahili": ["maandamano", "ghasia", "tatu", "mapinduzi", "hatari", "polisi", "karao", "kongea", "mambo", "walevi", "wanakambo", "tishio", "ghasia", "vita", "fujo"],
            "english": ["protest", "demonstration", "unrest", "violence", "threat", "police", "security", "riot", "civil", "unrest", "danger", "attack", "crime"],
            "sheng": ["risto", "mambo", "kongea", "maandamano", "karao", "walevi", "wanakambo", "tishio", "ghasia"]
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
        Analyze text for threat detection using rule-based approach with keyword detection
        This replaces the unreliable ML models with deterministic logic
        """
        try:
            logger.info(f"Starting threat analysis for: '{text[:50]}...'")
            
            # Normalize Sheng slang first
            normalized_text = self.normalize_sheng_text(text)
            
            # Detect language
            detected_language = self.detect_language(normalized_text)
            
            # Extract threat keywords FIRST
            threat_keywords = self._extract_multilingual_threat_keywords(normalized_text)
            
            # RULE-BASED CLASSIFICATION (replaces ML models)
            classification_result = self._rule_based_threat_classification(normalized_text, threat_keywords, detected_language)
            
            # Extract Sheng words
            sheng_words = self._extract_sheng_words(text)
            
            # Analyze sentiment
            sentiment_scores = self._analyze_multilingual_sentiment(text)
            
            # Context enhancement
            context_enhancement = self._analyze_contextual_factors(text, context)
            
            # Calculate risk score
            risk_score = self._calculate_contextual_risk_score(classification_result["threat_probabilities"], context_enhancement)
            
            result = {
                "original_text": text,
                "normalized_text": normalized_text,
                "sheng_words_detected": sheng_words,
                "detected_language": detected_language,
                "classification": classification_result["classification"],
                "confidence": classification_result["confidence"],
                "threat_probabilities": classification_result["threat_probabilities"],
                "sentiment_scores": sentiment_scores,
                "threat_keywords": threat_keywords,
                "risk_score": risk_score,
                "context_enhancement": context_enhancement,
                "recommendations": self._generate_culturally_aware_recommendations(classification_result["classification"], classification_result["confidence"], text),
                "processing_metadata": {"method": "rule_based", "language": detected_language}
            }
            
            logger.info(f"Rule-based analysis: {result['classification']} (confidence: {result['confidence']}) - Keywords: {threat_keywords}")
            return result
            
        except Exception as e:
            logger.error(f"Error in rule-based threat analysis: {e}")
            return self._get_default_response()
    
    def _rule_based_threat_classification(self, text: str, threat_keywords: List[str], language: str) -> Dict[str, any]:
        """
        Rule-based threat classification using keyword detection and patterns
        This replaces unreliable ML models with deterministic logic
        """
        text_lower = text.lower()
        
        # High-priority threat keywords (immediate danger)
        high_priority_keywords = ["violence", "riot", "attack", "kill", "bomb", "terrorist", "gun", "weapon", "vita", "ua", "kufa"]
        
        # Medium-priority threat keywords (potential danger)
        medium_priority_keywords = ["protest", "demonstration", "unrest", "maandamano", "ghasia", "tatu", "mapinduzi", "police", "karao", "security", "threat", "hatari"]
        
        # Low-priority threat keywords (suspicious activity)
        low_priority_keywords = ["suspicious", "walevi", "wanakambo", "kongea", "mambo", "crime", "theft", "noma"]
        
        # Count keywords by priority
        high_count = sum(1 for keyword in high_priority_keywords if keyword in text_lower)
        medium_count = sum(1 for keyword in medium_priority_keywords if keyword in text_lower)
        low_count = sum(1 for keyword in low_priority_keywords if keyword in text_lower)
        
        total_keywords = high_count + medium_count + low_count
        
        logger.info(f"Keyword counts - High: {high_count}, Medium: {medium_count}, Low: {low_count}")
        
        # Determine classification based on keyword analysis
        if high_count >= 1:
            classification = "threat"
            confidence = min(0.95, 0.6 + (high_count * 0.15))
            threat_prob = 0.8 + (high_count * 0.05)
        elif medium_count >= 2:
            classification = "threat"
            confidence = min(0.85, 0.5 + (medium_count * 0.15))
            threat_prob = 0.6 + (medium_count * 0.1)
        elif medium_count >= 1:
            classification = "suspicious"
            confidence = min(0.75, 0.4 + (medium_count * 0.2))
            threat_prob = 0.3 + (medium_count * 0.15)
        elif low_count >= 2:
            classification = "suspicious"
            confidence = min(0.65, 0.3 + (low_count * 0.15))
            threat_prob = 0.2 + (low_count * 0.1)
        elif low_count >= 1:
            classification = "suspicious"
            confidence = 0.4
            threat_prob = 0.2
        else:
            classification = "benign"
            confidence = 0.8
            threat_prob = 0.05
        
        # Calculate civil_unrest probability
        civil_unrest_keywords = ["protest", "demonstration", "unrest", "maandamano", "ghasia", "tatu", "mapinduzi"]
        civil_unrest_count = sum(1 for keyword in civil_unrest_keywords if keyword in text_lower)
        civil_unrest_prob = min(0.9, civil_unrest_count * 0.3)
        
        # Normalize probabilities
        benign_prob = 1.0 - confidence if classification == "benign" else 0.1
        suspicious_prob = 1.0 - threat_prob - benign_prob - civil_unrest_prob
        suspicious_prob = max(0.0, suspicious_prob)
        
        threat_probabilities = {
            "benign": max(0.0, benign_prob),
            "suspicious": max(0.0, suspicious_prob),
            "threat": max(0.0, threat_prob),
            "civil_unrest": max(0.0, civil_unrest_prob)
        }
        
        # Normalize to sum to 1.0
        total_prob = sum(threat_probabilities.values())
        if total_prob > 0:
            threat_probabilities = {k: v/total_prob for k, v in threat_probabilities.items()}
        
        return {
            "classification": classification,
            "confidence": confidence,
            "threat_probabilities": threat_probabilities
        }
    
    def _get_default_response(self) -> Dict[str, any]:
        """
        Default response when analysis fails
        """
        return {
            "original_text": "",
            "normalized_text": "",
            "sheng_words_detected": [],
            "detected_language": "unknown",
            "classification": "benign",
            "confidence": 0.5,
            "threat_probabilities": {"benign": 0.7, "suspicious": 0.2, "threat": 0.1, "civil_unrest": 0.0},
            "sentiment_scores": {"positive": 0.3, "negative": 0.3, "neutral": 0.4},
            "threat_keywords": [],
            "risk_score": 0.2,
            "context_enhancement": {},
            "recommendations": ["Continue monitoring - system in degraded mode"],
            "processing_metadata": {"method": "default_fallback"}
        }
    
    def _analyze_multilingual_sentiment(self, text: str) -> Dict[str, float]:
        """
        Analyze sentiment using simple keyword-based approach
        """
        text_lower = text.lower()
        
        # Positive keywords (Swahili, English, Sheng)
        positive_keywords = ["poa", "nzuri", "safi", "good", "great", "happy", "joy", "love", "beautiful", "amazing", "excellent"]
        
        # Negative keywords
        negative_keywords = ["mbaya", "shida", "problem", "bad", "evil", "hate", "angry", "sad", "terrible", "awful", "dangerous"]
        
        positive_count = sum(1 for keyword in positive_keywords if keyword in text_lower)
        negative_count = sum(1 for keyword in negative_keywords if keyword in text_lower)
        
        total_words = len(text_lower.split())
        if total_words == 0:
            return {"positive": 0.5, "negative": 0.5, "neutral": 0.0}
        
        positive_score = positive_count / total_words
        negative_score = negative_count / total_words
        neutral_score = 1.0 - positive_score - negative_score
        
        return {
            "positive": max(0.0, positive_score),
            "negative": max(0.0, negative_score),
            "neutral": max(0.0, neutral_score)
        }
    
    def _analyze_contextual_factors(self, text: str, context: str = None) -> Dict[str, any]:
        """
        Analyze contextual factors for East African relevance
        """
        text_lower = text.lower()
        
        # East African locations
        east_african_locations = ["kenya", "nairobi", "mombasa", "kisumu", "nakuru", "eldoret", "tanzania", "uganda", "rwanda", "burundi"]
        
        # Political indicators
        political_keywords = ["election", "government", "president", "politics", "vote", "democracy"]
        
        # Civil unrest indicators
        civil_unrest_keywords = ["protest", "demonstration", "riot", "unrest", "maandamano", "ghasia"]
        
        # Calculate relevance scores
        east_african_relevance = 0.0
        political_context = False
        civil_unrest_indicators = []
        
        # Check for East African relevance
        for location in east_african_locations:
            if location in text_lower:
                east_african_relevance += 0.2
        
        east_african_relevance = min(1.0, east_african_relevance)
        
        # Check for political context
        political_context = any(keyword in text_lower for keyword in political_keywords)
        
        # Check for civil unrest indicators
        for keyword in civil_unrest_keywords:
            if keyword in text_lower:
                civil_unrest_indicators.append(keyword)
        
        return {
            "east_african_relevance": east_african_relevance,
            "political_context": political_context,
            "civil_unrest_indicators": civil_unrest_indicators
        }
    
    def _analyze_with_english_model(self, text: str, context: str = None) -> Dict[str, any]:
        """
        Fallback method - use rule-based approach for English text
        """
        logger.info("Using rule-based English analysis (ML models disabled)")
        
        # Extract threat keywords
        threat_keywords = self._extract_multilingual_threat_keywords(text)
        
        # Use rule-based classification
        return self._rule_based_threat_classification(text, threat_keywords, "english")
    
    def _normalize_swahili_sheng_text(self, text: str) -> str:
        """
        Normalize Sheng slang to standard Swahili/English
        """
        normalized = text.lower()
        
        # Apply Sheng dictionary normalization
        for sheng_word, standard_word in self.sheng_dictionary.items():
            normalized = normalized.replace(sheng_word, standard_word)
        
        return normalized
    
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
        
        logger.info(f"Searching for threat keywords in: '{text_lower}'")
        logger.info(f"Available threat keywords: {all_threat_keywords}")
        
        for keyword in all_threat_keywords:
            if keyword in text_lower:
                found_keywords.append(keyword)
                logger.info(f"Found threat keyword: '{keyword}'")
        
        logger.info(f"Total threat keywords found: {found_keywords}")
        return found_keywords[:10]  # Return top 10
    
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
        
        logger.info(f"Searching for threat keywords in: '{text_lower}'")
        logger.info(f"Available threat keywords: {all_threat_keywords}")
        
        for keyword in all_threat_keywords:
            if keyword in text_lower:
                found_keywords.append(keyword)
                logger.info(f"Found threat keyword: '{keyword}'")
        
        logger.info(f"Total threat keywords found: {found_keywords}")
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
        Improved logic for better differentiation between threat levels
        """
        # Base risk calculation with more differentiated weights
        weights = {"benign": 0.0, "suspicious": 0.3, "threat": 0.8, "civil_unrest": 0.9}
        
        risk_score = sum(
            probability * weights.get(category, 0.0)
            for category, probability in threat_probabilities.items()
        )
        
        # Check for threat keywords - this is crucial for proper classification
        threat_keywords_found = context.get("threat_keywords", []) if context else []
        has_threat_keywords = len(threat_keywords_found) > 0
        
        # Apply context enhancement only if threat keywords are found
        if context:
            # East African relevance boost (only for actual threats)
            if context.get("east_african_relevance", 0.0) > 0.5 and has_threat_keywords:
                risk_score += 0.15
            
            # Political context boost (only for actual threats)
            if context.get("political_context", False) and has_threat_keywords:
                risk_score += 0.2
            
            # Civil unrest indicators boost
            if context.get("civil_unrest_indicators") and len(context.get("civil_unrest_indicators")) > 0:
                risk_score += 0.25
        
        # Apply emergency context boost
        if context and any(keyword in str(context.get("emergency_keywords", "")).lower() 
                   for keyword in ["emergency", "urgent", "immediate", "critical"]):
            risk_score += 0.3
        
        # If no threat keywords found, significantly reduce risk score
        if not has_threat_keywords:
            risk_score = min(risk_score, 0.3)  # Cap at 30% for non-threat content
        
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
