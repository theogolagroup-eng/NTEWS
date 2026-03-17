'use client';

import React, { useState, useEffect } from 'react';
import { Card, List, Badge, Tag, Space, Spin, Button, Tooltip } from 'antd';
import { 
  FireOutlined, 
  ClockCircleOutlined, 
  EyeOutlined,
  HeartOutlined,
  MessageOutlined,
  ShareAltOutlined,
  ExclamationCircleOutlined,
  BulbOutlined
} from '@ant-design/icons';

interface BlueskyPost {
  id: string;
  author: string;
  author_handle: string;
  text: string;
  cleaned_text: string;
  timestamp: string;
  url: string;
  language: string;
  hashtags: string[];
  metrics: {
    likes: number;
    shares: number;
    replies: number;
    engagement_score: number;
  };
  threat_level: {
    score: number;
    category: string;
    keywords: string[];
  };
  ai_analysis: {
    risk_score: number;
    confidence: number;
    classification: string;
  };
}

interface BlueskyTweetsPanelProps {
  isDarkMode?: boolean;
  themeStyles?: any;
}

const BlueskyTweetsPanel: React.FC<BlueskyTweetsPanelProps> = ({ 
  isDarkMode = false, 
  themeStyles = {} 
}) => {
  const [tweets, setTweets] = useState<BlueskyPost[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'trending' | 'recent'>('trending');

  useEffect(() => {
    fetchTweets();
    const interval = setInterval(fetchTweets, 30000); // Refresh every 30 seconds
    return () => clearInterval(interval);
  }, [activeTab]);

  const fetchTweets = async () => {
    try {
      setLoading(true);
      const response = await fetch(`http://localhost:8080/api/bluesky/metrics/${activeTab === 'trending' ? 'trending' : 'top-posts'}?limit=10`);
      
      // Check if response is actually OK before calling .json()
      if (!response.ok) {
        console.warn(`⚠️ Backend returned ${response.status}, showing empty list for now...`);
        setTweets([]); // Show empty list instead of retrying
        return;
      }
      
      const data = await response.json();
      
      console.log('🔍 Raw API data:', data);
      console.log('🔍 First post confidence:', data[0]?.confidence);
      console.log('🔍 First post metrics:', data[0]?.metrics);
      
      const transformedTweets = data.map((post: any) => {
        // Generate a fallback ID for posts with empty postUri
        const postId = post.postUri || post.id || post.post_id || `post-${post.id}`;
        
        return {
          id: postId,
          author: post.author || 'Unknown',
          author_handle: post.author_handle || '@unknown',
          text: post.text || '',
          cleaned_text: post.cleaned_text || '',
          timestamp: post.timestamp || new Date().toISOString(),
          url: post.url || `https://bsky.app/profile/${post.author_handle || 'unknown'}/post/${postId}`,
          language: post.language || 'en',
          hashtags: post.hashtags || [],
          metrics: {
            likes: post.metrics?.likes || 0,
            shares: post.metrics?.reposts || 0,
            replies: post.metrics?.replies || 0,
            engagement_score: post.metrics?.engagement_score || 0
          },
          threat_level: {
            score: post.threat_level?.score || 0,
            category: post.threat_level?.category || 'low',
            keywords: post.threat_level?.keywords || []
          },
          ai_analysis: {
            confidence: post.confidence || 0.7
          }
        };
      });
      
      console.log('🔍 Transformed tweets:', transformedTweets);
      console.log('🔍 First transformed confidence:', transformedTweets[0]?.ai_analysis?.confidence);
      console.log('🔍 First transformed engagement:', transformedTweets[0]?.metrics?.engagement_score);
      
      setTweets(transformedTweets);
    } catch (error) {
      console.error('Error fetching Bluesky tweets:', error);
      // Set empty array if error - real data will come from backend
      setTweets([]);
    } finally {
      setLoading(false);
    }
  };

  const getThreatColor = (score: number) => {
    if (score >= 0.7) return '#ff4d4f';
    if (score >= 0.4) return '#faad14';
    return '#52c41a';
  };

  const getThreatIcon = (score: number) => {
    if (score >= 0.7) return <ExclamationCircleOutlined style={{ color: '#ff4d4f' }} />;
    return <FireOutlined style={{ color: '#faad14' }} />;
  };

  return (
    <Card
      title={
        <div style={{ color: themeStyles.textColor, fontSize: '14px', fontWeight: '600' }}>
          <span style={{ 
            color: '#1DA1F2',
            fontSize: '16px',
            marginRight: '8px',
            display: 'inline-block',
            verticalAlign: 'middle'
          }}>𝕏</span>
          <span style={{ color: themeStyles.textColor }}>BLUESKY TWEETS</span>
          <Badge count={tweets.length} style={{ marginLeft: '8px', backgroundColor: '#1DA1F2' }} />
        </div>
      }
      extra={
        <Space>
          <Button 
            size="small" 
            type={activeTab === 'trending' ? 'primary' : 'default'}
            onClick={() => setActiveTab('trending')}
          >
            Trending
          </Button>
          <Button 
            size="small" 
            type={activeTab === 'recent' ? 'primary' : 'default'}
            onClick={() => setActiveTab('recent')}
          >
            Recent
          </Button>
        </Space>
      }
      styles={{ body: { padding: '12px' } }}
    >
      {loading ? (
        <div style={{ textAlign: 'center', padding: '20px' }}>
          <Spin />
        </div>
      ) : (
        <List
          dataSource={tweets}
          renderItem={(tweet: BlueskyPost) => {
            console.log('🎨 Rendering tweet:', tweet);
            console.log('🎨 Tweet confidence:', tweet.ai_analysis?.confidence);
            console.log('🎨 Tweet engagement:', tweet.metrics?.engagement_score);
            
            return (
              <List.Item style={{ 
                padding: '8px 0',
                borderBottom: isDarkMode ? '1px solid rgba(255,255,255,0.05)' : '1px solid rgba(0,0,0,0.06)'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', width: '100%' }}>
                  <div style={{ flex: 1 }}>
                    <div style={{ 
                      fontSize: '13px', 
                      fontWeight: '600', 
                      color: themeStyles.textColor,
                      marginBottom: '4px',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px'
                    }}>
                      <span>{tweet.author}</span>
                      <span style={{ opacity: 0.7, fontSize: '12px' }}>{tweet.author_handle}</span>
                      {tweet.threat_level.score > 0.4 && getThreatIcon(tweet.threat_level.score)}
                    </div>
                  
                  <div style={{ 
                    fontSize: '12px', 
                    color: themeStyles.textColor,
                    marginBottom: '6px',
                    lineHeight: '1.4'
                  }}>
                    {tweet.text.length > 150 ? `${tweet.text.substring(0, 150)}...` : tweet.text}
                  </div>
                  
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '6px' }}>
                    <Space size="small">
                      <span style={{ fontSize: '11px', opacity: 0.7 }}>
                        <ClockCircleOutlined /> {new Date(tweet.timestamp).toLocaleTimeString()}
                      </span>
                      <span style={{ fontSize: '11px', opacity: 0.7 }}>
                        <HeartOutlined /> {tweet.metrics.likes}
                      </span>
                      <span style={{ fontSize: '11px', opacity: 0.7 }}>
                        <ShareAltOutlined /> {tweet.metrics.shares}
                      </span>
                      <span style={{ fontSize: '11px', opacity: 0.7 }}>
                        <MessageOutlined /> {tweet.metrics.replies}
                      </span>
                    </Space>
                  </div>
                  
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                    {tweet.hashtags.slice(0, 3).map((tag, index) => (
                      <Tag key={index} color="blue" style={{ fontSize: '10px' }}>
                        #{tag}
                      </Tag>
                    ))}
                    
                    {tweet.threat_level.keywords.slice(0, 2).map((keyword, index) => (
                      <Tag key={index} color="orange" style={{ fontSize: '10px' }}>
                        {keyword}
                      </Tag>
                    ))}
                    
                    <Tag 
                      color={tweet.threat_level.score >= 0.7 ? 'red' : tweet.threat_level.score >= 0.4 ? 'orange' : 'green'}
                      style={{ fontSize: '10px' }}
                    >
                      Risk: {Math.round(tweet.threat_level.score * 100)}%
                    </Tag>
                  </div>
                </div>
                
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '6px' }}>
                  {/* AI Analysis Badge */}
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                    padding: '3px 8px',
                    borderRadius: '12px',
                    fontSize: '11px',
                    fontWeight: '500',
                    backgroundColor: tweet.ai_analysis.confidence >= 0.8 
                      ? 'rgba(220, 38, 38, 0.1)' 
                      : tweet.ai_analysis.confidence >= 0.6 
                      ? 'rgba(245, 158, 11, 0.1)' 
                      : 'rgba(34, 197, 94, 0.1)',
                    color: tweet.ai_analysis.confidence >= 0.8 
                      ? '#dc2626' 
                      : tweet.ai_analysis.confidence >= 0.6 
                      ? '#f59e0b'
                      : '#22c55e',
                    border: `1px solid ${
                      tweet.ai_analysis.confidence >= 0.8 
                        ? '#dc2626' 
                        : tweet.ai_analysis.confidence >= 0.6 
                        ? '#f59e0b'
                        : '#22c55e'
                    }`
                  }}>
                    <BulbOutlined style={{ fontSize: '10px' }} />
                    AI: {Math.round(tweet.ai_analysis.confidence * 100)}%
                  </div>
                  
                  {/* Engagement Metrics */}
                  <div style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px',
                    padding: '2px 6px',
                    borderRadius: '8px',
                    fontSize: '10px',
                    color: themeStyles.textColor,
                    backgroundColor: isDarkMode ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.03)',
                    opacity: 0.8
                  }}>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '2px' }}>
                      <HeartOutlined style={{ fontSize: '10px' }} />
                      {tweet.metrics.likes}
                    </span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '2px' }}>
                      <ShareAltOutlined style={{ fontSize: '10px' }} />
                      {tweet.metrics.shares}
                    </span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '2px' }}>
                      <MessageOutlined style={{ fontSize: '10px' }} />
                      {tweet.metrics.replies}
                    </span>
                  </div>
                </div>
              </div>
            </List.Item>
            );
          }}
        />
      )}
    </Card>
  );
};
export default BlueskyTweetsPanel;
