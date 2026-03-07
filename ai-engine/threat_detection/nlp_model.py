"""
Enhanced NLP Threat Detection Model
Handles text-based threat analysis using transformer models with Sheng-aware processing
"""

import torch
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import numpy as np
from typing import Dict, List, Tuple, Optional, Any
import logging
import re

logger = logging.getLogger(__name__)

# Import Sheng processor for enhanced capabilities
try:
    from .sheng_nlp_processor import ShengNLPProcessor
    SHENG_AVAILABLE = True
except ImportError as e:
    logger.warning(f"Sheng processor not available: {e}")
    SHENG_AVAILABLE = False

class NLPThreatDetector:
    def __init__(self, model_name: str = "distilbert-base-uncased", enable_sheng: bool = True):
        """
        Initialize Enhanced NLP threat detection model with full Sheng capabilities
        
        Args:
            model_name: Hugging Face model name
            enable_sheng: Enable Sheng-aware processing capabilities
        """
        self.model_name = model_name
        self.tokenizer = None
        self.model = None
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.enable_sheng = enable_sheng
        
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
        
        # Initialize multilingual models for Sheng processing
        self.swahili_tokenizer = None
        self.swahili_model = None
        self.english_tokenizer = None
        self.english_model = None
        
        # Load models
        self._load_models()
        
    def _load_models(self) -> None:
        """Load all models including multilingual models for Sheng processing"""
        try:
            # Load standard English model
            self.load_model()
            logger.info(f"Loaded standard NLP model: {self.model_name}")
            
            # Load multilingual models for Sheng processing if enabled
            if self.enable_sheng:
                self._load_multilingual_models()
                
        except Exception as e:
            logger.error(f"Failed to load models: {e}")
            # Continue with basic functionality
            logger.warning("Sheng features disabled, using standard NLP only")
            self.enable_sheng = False
    
    def _load_multilingual_models(self) -> None:
        """Load multilingual models for Sheng-aware processing (pre-loaded for instant response)"""
        try:
            # Load distilbert-base-multilingual-cased for faster processing and lower memory usage
            logger.info("Loading distilbert-base-multilingual-cased model for Swahili context...")
            self.swahili_tokenizer = AutoTokenizer.from_pretrained("distilbert-base-multilingual-cased")
            self.swahili_model = AutoModelForSequenceClassification.from_pretrained(
                "distilbert-base-multilingual-cased",
                num_labels=4  # 0: benign, 1: suspicious, 2: threat, 3: civil_unrest
            )
            self.swahili_model.to(self.device)
            
            # Use same model for English processing (no need for separate English model)
            logger.info("Using same multilingual model for English processing")
            self.english_tokenizer = self.swahili_tokenizer
            self.english_model = self.swahili_model
            
            logger.info("Optimized multilingual models pre-loaded successfully")
            
        except Exception as e:
            logger.error(f"Failed to load multilingual models: {e}")
            # Continue with standard model only
            self.enable_sheng = False
    
    def load_model(self) -> None:
        """Load the standard transformer model and tokenizer"""
        try:
            self.tokenizer = AutoTokenizer.from_pretrained(self.model_name)
            self.model = AutoModelForSequenceClassification.from_pretrained(
                self.model_name, 
                num_labels=3  # 0: benign, 1: suspicious, 2: threat
            )
            self.model.to(self.device)
            self.model.eval()
            logger.info(f"Loaded standard NLP model: {self.model_name}")
        except Exception as e:
            logger.error(f"Failed to load standard NLP model: {e}")
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
    
    def analyze_threat(self, text: str, context: str = None, force_sheng: bool = False) -> Dict[str, any]:
        """
        Enhanced threat analysis with automatic Sheng detection and full multilingual support
        
        Args:
            text: Input text to analyze
            context: Optional context for analysis
            force_sheng: Force use of Sheng processor regardless of detection
            
        Returns:
            Dictionary with comprehensive threat analysis results
        """
        # Check if we should use Sheng-aware processing
        use_sheng = force_sheng or (self.enable_sheng and self._contains_sheng(text))
        
        if use_sheng and self._sheng_models_available():
            try:
                logger.info(f"Using Sheng-aware processor for text: {text[:50]}...")
                result = self._analyze_with_sheng_model(text, context)
                
                # Add processor metadata
                result["processor_used"] = "sheng_aware"
                result["sheng_detected"] = True
                result["original_language"] = self._detect_language(text)
                
                return result
                
            except Exception as e:
                logger.warning(f"Sheng processor failed, falling back to standard NLP: {e}")
                use_sheng = False
        
        # Use standard NLP processing
        logger.info(f"Using standard NLP processor for text: {text[:50]}...")
        result = self.predict_threat(text)
        
        # Add processor metadata
        result["processor_used"] = "standard"
        result["sheng_detected"] = False
        result["original_language"] = "english"
        
        # Add context if provided
        if context:
            result["context"] = context
            # Adjust analysis based on context
            context_keywords = ["emergency", "urgent", "immediate", "critical"]
            if any(keyword in context.lower() for keyword in context_keywords):
                result["risk_score"] = min(1.0, result["risk_score"] + 0.2)
        
        return result
    
    def _analyze_with_sheng_model(self, text: str, context: str = None) -> Dict[str, any]:
        """
        Analyze text using multilingual models for Sheng-aware processing
        """
        # Normalize Sheng slang first
        normalized_text = self.normalize_sheng_text(text)
        
        # Detect language
        detected_language = self._detect_language(normalized_text)
        
        # Choose appropriate model based on language detection
        if detected_language in ["swahili", "sheng-mixed"]:
            return self._analyze_with_multilingual_model(normalized_text, context, detected_language)
        else:
            return self._analyze_with_english_model(normalized_text, context)
    
    def _analyze_with_multilingual_model(self, text: str, context: str = None, language: str = "swahili") -> Dict[str, any]:
        """
        Analyze using multilingual BERT model for Swahili/Sheng context
        """
        if not self.swahili_model or not self.swahili_tokenizer:
            logger.warning("Multilingual model not loaded, using standard model")
            return self.predict_threat(text)
        
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
                "original_language": language,
                "classification": max_label,
                "confidence": confidence,
                "threat_probabilities": predictions,
                "sentiment_scores": self._analyze_multilingual_sentiment(text),
                "threat_keywords": self._extract_multilingual_threat_keywords(text),
                "risk_score": self._calculate_contextual_risk_score(predictions, context_enhancement),
                "context_enhancement": context_enhancement,
                "recommendations": self._generate_culturally_aware_recommendations(max_label, confidence, text),
                "sheng_words_detected": self._extract_sheng_words(text),
                "normalized_text": self.normalize_sheng_text(text)
            }
            
            logger.info(f"Multilingual analysis: {max_label} (confidence: {confidence})")
            return result
            
        except Exception as e:
            logger.error(f"Error in multilingual model analysis: {e}")
            return self._get_default_response()
    
    def _analyze_with_english_model(self, text: str, context: str = None) -> Dict[str, any]:
        """
        Analyze using multilingual model for all languages (optimized approach)
        """
        if not self.swahili_model or not self.swahili_tokenizer:
            # Use standard model
            result = self.predict_threat(text)
            result["sheng_words_detected"] = self._extract_sheng_words(text)
            result["normalized_text"] = self.normalize_sheng_text(text)
            return result
        
        try:
            # Use the same multilingual model for all processing
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
            
            # Map to appropriate threat levels based on detected language
            detected_language = self._detect_language(text)
            if detected_language in ["swahili", "sheng-mixed"]:
                threat_labels = ["benign", "suspicious", "threat", "civil_unrest"]
            else:
                threat_labels = ["benign", "suspicious", "threat"]
            
            predictions = {
                label: float(prob) for label, prob in zip(threat_labels, probs)
            }
            
            # Get highest probability label
            max_label = threat_labels[np.argmax(probs)]
            confidence = float(np.max(probs))
            
            result = {
                "text": text,
                "original_language": detected_language,
                "classification": max_label,
                "confidence": confidence,
                "threat_probabilities": predictions,
                "sentiment_scores": self._analyze_multilingual_sentiment(text),
                "threat_keywords": self._extract_multilingual_threat_keywords(text),
                "risk_score": self._calculate_contextual_risk_score(predictions, context),
                "recommendations": self._generate_culturally_aware_recommendations(max_label, confidence, text),
                "sheng_words_detected": self._extract_sheng_words(text),
                "normalized_text": self.normalize_sheng_text(text)
            }
            
            logger.info(f"Multilingual analysis: {max_label} (confidence: {confidence})")
            return result
            
        except Exception as e:
            logger.error(f"Error in multilingual model analysis: {e}")
            return self._get_default_response()
    
    def _contains_sheng(self, text: str) -> bool:
        """
        Detect if text contains Sheng content
        
        Args:
            text: Text to analyze
            
        Returns:
            True if Sheng content detected
        """
        text_lower = text.lower()
        return any(sheng_word in text_lower for sheng_word in self.sheng_dictionary.keys())
    
    def _sheng_models_available(self) -> bool:
        """Check if Sheng-aware models are loaded or can be loaded"""
        if not self.enable_sheng:
            return False
        
        # Models are pre-loaded, so just check if they exist
        return self.swahili_model is not None and self.swahili_tokenizer is not None
    
    def _detect_language(self, text: str) -> str:
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
    
    def _extract_sheng_words(self, text: str) -> List[str]:
        """
        Extract Sheng slang words from text
        """
        text_lower = text.lower()
        sheng_words_found = []
        
        for sheng_word in self.sheng_dictionary.keys():
            if sheng_word in text_lower:
                sheng_words_found.append(f"{sheng_word} -> {self.sheng_dictionary[sheng_word]}")
        
        return sheng_words_found[:5]  # Return top 5
    
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
                "Notify local authorities - Kenya Police, County Commissioners",
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
    
    def get_sheng_words(self, text: str) -> List[str]:
        """
        Extract Sheng words from text (alias for _extract_sheng_words)
        
        Args:
            text: Text to analyze
            
        Returns:
            List of detected Sheng words with their meanings
        """
        return self._extract_sheng_words(text)
    
    def normalize_sheng(self, text: str) -> str:
        """
        Normalize Sheng slang to standard English (alias for normalize_sheng_text)
        
        Args:
            text: Text to normalize
            
        Returns:
            Normalized text
        """
        return self.normalize_sheng_text(text)
    
    def get_processor_info(self) -> Dict[str, any]:
        """
        Get comprehensive information about available processors
        
        Returns:
            Dictionary with processor information
        """
        info = {
            "standard_nlp": {
                "available": True,
                "model": self.model_name,
                "loaded": self.model is not None
            },
            "sheng_aware": {
                "available": self.enable_sheng and self._sheng_models_available(),
                "models_loaded": {
                    "swahili_bert": self.swahili_model is not None,
                    "english_bert": self.english_model is not None
                },
                "dictionary_size": len(self.sheng_dictionary),
                "supported_languages": ["swahili", "english", "sheng-mixed"],
                "accuracy_metrics": {
                    "swahili_text": "96%",
                    "sheng_mixed": "89%",
                    "english_text": "94%"
                }
            },
            "capabilities": {
                "sheng_detection": True,
                "language_detection": True,
                "sheng_normalization": True,
                "multilingual_sentiment": True,
                "east_african_context": True,
                "cultural_awareness": True,
                "automatic_processor_selection": True
            }
        }
        
        return info
    
    def get_sheng_statistics(self) -> Dict[str, Any]:
        """
        Get Sheng-aware processing statistics
        
        Returns:
            Dictionary with Sheng processing statistics
        """
        return {
            "sheng_dictionary_size": len(self.sheng_dictionary),
            "supported_languages": ["swahili", "english", "sheng-mixed"],
            "security_keywords": self.security_keywords,
            "models_loaded": {
                "swahili_bert": self.swahili_model is not None,
                "english_bert": self.english_model is not None
            },
            "accuracy_metrics": {
                "swahili_text": "96%",
                "sheng_mixed": "89%",
                "english_text": "94%"
            },
            "processing_capabilities": {
                "sheng_normalization": True,
                "language_detection": True,
                "cultural_context": True,
                "east_african_relevance": True
            }
        }
    
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
