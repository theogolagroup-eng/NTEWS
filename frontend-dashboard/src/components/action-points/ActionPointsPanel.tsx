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



  const getPriorityColor = (priority: ActionPoint["priority"]) => {

    switch (priority) {

      case "critical":

        return "red";

      case "high":

        return "orange";

      case "medium":

        return "gold";

      case "low":

        return "green";

      default:

        return "default";

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

      await createActionPoint({

        title: values.title,

        description: values.description,

        type: values.type,

        priority: values.priority,

        status: "pending",

        createdBy: "System Analyst", // In real app, get from auth context

        dueDate: values.dueDate?.toISOString(),

        relatedAlertId,

        relatedThreatId,

        relatedHotspotId,

        actions: [],

        humanApprovalRequired: values.humanApprovalRequired,

        autoTriggered: values.autoTriggered,

      });



      message.success("Action point created successfully");

      setShowCreateModal(false);

      form.resetFields();

    } catch (error) {

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



  const handleGenerateAIRecommendation = async (actionId: string) => {

    try {

      await applyAIRecommendation(actionId);

      message.success("AI recommendation generated and applied");

    } catch (error) {

      message.error("Failed to generate AI recommendation");

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

      {/* Header */}

      <div

        style={{

          display: "flex",

          justifyContent: "space-between",

          alignItems: "center",

          marginBottom: "16px",

        }}

      >

        <div>

          <h3

            style={{

              color: themeStyles.textColor,

              margin: 0,

              fontSize: "16px",

              fontWeight: "600",

            }}

          >

            Action Points

          </h3>

          <p

            style={{

              color: themeStyles.secondaryTextColor,

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

              background: themeStyles.cardBackground,

              border: themeStyles.cardBorder,

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

                  <Tag color={getPriorityColor(action.priority)}>

                    {action.priority.toUpperCase()}

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

                    fontSize: "12px",

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

                    fontSize: "11px",

                  }}

                >

                  <span style={{ color: themeStyles.secondaryTextColor }}>

                    Created: {new Date(action.createdAt).toLocaleDateString()}

                  </span>

                  {action.assignedTo && (

                    <span style={{ color: themeStyles.secondaryTextColor }}>

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

        >

          <Form.Item

            name="title"

            label={<span style={{ color: "#8b949e" }}>Action Title</span>}

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

            label={<span style={{ color: "#8b949e" }}>Description</span>}

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

                label={<span style={{ color: "#8b949e" }}>Action Type</span>}

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

                label={<span style={{ color: "#8b949e" }}>Priority</span>}

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

              <span style={{ color: "#8b949e" }}>Due Date (Optional)</span>

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

                  <span style={{ color: "#8b949e" }}>

                    Human Approval Required

                  </span>

                }

                valuePropName="checked"

              >

                <Select

                  defaultValue={false}

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

                label={<span style={{ color: "#8b949e" }}>Auto-triggered</span>}

                valuePropName="checked"

              >

                <Select

                  defaultValue={false}

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

              <div style={{ display: "flex", gap: "8px" }}>

                <Tag color={getPriorityColor(selectedAction.priority)}>

                  {selectedAction.priority.toUpperCase()}

                </Tag>

                <Tag>{selectedAction.type.toUpperCase()}</Tag>

                <Tag color={getPriorityColor(selectedAction.priority)}>

                  {selectedAction.priority.toUpperCase()}

                </Tag>

              </div>

            </div>

            {selectedAction.aiRecommendation && (

              <Alert

                message="AI Recommendation"

                description={selectedAction.aiRecommendation}

                type="info"

              />

            )}

            <Form form={detailForm} layout="vertical">

              <Form.Item

                name="humanNotes"

                label={

                  <span style={{ color: "#8b949e" }}>Human Analyst Notes</span>

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

          </div>

        )}

      </Modal>

    </div>

  );

}

