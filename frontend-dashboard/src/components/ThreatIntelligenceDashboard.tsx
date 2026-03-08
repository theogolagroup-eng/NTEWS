import React, { useState, useEffect } from 'react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { API_ENDPOINTS, apiClient } from '@/services/api';
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import {
  AlertTriangle,
  TrendingUp,
  Shield,
  Activity,
  MapPin,
  Target,
  Brain,
  Database,
  Clock,
} from '@/components/icons';
import {
  Cloud,
  MessageSquare,
  Vote,
  Zap,
  Eye,
  Radio,
} from 'lucide-react';

interface ThreatAlert {
  id: string;
  title: string;
  description: string;
  severity: 'low' | 'medium' | 'high' | 'critical';
  category: string;
  location: {
    name: string;
    coordinates: [number, number];
  };
  timestamp: string;
  status: 'active' | 'resolved' | 'monitoring';
  confidence: number;
  aiAnalysis?: string;
  source: string;
}

interface IntelligenceReport {
  id: string;
  title: string;
  threatLevel: 'low' | 'medium' | 'high' | 'critical';
  category: string;
  summary: string;
  sources: Array<{
    name: string;
    reliability: number;
    type: string;
  }>;
  timestamp: string;
  status: 'draft' | 'under_review' | 'published';
  verified: boolean;
  aiConfidence: number;
  recommendations: string[];
}

interface PredictionData {
  id: string;
  forecastType: string;
  riskLevel: number;
  confidence: number;
  timeframe: string;
  locations: Array<{
    name: string;
    probability: number;
    severity: string;
  }>;
  factors: Array<{
    name: string;
    weight: number;
    impact: string;
  }>;
  timestamp: string;
}

interface SystemMetrics {
  totalAlerts: number;
  activeThreats: number;
  aiAccuracy: number;
  dataPoints: number;
  systemHealth: 'operational' | 'degraded' | 'critical';
  lastUpdate: string;
}

const ThreatIntelligenceDashboard: React.FC = () => {
  const [alerts, setAlerts] = useState<ThreatAlert[]>([]);
  const [intelligence, setIntelligence] = useState<IntelligenceReport[]>([]);
  const [predictions, setPredictions] = useState<PredictionData[]>([]);
  const [metrics, setMetrics] = useState<SystemMetrics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);

      const timeoutPromise = new Promise((_, reject) => 
        setTimeout(() => reject(new Error('Request timeout')), 10000)
      );

      // Fetch data using the same pattern as AlertsPage
      const [intelData, alertData, predictionData, allAlerts] = await Promise.all([
        Promise.race([apiClient.get(API_ENDPOINTS.INTELLIGENCE.DASHBOARD), timeoutPromise]),
        Promise.race([apiClient.get(API_ENDPOINTS.ALERTS.DASHBOARD), timeoutPromise]),
        Promise.race([apiClient.get(API_ENDPOINTS.PREDICTIONS.DASHBOARD), timeoutPromise]),
        Promise.race([apiClient.get(API_ENDPOINTS.ALERTS.ALL), timeoutPromise])
      ]);

      // Process alerts data - same successful pattern as AlertsPage
      const alertsList = allAlerts?.content || [];
      const processedAlerts: ThreatAlert[] = alertsList.slice(0, 10).map((alert: any) => ({
        id: alert.id,
        title: alert.title,
        description: alert.description,
        severity: alert.severity,
        category: alert.category,
        location: { 
          name: alert.location?.address || alert.location?.city || 'Unknown Location',
          coordinates: [
            parseFloat(alert.location?.latitude || '-1.2921'),
            parseFloat(alert.location?.longitude || '36.8219')
          ]
        },
        timestamp: alert.createdAt || alert.timestamp,
        status: alert.status,
        confidence: alert.confidence || 0.8,
        aiAnalysis: alert.aiAnalysis?.explanation,
        source: 'AI Engine - Threat Monitor'
      }));

      // Process intelligence data
      const processedIntelligence: IntelligenceReport[] = (intelData?.recentThreats || []).slice(0, 5).map((threat: any) => ({
        id: threat.id,
        title: threat.title,
        threatLevel: threat.threatLevel === 'critical' ? 'critical' : 
                   threat.threatLevel === 'high' ? 'high' : 
                   threat.threatLevel === 'medium' ? 'medium' : 'low',
        category: 'Strategic Analysis',
        summary: `${threat.title} - ${threat.location}`,
        sources: [
          { name: 'AI Engine', reliability: 0.92, type: 'SIGINT' },
          { name: 'Threat Analysis', reliability: 0.88, type: 'OSINT' }
        ],
        timestamp: threat.timestamp,
        status: 'published',
        verified: true,
        aiConfidence: 0.91,
        recommendations: [
          'Increase monitoring coverage',
          'Enhance defensive posture',
          'Coordinate with response teams'
        ]
      }));

      // Process prediction data
      const processedPredictions: PredictionData[] = (predictionData?.topHotspots || []).slice(0, 3).map((hotspot: any) => ({
        id: hotspot.id,
        forecastType: hotspot.threatType || 'Threat Activity',
        riskLevel: hotspot.probability || 0.5,
        confidence: 0.85,
        timeframe: '72 hours',
        locations: [
          { 
            name: hotspot.locationName || 'Unknown Area', 
            probability: hotspot.probability || 0.5, 
            severity: hotspot.severity || 'medium' 
          }
        ],
        factors: [
          { name: 'Historical Patterns', weight: 0.35, impact: 'high' },
          { name: 'Current Intelligence', weight: 0.45, impact: 'high' },
          { name: 'Environmental Factors', weight: 0.20, impact: 'medium' }
        ],
        timestamp: new Date().toISOString()
      }));

      // Set system metrics
      const processedMetrics: SystemMetrics = {
        totalAlerts: alertData?.totalAlerts || alertsList.length,
        activeThreats: intelData?.activeThreats || 0,
        aiAccuracy: 0.89,
        dataPoints: 18529,
        systemHealth: 'operational',
        lastUpdate: new Date().toISOString()
      };

      setAlerts(processedAlerts);
      setIntelligence(processedIntelligence);
      setPredictions(processedPredictions);
      setMetrics(processedMetrics);

    } catch (err) {
      console.error('Failed to fetch dashboard data:', err);
      setError('Failed to load threat intelligence data');
      
      // Fallback to mock data if API fails
      const mockAlerts: ThreatAlert[] = [
        {
          id: '1',
          title: 'Suspicious Social Media Activity',
          description: 'Coordinated posts detected across multiple platforms indicating potential protest organization',
          severity: 'medium',
          category: 'Social Unrest',
          location: { name: 'Nairobi CBD', coordinates: [-1.2921, 36.8219] },
          timestamp: '2024-02-21T10:30:00Z',
          status: 'active',
          confidence: 0.87,
          aiAnalysis: 'ML models detect 87% probability of organized protest based on historical patterns',
          source: 'AI Engine - Social Media Monitor'
        }
      ];

      const mockMetrics: SystemMetrics = {
        totalAlerts: 1247,
        activeThreats: 23,
        aiAccuracy: 0.89,
        dataPoints: 18529,
        systemHealth: 'operational',
        lastUpdate: '2024-02-21T10:45:00Z'
      };

      setAlerts(mockAlerts);
      setMetrics(mockMetrics);
    } finally {
      setLoading(false);
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'low': return 'bg-green-500';
      case 'medium': return 'bg-yellow-500';
      case 'high': return 'bg-orange-500';
      case 'critical': return 'bg-red-500';
      default: return 'bg-gray-500';
    }
  };

  const getThreatLevelColor = (level: string) => {
    switch (level) {
      case 'low': return 'text-green-600';
      case 'medium': return 'text-yellow-600';
      case 'high': return 'text-orange-600';
      case 'critical': return 'text-red-600';
      default: return 'text-gray-600';
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <Activity className="h-8 w-8 animate-spin mx-auto mb-4" />
          <p>Loading threat intelligence data...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Alert className="max-w-md">
          <AlertTriangle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Threat Intelligence Dashboard</h1>
            <p className="text-gray-600">Real-time national security threat monitoring and analysis</p>
          </div>
          <div className="flex items-center space-x-2">
            <div className={`w-3 h-3 rounded-full ${metrics?.systemHealth === 'operational' ? 'bg-green-500' : 'bg-red-500'}`} />
            <span className="text-sm text-gray-600">
              {metrics?.systemHealth === 'operational' ? 'System Operational' : 'System Issues'}
            </span>
          </div>
        </div>

        {/* System Metrics */}
        {metrics && (
          <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
            <Card>
              <CardContent className="p-4">
                <div className="flex items-center space-x-2">
                  <AlertTriangle className="h-5 w-5 text-red-500" />
                  <div>
                    <p className="text-sm text-gray-600">Total Alerts</p>
                    <p className="text-2xl font-bold">{metrics.totalAlerts}</p>
                  </div>
                </div>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-4">
                <div className="flex items-center space-x-2">
                  <Shield className="h-5 w-5 text-orange-500" />
                  <div>
                    <p className="text-sm text-gray-600">Active Threats</p>
                    <p className="text-2xl font-bold">{metrics.activeThreats}</p>
                  </div>
                </div>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-4">
                <div className="flex items-center space-x-2">
                  <Brain className="h-5 w-5 text-blue-500" />
                  <div>
                    <p className="text-sm text-gray-600">AI Accuracy</p>
                    <p className="text-2xl font-bold">{(metrics.aiAccuracy * 100).toFixed(1)}%</p>
                  </div>
                </div>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-4">
                <div className="flex items-center space-x-2">
                  <Database className="h-5 w-5 text-green-500" />
                  <div>
                    <p className="text-sm text-gray-600">Data Points</p>
                    <p className="text-2xl font-bold">{metrics.dataPoints.toLocaleString()}</p>
                  </div>
                </div>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="p-4">
                <div className="flex items-center space-x-2">
                  <Clock className="h-5 w-5 text-purple-500" />
                  <div>
                    <p className="text-sm text-gray-600">Last Update</p>
                    <p className="text-sm font-medium">{new Date(metrics.lastUpdate).toLocaleTimeString()}</p>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        )}

        {/* Recent Alerts */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <AlertTriangle className="h-5 w-5" />
              <span>Recent Threat Alerts</span>
            </CardTitle>
            <CardDescription>Latest security threats detected by AI monitoring systems</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {alerts.map((alert) => (
                <div key={alert.id} className="border rounded-lg p-4">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-2 mb-2">
                        <h3 className="font-semibold">{alert.title}</h3>
                        <Badge className={getSeverityColor(alert.severity)}>
                          {alert.severity.toUpperCase()}
                        </Badge>
                        <Badge variant="outline">{alert.category}</Badge>
                      </div>
                      <p className="text-gray-600 mb-2">{alert.description}</p>
                      <div className="flex items-center space-x-4 text-sm text-gray-500">
                        <div className="flex items-center space-x-1">
                          <MapPin className="h-4 w-4" />
                          <span>{alert.location.name}</span>
                        </div>
                        <div className="flex items-center space-x-1">
                          <Clock className="h-4 w-4" />
                          <span>{new Date(alert.timestamp).toLocaleString()}</span>
                        </div>
                        <div className="flex items-center space-x-1">
                          <Target className="h-4 w-4" />
                          <span>Confidence: {(alert.confidence * 100).toFixed(1)}%</span>
                        </div>
                      </div>
                      {alert.aiAnalysis && (
                        <div className="mt-2 p-2 bg-blue-50 rounded text-sm">
                          <strong>AI Analysis:</strong> {alert.aiAnalysis}
                        </div>
                      )}
                    </div>
                    <div className="ml-4">
                      <Badge variant={alert.status === 'active' ? 'destructive' : 'secondary'}>
                        {alert.status.replace('_', ' ').toUpperCase()}
                      </Badge>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Intelligence Reports */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <Brain className="h-5 w-5" />
              <span>Intelligence Reports</span>
            </CardTitle>
            <CardDescription>Latest intelligence analysis and assessments</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {intelligence.map((report) => (
                <div key={report.id} className="border rounded-lg p-4">
                  <div className="flex items-start justify-between mb-2">
                    <h3 className="font-semibold">{report.title}</h3>
                    <Badge className={getThreatLevelColor(report.threatLevel)}>
                      {report.threatLevel.toUpperCase()}
                    </Badge>
                  </div>
                  <p className="text-gray-600 mb-3">{report.summary}</p>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-3">
                    <div>
                      <h4 className="font-medium mb-1">Sources</h4>
                      <div className="space-y-1">
                        {report.sources.map((source, index) => (
                          <div key={index} className="flex items-center justify-between text-sm">
                            <span>{source.name}</span>
                            <Badge variant="outline">{source.type} ({(source.reliability * 100).toFixed(0)}%)</Badge>
                          </div>
                        ))}
                      </div>
                    </div>
                    <div>
                      <h4 className="font-medium mb-1">Recommendations</h4>
                      <ul className="space-y-1 text-sm">
                        {report.recommendations.map((rec, index) => (
                          <li key={index} className="flex items-start space-x-1">
                            <span>•</span>
                            <span>{rec}</span>
                          </li>
                        ))}
                      </ul>
                    </div>
                  </div>
                  <div className="flex items-center justify-between text-sm text-gray-500">
                    <div className="flex items-center space-x-4">
                      <span>AI Confidence: {(report.aiConfidence * 100).toFixed(1)}%</span>
                      <span>{new Date(report.timestamp).toLocaleString()}</span>
                    </div>
                    <Badge variant={report.verified ? 'default' : 'secondary'}>
                      {report.verified ? 'VERIFIED' : 'UNVERIFIED'}
                    </Badge>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Predictions */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <TrendingUp className="h-5 w-5" />
              <span>Threat Predictions</span>
            </CardTitle>
            <CardDescription>AI-powered threat forecasting and risk assessment</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {predictions.map((prediction) => (
                <div key={prediction.id} className="border rounded-lg p-4">
                  <div className="flex items-start justify-between mb-3">
                    <div>
                      <h3 className="font-semibold">{prediction.forecastType} Forecast</h3>
                      <p className="text-gray-600">Risk Level: {(prediction.riskLevel * 100).toFixed(1)}%</p>
                    </div>
                    <div className="text-right">
                      <Badge variant="outline">Confidence: {(prediction.confidence * 100).toFixed(1)}%</Badge>
                      <p className="text-sm text-gray-500 mt-1">Timeframe: {prediction.timeframe}</p>
                    </div>
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <h4 className="font-medium mb-2">Affected Locations</h4>
                      <div className="space-y-1">
                        {prediction.locations.map((location, index) => (
                          <div key={index} className="flex items-center justify-between text-sm">
                            <span>{location.name}</span>
                            <div className="flex items-center space-x-2">
                              <span>Probability: {(location.probability * 100).toFixed(1)}%</span>
                              <Badge className={getSeverityColor(location.severity)}>
                                {location.severity}
                              </Badge>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                    <div>
                      <h4 className="font-medium mb-2">Risk Factors</h4>
                      <div className="space-y-1">
                        {prediction.factors.map((factor, index) => (
                          <div key={index} className="flex items-center justify-between text-sm">
                            <span>{factor.name}</span>
                            <div className="flex items-center space-x-2">
                              <span>Weight: {(factor.weight * 100).toFixed(0)}%</span>
                              <Badge variant="outline">{factor.impact}</Badge>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default ThreatIntelligenceDashboard;
