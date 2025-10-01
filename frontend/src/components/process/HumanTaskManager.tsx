import React, { useState, useEffect, useCallback } from 'react';
import { 
  User, 
  Users, 
  CheckCircle, 
  Clock,
  AlertCircle,
  MessageSquare,
  Paperclip,
  Send,
  Calendar,
  Filter,
  Search,
  ChevronRight,
  Edit3,
  UserPlus,
  Mail
} from 'lucide-react';
import { apiClient } from '@/lib/api-client';
import { logger } from '@/lib/logger';

interface HumanTaskManagerProps {
  processInstanceId?: string;
  userId?: string;
}

interface HumanTask {
  id: string;
  name: string;
  description: string;
  processInstanceId: string;
  processName: string;
  assignee?: string;
  candidateUsers: string[];
  candidateGroups: string[];
  priority: 'low' | 'medium' | 'high' | 'critical';
  dueDate?: string;
  status: 'pending' | 'claimed' | 'completed' | 'cancelled';
  createdTime: string;
  claimedTime?: string;
  completedTime?: string;
  formData?: Record<string, any>;
  attachments: Attachment[];
  comments: Comment[];
}

interface Attachment {
  id: string;
  name: string;
  size: number;
  uploadedBy: string;
  uploadedAt: string;
}

interface Comment {
  id: string;
  text: string;
  author: string;
  timestamp: string;
}

interface TaskFilter {
  status?: string;
  priority?: string;
  assignee?: string;
  search?: string;
}

export const HumanTaskManager: React.FC<HumanTaskManagerProps> = ({
  processInstanceId,
  userId = 'current-user'
}) => {
  const [tasks, setTasks] = useState<HumanTask[]>([]);
  const [selectedTask, setSelectedTask] = useState<HumanTask | null>(null);
  const [filter, setFilter] = useState<TaskFilter>({});
  const [isLoading, setIsLoading] = useState(true);
  const [formValues, setFormValues] = useState<Record<string, any>>({});
  const [newComment, setNewComment] = useState('');
  const [showAssignDialog, setShowAssignDialog] = useState(false);
  const [assigneeEmail, setAssigneeEmail] = useState('');

  const loadTasks = useCallback(async () => {
    setIsLoading(true);
    try {
      // In a real implementation, this would fetch from the process engine
      const mockTasks: HumanTask[] = [
        {
          id: '1',
          name: 'Approve Order',
          description: 'Please review and approve the order #12345 for customer ABC Corp.',
          processInstanceId: processInstanceId || 'proc-1',
          processName: 'Order Processing',
          assignee: userId,
          candidateUsers: ['manager1', 'manager2'],
          candidateGroups: ['managers'],
          priority: 'high',
          dueDate: new Date(Date.now() + 86400000).toISOString(),
          status: 'claimed',
          createdTime: new Date(Date.now() - 3600000).toISOString(),
          claimedTime: new Date(Date.now() - 1800000).toISOString(),
          formData: {
            orderId: '12345',
            customerName: 'ABC Corp',
            orderAmount: 15000,
            items: 5
          },
          attachments: [
            {
              id: 'att1',
              name: 'order-details.pdf',
              size: 245000,
              uploadedBy: 'system',
              uploadedAt: new Date(Date.now() - 3000000).toISOString()
            }
          ],
          comments: [
            {
              id: 'comm1',
              text: 'This order requires manager approval due to high value.',
              author: 'system',
              timestamp: new Date(Date.now() - 3000000).toISOString()
            }
          ]
        },
        {
          id: '2',
          name: 'Review Document',
          description: 'Review and provide feedback on the technical specification document.',
          processInstanceId: 'proc-2',
          processName: 'Document Review',
          candidateUsers: [userId, 'reviewer1', 'reviewer2'],
          candidateGroups: ['reviewers'],
          priority: 'medium',
          dueDate: new Date(Date.now() + 172800000).toISOString(),
          status: 'pending',
          createdTime: new Date(Date.now() - 7200000).toISOString(),
          formData: {
            documentId: 'DOC-789',
            documentType: 'Technical Specification',
            version: '2.1'
          },
          attachments: [],
          comments: []
        },
        {
          id: '3',
          name: 'Customer Verification',
          description: 'Verify customer identity and documentation for account activation.',
          processInstanceId: 'proc-3',
          processName: 'Customer Onboarding',
          assignee: 'agent1',
          candidateUsers: [],
          candidateGroups: ['support'],
          priority: 'critical',
          dueDate: new Date(Date.now() + 14400000).toISOString(),
          status: 'claimed',
          createdTime: new Date(Date.now() - 1800000).toISOString(),
          claimedTime: new Date(Date.now() - 900000).toISOString(),
          formData: {
            customerId: 'CUST-456',
            verificationType: 'identity',
            documents: ['passport', 'utility_bill']
          },
          attachments: [],
          comments: []
        }
      ];

      // Apply filters
      let filteredTasks = mockTasks;
      
      if (processInstanceId) {
        filteredTasks = filteredTasks.filter(t => t.processInstanceId === processInstanceId);
      }
      
      if (filter.status) {
        filteredTasks = filteredTasks.filter(t => t.status === filter.status);
      }
      
      if (filter.priority) {
        filteredTasks = filteredTasks.filter(t => t.priority === filter.priority);
      }
      
      if (filter.assignee) {
        filteredTasks = filteredTasks.filter(t => t.assignee === filter.assignee);
      }
      
      if (filter.search) {
        const search = filter.search.toLowerCase();
        filteredTasks = filteredTasks.filter(t => 
          t.name.toLowerCase().includes(search) ||
          t.description.toLowerCase().includes(search) ||
          t.processName.toLowerCase().includes(search)
        );
      }

      setTasks(filteredTasks);
      
      if (filteredTasks.length > 0 && !selectedTask) {
        setSelectedTask(filteredTasks[0]);
        setFormValues(filteredTasks[0].formData || {});
      }
    } catch (error) {
      logger.error('Failed to load tasks:', error);
    } finally {
      setIsLoading(false);
    }
  }, [processInstanceId, filter, selectedTask, userId]);

  useEffect(() => {
    loadTasks();
  }, [loadTasks]);

  const claimTask = async (taskId: string) => {
    try {
      const response = await apiClient.post(`/api/process-engine/task/${taskId}/claim`, {
        userId
      });
      
      if (response.data.success) {
        logger.info('Task claimed successfully');
        loadTasks();
      }
    } catch (error) {
      logger.error('Failed to claim task:', error);
    }
  };

  const completeTask = async (taskId: string) => {
    try {
      const response = await apiClient.post(`/api/process-engine/task/${taskId}/complete`, {
        variables: formValues
      });
      
      if (response.data.success) {
        logger.info('Task completed successfully');
        setSelectedTask(null);
        loadTasks();
      }
    } catch (error) {
      logger.error('Failed to complete task:', error);
    }
  };

  const assignTask = async (taskId: string, assignee: string) => {
    try {
      // In real implementation, this would call the process engine API
      logger.info(`Task ${taskId} assigned to ${assignee}`);
      setShowAssignDialog(false);
      setAssigneeEmail('');
      loadTasks();
    } catch (error) {
      logger.error('Failed to assign task:', error);
    }
  };

  const addComment = async () => {
    if (!selectedTask || !newComment.trim()) return;
    
    try {
      // In real implementation, this would call the API
      const comment: Comment = {
        id: `comm-${Date.now()}`,
        text: newComment,
        author: userId,
        timestamp: new Date().toISOString()
      };
      
      selectedTask.comments.push(comment);
      setNewComment('');
      logger.info('Comment added successfully');
    } catch (error) {
      logger.error('Failed to add comment:', error);
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'critical': return 'text-red-600 bg-red-50';
      case 'high': return 'text-orange-600 bg-orange-50';
      case 'medium': return 'text-yellow-600 bg-yellow-50';
      case 'low': return 'text-green-600 bg-green-50';
      default: return 'text-gray-600 bg-gray-50';
    }
  };

  const getTimeRemaining = (dueDate?: string) => {
    if (!dueDate) return null;
    
    const now = Date.now();
    const due = new Date(dueDate).getTime();
    const diff = due - now;
    
    if (diff < 0) return 'Overdue';
    if (diff < 3600000) return `${Math.floor(diff / 60000)}m`;
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}h`;
    return `${Math.floor(diff / 86400000)}d`;
  };

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm h-full">
      {/* Header */}
      <div className="p-4 border-b border-gray-200 dark:border-gray-700">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Users className="w-5 h-5 text-blue-500" />
            <h3 className="font-semibold">Human Tasks</h3>
          </div>
          
          {/* Filters */}
          <div className="flex items-center gap-3">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search tasks..."
                value={filter.search || ''}
                onChange={(e) => setFilter({ ...filter, search: e.target.value })}
                className="pl-9 pr-3 py-1.5 border border-gray-300 dark:border-gray-600 rounded-lg text-sm"
              />
            </div>
            
            <select
              value={filter.status || ''}
              onChange={(e) => setFilter({ ...filter, status: e.target.value })}
              className="px-3 py-1.5 border border-gray-300 dark:border-gray-600 rounded-lg text-sm"
            >
              <option value="">All Status</option>
              <option value="pending">Pending</option>
              <option value="claimed">Claimed</option>
              <option value="completed">Completed</option>
            </select>
            
            <select
              value={filter.priority || ''}
              onChange={(e) => setFilter({ ...filter, priority: e.target.value })}
              className="px-3 py-1.5 border border-gray-300 dark:border-gray-600 rounded-lg text-sm"
            >
              <option value="">All Priority</option>
              <option value="critical">Critical</option>
              <option value="high">High</option>
              <option value="medium">Medium</option>
              <option value="low">Low</option>
            </select>
          </div>
        </div>
      </div>

      <div className="flex h-[calc(100%-65px)]">
        {/* Task List */}
        <div className="w-1/3 border-r border-gray-200 dark:border-gray-700 overflow-y-auto">
          {isLoading ? (
            <div className="flex items-center justify-center h-full">
              <p className="text-gray-500">Loading tasks...</p>
            </div>
          ) : tasks.length === 0 ? (
            <div className="flex items-center justify-center h-full">
              <p className="text-gray-500">No tasks found</p>
            </div>
          ) : (
            <div className="p-2 space-y-2">
              {tasks.map(task => (
                <div
                  key={task.id}
                  onClick={() => {
                    setSelectedTask(task);
                    setFormValues(task.formData || {});
                  }}
                  className={`
                    p-3 rounded-lg cursor-pointer transition-all
                    ${selectedTask?.id === task.id 
                      ? 'bg-blue-50 dark:bg-blue-900/20 border border-blue-300 dark:border-blue-700' 
                      : 'hover:bg-gray-50 dark:hover:bg-gray-700 border border-transparent'
                    }
                  `}
                >
                  <div className="flex items-start justify-between mb-2">
                    <h4 className="font-medium text-sm">{task.name}</h4>
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${getPriorityColor(task.priority)}`}>
                      {task.priority}
                    </span>
                  </div>
                  
                  <p className="text-xs text-gray-600 dark:text-gray-400 mb-2 line-clamp-2">
                    {task.description}
                  </p>
                  
                  <div className="flex items-center justify-between text-xs">
                    <div className="flex items-center gap-2">
                      {task.status === 'claimed' && task.assignee ? (
                        <>
                          <User className="w-3 h-3" />
                          <span>{task.assignee}</span>
                        </>
                      ) : (
                        <>
                          <Users className="w-3 h-3" />
                          <span>Unassigned</span>
                        </>
                      )}
                    </div>
                    
                    {task.dueDate && (
                      <div className="flex items-center gap-1 text-gray-500">
                        <Clock className="w-3 h-3" />
                        <span>{getTimeRemaining(task.dueDate)}</span>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Task Details */}
        {selectedTask ? (
          <div className="flex-1 flex flex-col">
            {/* Task Header */}
            <div className="p-4 border-b border-gray-200 dark:border-gray-700">
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="text-lg font-semibold">{selectedTask.name}</h3>
                  <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                    {selectedTask.processName} • {selectedTask.processInstanceId}
                  </p>
                </div>
                
                <div className="flex items-center gap-2">
                  {selectedTask.status === 'pending' && (
                    <button
                      onClick={() => claimTask(selectedTask.id)}
                      className="px-3 py-1.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm"
                    >
                      Claim Task
                    </button>
                  )}
                  
                  {selectedTask.status === 'claimed' && selectedTask.assignee === userId && (
                    <button
                      onClick={() => completeTask(selectedTask.id)}
                      className="px-3 py-1.5 bg-green-600 text-white rounded-lg hover:bg-green-700 text-sm"
                    >
                      Complete Task
                    </button>
                  )}
                  
                  <button
                    onClick={() => setShowAssignDialog(true)}
                    className="p-1.5 text-gray-400 hover:text-gray-600"
                    title="Assign"
                  >
                    <UserPlus className="w-4 h-4" />
                  </button>
                </div>
              </div>
              
              <div className="flex items-center gap-4 mt-3 text-sm">
                <span className={`px-2 py-1 rounded-full font-medium ${getPriorityColor(selectedTask.priority)}`}>
                  {selectedTask.priority}
                </span>
                
                {selectedTask.dueDate && (
                  <div className="flex items-center gap-1 text-gray-600 dark:text-gray-400">
                    <Calendar className="w-4 h-4" />
                    <span>Due {new Date(selectedTask.dueDate).toLocaleDateString()}</span>
                  </div>
                )}
                
                <div className="flex items-center gap-1 text-gray-600 dark:text-gray-400">
                  {selectedTask.status === 'claimed' ? (
                    <>
                      <User className="w-4 h-4" />
                      <span>{selectedTask.assignee}</span>
                    </>
                  ) : (
                    <>
                      <Users className="w-4 h-4" />
                      <span>Unassigned</span>
                    </>
                  )}
                </div>
              </div>
            </div>

            {/* Task Content */}
            <div className="flex-1 overflow-y-auto p-4">
              {/* Description */}
              <div className="mb-6">
                <h4 className="font-medium mb-2">Description</h4>
                <p className="text-gray-700 dark:text-gray-300">
                  {selectedTask.description}
                </p>
              </div>

              {/* Form Data */}
              {Object.keys(selectedTask.formData || {}).length > 0 && (
                <div className="mb-6">
                  <h4 className="font-medium mb-3">Task Data</h4>
                  <div className="space-y-3">
                    {Object.entries(selectedTask.formData || {}).map(([key, value]) => (
                      <div key={key}>
                        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                          {key.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())}
                        </label>
                        <input
                          type="text"
                          value={formValues[key] || ''}
                          onChange={(e) => setFormValues({ ...formValues, [key]: e.target.value })}
                          disabled={selectedTask.status !== 'claimed' || selectedTask.assignee !== userId}
                          className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg disabled:bg-gray-100 dark:disabled:bg-gray-700"
                        />
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Attachments */}
              {selectedTask.attachments.length > 0 && (
                <div className="mb-6">
                  <h4 className="font-medium mb-3 flex items-center gap-2">
                    <Paperclip className="w-4 h-4" />
                    Attachments
                  </h4>
                  <div className="space-y-2">
                    {selectedTask.attachments.map(attachment => (
                      <div
                        key={attachment.id}
                        className="flex items-center justify-between p-2 bg-gray-50 dark:bg-gray-700 rounded"
                      >
                        <div>
                          <p className="text-sm font-medium">{attachment.name}</p>
                          <p className="text-xs text-gray-500">
                            {(attachment.size / 1024).toFixed(1)} KB • {attachment.uploadedBy}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Comments */}
              <div>
                <h4 className="font-medium mb-3 flex items-center gap-2">
                  <MessageSquare className="w-4 h-4" />
                  Comments
                </h4>
                <div className="space-y-3 mb-4">
                  {selectedTask.comments.map(comment => (
                    <div key={comment.id} className="bg-gray-50 dark:bg-gray-700 rounded p-3">
                      <div className="flex items-center justify-between mb-1">
                        <span className="text-sm font-medium">{comment.author}</span>
                        <span className="text-xs text-gray-500">
                          {new Date(comment.timestamp).toLocaleString()}
                        </span>
                      </div>
                      <p className="text-sm text-gray-700 dark:text-gray-300">
                        {comment.text}
                      </p>
                    </div>
                  ))}
                </div>
                
                {/* Add Comment */}
                <div className="flex gap-2">
                  <input
                    type="text"
                    placeholder="Add a comment..."
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && addComment()}
                    className="flex-1 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg"
                  />
                  <button
                    onClick={addComment}
                    className="p-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                  >
                    <Send className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          </div>
        ) : (
          <div className="flex-1 flex items-center justify-center text-gray-500">
            <p>Select a task to view details</p>
          </div>
        )}
      </div>

      {/* Assign Dialog */}
      {showAssignDialog && selectedTask && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white dark:bg-gray-800 rounded-lg p-6 w-96">
            <h3 className="text-lg font-semibold mb-4">Assign Task</h3>
            <div className="mb-4">
              <label className="block text-sm font-medium mb-2">
                Assignee Email
              </label>
              <div className="flex gap-2">
                <Mail className="w-4 h-4 text-gray-400 mt-2" />
                <input
                  type="email"
                  value={assigneeEmail}
                  onChange={(e) => setAssigneeEmail(e.target.value)}
                  placeholder="user@example.com"
                  className="flex-1 px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg"
                />
              </div>
            </div>
            <div className="flex gap-2 justify-end">
              <button
                onClick={() => setShowAssignDialog(false)}
                className="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg"
              >
                Cancel
              </button>
              <button
                onClick={() => assignTask(selectedTask.id, assigneeEmail)}
                disabled={!assigneeEmail}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                Assign
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};