"use client";

import React, { useState } from "react";
import {
  Card,
  List,
  Button,
  Tag,
  Space,
  Modal,
  Form,
  Input,
  Select,
  DatePicker,
  message,
  Tooltip,
  Badge,
  Avatar,
  Divider,
  Alert,
  Progress,
  Row,
  Col,
} from "antd";
import {
  PlusOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  ExclamationCircleOutlined,
  UserOutlined,
  RobotOutlined,
  AlertOutlined,
  FireOutlined,
  SafetyOutlined,
  EyeOutlined,
  EditOutlined,
  DeleteOutlined,
  SendOutlined,
  ThunderboltOutlined,
} from "@ant-design/icons";
import { useActionPoints, ActionPoint } from "@/contexts/ActionPointsContext";
import { useTheme } from "@/contexts/ThemeContext";

const { TextArea } = Input;
const { Option } = Select;

interface ActionPointsPanelProps {
  relatedAlertId?: string;
  relatedThreatId?: string;
  relatedHotspotId?: string;
}

export default function ActionPointsPanel({
  relatedAlertId,
  relatedThreatId,
  relatedHotspotId,
}: ActionPointsPanelProps) {
  const { themeStyles } = useTheme();
  const {
    actionPoints,
    createActionPoint,
    updateActionPoint,
    deleteActionPoint,
    assignActionPoint,
    completeActionPoint,
    approveActionPoint,
    rejectActionPoint,
    generateAIRecommendation,
    applyAIRecommendation,
    loading,
  } = useActionPoints();

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [selectedAction, setSelectedAction] = useState<ActionPoint | null>(
    null,
  );
  const [form] = Form.useForm();
  const [detailForm] = Form.useForm();

  // Filter action points related to current context
  const relatedActionPoints = actionPoints.filter((ap) => {
    // If no context is provided, show all action points
    if (!relatedAlertId && !relatedThreatId && !relatedHotspotId) {
      return true;
    }
    
    // Otherwise, filter by context
    if (relatedAlertId && ap.relatedAlertId === relatedAlertId) return true;
    if (relatedThreatId && ap.relatedThreatId === relatedThreatId) return true;
    if (relatedHotspotId && ap.relatedHotspotId === relatedHotspotId)
      return true;
    return false;
  });

  const getStatusColor = (status: ActionPoint["status"]) => {
    const colors = {
      completed: 'green',
      in_progress: 'blue',
      pending: 'orange',
      cancelled: 'default'
    };
    return colors[status] || 'default';
  };

  const getPriorityColor = (priority: ActionPoint["priority"]) => {
    const colors = {
      critical: 'red',
      high: 'orange',
      medium: 'gold',
      low: 'green'
    };
    return colors[priority] || 'default';
  };

  const formatDate = (timestamp: string) => {
    try {
      return new Date(timestamp).toLocaleString();
    } catch (error) {
      return 'Invalid Date';
    }
  };

  const getStatusIcon = (status: ActionPoint["status"]) => {
    switch (status) {
      case "completed":
        return <CheckCircleOutlined style={{ color: "#52c41a" }} />;
      case "in_progress":
        return <ClockCircleOutlined style={{ color: "#1890ff" }} />;
      case "pending":
        return <ExclamationCircleOutlined style={{ color: "#fa8c16" }} />;
      case "cancelled":
        return <DeleteOutlined style={{ color: "#ff4d4f" }} />;
      default:
        return <ClockCircleOutlined style={{ color: "#8c8c8c" }} />;
    }
  };

  const getTypeIcon = (type: ActionPoint["type"]) => {
    switch (type) {
      case "investigate":
        return <EyeOutlined />;
      case "acknowledge":
        return <CheckCircleOutlined />;
      case "escalate":
        return <ThunderboltOutlined />;
      case "resolve":
        return <SafetyOutlined />;
      case "monitor":
        return <AlertOutlined />;
      case "report":
        return <FireOutlined />;
      default:
        return <AlertOutlined />;
    }
  };

  const handleCreateAction = async (values: any) => {
    try {
      // Create minimal action point data to avoid serialization issues
      const actionPointData: any = {
        title: values.title,
        description: values.description || '',
        type: values.type || 'investigate',
        priority: values.priority || 'medium',
        status: 'pending' as const,
        createdBy: 'System Analyst',
        autoTriggered: false,
        humanApprovalRequired: false,
        actions: []
      };

      // Only add optional fields if they exist
      if (values.dueDate) {
        actionPointData.dueDate = values.dueDate.toISOString();
      }
      if (relatedAlertId) {
        actionPointData.relatedAlertId = relatedAlertId;
      }
      if (relatedThreatId) {
        actionPointData.relatedThreatId = relatedThreatId;
      }
      if (relatedHotspotId) {
        actionPointData.relatedHotspotId = relatedHotspotId;
      }

      console.log('Sending action point data:', actionPointData);
      await createActionPoint(actionPointData);

      message.success("Action point created successfully");
      setShowCreateModal(false);
      form.resetFields();
      
      // Manually refresh the action points list to ensure it appears
      setTimeout(() => {
        window.location.reload(); // Simple refresh for now
      }, 500);
    } catch (error) {
      message.error("Failed to create action point");
    }
  };

  const handleAssignAction = async (actionId: string) => {
    try {
      await assignActionPoint(actionId, "Current Analyst"); // In real app, get from auth context
      message.success("Action point assigned successfully");
    } catch (error) {
      message.error("Failed to assign action point");
    }
  };

  const handleCompleteAction = async (actionId: string) => {
    try {
      await completeActionPoint(actionId, "Completed by system analyst");
      message.success("Action point completed successfully");
    } catch (error) {
      message.error("Failed to complete action point");
    }
  };

  const handleApproveAction = async (actionId: string) => {
    try {
      await approveActionPoint(actionId, "Approved by system analyst");
      message.success("Action point approved successfully");
    } catch (error) {
      message.error("Failed to approve action point");
    }
  };

  const handleRejectAction = async (actionId: string, reason: string) => {
    try {
      await rejectActionPoint(actionId, reason);
      message.success("Action point rejected successfully");
    } catch (error) {
      message.error("Failed to reject action point");
    }
  };

  const handleGenerateAIRecommendation = async (actionId: string) => {
    try {
      await applyAIRecommendation(actionId);
      message.success("AI recommendation generated and applied");
    } catch (error) {
      message.error("Failed to generate AI recommendation");
    }
  };

  const handleDeleteAction = async (actionId: string) => {
    try {
      await deleteActionPoint(actionId);
      message.success("Action point deleted successfully");
    } catch (error) {
      message.error("Failed to delete action point");
    }
  };

  const showActionDetails = (action: ActionPoint) => {
    setSelectedAction(action);
    detailForm.setFieldsValue({
      humanNotes: action.humanNotes || "",
    });
    setShowDetailModal(true);
  };

  const completionPercentage = (action: ActionPoint) => {
    if (!action.actions || action.actions.length === 0) return 0;
    const completed = action.actions.filter((a) => a.completed).length;
    return Math.round((completed / action.actions.length) * 100);
  };

  return (
    <div>
      {/* Simple header matching alerts page style */}
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: "16px",
        }}
      >
        <h3
          style={{
            color: themeStyles.textColor,
            margin: 0,
            fontSize: "16px",
            fontWeight: "600",
            textShadow: "0 1px 2px rgba(0,0,0,0.8)",
          }}
        >
          Action Points ({relatedActionPoints.length})
        </h3>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => setShowCreateModal(true)}
          style={{
            background: "#1890ff",
            borderColor: "#1890ff",
          }}
        >
          Create Action
        </Button>
      </div>

      {/* Action Points List */}
      <List
        dataSource={relatedActionPoints}
        renderItem={(action) => (
          <Card
            size="small"
            style={{
              background: themeStyles.cardBackground,
              border: themeStyles.cardBorder,
              marginBottom: "8px",
            }}
            styles={{ body: { padding: "12px" } }}
          >
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "flex-start",
              }}
            >
              <div style={{ flex: 1 }}>
                <div
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: "8px",
                    marginBottom: "4px",
                  }}
                >
                  {getStatusIcon(action.status)}
                  {getTypeIcon(action.type)}
                  <span
                    style={{
                      color: themeStyles.textColor,
                      fontWeight: "600",
                      fontSize: "14px",
                      textShadow: "0 1px 2px rgba(0,0,0,0.8)",
                    }}
                  >
                    {action.title}
                  </span>
                  <Tag color={getPriorityColor(action.priority)}>
                    {action.priority?.toUpperCase()}
                  </Tag>
                  <Tag color={getStatusColor(action.status)}>
                    {action.status?.replace('_', ' ').toUpperCase()}
                  </Tag>
                  {action.autoTriggered && (
                    <Tooltip title="AI-triggered action">
                      <RobotOutlined style={{ color: "#722ed1" }} />
                    </Tooltip>
                  )}
                </div>

                <p
                  style={{
                    color: themeStyles.secondaryTextColor,
                    margin: "4px 0",
                    fontSize: "13px",
                    fontWeight: "400",
                    textShadow: "0 1px 1px rgba(0,0,0,0.5)",
                    lineHeight: "1.4",
                  }}
                >
                  {action.description}
                </p>

                {action.aiRecommendation && (
                  <Alert
                    message="AI Recommendation"
                    description={action.aiRecommendation}
                    type="info"
                    style={{ marginBottom: "8px" }}
                  />
                )}

                {action.actions && action.actions.length > 0 && (
                  <div style={{ marginBottom: "8px" }}>
                    <Progress
                      percent={completionPercentage(action)}
                      size="small"
                      strokeColor="#52c41a"
                      format={() => `${completionPercentage(action)}%`}
                    />
                  </div>
                )}

                <div
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: "8px",
                    fontSize: "12px",
                  }}
                >
                  <span style={{ color: themeStyles.secondaryTextColor, textShadow: "0 1px 1px rgba(0,0,0,0.5)" }}>
                    Created: {formatDate(action.createdAt)}
                  </span>
                  {action.assignedTo && (
                    <span style={{ color: themeStyles.secondaryTextColor, textShadow: "0 1px 1px rgba(0,0,0,0.5)" }}>
                      • Assigned to: {action.assignedTo}
                    </span>
                  )}
                  {action.dueDate && (
                    <span style={{ color: themeStyles.secondaryTextColor, textShadow: "0 1px 1px rgba(0,0,0,0.5)" }}>
                      • Due: {formatDate(action.dueDate)}
                    </span>
                  )}
                  {action.humanApprovalRequired && (
                    <Tag color="blue">
                      Human Approval Required
                    </Tag>
                  )}
                </div>
              </div>

              <div style={{ display: "flex", gap: "4px" }}>
                <Tooltip title="View Details">
                  <Button
                    type="text"
                    size="small"
                    icon={<EyeOutlined />}
                    onClick={() => showActionDetails(action)}
                  />
                </Tooltip>

                {action.status === "pending" && (
                  <>
                    <Tooltip title="Assign to Me">
                      <Button
                        type="text"
                        size="small"
                        icon={<UserOutlined />}
                        onClick={() => handleAssignAction(action.id)}
                      />
                    </Tooltip>
                    <Tooltip title="Approve">
                      <Button
                        type="text"
                        size="small"
                        icon={<CheckCircleOutlined />}
                        onClick={() => handleApproveAction(action.id)}
                      />
                    </Tooltip>
                  </>
                )}

                {action.status === "in_progress" && (
                  <Tooltip title="Complete">
                    <Button
                      type="text"
                      size="small"
                      icon={<CheckCircleOutlined />}
                      onClick={() => handleCompleteAction(action.id)}
                    />
                  </Tooltip>
                )}

                {!action.aiRecommendation && (
                  <Tooltip title="Generate AI Recommendation">
                    <Button
                      type="text"
                      size="small"
                      icon={<RobotOutlined />}
                      onClick={() => handleGenerateAIRecommendation(action.id)}
                    />
                  </Tooltip>
                )}

                <Tooltip title="Delete Action Point">
                  <Button
                    type="text"
                    size="small"
                    icon={<DeleteOutlined />}
                    onClick={() => handleDeleteAction(action.id)}
                    danger
                  />
                </Tooltip>
              </div>
            </div>
          </Card>
        )}
        locale={{ emptyText: "No action points found" }}
      />

      {/* Create Action Modal */}
      <Modal
        title={
          <span style={{ color: "#e6edf3", fontWeight: 600 }}>
            Create Action Point
          </span>
        }
        open={showCreateModal}
        onCancel={() => setShowCreateModal(false)}
        footer={null}
        width={600}
        styles={{
          content: {
            background: "#161b22",
            border: "1px solid #30363d",
            padding: 0,
          },
          header: {
            background: "#161b22",
            borderBottom: "1px solid #30363d",
            padding: "16px 20px",
            marginBottom: 0,
          },
          body: { background: "#161b22", padding: "20px" },
          mask: { backdropFilter: "blur(2px)" },
        }}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleCreateAction}
          style={{ color: "#e6edf3" }}
          initialValues={{
            humanApprovalRequired: false,
            autoTriggered: false
          }}
        >
          <Form.Item
            name="title"
            label={<span style={{ color: themeStyles.textColor, fontWeight: "500", textShadow: "0 1px 1px rgba(0,0,0,0.5)" }}>Action Title</span>}
            rules={[{ required: true, message: "Please enter action title" }]}
          >
            <Input
              placeholder="Enter action title"
              style={{
                background: "#21262d",
                border: "1px solid #30363d",
                color: "#e6edf3",
              }}
            />
          </Form.Item>

          <Form.Item
            name="description"
            label={<span style={{ color: themeStyles.textColor, fontWeight: "500", textShadow: "0 1px 1px rgba(0,0,0,0.5)" }}>Description</span>}
            rules={[{ required: true, message: "Please enter description" }]}
          >
            <TextArea
              rows={3}
              placeholder="Describe the action to be taken"
              style={{
                background: "#21262d",
                border: "1px solid #30363d",
                color: "#e6edf3",
              }}
            />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="type"
                label={<span style={{ color: themeStyles.textColor, fontWeight: "500", textShadow: "0 1px 1px rgba(0,0,0,0.5)" }}>Action Type</span>}
                rules={[
                  { required: true, message: "Please select action type" },
                ]}
              >
                <Select
                  placeholder="Select action type"
                  style={{ width: "100%" }}
                  styles={{
                    popup: {
                      root: {
                        background: "#21262d",
                        border: "1px solid #30363d",
                      },
                    },
                  }}
                >
                  <Option value="investigate">Investigate</Option>
                  <Option value="acknowledge">Acknowledge</Option>
                  <Option value="escalate">Escalate</Option>
                  <Option value="resolve">Resolve</Option>
                  <Option value="monitor">Monitor</Option>
                  <Option value="report">Report</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="priority"
                label={<span style={{ color: themeStyles.textColor, fontWeight: "500", textShadow: "0 1px 1px rgba(0,0,0,0.5)" }}>Priority</span>}
                rules={[
                  { required: true, message: "Please select priority" },
                ]}
              >
                <Select
                  placeholder="Select priority"
                  style={{ width: "100%" }}
                  styles={{
                    popup: {
                      root: {
                        background: "#21262d",
                        border: "1px solid #30363d",
                      },
                    },
                  }}
                >
                  <Option value="low">Low</Option>
                  <Option value="medium">Medium</Option>
                  <Option value="high">High</Option>
                  <Option value="critical">Critical</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="dueDate"
            label={
              <span style={{ color: themeStyles.textColor, fontWeight: "500", textShadow: "0 1px 1px rgba(0,0,0,0.5)" }}>Due Date (Optional)</span>
            }
          >
            <DatePicker
              style={{
                width: "100%",
                background: "#21262d",
                border: "1px solid #30363d",
                color: "#e6edf3",
              }}
            />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="humanApprovalRequired"
                label={
                  <span style={{ color: themeStyles.textColor, fontWeight: "500", textShadow: "0 1px 1px rgba(0,0,0,0.5)" }}>
                    Human Approval Required
                  </span>
                }
                valuePropName="checked"
              >
                <Select
                  style={{ width: "100%" }}
                  styles={{
                    popup: {
                      root: {
                        background: "#21262d",
                        border: "1px solid #30363d",
                      },
                    },
                  }}
                >
                  <Option value={true}>Yes</Option>
                  <Option value={false}>No</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="autoTriggered"
                label={<span style={{ color: themeStyles.textColor, fontWeight: "500", textShadow: "0 1px 1px rgba(0,0,0,0.5)" }}>Auto-triggered</span>}
                valuePropName="checked"
              >
                <Select
                  style={{ width: "100%" }}
                  styles={{
                    popup: {
                      root: {
                        background: "#21262d",
                        border: "1px solid #30363d",
                      },
                    },
                  }}
                >
                  <Option value={true}>Yes</Option>
                  <Option value={false}>No</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Form.Item style={{ marginBottom: 0, textAlign: "right" }}>
            <Space>
              <Button
                onClick={() => setShowCreateModal(false)}
                style={{
                  background: "#21262d",
                  border: "1px solid #30363d",
                  color: "#e6edf3",
                }}
              >
                Cancel
              </Button>
              <Button type="primary" htmlType="submit" loading={loading}>
                Create Action
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* Action Details Modal */}
      <Modal
        title={
          <span style={{ color: "#e6edf3", fontWeight: 600 }}>
            Action Point Details
          </span>
        }
        open={showDetailModal}
        onCancel={() => setShowDetailModal(false)}
        footer={null}
        width={700}
        styles={{
          content: {
            background: "#161b22",
            border: "1px solid #30363d",
            padding: 0,
          },
          header: {
            background: "#161b22",
            borderBottom: "1px solid #30363d",
            padding: "16px 20px",
            marginBottom: 0,
          },
          body: { background: "#161b22", padding: "20px" },
          mask: { backdropFilter: "blur(2px)" },
        }}
      >
        {selectedAction && (
          <div>
            <div style={{ marginBottom: "16px" }}>
              <h4 style={{ color: "#e6edf3", marginBottom: "8px" }}>
                {selectedAction.title}
              </h4>
              <p style={{ color: "#8b949e", marginBottom: "8px" }}>
                {selectedAction.description}
              </p>
              <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
                <Tag color={getPriorityColor(selectedAction.priority)}>
                  {selectedAction.priority?.toUpperCase()}
                </Tag>
                <Tag>{selectedAction.type?.toUpperCase()}</Tag>
                <Tag color={getStatusColor(selectedAction.status)}>
                  {selectedAction.status?.replace('_', ' ').toUpperCase()}
                </Tag>
              </div>
              <div style={{ marginTop: "8px", fontSize: "12px", color: "#8b949e" }}>
                <div>Created: {formatDate(selectedAction.createdAt)}</div>
                {selectedAction.dueDate && <div>Due: {formatDate(selectedAction.dueDate)}</div>}
                {selectedAction.assignedTo && <div>Assigned to: {selectedAction.assignedTo}</div>}
              </div>
            </div>

            {selectedAction.aiRecommendation && (
              <Alert
                message="AI Recommendation"
                description={selectedAction.aiRecommendation}
                type="info"
                style={{ marginBottom: "16px" }}
              />
            )}

            <Form form={detailForm} layout="vertical">
              <Form.Item
                name="humanNotes"
                label={
                  <span style={{ color: themeStyles.textColor, fontWeight: "500", textShadow: "0 1px 1px rgba(0,0,0,0.5)" }}>Human Analyst Notes</span>
                }
              >
                <TextArea
                  rows={3}
                  placeholder="Add your notes and observations..."
                  style={{
                    background: "#21262d",
                    border: "1px solid #30363d",
                    color: "#e6edf3",
                  }}
                  onChange={(e) =>
                    updateActionPoint(selectedAction.id, {
                      humanNotes: e.target.value,
                    })
                  }
                />
              </Form.Item>
            </Form>

            <Divider style={{ borderColor: "#30363d" }} />

            <div style={{ textAlign: "right" }}>
              <Space>
                {selectedAction.status === "pending" && (
                  <>
                    <Button
                      style={{
                        background: "#21262d",
                        border: "1px solid #30363d",
                        color: "#e6edf3",
                      }}
                      onClick={() =>
                        handleRejectAction(
                          selectedAction.id,
                          "Rejected by analyst",
                        )
                      }
                    >
                      Reject
                    </Button>
                    <Button
                      style={{
                        background: "#21262d",
                        border: "1px solid #30363d",
                        color: "#e6edf3",
                      }}
                      onClick={() => handleAssignAction(selectedAction.id)}
                    >
                      Assign to Me
                    </Button>
                    <Button
                      type="primary"
                      onClick={() => handleApproveAction(selectedAction.id)}
                    >
                      Approve
                    </Button>
                  </>
                )}
                {selectedAction.status === "in_progress" && (
                  <Button
                    type="primary"
                    onClick={() => handleCompleteAction(selectedAction.id)}
                  >
                    Complete Action
                  </Button>
                )}
              </Space>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
