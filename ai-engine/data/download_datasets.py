#!/usr/bin/env python3
"""
Dataset Download and Preparation Script for NTEWS MVP
"""

import os
import pandas as pd
import numpy as np
from pathlib import Path
import kaggle
import requests
import zipfile
from typing import List, Dict
import logging

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class DatasetManager:
    def __init__(self, base_path: str = "data/raw"):
        self.base_path = Path(base_path)
        self.base_path.mkdir(parents=True, exist_ok=True)
        
    def download_kaggle_dataset(self, dataset_name: str, extract: bool = True) -> str:
        """Download dataset from Kaggle"""
        try:
            logger.info(f"Downloading {dataset_name} from Kaggle...")
            
            # Download using kaggle API
            kaggle.api.dataset_download_files(
                dataset_name, 
                path=str(self.base_path / dataset_name.split('/')[-1]),
                unzip=extract
            )
            
            logger.info(f"Successfully downloaded {dataset_name}")
            return str(self.base_path / dataset_name.split('/')[-1])
            
        except Exception as e:
            logger.error(f"Failed to download {dataset_name}: {str(e)}")
            return None

    def download_url_dataset(self, url: str, filename: str) -> str:
        """Download dataset from URL"""
        try:
            logger.info(f"Downloading {filename} from {url}...")
            
            response = requests.get(url, stream=True)
            response.raise_for_status()
            
            file_path = self.base_path / filename
            with open(file_path, 'wb') as f:
                for chunk in response.iter_content(chunk_size=8192):
                    f.write(chunk)
            
            logger.info(f"Successfully downloaded {filename}")
            return str(file_path)
            
        except Exception as e:
            logger.error(f"Failed to download {filename}: {str(e)}")
            return None

    def prepare_crisis_data(self) -> pd.DataFrame:
        """Prepare crisis detection dataset"""
        datasets = [
            "thoughtvector/crisisNLP",
            "vstepanenko/disaster-tweets",
            "gpreda/covid19-tweets"
        ]
        
        all_data = []
        
        for dataset in datasets:
            path = self.download_kaggle_dataset(dataset)
            if path:
                # Find CSV files
                for csv_file in Path(path).glob("*.csv"):
                    try:
                        df = pd.read_csv(csv_file)
                        
                        # Standardize columns
                        if 'text' in df.columns:
                            df = df.rename(columns={'text': 'content'})
                        elif 'tweet' in df.columns:
                            df = df.rename(columns={'tweet': 'content'})
                        
                        # Add metadata
                        df['source_dataset'] = dataset
                        df['data_type'] = 'crisis'
                        all_data.append(df)
                        
                    except Exception as e:
                        logger.error(f"Error processing {csv_file}: {str(e)}")
        
        if all_data:
            combined_df = pd.concat(all_data, ignore_index=True)
            return self.clean_text_data(combined_df)
        
        return pd.DataFrame()

    def prepare_geospatial_data(self) -> pd.DataFrame:
        """Prepare geospatial threat data"""
        datasets = [
            "gkumarikanth/crime-data",
            "chirag19/urban-security-events",
            "sobhanmozzami/us-accidents"
        ]
        
        all_data = []
        
        for dataset in datasets:
            path = self.download_kaggle_dataset(dataset)
            if path:
                for csv_file in Path(path).glob("*.csv"):
                    try:
                        df = pd.read_csv(csv_file)
                        
                        # Standardize location columns
                        location_cols = ['latitude', 'longitude', 'lat', 'lng', 'lat_long']
                        for col in location_cols:
                            if col in df.columns:
                                if col == 'lat':
                                    df['latitude'] = df[col]
                                elif col == 'lng':
                                    df['longitude'] = df[col]
                        
                        # Add metadata
                        df['source_dataset'] = dataset
                        df['data_type'] = 'geospatial'
                        all_data.append(df)
                        
                    except Exception as e:
                        logger.error(f"Error processing {csv_file}: {str(e)}")
        
        if all_data:
            combined_df = pd.concat(all_data, ignore_index=True)
            return self.clean_geospatial_data(combined_df)
        
        return pd.DataFrame()

    def clean_text_data(self, df: pd.DataFrame) -> pd.DataFrame:
        """Clean and preprocess text data"""
        # Remove duplicates
        df = df.drop_duplicates(subset=['content'] if 'content' in df.columns else [])
        
        # Remove empty content
        if 'content' in df.columns:
            df = df[df['content'].notna() & (df['content'].str.strip() != '')]
        
        # Add basic features
        if 'content' in df.columns:
            df['content_length'] = df['content'].str.len()
            df['word_count'] = df['content'].str.split().str.len()
        
        return df

    def clean_geospatial_data(self, df: pd.DataFrame) -> pd.DataFrame:
        """Clean and preprocess geospatial data"""
        # Filter valid coordinates
        if 'latitude' in df.columns and 'longitude' in df.columns:
            df = df[
                (df['latitude'].between(-90, 90)) & 
                (df['longitude'].between(-180, 180))
            ]
        
        return df

def main():
    """Main execution function"""
    manager = DatasetManager()
    
    print("🚀 Starting dataset download and preparation for NTEWS MVP...")
    
    # Prepare crisis detection data
    print("\n📊 Preparing crisis detection datasets...")
    crisis_data = manager.prepare_crisis_data()
    
    if not crisis_data.empty:
        crisis_data.to_csv("data/processed/crisis_data.csv", index=False)
        print(f" Crisis data prepared: {len(crisis_data)} records")
        print(f" Columns: {list(crisis_data.columns)}")
    
    # Prepare geospatial data
    print("\n Preparing geospatial threat datasets...")
    geo_data = manager.prepare_geospatial_data()
    
    if not geo_data.empty:
        geo_data.to_csv("data/processed/geospatial_data.csv", index=False)
        print(f" Geospatial data prepared: {len(geo_data)} records")
        print(f" Columns: {list(geo_data.columns)}")
    
    print("\n🎉 Dataset preparation completed!")
    print("📁 Data saved to: data/processed/")

if __name__ == "__main__":
    main()
