// API Configuration and Client
import axios from 'axios';

// Base API configuration
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

// API Endpoints
export const API_ENDPOINTS = {
  // Authentication Service
  AUTH: {
    LOGIN: `${API_BASE_URL}/api/auth/login`,
    LOGOUT: `${API_BASE_URL}/api/auth/logout`,
    VALIDATE: `${API_BASE_URL}/api/auth/validate`,
    STATUS: `${API_BASE_URL}/api/auth/status`,
  },
  
  // Intelligence Service
  INTELLIGENCE: {
    DASHBOARD: `${API_BASE_URL}/api/intelligence/dashboard/summary`,
    REPORTS: `${API_BASE_URL}/api/intelligence/reports`,
    REPORT: (id: string) => `${API_BASE_URL}/api/intelligence/reports/${id}`,
    CREATE: `${API_BASE_URL}/api/intelligence/reports`,
    UPDATE: (id: string) => `${API_BASE_URL}/api/intelligence/reports/${id}`,
    VERIFY: (id: string) => `${API_BASE_URL}/api/intelligence/reports/${id}/verify`,
    THREAT_TRENDS: `${API_BASE_URL}/api/intelligence/threat-trends`,
    THREAT_MAP: `${API_BASE_URL}/api/intelligence/threat-map`,
    PREDICTIONS: `${API_BASE_URL}/api/intelligence/predictions`,
  },
  
  // AI Engine Service (through API Gateway)
  AI_ENGINE: {
    PREDICT: `${API_BASE_URL}/api/ai/predict`,
    ANALYZE: `${API_BASE_URL}/api/ai/analyze`,
    RECOMMEND: `${API_BASE_URL}/api/ai/recommend`,
    CLASSIFY: `${API_BASE_URL}/api/ai/classify`,
    FORECAST: `${API_BASE_URL}/api/ai/forecast`,
  },
  
  // Alert Service
  ALERTS: {
    DASHBOARD: `${API_BASE_URL}/api/alerts/dashboard/summary`,
    ALL: `${API_BASE_URL}/api/alerts`,
    ALERT: (id: string) => `${API_BASE_URL}/api/alerts/${id}`,
    CREATE: `${API_BASE_URL}/api/alerts`,
    UPDATE: (id: string) => `${API_BASE_URL}/api/alerts/${id}`,
    ACKNOWLEDGE: (id: string) => `${API_BASE_URL}/api/alerts/${id}/acknowledge`,
    RESOLVE: (id: string) => `${API_BASE_URL}/api/alerts/${id}/resolve`,
    ASSIGN: (id: string) => `${API_BASE_URL}/api/alerts/${id}/assign`,
    ACTIVE: `${API_BASE_URL}/api/alerts/active`,
    UNACKNOWLEDGED: `${API_BASE_URL}/api/alerts/unacknowledged`,
    STATISTICS: `${API_BASE_URL}/api/alerts/statistics`,
    // NLP Analysis
    NLP_ANALYZE_TEXT: `${API_BASE_URL}/api/alerts/nlp/analyze-text`,
    NLP_BATCH_ANALYZE: `${API_BASE_URL}/api/alerts/nlp/batch-analyze`,
    NLP_CAPABILITIES: `${API_BASE_URL}/api/alerts/nlp/capabilities`,
    NLP_ANALYZE_ALERT: (id: string) => `${API_BASE_URL}/api/alerts/${id}/nlp-analyze`,
  },
  
  // Action Points Service (integrated with Alert Service)
  ACTION_POINTS: {
    ALL: `${API_BASE_URL}/api/action-points`,
    ACTION_POINT: (id: string) => `${API_BASE_URL}/api/action-points/${id}`,
    CREATE: `${API_BASE_URL}/api/action-points`,
    UPDATE: (id: string) => `${API_BASE_URL}/api/action-points/${id}`,
    DELETE: (id: string) => `${API_BASE_URL}/api/action-points/${id}`,
    ASSIGN: (id: string) => `${API_BASE_URL}/api/action-points/${id}/assign`,
    COMPLETE: (id: string) => `${API_BASE_URL}/api/action-points/${id}/complete`,
    APPROVE: (id: string) => `${API_BASE_URL}/api/action-points/${id}/approve`,
    REJECT: (id: string) => `${API_BASE_URL}/api/action-points/${id}/reject`,
    BY_STATUS: (status: string) => `${API_BASE_URL}/api/action-points/status/${status}`,
    BY_ALERT: (alertId: string) => `${API_BASE_URL}/api/action-points/alert/${alertId}`,
    BY_THREAT: (threatId: string) => `${API_BASE_URL}/api/action-points/threat/${threatId}`,
    BY_HOTSPOT: (hotspotId: string) => `${API_BASE_URL}/api/action-points/hotspot/${hotspotId}`,
    CREATE_BULK: `${API_BASE_URL}/api/action-points/bulk`,
    UPDATE_BULK: `${API_BASE_URL}/api/action-points/bulk/update`,
    DELETE_BULK: `${API_BASE_URL}/api/action-points/bulk/delete`,
    STATISTICS: `${API_BASE_URL}/api/action-points/statistics`,
    ESCALATE: (id: string) => `${API_BASE_URL}/api/action-points/${id}/escalate`,
    DELEGATE: (id: string) => `${API_BASE_URL}/api/action-points/${id}/delegate`,
    SEARCH: `${API_BASE_URL}/api/action-points/search`,
    TRIGGER_FOR_ALERT: (alertId: string) => `${API_BASE_URL}/api/action-points/trigger/${alertId}`,
    AI_RECOMMENDATION: (id: string) => `${API_BASE_URL}/api/action-points/${id}/ai-recommendation`,
    APPLY_AI_RECOMMENDATION: (id: string) => `${API_BASE_URL}/api/action-points/${id}/apply-ai-recommendation`,
  },
};

// API Client Class
class ApiClient {
  private baseURL: string;
  private authToken: string | null = null;

  constructor(baseURL: string = API_BASE_URL) {
    this.baseURL = baseURL;
  }

  setAuthToken(token: string) {
    this.authToken = token;
    if (typeof window !== 'undefined') {
      localStorage.setItem('authToken', token);
    }
  }

  clearAuthToken() {
    this.authToken = null;
    if (typeof window !== 'undefined') {
      localStorage.removeItem('authToken');
    }
  }

  getAuthHeaders(): Record<string, string> {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };

    if (this.authToken) {
      headers['Authorization'] = `Bearer ${this.authToken}`;
    }

    return headers;
  }

  async request<T = any>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = endpoint.startsWith('http') ? endpoint : `${this.baseURL}${endpoint}`;
    const config: RequestInit = {
      headers: this.getAuthHeaders(),
      ...options,
    };

    try {
      const response = await fetch(url, config);
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(`API Error: ${response.status} - ${JSON.stringify(errorData)}`);
      }

      // Handle empty responses (like DELETE requests)
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        return await response.json();
      } else if (response.status === 204 || response.status === 200) {
        // For DELETE requests that return empty body
        return {} as T;
      } else {
        return await response.json();
      }
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

  async get<T = any>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'GET' });
  }

  async post<T = any>(endpoint: string, data?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async put<T = any>(endpoint: string, data?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  async delete<T = any>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE' });
  }

  async login(credentials: { username: string; password: string }) {
    try {
      const response = await this.post('/api/auth/login', credentials);
      if (response.token) {
        this.setAuthToken(response.token);
      }
      return response;
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  }

  async logout() {
    try {
      await this.post('/api/auth/logout');
    } catch (error) {
      console.error('Logout failed:', error);
    } finally {
      this.clearAuthToken();
    }
  }

  async validateToken() {
    if (!this.authToken) {
      return { valid: false, user: null };
    }

    try {
      const response = await this.get('/api/auth/validate');
      return { valid: true, user: response.user };
    } catch (error) {
      this.clearAuthToken();
      return { valid: false, user: null };
    }
  }
}

// Create and export API client instance
export const apiClient = new ApiClient();

// AI Engine Client Class
class AiEngineClient {
  private baseURL: string;

  constructor(baseURL: string = 'http://localhost:8080') {
    this.baseURL = baseURL;
  }

  async request<T = any>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.baseURL}${endpoint}`;
    const config: RequestInit = {
      headers: {
        'Content-Type': 'application/json',
      },
      ...options,
    };

    try {
      const response = await fetch(url, config);
      
      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(`AI Engine Error: ${response.status} - ${JSON.stringify(errorData)}`);
      }

      return await response.json();
    } catch (error) {
      console.error('AI Engine request failed:', error);
      throw error;
    }
  }

  async post<T = any>(endpoint: string, data?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async get<T = any>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'GET' });
  }
}

// Create and export AI Engine client instance
export const aiEngineClient = new AiEngineClient();

// Export individual API methods for convenience
export const api = {
  get: <T = any>(endpoint: string) => apiClient.get<T>(endpoint),
  post: <T = any>(endpoint: string, data?: any) => apiClient.post<T>(endpoint, data),
  put: <T = any>(endpoint: string, data?: any) => apiClient.put<T>(endpoint, data),
  delete: <T = any>(endpoint: string) => apiClient.delete<T>(endpoint),
  login: (credentials: { username: string; password: string }) => apiClient.login(credentials),
  logout: () => apiClient.logout(),
  validateToken: () => apiClient.validateToken(),
  setAuthToken: (token: string) => apiClient.setAuthToken(token),
  clearAuthToken: () => apiClient.clearAuthToken(),
};

export default apiClient;
