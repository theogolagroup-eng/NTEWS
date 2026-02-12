import { useState, useEffect, useCallback, useRef } from 'react';
import { debounce, MemoCache } from '@/utils/performance';

interface UseDataFetchOptions {
  cacheKey?: string;
  cacheSize?: number;
  refreshInterval?: number;
  timeout?: number;
  retryAttempts?: number;
  retryDelay?: number;
  onSuccess?: (data: any) => void;
  onError?: (error: Error) => void;
}

interface UseDataFetchResult<T> {
  data: T | null;
  loading: boolean;
  error: string | null;
  refetch: () => void;
  lastUpdated: Date | null;
}

// Global cache instance
const globalCache = new MemoCache<string, any>(100);

export function useDataFetch<T>(
  fetchFunction: () => Promise<T>,
  options: UseDataFetchOptions = {}
): UseDataFetchResult<T> {
  const {
    cacheKey,
    cacheSize = 50,
    refreshInterval,
    timeout = 10000,
    retryAttempts = 3,
    retryDelay = 1000,
    onSuccess,
    onError
  } = options;

  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
  
  const abortControllerRef = useRef<AbortController | null>(null);
  const retryCountRef = useRef(0);
  const cacheRef = useRef<MemoCache<string, any>>(
    cacheKey ? new MemoCache<string, any>(cacheSize) : globalCache
  );

  const fetchData = useCallback(async (useCache = true) => {
    // Cancel previous request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }

    abortControllerRef.current = new AbortController();

    try {
      setLoading(true);
      setError(null);

      // Check cache first
      if (useCache && cacheKey) {
        const cachedData = cacheRef.current.get(cacheKey);
        if (cachedData) {
          setData(cachedData);
          setLastUpdated(new Date());
          setLoading(false);
          onSuccess?.(cachedData);
          return;
        }
      }

      // Create timeout promise
      const timeoutPromise = new Promise<never>((_, reject) => {
        setTimeout(() => reject(new Error('Request timeout')), timeout);
      });

      // Fetch data with timeout
      const result = await Promise.race([
        fetchFunction(),
        timeoutPromise
      ]);

      // Cache the result
      if (cacheKey) {
        cacheRef.current.set(cacheKey, result);
      }

      setData(result);
      setLastUpdated(new Date());
      retryCountRef.current = 0;
      onSuccess?.(result);

    } catch (err) {
      const error = err as Error;
      
      // Retry logic
      if (retryCountRef.current < retryAttempts && 
          !error.message.includes('aborted') &&
          !error.message.includes('timeout')) {
        retryCountRef.current++;
        console.warn(`Retrying fetch attempt ${retryCountRef.current}/${retryAttempts}`);
        
        setTimeout(() => {
          fetchData(false);
        }, retryDelay * retryCountRef.current);
        
        return;
      }

      setError(error.message);
      onError?.(error);
    } finally {
      setLoading(false);
    }
  }, [fetchFunction, cacheKey, timeout, retryAttempts, retryDelay, onSuccess, onError]);

  // Debounced refetch function
  const debouncedRefetch = useCallback(
    debounce(() => fetchData(false), 500),
    [fetchData]
  );

  // Initial fetch
  useEffect(() => {
    fetchData(true);

    // Set up refresh interval
    let intervalId: NodeJS.Timeout | null = null;
    
    if (refreshInterval) {
      intervalId = setInterval(() => {
        fetchData(false);
      }, refreshInterval);
    }

    return () => {
      if (intervalId) clearInterval(intervalId);
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [fetchData, refreshInterval]);

  return {
    data,
    loading,
    error,
    refetch: debouncedRefetch,
    lastUpdated
  };
}

// Hook for real-time data with WebSocket
export function useRealTimeData<T>(
  fetchFunction: () => Promise<T>,
  websocketUrl: string,
  options: UseDataFetchOptions = {}
): UseDataFetchResult<T> {
  const { refetch, ...result } = useDataFetch(fetchFunction, options);
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectAttempts = useRef(0);
  const maxReconnectAttempts = 5;

  useEffect(() => {
    const connectWebSocket = () => {
      try {
        wsRef.current = new WebSocket(websocketUrl);

        wsRef.current.onopen = () => {
          console.log('WebSocket connected');
          reconnectAttempts.current = 0;
        };

        wsRef.current.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data);
            console.log('Real-time data received:', data);
            refetch(); // Refresh data when new message arrives
          } catch (error) {
            console.error('Error parsing WebSocket message:', error);
          }
        };

        wsRef.current.onclose = () => {
          console.log('WebSocket disconnected');
          
          // Attempt to reconnect
          if (reconnectAttempts.current < maxReconnectAttempts) {
            reconnectAttempts.current++;
            setTimeout(connectWebSocket, 1000 * reconnectAttempts.current);
          }
        };

        wsRef.current.onerror = (error) => {
          console.error('WebSocket error:', error);
        };

      } catch (error) {
        console.error('Failed to create WebSocket connection:', error);
      }
    };

    connectWebSocket();

    return () => {
      if (wsRef.current) {
        wsRef.current.close();
      }
    };
  }, [websocketUrl, refetch]);

  return {
    ...result,
    refetch
  };
}
