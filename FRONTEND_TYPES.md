# NPS Frontend TypeScript Type Definitions

This document provides comprehensive TypeScript type definitions for all NPS API request and response DTOs to support frontend development.

## 📋 **Authentication Types**

```typescript
// Login Request
interface LoginRequestDto {
  clientId: string;
  apiKey: string;
}

// Login Response
interface LoginResponseDto {
  token: string;
  clientId: string;
  clientName: string;
  clientType: 'BANK' | 'FIN' | 'PAY';
  permissions: string[];
  expiresAt: string;
  refreshToken: string;
}

// User Profile
interface UserProfileDto {
  id: string;
  clientName: string;
  email: string;
  phone: string;
  clientType: 'BANK' | 'FIN' | 'PAY';
  isActive: boolean;
  lastLogin: string;
  createdAt: string;
  updatedAt: string;
}

// Password Change Request
interface PasswordChangeRequestDto {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}
```

## 💰 **Payment Types**

```typescript
// PACS.008 Payment Request
interface Pacs008RequestDto {
  transactionId: string;
  amount: string;
  currency: string;
  debtorAccount: string;
  creditorAccount: string;
  debtorBankCode: string;
  creditorBankCode: string;
  narration: string;
  reference: string;
}

// PACS.008 Payment Response
interface Pacs008ResponseDto {
  transactionId: string;
  messageId: string;
  status: 'PENDING' | 'SUCCESS' | 'FAILED';
  responseCode: string;
  responseMessage: string;
  processedAt: string;
}

// PACS.002 Status Report Request
interface Pacs002RequestDto {
  messageId: string;
  originalMessageId: string;
  transactionId: string;
  paymentStatus: 'SUCCESS' | 'FAILED' | 'PENDING';
  responseCode: string;
  responseMessage: string;
  processedAt: string;
}

// PACS.002 Status Report Response
interface Pacs002ResponseDto {
  messageId: string;
  originalMessageId: string;
  responseCode: string;
  responseMessage: string;
  status: 'SUCCESS' | 'FAILED';
  paymentStatus: 'SUCCESS' | 'FAILED' | 'PENDING';
  createdAt: string;
}

// PACS.028 Status Request
interface Pacs028RequestDto {
  messageId: string;
  originalMessageId: string;
  originalTransactionId: string;
  requestDate: string;
}

// PACS.028 Status Response
interface Pacs028ResponseDto {
  messageId: string;
  originalMessageId: string;
  responseCode: string;
  responseMessage: string;
  status: 'SUCCESS' | 'FAILED';
  createdAt: string;
}

// Payment History Request
interface PaymentHistoryRequest {
  page?: number;
  size?: number;
  from?: string;
  to?: string;
  status?: 'PENDING' | 'SUCCESS' | 'FAILED';
  transactionId?: string;
}

// Payment History Response
interface PaymentHistoryResponse {
  content: PaymentTransaction[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
}

// Payment Transaction
interface PaymentTransaction {
  transactionId: string;
  amount: string;
  currency: string;
  status: 'PENDING' | 'SUCCESS' | 'FAILED';
  debtorAccount: string;
  creditorAccount: string;
  narration: string;
  createdAt: string;
  processedAt?: string;
  responseCode?: string;
  responseMessage?: string;
}
```

## 🔍 **Account Verification Types**

```typescript
// ACMT.023 Verification Request
interface Acmt023RequestDto {
  messageId: string;
  accountNumber: string;
  bankCode: string;
  accountName: string;
}

// ACMT.023 Verification Response
interface Acmt023ResponseDto {
  messageId: string;
  responseCode: string;
  responseMessage: string;
  status: 'SUCCESS' | 'FAILED';
  accountVerified: boolean;
  createdAt: string;
}

// ACMT.024 Verification Report Request
interface Acmt024RequestDto {
  messageId: string;
  originalMessageId: string;
  accountNumber: string;
  bankCode: string;
  accountName: string;
  verificationStatus: 'SUCCESS' | 'FAILED';
  accountVerified: boolean;
  responseCode: string;
  responseMessage: string;
  processedAt: string;
}

// ACMT.024 Verification Report Response
interface Acmt024ResponseDto {
  messageId: string;
  originalMessageId: string;
  responseCode: string;
  responseMessage: string;
  status: 'SUCCESS' | 'FAILED';
  verificationStatus: 'SUCCESS' | 'FAILED';
  accountVerified: boolean;
  createdAt: string;
}
```

## 👥 **Client Management Types**

```typescript
// Create Client Request
interface CreateClientRequestDto {
  clientName: string;
  clientType: 'BANK' | 'FIN' | 'PAY';
  description: string;
  contactEmail: string;
  contactPhone: string;
}

// Client Response
interface ClientResponseDto {
  clientId: string;
  apiKey?: string;
  clientName: string;
  clientType: 'BANK' | 'FIN' | 'PAY';
  description: string;
  contactEmail: string;
  contactPhone: string;
  isActive: boolean;
  createdAt: string;
  lastActivity?: string;
  totalTransactions?: number;
  successRate?: number;
  updatedAt?: string;
}

// Client List Request
interface ClientListRequest {
  page?: number;
  size?: number;
  search?: string;
  clientType?: 'BANK' | 'FIN' | 'PAY';
  isActive?: boolean;
}

// Client List Response
interface ClientListResponse {
  content: ClientResponseDto[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
}

// Update Client Request
interface UpdateClientRequestDto {
  clientName?: string;
  description?: string;
  contactEmail?: string;
  contactPhone?: string;
}

// Regenerate API Key Response
interface RegenerateApiKeyResponseDto {
  clientId: string;
  newApiKey: string;
  regeneratedAt: string;
}
```

## 🚨 **Alert Management Types**

```typescript
// Alert Configuration
interface AlertConfigDto {
  enabledMessageTypes: string[];
  suppressedAlertTypes: string[];
  isSuppressed: boolean;
  suppressionEndTime?: string;
  suppressionReason?: string;
  suppressedBy?: string;
  maxAlertsPerHour: number;
  analyticsRetentionDays: number;
}

// Alert
interface AlertDto {
  id: number;
  name: string;
  message: string;
  severity: 'CRITICAL' | 'WARNING' | 'INFO';
  status: 'ACTIVE' | 'RESOLVED' | 'SUPPRESSED';
  metricType: string;
  metricName: string;
  context: string;
  notificationChannels: string[];
  createdAt: string;
  updatedAt: string;
}

// Alert List Response
interface AlertListResponse {
  content: AlertDto[];
  totalElements?: number;
}

// Alert Analytics
interface AlertAnalyticsDto {
  totalAlerts: number;
  enabledMessageTypes: string[];
  suppressedAlertTypes: string[];
  isSuppressed: boolean;
  alertCountsByType: Record<string, number>;
  lastAlertTimes: Record<string, string>;
}

// Suppress Alert Request
interface SuppressAlertRequest {
  hours: number;
  reason: string;
  suppressedBy?: string;
}

// Suppress Alert Response
interface SuppressAlertResponse {
  message: string;
  suppressedUntil: string;
  reason: string;
  suppressedBy?: string;
}

// Test Alert Request
interface TestAlertRequest {
  messageType: string;
  severity: 'CRITICAL' | 'WARNING' | 'INFO';
  transactionId?: string;
}

// Test Alert Response
interface TestAlertResponse {
  message: string;
  messageType: string;
  severity: 'CRITICAL' | 'WARNING' | 'INFO';
  transactionId?: string;
  timestamp: string;
}
```

## 📊 **Analytics & Reporting Types**

```typescript
// Real-time Dashboard
interface RealtimeDashboardDto {
  systemHealth: {
    status: 'HEALTHY' | 'WARNING' | 'CRITICAL';
    uptime: string;
    responseTime: number;
    lastUpdated: string;
  };
  transactionMetrics: {
    totalToday: number;
    successfulToday: number;
    failedToday: number;
    totalVolume: string;
    successRate: number;
  };
  clientMetrics: {
    activeClients: number;
    totalClients: number;
    newClientsThisMonth: number;
  };
  alertMetrics: {
    activeAlerts: number;
    criticalAlerts: number;
    resolvedToday: number;
  };
}

// Transaction Analytics Request
interface TransactionAnalyticsRequest {
  from: string;
  to: string;
  groupBy?: 'day' | 'week' | 'month';
  clientId?: string;
}

// Transaction Analytics Response
interface TransactionAnalyticsResponse {
  summary: {
    totalTransactions: number;
    totalVolume: string;
    successRate: number;
    averageResponseTime: number;
  };
  dailyData: {
    date: string;
    transactions: number;
    volume: string;
    successRate: number;
    averageResponseTime: number;
  }[];
  clientBreakdown: {
    clientId: string;
    clientName: string;
    transactions: number;
    volume: string;
    successRate: number;
  }[];
}

// Client Analytics Response
interface ClientAnalyticsResponse {
  clientId: string;
  clientName: string;
  period: {
    from: string;
    to: string;
  };
  metrics: {
    totalTransactions: number;
    totalVolume: string;
    successRate: number;
    averageResponseTime: number;
    peakHour: string;
    busiestDay: string;
  };
  trends: {
    transactionGrowth: number;
    volumeGrowth: number;
    successRateChange: number;
  };
}
```

## 🔗 **Integration Management Types**

```typescript
// Webhook Configuration
interface WebhookConfigDto {
  id?: number;
  name: string;
  url: string;
  apiKey: string;
  eventTypes: string[];
  isActive: boolean;
  retryAttempts: number;
  timeoutSeconds: number;
  description: string;
  createdAt?: string;
  updatedAt?: string;
}

// Rate Limit Configuration
interface RateLimitConfigDto {
  clientId: string;
  hourlyLimit: number;
  dailyLimit: number;
  burstLimit: number;
  updatedAt?: string;
}

// Client API Usage
interface ClientApiUsageDto {
  clientId: string;
  requestsToday: number;
  requestsThisHour: number;
  requestsThisMinute: number;
  rateLimitRemaining: number;
  lastRequest: string;
}

// Webhook Test Response
interface WebhookTestResponse {
  success: boolean;
  response: string;
  responseTime: number;
  timestamp: string;
}
```

## 📡 **Inbound Subscription Types**

```typescript
// Inbound Subscription Request
interface InboundSubscriptionRequestDto {
  clientId: string;
  messageType: string;
  callbackUrl: string;
  isActive: boolean;
  description: string;
}

// Inbound Subscription Response
interface InboundSubscriptionResponseDto {
  clientId: string;
  messageType: string;
  callbackUrl: string;
  isActive: boolean;
  description: string;
  createdAt: string;
  updatedAt?: string;
}

// Queue Status
interface QueueStatusDto {
  queueSize: number;
  processingRate: number;
  averageProcessingTime: number;
  lastProcessed: string;
  status: 'HEALTHY' | 'WARNING' | 'CRITICAL';
}

// Subscription History Response
interface SubscriptionHistoryResponse {
  content: {
    id: number;
    clientId: string;
    messageType: string;
    callbackUrl: string;
    isActive: boolean;
    createdAt: string;
    updatedAt: string;
    deactivatedAt?: string;
  }[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
}
```

## 🖥️ **System Monitoring Types**

```typescript
// System Health
interface SystemHealthDto {
  status: 'HEALTHY' | 'WARNING' | 'CRITICAL';
  uptime: string;
  version: string;
  timestamp: string;
  issues: string[];
  components: {
    database: 'HEALTHY' | 'WARNING' | 'CRITICAL';
    externalServices: 'HEALTHY' | 'WARNING' | 'CRITICAL';
    memory: 'HEALTHY' | 'WARNING' | 'CRITICAL';
  };
}

// System Metrics
interface SystemMetricsDto {
  resources: {
    memoryUsage: number;
    cpuUsage: number;
    diskUsage: number;
    networkLatency: number;
  };
  application: {
    activeSessions: number;
    totalRequests: number;
    errorCount: number;
    averageResponseTime: number;
  };
  database: {
    connectionPoolSize: number;
    activeConnections: number;
    queryCount: number;
    slowQueries: number;
  };
  external: {
    nibssRequests: number;
    emailSent: number;
    webhookDeliveries: number;
    failedRequests: number;
  };
}

// Log Entry
interface LogEntryDto {
  id: number;
  timestamp: string;
  level: 'INFO' | 'WARN' | 'ERROR' | 'DEBUG';
  source: string;
  message: string;
  details?: string;
  userId?: string;
  sessionId?: string;
}

// Log Statistics
interface LogStatisticsDto {
  totalLogs: number;
  logsByLevel: Record<string, number>;
  logsBySource: Record<string, number>;
  errorRate: number;
  timeRange: {
    from: string;
    to: string;
  };
}
```

## 📋 **Report Generation Types**

```typescript
// Report Configuration
interface ReportConfig {
  reportType: string;
  reportName: string;
  description?: string;
  fromDate: string;
  toDate: string;
  clientIds?: string[];
  filters?: Record<string, any>;
  groupBy?: string;
  format: 'json' | 'csv' | 'xlsx' | 'pdf';
}

// Report Response
interface ReportResponse {
  reportId: string;
  reportName: string;
  reportType: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  data?: any[];
  summary?: {
    totalRecords: number;
    generatedAt: string;
    reportType: string;
  };
  metadata?: {
    requestedBy: string;
    filters: Record<string, any>;
    groupBy?: string;
    format: string;
  };
  downloadUrl?: string;
  errorMessage?: string;
}

// Report Template
interface ReportTemplate {
  id: string;
  name: string;
  description: string;
  category: string;
  parameters: string[];
}

// Scheduled Report
interface ScheduledReport {
  id: string;
  reportType: string;
  reportName: string;
  schedule: {
    enabled: boolean;
    frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY';
    time: string;
    recipients: string[];
  };
  createdAt: string;
  updatedAt: string;
}
```

## 🔔 **Notification Configuration Types**

```typescript
// Notification Configuration
interface NotificationConfigDto {
  emailApiUrl: string;
  emailApiKey: string;
  emailSender: string;
  emailRecipients: string;
  slackWebhookUrl: string;
  webhookUrl: string;
  webhookApiKey: string;
  maxRetryAttempts: number;
  retryIntervalSeconds: number;
}

// Email Configuration Update
interface EmailConfigUpdateDto {
  recipients: string;
}

// Slack Configuration Update
interface SlackConfigUpdateDto {
  webhookUrl: string;
}

// Webhook Configuration Update
interface WebhookConfigUpdateDto {
  url: string;
  apiKey: string;
}

// Retry Configuration Update
interface RetryConfigUpdateDto {
  maxAttempts: number;
  intervalSeconds: number;
}
```

## 📊 **Audit & Compliance Types**

```typescript
// Audit Log
interface AuditLogDto {
  id: number;
  action: string;
  resource: string;
  resourceId?: string;
  clientId?: string;
  clientName?: string;
  timestamp: string;
  ipAddress?: string;
  userAgent?: string;
  success: boolean;
  responseTime?: number;
  details?: string;
}

// Audit Log Request
interface AuditLogRequest {
  page?: number;
  size?: number;
  from?: string;
  to?: string;
  action?: string;
  clientId?: string;
  success?: boolean;
}

// Audit Log Response
interface AuditLogResponse {
  content: AuditLogDto[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
}
```

## ❌ **Error Response Types**

```typescript
// Standard Error Response
interface ErrorResponse {
  error: string;
  message: string;
  timestamp: string;
  path: string;
  clientId?: string;
  details?: {
    field?: string;
    reason?: string;
  };
}

// Validation Error
interface ValidationError {
  field: string;
  message: string;
  rejectedValue?: any;
}

// API Error Codes
type ApiErrorCode = 
  | 'INVALID_API_KEY'
  | 'INSUFFICIENT_PERMISSIONS'
  | 'VALIDATION_ERROR'
  | 'TRANSACTION_NOT_FOUND'
  | 'CLIENT_NOT_FOUND'
  | 'RATE_LIMIT_EXCEEDED'
  | 'SYSTEM_ERROR';
```

## 🔄 **WebSocket Event Types**

```typescript
// WebSocket Message
interface WebSocketMessage {
  type: WebSocketEventType;
  payload: any;
  timestamp: string;
}

// WebSocket Event Types
type WebSocketEventType = 
  | 'ALERT_CREATED'
  | 'ALERT_RESOLVED'
  | 'TRANSACTION_UPDATED'
  | 'SYSTEM_STATUS'
  | 'CLIENT_ACTIVITY';

// Transaction Update Event
interface TransactionUpdateEvent {
  transactionId: string;
  status: 'PENDING' | 'SUCCESS' | 'FAILED';
  responseCode?: string;
  responseMessage?: string;
  updatedAt: string;
}

// Alert Event
interface AlertEvent {
  alertId: number;
  name: string;
  message: string;
  severity: 'CRITICAL' | 'WARNING' | 'INFO';
  timestamp: string;
}

// System Status Event
interface SystemStatusEvent {
  status: 'HEALTHY' | 'WARNING' | 'CRITICAL';
  uptime: string;
  responseTime: number;
  timestamp: string;
}
```

## 📱 **Frontend State Management Types**

```typescript
// Redux State Types
interface RootState {
  auth: AuthState;
  payments: PaymentState;
  alerts: AlertState;
  clients: ClientState;
  analytics: AnalyticsState;
  ui: UiState;
}

// Auth State
interface AuthState {
  user: UserProfileDto | null;
  token: string | null;
  isAuthenticated: boolean;
  permissions: string[];
  loading: boolean;
  error: string | null;
}

// Payment State
interface PaymentState {
  transactions: PaymentTransaction[];
  currentTransaction: PaymentTransaction | null;
  loading: boolean;
  error: string | null;
  pagination: {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
  };
}

// Alert State
interface AlertState {
  alerts: AlertDto[];
  config: AlertConfigDto | null;
  analytics: AlertAnalyticsDto | null;
  loading: boolean;
  error: string | null;
}

// Client State
interface ClientState {
  clients: ClientResponseDto[];
  currentClient: ClientResponseDto | null;
  loading: boolean;
  error: string | null;
  pagination: {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
  };
}

// Analytics State
interface AnalyticsState {
  dashboard: RealtimeDashboardDto | null;
  transactionAnalytics: TransactionAnalyticsResponse | null;
  clientAnalytics: ClientAnalyticsResponse | null;
  loading: boolean;
  error: string | null;
}

// UI State
interface UiState {
  sidebarOpen: boolean;
  theme: 'light' | 'dark';
  notifications: Notification[];
  loading: boolean;
}

// Notification
interface Notification {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  timestamp: string;
  read: boolean;
}
```

## 🎯 **Form Validation Types**

```typescript
// Form Field Validation
interface FormFieldValidation {
  required?: boolean;
  minLength?: number;
  maxLength?: number;
  pattern?: RegExp;
  custom?: (value: any) => string | null;
}

// Form Validation Rules
interface FormValidationRules {
  [fieldName: string]: FormFieldValidation;
}

// Form State
interface FormState<T> {
  values: T;
  errors: Partial<Record<keyof T, string>>;
  touched: Partial<Record<keyof T, boolean>>;
  isValid: boolean;
  isSubmitting: boolean;
}
```

---

This comprehensive type definition file provides all the TypeScript interfaces and types needed for frontend development with the NPS system. These types ensure type safety, better IDE support, and easier maintenance of the frontend application.
