'use client';

import { useActionPoints } from '@/contexts/ActionPointsContext';

// Action Trigger Service
export class ActionTriggerService {
  private static instance: ActionTriggerService;
  private actionPoints: ReturnType<typeof useActionPoints>;

  constructor(actionPointsHook: ReturnType<typeof useActionPoints>) {
    this.actionPoints = actionPointsHook;
  }

  static getInstance(actionPointsHook?: ReturnType<typeof useActionPoints>): ActionTriggerService {
    if (!ActionTriggerService.instance) {
      if (!actionPointsHook) {
        throw new Error('ActionPoints hook is required for first initialization');
      }
      ActionTriggerService.instance = new ActionTriggerService(actionPointsHook);
    }
    return ActionTriggerService.instance;
  }

  // Auto-trigger actions based on alert severity and type
  async triggerAlertActions(alert: any) {
    const { createActionPoint } = this.actionPoints;

    try {
      // Critical alerts get immediate action points
      if (alert.severity === 'critical') {
        await createActionPoint({
          title: `Critical Alert: ${alert.title}`,
          description: `Immediate investigation required for critical security alert: ${alert.description}`,
          type: 'investigate',
          priority: 'critical',
          status: 'pending',
          createdBy: 'System Auto-Trigger',
          relatedAlertId: alert.id,
          actions: [
            {
              id: 'immediate-review',
              description: 'Immediate security analyst review',
              completed: false,
              requiresHumanApproval: true,
              aiSuggested: true
            },
            {
              id: 'threat-assessment',
              description: 'Conduct comprehensive threat assessment',
              completed: false,
              requiresHumanApproval: false,
              aiSuggested: true
            }
          ],
          humanApprovalRequired: true,
          autoTriggered: true,
        });

        // Also create escalation action point
        await createActionPoint({
          title: `Escalate: ${alert.title}`,
          description: `Escalate critical alert to senior security team and management`,
          type: 'escalate',
          priority: 'critical',
          status: 'pending',
          createdBy: 'System Auto-Trigger',
          relatedAlertId: alert.id,
          actions: [
            {
              id: 'notify-management',
              description: 'Notify senior management of critical threat',
              completed: false,
              requiresHumanApproval: true,
              aiSuggested: true
            }
          ],
          humanApprovalRequired: true,
          autoTriggered: true,
        });
      }

      // High priority alerts get investigation action points
      else if (alert.severity === 'high') {
        await createActionPoint({
          title: `High Priority Alert: ${alert.title}`,
          description: `Investigate high priority security alert: ${alert.description}`,
          type: 'investigate',
          priority: 'high',
          status: 'pending',
          createdBy: 'System Auto-Trigger',
          relatedAlertId: alert.id,
          actions: [
            {
              id: 'preliminary-analysis',
              description: 'Conduct preliminary threat analysis',
              completed: false,
              requiresHumanApproval: false,
              aiSuggested: true
            }
          ],
          humanApprovalRequired: false,
          autoTriggered: true,
        });
      }

      // Medium priority alerts get monitoring action points
      else if (alert.severity === 'medium') {
        await createActionPoint({
          title: `Monitor: ${alert.title}`,
          description: `Monitor medium priority alert for escalation: ${alert.description}`,
          type: 'monitor',
          priority: 'medium',
          status: 'pending',
          createdBy: 'System Auto-Trigger',
          relatedAlertId: alert.id,
          actions: [
            {
              id: 'continuous-monitoring',
              description: 'Set up continuous monitoring for this alert',
              completed: false,
              requiresHumanApproval: false,
              aiSuggested: true
            }
          ],
          humanApprovalRequired: false,
          autoTriggered: true,
        });
      }

    } catch (error) {
      console.error('Failed to trigger alert actions:', error);
    }
  }

  // Auto-trigger actions based on threat intelligence
  async triggerThreatActions(threat: any) {
    const { createActionPoint } = this.actionPoints;

    try {
      if (threat.threatLevel === 'critical') {
        await createActionPoint({
          title: `Critical Threat Intelligence: ${threat.title}`,
          description: `Respond to critical threat intelligence: ${threat.description}`,
          type: 'investigate',
          priority: 'critical',
          status: 'pending',
          createdBy: 'System Auto-Trigger',
          relatedThreatId: threat.id,
          actions: [
            {
              id: 'intelligence-analysis',
              description: 'Deep analysis of threat intelligence',
              completed: false,
              requiresHumanApproval: true,
              aiSuggested: true
            },
            {
              id: 'countermeasures',
              description: 'Develop and implement countermeasures',
              completed: false,
              requiresHumanApproval: true,
              aiSuggested: true
            }
          ],
          humanApprovalRequired: true,
          autoTriggered: true,
        });
      }

      if (threat.category === 'cyber' && threat.severity === 'high') {
        await createActionPoint({
          title: `Cyber Threat Response: ${threat.title}`,
          description: `Coordinate cyber threat response: ${threat.description}`,
          type: 'escalate',
          priority: 'high',
          status: 'pending',
          createdBy: 'System Auto-Trigger',
          relatedThreatId: threat.id,
          actions: [
            {
              id: 'cyber-team-notification',
              description: 'Notify cyber security team',
              completed: false,
              requiresHumanApproval: false,
              aiSuggested: true
            }
          ],
          humanApprovalRequired: false,
          autoTriggered: true,
        });
      }

    } catch (error) {
      console.error('Failed to trigger threat actions:', error);
    }
  }

  // Auto-trigger actions based on prediction hotspots
  async triggerHotspotActions(hotspot: any) {
    const { createActionPoint } = this.actionPoints;

    try {
      if (hotspot.probability > 0.8 && hotspot.severity === 'high') {
        await createActionPoint({
          title: `High-Risk Hotspot: ${hotspot.locationName}`,
          description: `Address high-probability threat hotspot: ${hotspot.threatType} with ${(hotspot.probability * 100).toFixed(0)}% probability`,
          type: 'monitor',
          priority: 'high',
          status: 'pending',
          createdBy: 'System Auto-Trigger',
          relatedHotspotId: hotspot.id,
          actions: [
            {
              id: 'enhanced-surveillance',
              description: 'Implement enhanced surveillance in hotspot area',
              completed: false,
              requiresHumanApproval: false,
              aiSuggested: true
            },
            {
              id: 'resource-allocation',
              description: 'Allocate additional security resources',
              completed: false,
              requiresHumanApproval: true,
              aiSuggested: true
            }
          ],
          humanApprovalRequired: true,
          autoTriggered: true,
        });
      }

    } catch (error) {
      console.error('Failed to trigger hotspot actions:', error);
    }
  }

  // Generate AI-powered recommendations for action points
  async generateRecommendations(context: {
    alert?: any;
    threat?: any;
    hotspot?: any;
    systemStatus: any;
  }): Promise<string[]> {
    const recommendations: string[] = [];

    try {
      // Analyze context and generate recommendations
      if (context.alert) {
        if (context.alert.severity === 'critical') {
          recommendations.push('Immediate escalation to incident response team');
          recommendations.push('Activate emergency response protocols');
          recommendations.push('Notify all relevant stakeholders');
        } else if (context.alert.category === 'cyber') {
          recommendations.push('Engage cyber security team for forensic analysis');
          recommendations.push('Implement network isolation if necessary');
          recommendations.push('Review system logs for related activities');
        }
      }

      if (context.threat) {
        if (context.threat.category === 'physical') {
          recommendations.push('Coordinate with physical security teams');
          recommendations.push 'Review surveillance footage';
          recommendations.push('Increase physical security presence';
        } else if (context.threat.category === 'intelligence') {
          recommendations.push('Cross-reference with other intelligence sources');
          recommendations.push('Update threat assessment models');
          recommendations.push('Share relevant intelligence with partners';
        }
      }

      if (context.hotspot) {
        if (context.hotspot.probability > 0.7) {
          recommendations.push('Deploy additional monitoring resources');
          recommendations.push('Establish temporary security checkpoints');
          recommendations.push('Alert local authorities if applicable');
        }
      }

      // System-wide recommendations
      if (context.systemStatus.activeAlerts > 10) {
        recommendations.push('Consider declaring heightened alert status');
        recommendations.push('Activate additional analyst resources');
      }

      return recommendations;

    } catch (error) {
      console.error('Failed to generate recommendations:', error);
      return ['Unable to generate recommendations at this time'];
    }
  }

  // Check for overdue action points and create escalation actions
  async checkOverdueActions() {
    const { actionPoints, createActionPoint } = this.actionPoints;

    try {
      const now = new Date();
      const overdueActions = actionPoints.filter(ap => {
        if (ap.dueDate && ap.status !== 'completed') {
          return new Date(ap.dueDate) < now;
        }
        return false;
      });

      for (const overdueAction of overdueActions) {
        await createActionPoint({
          title: `Overdue Action Escalation: ${overdueAction.title}`,
          description: `Action point is overdue and requires immediate attention: ${overdueAction.description}`,
          type: 'escalate',
          priority: 'high',
          status: 'pending',
          createdBy: 'System Auto-Trigger',
          actions: [
            {
              id: 'immediate-attention',
              description: 'Requires immediate attention from supervisor',
              completed: false,
              requiresHumanApproval: false,
              aiSuggested: true
            }
          ],
          humanApprovalRequired: false,
          autoTriggered: true,
        });
      }

    } catch (error) {
      console.error('Failed to check overdue actions:', error);
    }
  }

  // Periodic system health check and action creation
  async performSystemHealthCheck(systemStatus: any) {
    const { createActionPoint } = this.actionPoints;

    try {
      // Check for unusual patterns
      if (systemStatus.errorRate > 0.1) {
        await createActionPoint({
          title: 'High System Error Rate Detected',
          description: `System error rate is ${(systemStatus.errorRate * 100).toFixed(1)}% - investigate immediately`,
          type: 'investigate',
          priority: 'high',
          status: 'pending',
          createdBy: 'System Health Monitor',
          actions: [
            {
              id: 'system-diagnosis',
              description: 'Run comprehensive system diagnostics',
              completed: false,
              requiresHumanApproval: false,
              aiSuggested: true
            }
          ],
          humanApprovalRequired: false,
          autoTriggered: true,
        });
      }

      if (systemStatus.responseTime > 5000) {
        await createActionPoint({
          title: 'System Performance Degradation',
          description: `System response time is ${systemStatus.responseTime}ms - performance optimization required`,
          type: 'investigate',
          priority: 'medium',
          status: 'pending',
          createdBy: 'System Health Monitor',
          actions: [
            {
              id: 'performance-optimization',
              description: 'Optimize system performance',
              completed: false,
              requiresHumanApproval: false,
              aiSuggested: true
            }
          ],
          humanApprovalRequired: false,
          autoTriggered: true,
        });
      }

    } catch (error) {
      console.error('Failed to perform system health check:', error);
    }
  }
}

export default ActionTriggerService;
