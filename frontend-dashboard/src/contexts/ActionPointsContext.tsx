'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';
import { message } from 'antd';
import { API_ENDPOINTS, apiClient } from '@/services/api';

// Action Point Types
export interface ActionPoint {
  id: string;
  title: string;
  description: string;
  type: 'investigate' | 'acknowledge' | 'escalate' | 'resolve' | 'monitor' | 'report';
  priority: 'low' | 'medium' | 'high' | 'critical';
  status: 'pending' | 'in_progress' | 'completed' | 'cancelled';
  assignedTo?: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
  dueDate?: string;
  relatedAlertId?: string;
  relatedThreatId?: string;
  relatedHotspotId?: string;
  actions: ActionItem[];
  humanApprovalRequired: boolean;
  autoTriggered: boolean;
  aiRecommendation?: string;
  humanNotes?: string;
}

export interface ActionItem {
  id: string;
  description: string;
  completed: boolean;
  completedBy?: string;
  completedAt?: string;
  requiresHumanApproval: boolean;
  aiSuggested: boolean;
}

export interface ActionPointWorkflow {
  id: string;
  name: string;
  description: string;
  triggers: string[];
  steps: WorkflowStep[];
  humanInterventionPoints: string[];
  autoExecute: boolean;
}

export interface WorkflowStep {
  id: string;
  name: string;
  description: string;
  type: 'automated' | 'human_required' | 'conditional';
  conditions?: string[];
  actions: string[];
  timeout?: number;
  escalationRules?: string[];
}

// Context Interface
interface ActionPointsContextType {
  actionPoints: ActionPoint[];
  workflows: ActionPointWorkflow[];
  loading: boolean;
  error: string | null;
  
  // Action Point Management
  createActionPoint: (actionPoint: Omit<ActionPoint, 'id' | 'createdAt' | 'updatedAt'>) => Promise<void>;
  updateActionPoint: (id: string, updates: Partial<ActionPoint>) => Promise<void>;
  deleteActionPoint: (id: string) => Promise<void>;
  assignActionPoint: (id: string, assignedTo: string) => Promise<void>;
  completeActionPoint: (id: string, notes?: string) => Promise<void>;
  
  // Workflow Management
  triggerWorkflow: (workflowId: string, context: any) => Promise<void>;
  executeWorkflowStep: (stepId: string, context: any) => Promise<void>;
  
  // Human-in-the-Loop
  requestHumanApproval: (actionPointId: string, reason: string) => Promise<void>;
  approveActionPoint: (id: string, notes?: string) => Promise<void>;
  rejectActionPoint: (id: string, reason: string) => Promise<void>;
  
  // AI Recommendations
  generateAIRecommendation: (context: any) => Promise<string>;
  applyAIRecommendation: (actionPointId: string) => Promise<void>;
  
  // Filtering and Search
  getActionPointsByStatus: (status: ActionPoint['status']) => ActionPoint[];
  getActionPointsByAssignee: (assignee: string) => ActionPoint[];
  getActionPointsByPriority: (priority: ActionPoint['priority']) => ActionPoint[];
  searchActionPoints: (query: string) => ActionPoint[];
  
  // Manual refresh
  refreshActionPoints: () => Promise<void>;
}

const ActionPointsContext = createContext<ActionPointsContextType>({
  actionPoints: [],
  workflows: [],
  loading: false,
  error: null,
  createActionPoint: async () => {},
  updateActionPoint: async () => {},
  deleteActionPoint: async () => {},
  assignActionPoint: async () => {},
  completeActionPoint: async () => {},
  triggerWorkflow: async () => {},
  executeWorkflowStep: async () => {},
  requestHumanApproval: async () => {},
  approveActionPoint: async () => {},
  rejectActionPoint: async () => {},
  generateAIRecommendation: async () => '',
  applyAIRecommendation: async () => {},
  getActionPointsByStatus: () => [],
  getActionPointsByAssignee: () => [],
  getActionPointsByPriority: () => [],
  searchActionPoints: () => [],
  refreshActionPoints: async () => {},
});

// Mock Data for Development
const mockWorkflows: ActionPointWorkflow[] = [
  {
    id: 'critical-alert-response',
    name: 'Critical Alert Response',
    description: 'Automated response workflow for critical security alerts',
    triggers: ['critical_alert_created', 'threat_level_critical'],
    steps: [
      {
        id: 'immediate-notification',
        name: 'Immediate Notification',
        description: 'Notify all security team members',
        type: 'automated',
        actions: ['send_notification', 'create_action_point']
      },
      {
        id: 'initial-investigation',
        name: 'Initial Investigation',
        description: 'AI-powered initial analysis',
        type: 'automated',
        actions: ['ai_analysis', 'threat_assessment']
      },
      {
        id: 'human-review',
        name: 'Human Analyst Review',
        description: 'Human analyst review and approval',
        type: 'human_required',
        actions: ['human_review', 'approval_decision']
      },
      {
        id: 'escalation-or-resolve',
        name: 'Escalation or Resolution',
        description: 'Based on human decision, escalate or resolve',
        type: 'conditional',
        conditions: ['human_approval'],
        actions: ['escalate_to_management', 'resolve_alert']
      }
    ],
    humanInterventionPoints: ['human-review'],
    autoExecute: true
  },
  {
    id: 'threat-hotspot-monitoring',
    name: 'Threat Hotspot Monitoring',
    description: 'Continuous monitoring of identified threat hotspots',
    triggers: ['hotspot_identified', 'risk_threshold_exceeded'],
    steps: [
      {
        id: 'enhanced-monitoring',
        name: 'Enhanced Monitoring',
        description: 'Increase monitoring frequency',
        type: 'automated',
        actions: ['increase_monitoring', 'data_collection']
      },
      {
        id: 'pattern-analysis',
        name: 'Pattern Analysis',
        description: 'AI analysis of threat patterns',
        type: 'automated',
        actions: ['pattern_detection', 'trend_analysis']
      },
      {
        id: 'analyst-assessment',
        name: 'Analyst Assessment',
        description: 'Human analyst assessment of threat patterns',
        type: 'human_required',
        actions: ['human_assessment', 'recommendation']
      }
    ],
    humanInterventionPoints: ['analyst-assessment'],
    autoExecute: true
  }
];

// Provider Component
export const ActionPointsProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [actionPoints, setActionPoints] = useState<ActionPoint[]>([]);
  const [workflows] = useState<ActionPointWorkflow[]>(mockWorkflows);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Load action points from backend on mount only
  useEffect(() => {
    fetchActionPoints();
  }, []); // Empty dependency array means run only once on mount

  const fetchActionPoints = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await apiClient.get(API_ENDPOINTS.ACTION_POINTS.ALL);
      setActionPoints(response || []);
    } catch (err) {
      console.error('Failed to load action points:', err);
      setError('Failed to load action points');
      // Don't re-throw to prevent continuous re-renders
    } finally {
      setLoading(false);
    }
  };

  // Action Point Management
  const createActionPoint = async (actionPointData: Omit<ActionPoint, 'id' | 'createdAt' | 'updatedAt'>) => {
    try {
      setLoading(true);
      
      const response = await apiClient.post(API_ENDPOINTS.ACTION_POINTS.CREATE, actionPointData);
      const newActionPoint = response;
      
      // Add to local state immediately without refresh
      setActionPoints(prev => [...prev, newActionPoint]);
      
      // Auto-trigger workflow if applicable
      if (newActionPoint.autoTriggered) {
        await triggerWorkflow('critical-alert-response', newActionPoint);
      }
    } catch (err) {
      setError('Failed to create action point');
      console.error('Create action point error:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const updateActionPoint = async (id: string, updates: Partial<ActionPoint>) => {
    try {
      setLoading(true);
      
      const response = await apiClient.put(API_ENDPOINTS.ACTION_POINTS.UPDATE(id), updates);
      const updatedActionPoint = response;
      
      setActionPoints(prev => 
        prev.map(ap => 
          ap.id === id ? updatedActionPoint : ap
        )
      );
    } catch (err) {
      setError('Failed to update action point');
      console.error('Update action point error:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const deleteActionPoint = async (id: string) => {
    try {
      setLoading(true);
      
      // Check if action point exists in local state before attempting delete
      const actionPointExists = actionPoints.some(ap => ap.id === id);
      if (!actionPointExists) {
        console.log('Action point already deleted, skipping:', id);
        message.info("Action point has already been deleted");
        return;
      }
      
      try {
        const response = await apiClient.delete(API_ENDPOINTS.ACTION_POINTS.DELETE(id));
        
        if (response) {
          // Remove from local state immediately
          setActionPoints(prev => prev.filter(ap => ap.id !== id));
          setError(null);
          message.success("Action point deleted successfully");
        } else {
          setError('Failed to delete action point');
          message.error("Failed to delete action point");
        }
      } catch (err: any) {
        // If it's a 404, the action point was already deleted on the backend
        // This can happen due to race conditions, so handle it gracefully
        const errorMessage = err?.message || '';
        const is404Error = errorMessage.includes('404') || 
                         errorMessage.includes('Not Found') ||
                         errorMessage.includes('404 Not Found') ||
                         err?.status === 404 ||
                         err?.response?.status === 404;
        
        if (is404Error) {
          console.log('Action point already deleted on backend, removing from local state:', id);
          setActionPoints(prev => prev.filter(ap => ap.id !== id));
          message.info("Action point has already been deleted");
          return; // Exit gracefully without re-throwing
        }
        
        // Re-throw non-404 errors to be handled by outer catch
        throw err;
      }
    } catch (err: any) {
      // Catch any remaining errors that might have been re-thrown
      const errorMessage = err?.message || '';
      const is404Error = errorMessage.includes('404') || 
                       errorMessage.includes('Not Found') ||
                       errorMessage.includes('404 Not Found') ||
                       err?.status === 404 ||
                       err?.response?.status === 404;
      
      // If it's still a 404 error, handle it gracefully here too
      if (is404Error) {
        console.log('Final catch: Action point already deleted, removing from local state:', id);
        setActionPoints(prev => prev.filter(ap => ap.id !== id));
        message.info("Action point has already been deleted");
        return; // Exit gracefully
      }
      
      setError('Failed to delete action point');
      console.error('Delete action point error:', err);
      message.error("Failed to delete action point");
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const assignActionPoint = async (id: string, assignedTo: string) => {
    try {
      setLoading(true);
      
      const response = await apiClient.post(API_ENDPOINTS.ACTION_POINTS.ASSIGN(id) + `?assignedTo=${encodeURIComponent(assignedTo)}`, null);
      const updatedActionPoint = response;
      
      setActionPoints(prev => 
        prev.map(ap => 
          ap.id === id ? updatedActionPoint : ap
        )
      );
    } catch (err) {
      setError('Failed to assign action point');
      console.error('Assign action point error:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const completeActionPoint = async (id: string, notes?: string) => {
    try {
      setLoading(true);
      
      const requestBody = notes ? { notes } : {};
      const response = await apiClient.post(API_ENDPOINTS.ACTION_POINTS.COMPLETE(id), requestBody);
      const updatedActionPoint = response;
      
      setActionPoints(prev => 
        prev.map(ap => 
          ap.id === id ? updatedActionPoint : ap
        )
      );
    } catch (err) {
      setError('Failed to complete action point');
      console.error('Complete action point error:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Workflow Management
  const triggerWorkflow = async (workflowId: string, context: any) => {
    try {
      setLoading(true);
      
      // Mock implementation - in real system, this would call workflow API
      console.log(`Triggering workflow: ${workflowId}`, context);
      
      // If the context is an alert, trigger actions for that alert
      if (context.relatedAlertId) {
        await apiClient.post(API_ENDPOINTS.ACTION_POINTS.TRIGGER_FOR_ALERT(context.relatedAlertId));
        await fetchActionPoints(); // Refresh action points
      }
    } catch (err) {
      setError('Failed to trigger workflow');
      console.error('Workflow trigger error:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const executeWorkflowStep = async (stepId: string, context: any) => {
    // Mock implementation - in real system, this would execute actual workflow logic
    console.log(`Executing workflow step: ${stepId}`, context);
  };

  // Human-in-the-Loop
  const requestHumanApproval = async (actionPointId: string, reason: string) => {
    await updateActionPoint(actionPointId, { 
      status: 'pending',
      humanNotes: reason
    });
  };

  const approveActionPoint = async (id: string, notes?: string) => {
    try {
      setLoading(true);
      
      const requestBody = notes ? { notes } : {};
      const response = await apiClient.post(API_ENDPOINTS.ACTION_POINTS.APPROVE(id), requestBody);
      const updatedActionPoint = response;
      
      setActionPoints(prev => 
        prev.map(ap => 
          ap.id === id ? updatedActionPoint : ap
        )
      );
    } catch (err) {
      setError('Failed to approve action point');
      console.error('Approve action point error:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const rejectActionPoint = async (id: string, reason: string) => {
    try {
      setLoading(true);
      
      const requestBody = { reason };
      const response = await apiClient.post(API_ENDPOINTS.ACTION_POINTS.REJECT(id), requestBody);
      const updatedActionPoint = response;
      
      setActionPoints(prev => 
        prev.map(ap => 
          ap.id === id ? updatedActionPoint : ap
        )
      );
    } catch (err) {
      setError('Failed to reject action point');
      console.error('Reject action point error:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // AI Recommendations
  const generateAIRecommendation = async (context: any): Promise<string> => {
    try {
      // Mock AI recommendation - in real system, this would call AI service
      const recommendations = [
        'Escalate to senior security analyst due to critical nature',
        'Initiate immediate incident response protocol',
        'Conduct thorough forensic analysis of affected systems',
        'Implement temporary security measures while investigating',
        'Coordinate with external security agencies if required'
      ];
      
      return recommendations[Math.floor(Math.random() * recommendations.length)];
    } catch (err) {
      console.error('Failed to generate AI recommendation:', err);
      return 'Unable to generate recommendation at this time';
    }
  };

  const applyAIRecommendation = async (actionPointId: string) => {
    try {
      setLoading(true);
      
      // Generate AI recommendation first
      await apiClient.post(API_ENDPOINTS.ACTION_POINTS.AI_RECOMMENDATION(actionPointId));
      
      // Then apply it
      const response = await apiClient.post(API_ENDPOINTS.ACTION_POINTS.APPLY_AI_RECOMMENDATION(actionPointId));
      const updatedActionPoint = response;
      
      setActionPoints(prev => 
        prev.map(ap => 
          ap.id === actionPointId ? updatedActionPoint : ap
        )
      );
    } catch (err) {
      setError('Failed to apply AI recommendation');
      console.error('Apply AI recommendation error:', err);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Filtering and Search
  const getActionPointsByStatus = (status: ActionPoint['status']) => 
    actionPoints.filter(ap => ap.status === status);

  const getActionPointsByAssignee = (assignee: string) => 
    actionPoints.filter(ap => ap.assignedTo === assignee);

  const getActionPointsByPriority = (priority: ActionPoint['priority']) => 
    actionPoints.filter(ap => ap.priority === priority);

  const searchActionPoints = (query: string) => {
    if (!query.trim()) return actionPoints;
    
    return actionPoints.filter(ap => 
      ap.title.toLowerCase().includes(query.toLowerCase()) ||
      ap.description.toLowerCase().includes(query.toLowerCase()) ||
      ap.humanNotes?.toLowerCase().includes(query.toLowerCase())
    );
  };

  const value: ActionPointsContextType = {
    actionPoints,
    workflows,
    loading,
    error,
    createActionPoint,
    updateActionPoint,
    deleteActionPoint,
    assignActionPoint,
    completeActionPoint,
    triggerWorkflow,
    executeWorkflowStep,
    requestHumanApproval,
    approveActionPoint,
    rejectActionPoint,
    generateAIRecommendation,
    applyAIRecommendation,
    getActionPointsByStatus,
    getActionPointsByAssignee,
    getActionPointsByPriority,
    searchActionPoints,
    refreshActionPoints: fetchActionPoints, // Add manual refresh function
  };

  return (
    <ActionPointsContext.Provider value={value}>
      {children}
    </ActionPointsContext.Provider>
  );
};

// Hook to use action points
export const useActionPoints = () => useContext(ActionPointsContext);

export default ActionPointsContext;
