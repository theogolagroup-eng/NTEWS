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

  const [expandedAIRecommendations, setExpandedAIRecommendations] = useState<Set<string>>(new Set());



  // Filter action points related to current context

  const relatedActionPoints = actionPoints.filter((ap) => {

    // If no related IDs are provided, show all action points

    if (!relatedAlertId && !relatedThreatId && !relatedHotspotId) {

      return true;

    }

    if (relatedAlertId && ap.relatedAlertId === relatedAlertId) return true;

    if (relatedThreatId && ap.relatedThreatId === relatedThreatId) return true;

    if (relatedHotspotId && ap.relatedHotspotId === relatedHotspotId)

      return true;

    return false;

  });





  const getStatusIcon = (status: ActionPoint["status"]) => {

    switch (status) {

      case "completed":

        return <CheckCircleOutlined style={{ color: themeStyles.successColor }} />;

      case "in_progress":

        return <ClockCircleOutlined style={{ color: themeStyles.infoColor }} />;

      case "pending":

        return <ExclamationCircleOutlined style={{ color: themeStyles.warningColor }} />;

      case "cancelled":

        return <DeleteOutlined style={{ color: themeStyles.errorColor }} />;

      default:

        return <ClockCircleOutlined style={{ color: themeStyles.mutedTextColor }} />;

    }

  };



  const getPriorityColor = (priority: ActionPoint["priority"]) => {

    switch (priority) {

      case "critical":

        return themeStyles.kenyanRed;

      case "high":

        return themeStyles.kenyanGreen;

      case "medium":

        return themeStyles.kenyanBlack;

      case "low":

        return themeStyles.kenyanWhite;

      default:

        return themeStyles.mutedTextColor;

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

      const actionData: any = {

        title: values.title,

        description: values.description,

        type: values.type, // This should be the string value, backend will handle enum conversion

        priority: values.priority, // This should be the string value, backend will handle enum conversion

        status: "pending",

        createdBy: "System Analyst",

        dueDate: values.dueDate?.toISOString(),

        humanApprovalRequired: values.humanApprovalRequired,

        autoTriggered: values.autoTriggered,

        actions: [], // Initialize with empty actions array

        escalationLevel: 0,

        completionPercentage: 0.0

      };

      // Only add related IDs if they exist and are not empty

      if (relatedAlertId) actionData.relatedAlertId = relatedAlertId;

      if (relatedThreatId) actionData.relatedThreatId = relatedThreatId;

      if (relatedHotspotId) actionData.relatedHotspotId = relatedHotspotId;

      console.log('Creating action point with data:', actionData);

      await createActionPoint(actionData);

      message.success("Action point created successfully");

      setShowCreateModal(false);

      form.resetFields();

    } catch (error) {

      console.error('Create action point error:', error);

      message.error("Failed to create action point");

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



  const handleDeleteAction = async (actionId: string) => {
    try {
      // Use the improved delete function from context
      await deleteActionPoint(actionId);
    } catch (error) {
      message.error("Failed to delete action point");
    }
  };

  const [deletingActionId, setDeletingActionId] = useState<string | null>(null);

  const handleDeleteActionWithLock = async (actionId: string) => {
    if (deletingActionId === actionId) {
      return; // Prevent multiple clicks
    }
    
    setDeletingActionId(actionId);
    try {
      await handleDeleteAction(actionId);
    } finally {
      setDeletingActionId(null);
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



  const toggleAIRecommendation = (actionId: string) => {

    setExpandedAIRecommendations(prev => {

      const newSet = new Set(prev);

      if (newSet.has(actionId)) {

        newSet.delete(actionId);

      } else {

        newSet.add(actionId);

      }

      return newSet;

    });

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
    <div style={{ 
      padding: "20px",
      background: "linear-gradient(135deg, " + themeStyles.kenyanBlack + " 25%, " + themeStyles.kenyanRed + " 25%, " + themeStyles.kenyanRed + " 50%, " + themeStyles.kenyanGreen + " 50%, " + themeStyles.kenyanBlack + " 75%)"
    }}>
      {/* Header with Kenyan flag theme */}
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: "16px",
          padding: "16px",
          background: themeStyles.kenyanRed,
          border: `2px solid ${themeStyles.kenyanBlack}`,
          borderRadius: "8px"
        }}
      >
        <div>
          <h3
            style={{
              color: themeStyles.kenyanWhite,
              margin: 0,
              fontSize: "16px",
              fontWeight: "bold",
            }}
          >
            ACTION POINTS
          </h3>
          <p
            style={{
              color: themeStyles.kenyanWhite,
              margin: 0,
              fontSize: "12px",
            }}
          >
            {relatedActionPoints.length} active actions
          </p>
        </div>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => setShowCreateModal(true)}
          style={{
            background: themeStyles.kenyanGreen,
            border: `2px solid ${themeStyles.kenyanWhite}`,
            color: themeStyles.kenyanBlack,
            fontWeight: "bold"
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

            style={{

              background: themeStyles.kenyanBlack,

              border: `2px solid ${themeStyles.kenyanRed}`,

              marginBottom: "8px",

            }}

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

                    }}

                  >

                    {action.title}

                  </span>

                  <Tag color={getPriorityColor(action.priority)} style={{ border: `1px solid ${themeStyles.kenyanWhite}`, color: themeStyles.kenyanWhite }}>

                    {action.priority.toUpperCase()}

                  </Tag>

                  <Tooltip title="View More Details">

                    <EyeOutlined 
                      style={{ color: themeStyles.kenyanWhite, fontSize: "16px", cursor: "pointer", marginLeft: "8px" }}
                      onClick={() => showActionDetails(action)}
                    />

                  </Tooltip>

                </div>



                <p
                  style={{
                    color: themeStyles.kenyanWhite,
                    margin: "4px 0",
                    fontSize: "13px",
                    fontWeight: "500"
                  }}
                >
                  {action.description}
                </p>

                {action.aiRecommendation && expandedAIRecommendations.has(action.id) && (
                  <Alert
                    message={<span style={{ color: themeStyles.kenyanBlack, fontWeight: "bold" }}>AI Recommendation</span>}
                    description={
                      <span style={{ color: themeStyles.kenyanBlack }}>
                        {action.aiRecommendation}
                      </span>
                    }
                    style={{ 
                      backgroundColor: themeStyles.kenyanRed,
                      border: `2px solid ${themeStyles.kenyanGreen}`,
                      marginBottom: "8px",
                      borderRadius: '6px'
                    }}
                    onClick={() => toggleAIRecommendation(action.id)}
                  />
                )}

                {action.actions && action.actions.length > 0 && (
                  <div style={{ marginBottom: "8px" }}>
                    <Progress
                      percent={completionPercentage(action) as number}
                      size="small"
                      strokeColor={themeStyles.successColor}
                      format={() => `${completionPercentage(action)}%`}
                    />
                  </div>
                )}

                <div

                  style={{

                    display: "flex",

                    alignItems: "center",

                    gap: "8px",

                    fontSize: "11px",

                  }}

                >

                  <span style={{ color: themeStyles.kenyanWhite, fontWeight: "bold" }}>

                    Created: {new Date(action.createdAt).toLocaleDateString()}

                  </span>

                  {action.assignedTo && (

                    <span style={{ color: themeStyles.kenyanWhite, fontWeight: "bold" }}>

                      • Assigned to: {action.assignedTo}

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

                <Tooltip title="Delete Action Point">

                  <Button

                    type="text"

                    size="small"

                    icon={<DeleteOutlined style={{ color: themeStyles.kenyanRed, fontSize: "14px" }} />}

                    onClick={() => handleDeleteActionWithLock(action.id)}

                    disabled={deletingActionId === action.id}

                    loading={deletingActionId === action.id}

                  />

                </Tooltip>

                {action.aiRecommendation && (
                  <Tooltip title={expandedAIRecommendations.has(action.id) ? "Hide AI Recommendation" : "Show AI Recommendation"}>
                    <Button
                      type="text"
                      size="small"
                      icon={<RobotOutlined style={{ color: themeStyles.kenyanRed, fontSize: "16px" }} />}
                      onClick={() => toggleAIRecommendation(action.id)}
                      style={{ 
                        border: `2px solid ${themeStyles.kenyanRed}`, 
                        color: themeStyles.kenyanRed,
                        backgroundColor: 'transparent',
                        fontWeight: 'bold'
                      }}
                    />
                  </Tooltip>
                )}



                {action.status === "pending" && (

                  <>

                    <Tooltip title="Assign to Me">

                      <Button

                        type="text"

                        size="small"

                        icon={<UserOutlined style={{ color: themeStyles.kenyanWhite, fontSize: "14px" }} />}

                        onClick={() => handleAssignAction(action.id)}

                        style={{ border: `1px solid ${themeStyles.kenyanWhite}`, color: themeStyles.kenyanWhite }}

                      />

                    </Tooltip>

                    <Tooltip title="Approve">

                      <Button

                        type="text"

                        size="small"

                        icon={<CheckCircleOutlined style={{ color: themeStyles.kenyanGreen, fontSize: "14px" }} />}

                        onClick={() => handleApproveAction(action.id)}

                        style={{ border: `1px solid ${themeStyles.kenyanGreen}`, color: themeStyles.kenyanGreen }}

                      />

                    </Tooltip>

                  </>

                )}



                {action.status === "in_progress" && (

                  <Tooltip title="Complete">

                    <Button

                      type="text"

                      size="small"

                      icon={<CheckCircleOutlined style={{ color: themeStyles.kenyanGreen, fontSize: "14px" }} />}

                      onClick={() => handleCompleteAction(action.id)}

                      style={{ border: `1px solid ${themeStyles.kenyanGreen}`, color: themeStyles.kenyanGreen }}

                    />

                  </Tooltip>

                )}



                {!action.aiRecommendation && (

                  <Tooltip title="Generate AI Recommendation">

                    <Button

                      type="text"

                      size="small"

                      icon={<RobotOutlined style={{ color: themeStyles.kenyanRed, fontSize: "14px" }} />}

                      onClick={() => handleGenerateAIRecommendation(action.id)}

                      style={{ border: `1px solid ${themeStyles.kenyanRed}`, color: themeStyles.kenyanRed }}

                    />

                  </Tooltip>

                )}

              </div>

            </div>

          </Card>

        )}

        locale={{ emptyText: "No action points found" }}

      />



      {/* Create Action Modal */}

      <Modal

        title={

          <span style={{ color: themeStyles.kenyanWhite, fontWeight: 600 }}>

            Create Action Point

          </span>

        }

        open={showCreateModal}

        onCancel={() => setShowCreateModal(false)}

        footer={null}

        width={600}

        styles={{

          content: {

            background: themeStyles.cardBackground,

            border: themeStyles.cardBorder,

            padding: 0,

          },

          header: {

            background: themeStyles.cardBackground,

            borderBottom: themeStyles.cardBorder,

            padding: "16px 20px",

            marginBottom: 0,

          },

          body: { background: themeStyles.cardBackground, padding: "20px" },

          mask: { backdropFilter: "blur(2px)" },

        }}

      >

        <Form

          form={form}

          layout="vertical"

          onFinish={handleCreateAction}

          initialValues={{

            humanApprovalRequired: false,

            autoTriggered: false

          }}

          style={{ color: themeStyles.textColor }}

        >

          <Form.Item

            name="title"

            label={<span style={{ color: themeStyles.secondaryTextColor }}>Action Title</span>}

            rules={[{ required: true, message: "Please enter action title" }]}

          >

            <Input

              placeholder="Enter action title"

              style={{

                background: themeStyles.glassBackground,

                border: themeStyles.cardBorder,

                color: themeStyles.textColor,

              }}

            />

          </Form.Item>



          <Form.Item

            name="description"

            label={<span style={{ color: themeStyles.secondaryTextColor }}>Description</span>}

            rules={[{ required: true, message: "Please enter description" }]}

          >

            <TextArea

              rows={3}

              placeholder="Describe the action to be taken"

              style={{

                background: themeStyles.glassBackground,

                border: themeStyles.cardBorder,

                color: themeStyles.textColor,

              }}

            />

          </Form.Item>



          <Row gutter={16}>

            <Col span={12}>

              <Form.Item

                name="type"

                label={<span style={{ color: themeStyles.secondaryTextColor }}>Action Type</span>}

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

                label={<span style={{ color: themeStyles.secondaryTextColor }}>Priority</span>}

                rules={[{ required: true, message: "Please select priority" }]}

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

              <span style={{ color: themeStyles.secondaryTextColor }}>

                Due Date (Optional)

              </span>

            }

          >

            <DatePicker

              style={{

                width: "100%",

                background: themeStyles.glassBackground,

                border: themeStyles.cardBorder,

                color: themeStyles.textColor,

              }}

            />

          </Form.Item>



          <Row gutter={16}>

            <Col span={12}>

              <Form.Item

                name="humanApprovalRequired"

                label={

                  <span style={{ color: themeStyles.secondaryTextColor }}>

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

                        background: themeStyles.glassBackground,

                        border: themeStyles.cardBorder,

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

                label={<span style={{ color: themeStyles.secondaryTextColor }}>Auto-triggered</span>}

                valuePropName="checked"

              >

                <Select

                  style={{ width: "100%" }}

                  styles={{

                    popup: {

                      root: {

                        background: themeStyles.glassBackground,

                        border: themeStyles.cardBorder,

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

                  background: themeStyles.glassBackground,

                  border: themeStyles.cardBorder,

                  color: themeStyles.textColor,

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
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <EyeOutlined style={{ color: themeStyles.kenyanWhite, fontSize: '18px' }} />
            <span style={{ color: themeStyles.kenyanWhite, fontWeight: 600 }}>
              Action Point Details
            </span>
          </div>
        }

        open={showDetailModal}

        onCancel={() => setShowDetailModal(false)}

        footer={null}

        width={700}

        styles={{
          content: {
            background: themeStyles.kenyanBlack,
            border: `2px solid ${themeStyles.kenyanRed}`,
            padding: 0,
          },
          header: {
            background: themeStyles.kenyanRed,
            borderBottom: `2px solid ${themeStyles.kenyanBlack}`,
            padding: "16px 20px",
            marginBottom: 0,
          },
          body: { background: themeStyles.kenyanBlack, padding: "20px" },
          mask: { backdropFilter: "blur(2px)" },
        }}

      >

        {selectedAction && (

          <div>

            <div style={{ marginBottom: "16px" }}>

              <h4 style={{ color: themeStyles.kenyanWhite, marginBottom: "8px" }}>

                {selectedAction?.title}

              </h4>

              <p style={{ color: themeStyles.kenyanWhite, marginBottom: "8px" }}>

                {selectedAction?.description}

              </p>

              <div style={{ display: "flex", gap: "8px" }}>

                <Tag color={selectedAction?.priority ? getPriorityColor(selectedAction?.priority) : themeStyles.kenyanRed} style={{ border: `1px solid ${themeStyles.kenyanWhite}`, color: themeStyles.kenyanWhite }}>
                  {selectedAction?.priority?.toUpperCase()}
                </Tag>
                <Tag color={themeStyles.kenyanGreen} style={{ border: `1px solid ${themeStyles.kenyanWhite}`, color: themeStyles.kenyanBlack }}>
                  {selectedAction?.type?.toUpperCase()}
                </Tag>

              </div>

            </div>

            {selectedAction?.aiRecommendation && (
              <Alert
                message="AI Recommendation"
                description={selectedAction?.aiRecommendation}
                style={{ 
                  backgroundColor: themeStyles.kenyanRed,
                  border: `2px solid ${themeStyles.kenyanGreen}`,
                  borderRadius: '6px'
                }}
              />
            )}

            <Form form={detailForm} layout="vertical">
              <Form.Item
                name="humanNotes"
                label={
                  <span style={{ color: themeStyles.kenyanWhite, fontWeight: "bold" }}>

                    Human Analyst Notes

                  </span>

                }

              >

                <TextArea

                  rows={3}

                  placeholder="Add your notes and observations..."

                  style={{
                    background: themeStyles.kenyanBlack,
                    border: `1px solid ${themeStyles.kenyanRed}`,
                    color: themeStyles.kenyanWhite,
                  }}

                  onChange={(e) =>
                    selectedAction?.id && updateActionPoint(selectedAction.id, {
                      humanNotes: e.target.value,
                    })
                  }

                />

              </Form.Item>

            </Form>

            <Divider style={{ borderColor: themeStyles.kenyanRed }} />

          </div>

        )}

      </Modal>

    </div>

  );

}

