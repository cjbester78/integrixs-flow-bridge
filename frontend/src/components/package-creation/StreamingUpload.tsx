import React, { useState, useEffect, useRef } from 'react';
import {
  Upload,
  Progress,
  Typography,
  Alert,
  Button,
  Space,
  Card,
  Descriptions,
  Tag
} from 'antd';
import {
  CloudUploadOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined
} from '@ant-design/icons';
import type { UploadProps } from 'antd';
import useWebSocket from 'react-use-websocket';

const { Title, Text } = Typography;
const { Dragger } = Upload;

interface StreamingUploadProps {
  onStructureExtracted?: (structure: any) => void;
  maxFileSize?: number;
  acceptedFormats?: string[];
}

interface UploadSession {
  sessionId: string;
  fileName: string;
  fileSize: number;
  status: 'processing' | 'completed' | 'failed' | 'cancelled';
  elementsProcessed: number;
  durationMs?: number;
  structure?: any;
  errors?: string[];
}

interface ProgressUpdate {
  sessionId: string;
  fileName: string;
  bytesRead: number;
  totalBytes: number;
  percentComplete: number;
  elementsProcessed: number;
  status: string;
}

const StreamingUpload: React.FC<StreamingUploadProps> = ({
  onStructureExtracted,
  maxFileSize = 500 * 1024 * 1024, // 500MB default
  acceptedFormats = ['.xml', '.json']
}) => {
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [currentSession, setCurrentSession] = useState<UploadSession | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [structure, setStructure] = useState<any>(null);
  
  const sessionIdRef = useRef<string | null>(null);
  const abortControllerRef = useRef<AbortController | null>(null);

  // WebSocket connection for progress updates
  const { sendMessage, lastJsonMessage, readyState } = useWebSocket(
    process.env.REACT_APP_WS_URL ? 
      `${process.env.REACT_APP_WS_URL}/ws/streaming-progress` : 
      `ws://localhost:8080/ws/streaming-progress`,
    {
      shouldReconnect: () => true,
      reconnectInterval: 3000,
      onOpen: () => {
        console.log('Streaming progress WebSocket connected');
        // Subscribe to session if we have one
        if (sessionIdRef.current) {
          sendMessage(JSON.stringify({
            action: 'subscribe',
            streamingId: sessionIdRef.current
          }));
        }
      }
    }
  );

  // Handle WebSocket messages
  useEffect(() => {
    if (lastJsonMessage) {
      const update = lastJsonMessage as ProgressUpdate;
      if (update.sessionId === sessionIdRef.current) {
        setUploadProgress(update.percentComplete);
        
        if (currentSession) {
          setCurrentSession({
            ...currentSession,
            elementsProcessed: update.elementsProcessed,
            status: update.status as any
          });
        }
      }
    }
  }, [lastJsonMessage, currentSession]);

  const uploadProps: UploadProps = {
    name: 'file',
    multiple: false,
    accept: acceptedFormats.join(','),
    showUploadList: false,
    beforeUpload: (file) => {
      // Validate file size
      if (file.size > maxFileSize) {
        setError(`File too large. Maximum size is ${maxFileSize / (1024 * 1024)}MB`);
        return false;
      }
      
      // Clear previous state
      setError(null);
      setStructure(null);
      setUploadProgress(0);
      
      // Handle upload
      handleUpload(file);
      
      return false; // Prevent default upload
    }
  };

  const handleUpload = async (file: File) => {
    setUploading(true);
    
    const sessionId = generateSessionId();
    sessionIdRef.current = sessionId;
    
    // Subscribe to progress updates
    if (readyState === WebSocket.OPEN) {
      sendMessage(JSON.stringify({
        action: 'subscribe',
        streamingId: sessionId
      }));
    }
    
    const formData = new FormData();
    formData.append('file', file);
    formData.append('sessionId', sessionId);
    
    // Determine endpoint based on file type
    const isJson = file.name.toLowerCase().endsWith('.json');
    const endpoint = isJson ? '/api/streaming/upload/json' : '/api/streaming/upload/xml';
    
    // Create abort controller
    abortControllerRef.current = new AbortController();
    
    try {
      const response = await fetch(endpoint, {
        method: 'POST',
        body: formData,
        signal: abortControllerRef.current.signal,
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        }
      });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Upload failed');
      }
      
      const result = await response.json();
      
      setCurrentSession({
        sessionId: result.sessionId,
        fileName: result.fileName,
        fileSize: result.fileSize,
        status: 'completed',
        elementsProcessed: result.elementsProcessed,
        durationMs: result.processingTimeMs,
        structure: result.structure
      });
      
      setStructure(result.structure);
      setUploadProgress(100);
      
      // Notify parent component
      if (onStructureExtracted && result.structure) {
        onStructureExtracted(result.structure);
      }
      
    } catch (err: any) {
      if (err.name === 'AbortError') {
        setError('Upload cancelled');
      } else {
        setError(err.message || 'Upload failed');
      }
      
      if (currentSession) {
        setCurrentSession({
          ...currentSession,
          status: 'failed',
          errors: [err.message]
        });
      }
    } finally {
      setUploading(false);
      
      // Unsubscribe from progress updates
      if (readyState === WebSocket.OPEN && sessionIdRef.current) {
        sendMessage(JSON.stringify({
          action: 'unsubscribe'
        }));
      }
      
      sessionIdRef.current = null;
      abortControllerRef.current = null;
    }
  };

  const handleCancel = async () => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    
    if (sessionIdRef.current) {
      try {
        await fetch(`/api/streaming/session/${sessionIdRef.current}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`
          }
        });
      } catch (err) {
        console.error('Failed to cancel session', err);
      }
    }
  };

  const generateSessionId = () => {
    return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  };

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  const formatDuration = (ms: number) => {
    if (ms < 1000) return `${ms} ms`;
    return `${(ms / 1000).toFixed(1)} s`;
  };

  return (
    <div>
      <Title level={4}>
        <CloudUploadOutlined /> Large File Upload (Streaming)
      </Title>
      
      {!currentSession && (
        <Dragger {...uploadProps} disabled={uploading}>
          <p className="ant-upload-drag-icon">
            <FileTextOutlined />
          </p>
          <p className="ant-upload-text">
            Click or drag file to upload
          </p>
          <p className="ant-upload-hint">
            Supports XML and JSON files up to {maxFileSize / (1024 * 1024)}MB.
            Files are processed in streaming mode without loading entirely into memory.
          </p>
        </Dragger>
      )}
      
      {uploading && (
        <Card style={{ marginTop: 16 }}>
          <Space direction="vertical" style={{ width: '100%' }}>
            <Text>Processing {currentSession?.fileName || 'file'}...</Text>
            <Progress percent={uploadProgress} status="active" />
            {currentSession?.elementsProcessed > 0 && (
              <Text type="secondary">
                Elements processed: {currentSession.elementsProcessed.toLocaleString()}
              </Text>
            )}
            <Button danger onClick={handleCancel}>
              Cancel Upload
            </Button>
          </Space>
        </Card>
      )}
      
      {error && (
        <Alert
          message="Upload Error"
          description={error}
          type="error"
          showIcon
          closable
          onClose={() => setError(null)}
          style={{ marginTop: 16 }}
        />
      )}
      
      {currentSession && currentSession.status === 'completed' && (
        <Card style={{ marginTop: 16 }}>
          <Descriptions title="Upload Complete" bordered>
            <Descriptions.Item label="File Name">
              {currentSession.fileName}
            </Descriptions.Item>
            <Descriptions.Item label="File Size">
              {formatFileSize(currentSession.fileSize)}
            </Descriptions.Item>
            <Descriptions.Item label="Processing Time">
              {currentSession.durationMs ? formatDuration(currentSession.durationMs) : 'N/A'}
            </Descriptions.Item>
            <Descriptions.Item label="Elements Processed" span={2}>
              {currentSession.elementsProcessed.toLocaleString()}
            </Descriptions.Item>
            <Descriptions.Item label="Status">
              <Tag icon={<CheckCircleOutlined />} color="success">
                Completed
              </Tag>
            </Descriptions.Item>
          </Descriptions>
          
          {structure && (
            <div style={{ marginTop: 16 }}>
              <Title level={5}>Extracted Structure</Title>
              <pre style={{
                backgroundColor: '#f5f5f5',
                padding: 12,
                borderRadius: 4,
                overflow: 'auto',
                maxHeight: 300
              }}>
                {JSON.stringify(structure, null, 2)}
              </pre>
            </div>
          )}
          
          <Button 
            type="primary" 
            style={{ marginTop: 16 }}
            onClick={() => {
              setCurrentSession(null);
              setStructure(null);
              setUploadProgress(0);
            }}
          >
            Upload Another File
          </Button>
        </Card>
      )}
    </div>
  );
};

export default StreamingUpload;