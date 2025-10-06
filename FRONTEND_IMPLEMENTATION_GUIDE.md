# NPS Frontend Implementation Guide

## 📋 **Overview**

This guide provides detailed technical implementation instructions for building the NPS frontend application. It covers architecture decisions, component specifications, and step-by-step implementation procedures.

---

## 🏗️ **Project Setup & Architecture**

### **1. Project Initialization**

#### **React + TypeScript Setup**
```bash
# Create new React app with TypeScript
npx create-react-app nps-frontend --template typescript
cd nps-frontend

# Install additional dependencies
npm install @reduxjs/toolkit react-redux
npm install @mui/material @emotion/react @emotion/styled
npm install @mui/icons-material
npm install axios
npm install react-router-dom
npm install react-hook-form @hookform/resolvers yup
npm install recharts
npm install date-fns
npm install @types/node
npm install socket.io-client
npm install react-query @tanstack/react-query
npm install react-table @tanstack/react-table
npm install file-saver
npm install jspdf html2canvas
npm install react-dropzone
npm install react-hot-toast

# Development dependencies
npm install -D @types/react @types/react-dom
npm install -D eslint @typescript-eslint/eslint-plugin
npm install -D prettier
npm install -D @testing-library/react @testing-library/jest-dom
```

#### **Project Structure**
```
src/
├── components/           # Reusable UI components
│   ├── common/          # Generic components
│   ├── forms/           # Form components
│   ├── charts/          # Chart components
│   ├── layout/          # Layout components
│   ├── reports/         # Report components
│   ├── integrations/    # Integration components
│   └── monitoring/      # System monitoring components
├── pages/               # Page components
│   ├── admin/           # Admin pages
│   │   ├── reports/     # Reporting pages
│   │   ├── integrations/# Integration management
│   │   ├── monitoring/  # System monitoring
│   │   └── users/       # User management
│   ├── client/          # Client pages
│   └── auth/            # Authentication pages
├── services/            # API services
│   ├── auth/            # Authentication services
│   ├── reports/         # Reporting services
│   ├── integrations/    # Integration services
│   └── monitoring/      # Monitoring services
├── store/               # Redux store
│   ├── slices/          # Redux slices
│   └── middleware/      # Custom middleware
├── hooks/               # Custom React hooks
├── utils/               # Utility functions
├── types/               # TypeScript type definitions
├── constants/           # Application constants
└── styles/              # Global styles
```

### **2. State Management Architecture**

#### **Redux Store Configuration**
```typescript
// src/store/index.ts
import { configureStore } from '@reduxjs/toolkit';
import { authSlice } from './slices/authSlice';
import { paymentSlice } from './slices/paymentSlice';
import { alertSlice } from './slices/alertSlice';
import { clientSlice } from './slices/clientSlice';
import { analyticsSlice } from './slices/analyticsSlice';
import { uiSlice } from './slices/uiSlice';

export const store = configureStore({
  reducer: {
    auth: authSlice.reducer,
    payments: paymentSlice.reducer,
    alerts: alertSlice.reducer,
    clients: clientSlice.reducer,
    analytics: analyticsSlice.reducer,
    ui: uiSlice.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST'],
      },
    }),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
```

#### **Auth Slice Example**
```typescript
// src/store/slices/authSlice.ts
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { authService } from '../../services/authService';

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  permissions: string[];
  loading: boolean;
  error: string | null;
}

const initialState: AuthState = {
  user: null,
  token: localStorage.getItem('authToken'),
  isAuthenticated: false,
  permissions: [],
  loading: false,
  error: null,
};

export const login = createAsyncThunk(
  'auth/login',
  async (credentials: LoginCredentials, { rejectWithValue }) => {
    try {
      const response = await authService.login(credentials);
      localStorage.setItem('authToken', response.token);
      return response;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Login failed');
    }
  }
);

export const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    logout: (state) => {
      state.user = null;
      state.token = null;
      state.isAuthenticated = false;
      state.permissions = [];
      localStorage.removeItem('authToken');
    },
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(login.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.loading = false;
        state.user = action.payload;
        state.isAuthenticated = true;
        state.permissions = action.payload.permissions;
      })
      .addCase(login.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload as string;
      });
  },
});

export const { logout, clearError } = authSlice.actions;
```

### **3. API Service Layer**

#### **Base API Client**
```typescript
// src/services/apiClient.ts
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { store } from '../store';
import { logout } from '../store/slices/authSlice';

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api/v1',
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor
    this.client.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('authToken');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor
    this.client.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          store.dispatch(logout());
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.client.get(url, config);
    return response.data;
  }

  async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.client.post(url, data, config);
    return response.data;
  }

  async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.client.put(url, data, config);
    return response.data;
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response: AxiosResponse<T> = await this.client.delete(url, config);
    return response.data;
  }
}

export const apiClient = new ApiClient();
```

#### **Payment Service**
```typescript
// src/services/paymentService.ts
import { apiClient } from './apiClient';
import { PaymentRequest, PaymentResponse, PaymentHistory, PaymentStatus } from '../types/payment';

export class PaymentService {
  async initiatePayment(paymentData: PaymentRequest): Promise<PaymentResponse> {
    return apiClient.post<PaymentResponse>('/payments/initiate', paymentData);
  }

  async checkPaymentStatus(transactionId: string, messageId: string): Promise<PaymentStatus> {
    return apiClient.post<PaymentStatus>('/payments/status', {
      originalTransactionId: transactionId,
      originalMessageId: messageId,
    });
  }

  async getPaymentHistory(params: {
    page?: number;
    size?: number;
    from?: string;
    to?: string;
    status?: string;
  }): Promise<PaymentHistory> {
    const queryParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined) {
        queryParams.append(key, value.toString());
      }
    });

    return apiClient.get<PaymentHistory>(`/payments/history?${queryParams}`);
  }
}

export const paymentService = new PaymentService();
```

---

## 🎨 **Component Implementation**

### **1. Layout Components**

#### **Main Layout**
```typescript
// src/components/layout/MainLayout.tsx
import React from 'react';
import { Box, AppBar, Toolbar, Typography, IconButton, Drawer, List, ListItem, ListItemIcon, ListItemText } from '@mui/material';
import { Menu as MenuIcon, Dashboard, Payment, AccountBalance, Warning, Analytics, People, Settings } from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { RootState } from '../../store';
import { logout } from '../../store/slices/authSlice';

const drawerWidth = 240;

interface MainLayoutProps {
  children: React.ReactNode;
}

export const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  const [mobileOpen, setMobileOpen] = React.useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const dispatch = useDispatch();
  const { user, isAuthenticated } = useSelector((state: RootState) => state.auth);

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  const menuItems = [
    { text: 'Dashboard', icon: <Dashboard />, path: '/dashboard' },
    { text: 'Payments', icon: <Payment />, path: '/payments' },
    { text: 'Account Verification', icon: <AccountBalance />, path: '/verification' },
    { text: 'Alerts', icon: <Warning />, path: '/alerts' },
    { text: 'Analytics', icon: <Analytics />, path: '/analytics' },
    { text: 'Clients', icon: <People />, path: '/clients' },
    { text: 'Settings', icon: <Settings />, path: '/settings' },
  ];

  const drawer = (
    <div>
      <Toolbar>
        <Typography variant="h6" noWrap component="div">
          NPS Admin
        </Typography>
      </Toolbar>
      <List>
        {menuItems.map((item) => (
          <ListItem
            button
            key={item.text}
            selected={location.pathname === item.path}
            onClick={() => navigate(item.path)}
          >
            <ListItemIcon>{item.icon}</ListItemIcon>
            <ListItemText primary={item.text} />
          </ListItem>
        ))}
      </List>
    </div>
  );

  if (!isAuthenticated) {
    return <>{children}</>;
  }

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar
        position="fixed"
        sx={{
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          ml: { sm: `${drawerWidth}px` },
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            Nigerian Payment Stack
          </Typography>
          <Typography variant="body2" sx={{ mr: 2 }}>
            Welcome, {user?.clientName}
          </Typography>
          <IconButton color="inherit" onClick={handleLogout}>
            <Settings />
          </IconButton>
        </Toolbar>
      </AppBar>
      <Box
        component="nav"
        sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
      >
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{
            keepMounted: true,
          }}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', sm: 'block' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { sm: `calc(100% - ${drawerWidth}px)` },
        }}
      >
        <Toolbar />
        {children}
      </Box>
    </Box>
  );
};
```

### **2. Form Components**

#### **Payment Form**
```typescript
// src/components/forms/PaymentForm.tsx
import React from 'react';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
} from '@mui/material';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '../../store';
import { initiatePayment } from '../../store/slices/paymentSlice';

const schema = yup.object({
  transactionId: yup.string().required('Transaction ID is required'),
  amount: yup
    .number()
    .positive('Amount must be positive')
    .required('Amount is required'),
  debtorAccount: yup.string().required('Debtor account is required'),
  creditorAccount: yup.string().required('Creditor account is required'),
  debtorBankCode: yup.string().required('Debtor bank code is required'),
  creditorBankCode: yup.string().required('Creditor bank code is required'),
  narration: yup.string().required('Narration is required'),
  reference: yup.string().required('Reference is required'),
});

interface PaymentFormData {
  transactionId: string;
  amount: number;
  debtorAccount: string;
  creditorAccount: string;
  debtorBankCode: string;
  creditorBankCode: string;
  narration: string;
  reference: string;
}

export const PaymentForm: React.FC = () => {
  const dispatch = useDispatch();
  const { loading, error } = useSelector((state: RootState) => state.payments);

  const {
    control,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<PaymentFormData>({
    resolver: yupResolver(schema),
    defaultValues: {
      transactionId: `TXN${Date.now()}`,
      amount: 0,
      debtorAccount: '',
      creditorAccount: '',
      debtorBankCode: '',
      creditorBankCode: '',
      narration: '',
      reference: '',
    },
  });

  const onSubmit = (data: PaymentFormData) => {
    dispatch(initiatePayment(data));
  };

  return (
    <Card>
      <CardContent>
        <Typography variant="h5" gutterBottom>
          Initiate Payment
        </Typography>
        
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Box component="form" onSubmit={handleSubmit(onSubmit)}>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6}>
              <Controller
                name="transactionId"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Transaction ID"
                    fullWidth
                    error={!!errors.transactionId}
                    helperText={errors.transactionId?.message}
                  />
                )}
              />
            </Grid>
            
            <Grid item xs={12} sm={6}>
              <Controller
                name="amount"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Amount"
                    type="number"
                    fullWidth
                    error={!!errors.amount}
                    helperText={errors.amount?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="debtorAccount"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Debtor Account"
                    fullWidth
                    error={!!errors.debtorAccount}
                    helperText={errors.debtorAccount?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="creditorAccount"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Creditor Account"
                    fullWidth
                    error={!!errors.creditorAccount}
                    helperText={errors.creditorAccount?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="debtorBankCode"
                control={control}
                render={({ field }) => (
                  <FormControl fullWidth error={!!errors.debtorBankCode}>
                    <InputLabel>Debtor Bank</InputLabel>
                    <Select {...field} label="Debtor Bank">
                      <MenuItem value="044">Access Bank</MenuItem>
                      <MenuItem value="058">GTBank</MenuItem>
                      <MenuItem value="011">First Bank</MenuItem>
                      <MenuItem value="014">Afribank</MenuItem>
                    </Select>
                  </FormControl>
                )}
              />
            </Grid>

            <Grid item xs={12} sm={6}>
              <Controller
                name="creditorBankCode"
                control={control}
                render={({ field }) => (
                  <FormControl fullWidth error={!!errors.creditorBankCode}>
                    <InputLabel>Creditor Bank</InputLabel>
                    <Select {...field} label="Creditor Bank">
                      <MenuItem value="044">Access Bank</MenuItem>
                      <MenuItem value="058">GTBank</MenuItem>
                      <MenuItem value="011">First Bank</MenuItem>
                      <MenuItem value="014">Afribank</MenuItem>
                    </Select>
                  </FormControl>
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Controller
                name="narration"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Narration"
                    fullWidth
                    multiline
                    rows={2}
                    error={!!errors.narration}
                    helperText={errors.narration?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Controller
                name="reference"
                control={control}
                render={({ field }) => (
                  <TextField
                    {...field}
                    label="Reference"
                    fullWidth
                    error={!!errors.reference}
                    helperText={errors.reference?.message}
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Box sx={{ display: 'flex', gap: 2 }}>
                <Button
                  type="submit"
                  variant="contained"
                  disabled={loading}
                  sx={{ minWidth: 120 }}
                >
                  {loading ? 'Processing...' : 'Initiate Payment'}
                </Button>
                <Button
                  type="button"
                  variant="outlined"
                  onClick={() => reset()}
                >
                  Reset
                </Button>
              </Box>
            </Grid>
          </Grid>
        </Box>
      </CardContent>
    </Card>
  );
};
```

### **3. Data Visualization Components**

#### **Transaction Chart**
```typescript
// src/components/charts/TransactionChart.tsx
import React from 'react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { Card, CardContent, Typography, Box } from '@mui/material';

interface TransactionData {
  date: string;
  transactions: number;
  volume: number;
  successRate: number;
}

interface TransactionChartProps {
  data: TransactionData[];
  title: string;
}

export const TransactionChart: React.FC<TransactionChartProps> = ({ data, title }) => {
  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          {title}
        </Typography>
        <Box sx={{ width: '100%', height: 300 }}>
          <ResponsiveContainer>
            <LineChart data={data}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis yAxisId="left" />
              <YAxis yAxisId="right" orientation="right" />
              <Tooltip />
              <Legend />
              <Line
                yAxisId="left"
                type="monotone"
                dataKey="transactions"
                stroke="#8884d8"
                name="Transactions"
              />
              <Line
                yAxisId="right"
                type="monotone"
                dataKey="volume"
                stroke="#82ca9d"
                name="Volume (₦)"
              />
              <Line
                yAxisId="left"
                type="monotone"
                dataKey="successRate"
                stroke="#ffc658"
                name="Success Rate (%)"
              />
            </LineChart>
          </ResponsiveContainer>
        </Box>
      </CardContent>
    </Card>
  );
};
```

### **4. Data Table Components**

#### **Payment History Table**
```typescript
// src/components/tables/PaymentHistoryTable.tsx
import React, { useState } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TablePagination,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import { Visibility, Refresh } from '@mui/icons-material';
import { format } from 'date-fns';
import { PaymentTransaction } from '../../types/payment';

interface PaymentHistoryTableProps {
  transactions: PaymentTransaction[];
  loading: boolean;
  onRefresh: () => void;
  onViewDetails: (transaction: PaymentTransaction) => void;
}

export const PaymentHistoryTable: React.FC<PaymentHistoryTableProps> = ({
  transactions,
  loading,
  onRefresh,
  onViewDetails,
}) => {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'SUCCESS':
        return 'success';
      case 'FAILED':
        return 'error';
      case 'PENDING':
        return 'warning';
      default:
        return 'default';
    }
  };

  const formatCurrency = (amount: string) => {
    return new Intl.NumberFormat('en-NG', {
      style: 'currency',
      currency: 'NGN',
    }).format(parseFloat(amount));
  };

  return (
    <Paper>
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Transaction ID</TableCell>
              <TableCell>Amount</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Debtor Account</TableCell>
              <TableCell>Creditor Account</TableCell>
              <TableCell>Created At</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {transactions
              .slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
              .map((transaction) => (
                <TableRow key={transaction.transactionId} hover>
                  <TableCell>{transaction.transactionId}</TableCell>
                  <TableCell>{formatCurrency(transaction.amount)}</TableCell>
                  <TableCell>
                    <Chip
                      label={transaction.status}
                      color={getStatusColor(transaction.status) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{transaction.debtorAccount}</TableCell>
                  <TableCell>{transaction.creditorAccount}</TableCell>
                  <TableCell>
                    {format(new Date(transaction.createdAt), 'MMM dd, yyyy HH:mm')}
                  </TableCell>
                  <TableCell>
                    <Tooltip title="View Details">
                      <IconButton
                        size="small"
                        onClick={() => onViewDetails(transaction)}
                      >
                        <Visibility />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Refresh Status">
                      <IconButton
                        size="small"
                        onClick={() => onRefresh()}
                      >
                        <Refresh />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        rowsPerPageOptions={[5, 10, 25]}
        component="div"
        count={transactions.length}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />
    </Paper>
  );
};
```

---

## 📱 **Page Implementation**

### **1. Dashboard Page**

```typescript
// src/pages/DashboardPage.tsx
import React, { useEffect } from 'react';
import { Grid, Card, CardContent, Typography, Box } from '@mui/material';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '../store';
import { fetchDashboardData } from '../store/slices/analyticsSlice';
import { TransactionChart } from '../components/charts/TransactionChart';
import { AlertSummary } from '../components/alerts/AlertSummary';

export const DashboardPage: React.FC = () => {
  const dispatch = useDispatch();
  const { dashboardData, loading } = useSelector((state: RootState) => state.analytics);

  useEffect(() => {
    dispatch(fetchDashboardData());
  }, [dispatch]);

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Dashboard
      </Typography>

      <Grid container spacing={3}>
        {/* KPI Cards */}
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Total Transactions Today
              </Typography>
              <Typography variant="h4">
                {dashboardData?.transactionMetrics.totalToday || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Success Rate
              </Typography>
              <Typography variant="h4">
                {dashboardData?.transactionMetrics.successRate || 0}%
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Total Volume
              </Typography>
              <Typography variant="h4">
                ₦{dashboardData?.transactionMetrics.totalVolume || '0.00'}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Active Alerts
              </Typography>
              <Typography variant="h4" color="error">
                {dashboardData?.alertMetrics.activeAlerts || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* Charts */}
        <Grid item xs={12} md={8}>
          <TransactionChart
            data={dashboardData?.transactionTrends || []}
            title="Transaction Trends"
          />
        </Grid>

        <Grid item xs={12} md={4}>
          <AlertSummary alerts={dashboardData?.recentAlerts || []} />
        </Grid>
      </Grid>
    </Box>
  );
};
```

### **2. Payment Management Page**

```typescript
// src/pages/PaymentPage.tsx
import React, { useState, useEffect } from 'react';
import { Box, Typography, Tabs, Tab, Grid } from '@mui/material';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from '../store';
import { fetchPaymentHistory } from '../store/slices/paymentSlice';
import { PaymentForm } from '../components/forms/PaymentForm';
import { PaymentHistoryTable } from '../components/tables/PaymentHistoryTable';
import { PaymentDetailsModal } from '../components/modals/PaymentDetailsModal';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`payment-tabpanel-${index}`}
      aria-labelledby={`payment-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

export const PaymentPage: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);
  const [selectedTransaction, setSelectedTransaction] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);

  const dispatch = useDispatch();
  const { transactions, loading } = useSelector((state: RootState) => state.payments);

  useEffect(() => {
    dispatch(fetchPaymentHistory({ page: 0, size: 50 }));
  }, [dispatch]);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleViewDetails = (transaction: any) => {
    setSelectedTransaction(transaction);
    setModalOpen(true);
  };

  const handleRefresh = () => {
    dispatch(fetchPaymentHistory({ page: 0, size: 50 }));
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Payment Management
      </Typography>

      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={handleTabChange}>
          <Tab label="Initiate Payment" />
          <Tab label="Payment History" />
        </Tabs>
      </Box>

      <TabPanel value={tabValue} index={0}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={8}>
            <PaymentForm />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={1}>
        <PaymentHistoryTable
          transactions={transactions}
          loading={loading}
          onRefresh={handleRefresh}
          onViewDetails={handleViewDetails}
        />
      </TabPanel>

      <PaymentDetailsModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        transaction={selectedTransaction}
      />
    </Box>
  );
};
```

---

## 🔄 **Real-time Updates**

### **WebSocket Service**

```typescript
// src/services/websocketService.ts
import { store } from '../store';
import { updateTransactionStatus } from '../store/slices/paymentSlice';
import { addAlert } from '../store/slices/alertSlice';
import { updateSystemHealth } from '../store/slices/analyticsSlice';

class WebSocketService {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectInterval = 5000;

  connect(token: string) {
    const wsUrl = `${process.env.REACT_APP_WS_URL}?token=${token}`;
    
    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      console.log('WebSocket connected');
      this.reconnectAttempts = 0;
    };

    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        this.handleMessage(data);
      } catch (error) {
        console.error('Error parsing WebSocket message:', error);
      }
    };

    this.ws.onclose = () => {
      console.log('WebSocket disconnected');
      this.attemptReconnect(token);
    };

    this.ws.onerror = (error) => {
      console.error('WebSocket error:', error);
    };
  }

  private handleMessage(data: any) {
    switch (data.type) {
      case 'TRANSACTION_UPDATED':
        store.dispatch(updateTransactionStatus(data.payload));
        break;
      case 'ALERT_CREATED':
        store.dispatch(addAlert(data.payload));
        break;
      case 'SYSTEM_STATUS':
        store.dispatch(updateSystemHealth(data.payload));
        break;
      default:
        console.log('Unknown message type:', data.type);
    }
  }

  private attemptReconnect(token: string) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      setTimeout(() => {
        console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
        this.connect(token);
      }, this.reconnectInterval);
    }
  }

  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }
}

export const websocketService = new WebSocketService();
```

---

## 🧪 **Testing Implementation**

### **1. Component Testing**

```typescript
// src/components/__tests__/PaymentForm.test.tsx
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { PaymentForm } from '../forms/PaymentForm';
import { paymentSlice } from '../../store/slices/paymentSlice';

const createMockStore = () => {
  return configureStore({
    reducer: {
      payments: paymentSlice.reducer,
    },
    preloadedState: {
      payments: {
        transactions: [],
        loading: false,
        error: null,
      },
    },
  });
};

describe('PaymentForm', () => {
  it('renders payment form fields', () => {
    const store = createMockStore();
    
    render(
      <Provider store={store}>
        <PaymentForm />
      </Provider>
    );

    expect(screen.getByLabelText(/transaction id/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/amount/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/debtor account/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/creditor account/i)).toBeInTheDocument();
  });

  it('validates required fields', async () => {
    const store = createMockStore();
    
    render(
      <Provider store={store}>
        <PaymentForm />
      </Provider>
    );

    const submitButton = screen.getByRole('button', { name: /initiate payment/i });
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/transaction id is required/i)).toBeInTheDocument();
      expect(screen.getByText(/amount is required/i)).toBeInTheDocument();
    });
  });

  it('submits form with valid data', async () => {
    const store = createMockStore();
    
    render(
      <Provider store={store}>
        <PaymentForm />
      </Provider>
    );

    fireEvent.change(screen.getByLabelText(/transaction id/i), {
      target: { value: 'TXN123' },
    });
    fireEvent.change(screen.getByLabelText(/amount/i), {
      target: { value: '1000' },
    });
    fireEvent.change(screen.getByLabelText(/debtor account/i), {
      target: { value: '1234567890' },
    });
    fireEvent.change(screen.getByLabelText(/creditor account/i), {
      target: { value: '0987654321' },
    });

    const submitButton = screen.getByRole('button', { name: /initiate payment/i });
    fireEvent.click(submitButton);

    // Verify that the payment initiation action was dispatched
    await waitFor(() => {
      const actions = store.getActions();
      expect(actions[0].type).toBe('payments/initiatePayment/pending');
    });
  });
});
```

### **2. Service Testing**

```typescript
// src/services/__tests__/paymentService.test.ts
import { paymentService } from '../paymentService';
import { apiClient } from '../apiClient';

jest.mock('../apiClient');

describe('PaymentService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should initiate payment successfully', async () => {
    const mockResponse = {
      transactionId: 'TXN123',
      messageId: 'MSG123',
      status: 'PENDING',
      responseCode: '00',
      responseMessage: 'Payment initiated successfully',
      processedAt: '2024-01-01T10:30:00Z',
    };

    (apiClient.post as jest.Mock).mockResolvedValue(mockResponse);

    const paymentData = {
      transactionId: 'TXN123',
      amount: 1000,
      currency: 'NGN',
      debtorAccount: '1234567890',
      creditorAccount: '0987654321',
      debtorBankCode: '044',
      creditorBankCode: '058',
      narration: 'Test payment',
      reference: 'REF123',
    };

    const result = await paymentService.initiatePayment(paymentData);

    expect(apiClient.post).toHaveBeenCalledWith('/payments/initiate', paymentData);
    expect(result).toEqual(mockResponse);
  });

  it('should handle payment initiation error', async () => {
    const mockError = new Error('Payment initiation failed');
    (apiClient.post as jest.Mock).mockRejectedValue(mockError);

    const paymentData = {
      transactionId: 'TXN123',
      amount: 1000,
      currency: 'NGN',
      debtorAccount: '1234567890',
      creditorAccount: '0987654321',
      debtorBankCode: '044',
      creditorBankCode: '058',
      narration: 'Test payment',
      reference: 'REF123',
    };

    await expect(paymentService.initiatePayment(paymentData)).rejects.toThrow(
      'Payment initiation failed'
    );
  });
});
```

---

## 🚀 **Deployment Configuration**

### **1. Environment Configuration**

```bash
# .env.development
REACT_APP_API_BASE_URL=http://localhost:8080/api/v1
REACT_APP_WS_URL=ws://localhost:8080/ws
REACT_APP_ENVIRONMENT=development

# .env.production
REACT_APP_API_BASE_URL=https://api.nps.payaza.africa/api/v1
REACT_APP_WS_URL=wss://api.nps.payaza.africa/ws
REACT_APP_ENVIRONMENT=production
```

### **2. Docker Configuration**

```dockerfile
# Dockerfile
FROM node:18-alpine as build

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

```nginx
# nginx.conf
events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    server {
        listen 80;
        server_name localhost;
        root /usr/share/nginx/html;
        index index.html;

        location / {
            try_files $uri $uri/ /index.html;
        }

        location /api {
            proxy_pass http://backend:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }

        location /ws {
            proxy_pass http://backend:8080;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
        }
    }
}
```

### **3. Build Scripts**

```json
// package.json
{
  "scripts": {
    "start": "react-scripts start",
    "build": "react-scripts build",
    "test": "react-scripts test",
    "eject": "react-scripts eject",
    "build:prod": "NODE_ENV=production npm run build",
    "test:coverage": "npm test -- --coverage --watchAll=false",
    "lint": "eslint src --ext .ts,.tsx",
    "lint:fix": "eslint src --ext .ts,.tsx --fix"
  }
}
```

---

## 📊 **Performance Optimization**

### **1. Code Splitting**

```typescript
// src/App.tsx
import React, { Suspense, lazy } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { CircularProgress, Box } from '@mui/material';

// Lazy load pages
const DashboardPage = lazy(() => import('./pages/DashboardPage'));
const PaymentPage = lazy(() => import('./pages/PaymentPage'));
const AlertPage = lazy(() => import('./pages/AlertPage'));
const ClientPage = lazy(() => import('./pages/ClientPage'));

const LoadingSpinner = () => (
  <Box display="flex" justifyContent="center" alignItems="center" minHeight="200px">
    <CircularProgress />
  </Box>
);

export const App: React.FC = () => {
  return (
    <Router>
      <Suspense fallback={<LoadingSpinner />}>
        <Routes>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/payments" element={<PaymentPage />} />
          <Route path="/alerts" element={<AlertPage />} />
          <Route path="/clients" element={<ClientPage />} />
        </Routes>
      </Suspense>
    </Router>
  );
};
```

### **2. Memoization**

```typescript
// src/components/charts/TransactionChart.tsx
import React, { memo, useMemo } from 'react';

export const TransactionChart = memo<TransactionChartProps>(({ data, title }) => {
  const chartData = useMemo(() => {
    return data.map(item => ({
      ...item,
      formattedDate: format(new Date(item.date), 'MMM dd'),
    }));
  }, [data]);

  return (
    // Chart component implementation
  );
});
```

---

## 📊 **Advanced Reporting Components**

### **Report Builder Component**
```typescript
// src/components/reports/ReportBuilder.tsx
import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Button,
  Grid,
  Chip,
  Autocomplete
} from '@mui/material';

interface ReportBuilderProps {
  onGenerate: (config: ReportConfig) => void;
  templates: ReportTemplate[];
}

export const ReportBuilder: React.FC<ReportBuilderProps> = ({ onGenerate, templates }) => {
  const [config, setConfig] = useState<ReportConfig>({
    reportType: '',
    reportName: '',
    fromDate: '',
    toDate: '',
    clientIds: [],
    filters: {},
    groupBy: '',
    format: 'json'
  });

  const handleSubmit = () => {
    onGenerate(config);
  };

  return (
    <Card>
      <CardContent>
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Report Type</InputLabel>
              <Select
                value={config.reportType}
                onChange={(e) => setConfig({...config, reportType: e.target.value})}
              >
                {templates.map(template => (
                  <MenuItem key={template.id} value={template.id}>
                    {template.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Report Name"
              value={config.reportName}
              onChange={(e) => setConfig({...config, reportName: e.target.value})}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="date"
              label="From Date"
              value={config.fromDate}
              onChange={(e) => setConfig({...config, fromDate: e.target.value})}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="date"
              label="To Date"
              value={config.toDate}
              onChange={(e) => setConfig({...config, toDate: e.target.value})}
              InputLabelProps={{ shrink: true }}
            />
          </Grid>

          <Grid item xs={12}>
            <Autocomplete
              multiple
              options={[]}
              value={config.clientIds}
              onChange={(_, value) => setConfig({...config, clientIds: value})}
              renderTags={(value, getTagProps) =>
                value.map((option, index) => (
                  <Chip variant="outlined" label={option} {...getTagProps({ index })} />
                ))
              }
              renderInput={(params) => (
                <TextField {...params} label="Client IDs" placeholder="Select clients" />
              )}
            />
          </Grid>

          <Grid item xs={12}>
            <Button
              variant="contained"
              onClick={handleSubmit}
              disabled={!config.reportType || !config.reportName}
            >
              Generate Report
            </Button>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};
```

### **Report Viewer Component**
```typescript
// src/components/reports/ReportViewer.tsx
import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton
} from '@mui/material';
import { Download, Refresh, Share } from '@mui/icons-material';

interface ReportViewerProps {
  report: Report;
  onExport: (format: string) => void;
  onRefresh: () => void;
}

export const ReportViewer: React.FC<ReportViewerProps> = ({ report, onExport, onRefresh }) => {
  return (
    <Card>
      <CardContent>
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
          <Typography variant="h5">{report.reportName}</Typography>
          <Box>
            <IconButton onClick={onRefresh}>
              <Refresh />
            </IconButton>
            <Button
              startIcon={<Download />}
              onClick={() => onExport('csv')}
              variant="outlined"
            >
              Export CSV
            </Button>
            <Button
              startIcon={<Download />}
              onClick={() => onExport('pdf')}
              variant="outlined"
            >
              Export PDF
            </Button>
          </Box>
        </Box>

        <Box mb={2}>
          <Chip
            label={report.status}
            color={report.status === 'COMPLETED' ? 'success' : 'warning'}
          />
          <Typography variant="body2" color="text.secondary" mt={1}>
            Generated: {new Date(report.generatedAt).toLocaleString()}
          </Typography>
        </Box>

        {report.data && (
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  {Object.keys(report.data[0] || {}).map(key => (
                    <TableCell key={key}>{key}</TableCell>
                  ))}
                </TableRow>
              </TableHead>
              <TableBody>
                {report.data.map((row, index) => (
                  <TableRow key={index}>
                    {Object.values(row).map((value, cellIndex) => (
                      <TableCell key={cellIndex}>{String(value)}</TableCell>
                    ))}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
      </CardContent>
    </Card>
  );
};
```

## 🔗 **Integration Management Components**

### **Webhook Configuration Component**
```typescript
// src/components/integrations/WebhookConfig.tsx
import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Switch,
  FormControlLabel,
  Button,
  Grid,
  Chip,
  Autocomplete,
  Typography
} from '@mui/material';

interface WebhookConfigProps {
  webhook: WebhookConfig;
  onSave: (webhook: WebhookConfig) => void;
  onTest: (webhookId: string) => void;
}

export const WebhookConfig: React.FC<WebhookConfigProps> = ({ webhook, onSave, onTest }) => {
  const [config, setConfig] = useState<WebhookConfig>(webhook);

  const eventTypes = [
    'PAYMENT_SUCCESS',
    'PAYMENT_FAILED',
    'ACCOUNT_VERIFIED',
    'ALERT_CREATED',
    'SYSTEM_HEALTH_WARNING'
  ];

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Webhook Configuration
        </Typography>
        
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Webhook Name"
              value={config.name}
              onChange={(e) => setConfig({...config, name: e.target.value})}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Webhook URL"
              value={config.url}
              onChange={(e) => setConfig({...config, url: e.target.value})}
            />
          </Grid>

          <Grid item xs={12}>
            <Autocomplete
              multiple
              options={eventTypes}
              value={config.eventTypes}
              onChange={(_, value) => setConfig({...config, eventTypes: value})}
              renderTags={(value, getTagProps) =>
                value.map((option, index) => (
                  <Chip variant="outlined" label={option} {...getTagProps({ index })} />
                ))
              }
              renderInput={(params) => (
                <TextField {...params} label="Event Types" placeholder="Select events" />
              )}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="API Key"
              type="password"
              value={config.apiKey}
              onChange={(e) => setConfig({...config, apiKey: e.target.value})}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Timeout (seconds)"
              type="number"
              value={config.timeoutSeconds}
              onChange={(e) => setConfig({...config, timeoutSeconds: parseInt(e.target.value)})}
            />
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={config.isActive}
                  onChange={(e) => setConfig({...config, isActive: e.target.checked})}
                />
              }
              label="Active"
            />
          </Grid>

          <Grid item xs={12}>
            <Box display="flex" gap={2}>
              <Button
                variant="contained"
                onClick={() => onSave(config)}
              >
                Save Configuration
              </Button>
              <Button
                variant="outlined"
                onClick={() => onTest(config.id)}
              >
                Test Webhook
              </Button>
            </Box>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};
```

## 🖥️ **System Monitoring Components**

### **System Health Dashboard**
```typescript
// src/components/monitoring/SystemHealthDashboard.tsx
import React from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  LinearProgress,
  Chip,
  List,
  ListItem,
  ListItemText,
  ListItemIcon
} from '@mui/material';
import {
  CheckCircle,
  Warning,
  Error,
  Memory,
  Speed,
  Storage
} from '@mui/icons-material';

interface SystemHealthDashboardProps {
  health: SystemHealth;
  metrics: SystemMetrics;
}

export const SystemHealthDashboard: React.FC<SystemHealthDashboardProps> = ({ health, metrics }) => {
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'HEALTHY': return 'success';
      case 'WARNING': return 'warning';
      case 'CRITICAL': return 'error';
      default: return 'default';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'HEALTHY': return <CheckCircle color="success" />;
      case 'WARNING': return <Warning color="warning" />;
      case 'CRITICAL': return <Error color="error" />;
      default: return <CheckCircle />;
    }
  };

  return (
    <Box>
      <Grid container spacing={3}>
        {/* System Status */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                System Status
              </Typography>
              <Box display="flex" alignItems="center" gap={2} mb={2}>
                {getStatusIcon(health.status)}
                <Chip
                  label={health.status}
                  color={getStatusColor(health.status)}
                  variant="outlined"
                />
              </Box>
              <Typography variant="body2" color="text.secondary">
                Uptime: {health.uptime}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Version: {health.version}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* Resource Usage */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Resource Usage
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Box display="flex" alignItems="center" gap={1} mb={1}>
                    <Memory />
                    <Typography variant="body2">Memory</Typography>
                  </Box>
                  <LinearProgress
                    variant="determinate"
                    value={metrics.resources.memoryUsage}
                    color={metrics.resources.memoryUsage > 80 ? 'error' : 'primary'}
                  />
                  <Typography variant="caption">
                    {metrics.resources.memoryUsage}%
                  </Typography>
                </Grid>

                <Grid item xs={12} sm={6}>
                  <Box display="flex" alignItems="center" gap={1} mb={1}>
                    <Speed />
                    <Typography variant="body2">CPU</Typography>
                  </Box>
                  <LinearProgress
                    variant="determinate"
                    value={metrics.resources.cpuUsage}
                    color={metrics.resources.cpuUsage > 80 ? 'error' : 'primary'}
                  />
                  <Typography variant="caption">
                    {metrics.resources.cpuUsage}%
                  </Typography>
                </Grid>

                <Grid item xs={12} sm={6}>
                  <Box display="flex" alignItems="center" gap={1} mb={1}>
                    <Storage />
                    <Typography variant="body2">Disk</Typography>
                  </Box>
                  <LinearProgress
                    variant="determinate"
                    value={metrics.resources.diskUsage}
                    color={metrics.resources.diskUsage > 80 ? 'error' : 'primary'}
                  />
                  <Typography variant="caption">
                    {metrics.resources.diskUsage}%
                  </Typography>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        {/* Component Health */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Component Health
              </Typography>
              <List>
                {Object.entries(health.components).map(([component, status]) => (
                  <ListItem key={component}>
                    <ListItemIcon>
                      {getStatusIcon(status)}
                    </ListItemIcon>
                    <ListItemText
                      primary={component}
                      secondary={
                        <Chip
                          label={status}
                          color={getStatusColor(status)}
                          size="small"
                        />
                      }
                    />
                  </ListItem>
                ))}
              </List>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
```

## 🔐 **Enhanced Authentication Components**

### **User Profile Management**
```typescript
// src/components/auth/UserProfile.tsx
import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Grid,
  Typography,
  Divider,
  Alert
} from '@mui/material';
import { useForm } from 'react-hook-form';

interface UserProfileProps {
  user: UserProfile;
  onUpdate: (data: Partial<UserProfile>) => void;
  onChangePassword: (data: PasswordChangeData) => void;
}

export const UserProfile: React.FC<UserProfileProps> = ({ user, onUpdate, onChangePassword }) => {
  const [showPasswordForm, setShowPasswordForm] = useState(false);
  const { register, handleSubmit, formState: { errors } } = useForm();

  const handleProfileUpdate = (data: any) => {
    onUpdate(data);
  };

  const handlePasswordChange = (data: any) => {
    onChangePassword(data);
    setShowPasswordForm(false);
  };

  return (
    <Box>
      <Card>
        <CardContent>
          <Typography variant="h5" gutterBottom>
            User Profile
          </Typography>
          
          <form onSubmit={handleSubmit(handleProfileUpdate)}>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Client Name"
                  defaultValue={user.clientName}
                  {...register('clientName', { required: 'Client name is required' })}
                  error={!!errors.clientName}
                  helperText={errors.clientName?.message}
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Email"
                  type="email"
                  defaultValue={user.email}
                  {...register('email', { 
                    required: 'Email is required',
                    pattern: {
                      value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                      message: 'Invalid email address'
                    }
                  })}
                  error={!!errors.email}
                  helperText={errors.email?.message}
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Phone"
                  defaultValue={user.phone}
                  {...register('phone')}
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Client Type"
                  value={user.clientType}
                  disabled
                />
              </Grid>

              <Grid item xs={12}>
                <Button type="submit" variant="contained">
                  Update Profile
                </Button>
              </Grid>
            </Grid>
          </form>

          <Divider sx={{ my: 3 }} />

          <Typography variant="h6" gutterBottom>
            Security
          </Typography>

          {!showPasswordForm ? (
            <Button
              variant="outlined"
              onClick={() => setShowPasswordForm(true)}
            >
              Change Password
            </Button>
          ) : (
            <form onSubmit={handleSubmit(handlePasswordChange)}>
              <Grid container spacing={3}>
                <Grid item xs={12} md={4}>
                  <TextField
                    fullWidth
                    label="Current Password"
                    type="password"
                    {...register('currentPassword', { required: 'Current password is required' })}
                    error={!!errors.currentPassword}
                    helperText={errors.currentPassword?.message}
                  />
                </Grid>

                <Grid item xs={12} md={4}>
                  <TextField
                    fullWidth
                    label="New Password"
                    type="password"
                    {...register('newPassword', { 
                      required: 'New password is required',
                      minLength: {
                        value: 8,
                        message: 'Password must be at least 8 characters'
                      }
                    })}
                    error={!!errors.newPassword}
                    helperText={errors.newPassword?.message}
                  />
                </Grid>

                <Grid item xs={12} md={4}>
                  <TextField
                    fullWidth
                    label="Confirm Password"
                    type="password"
                    {...register('confirmPassword', { 
                      required: 'Please confirm your password'
                    })}
                    error={!!errors.confirmPassword}
                    helperText={errors.confirmPassword?.message}
                  />
                </Grid>

                <Grid item xs={12}>
                  <Box display="flex" gap={2}>
                    <Button type="submit" variant="contained">
                      Change Password
                    </Button>
                    <Button
                      variant="outlined"
                      onClick={() => setShowPasswordForm(false)}
                    >
                      Cancel
                    </Button>
                  </Box>
                </Grid>
              </Grid>
            </form>
          )}
        </CardContent>
      </Card>
    </Box>
  );
};
```

---

This comprehensive implementation guide provides all the technical details needed to build a production-ready NPS frontend application with all the new features. The guide covers everything from project setup to deployment, ensuring a robust and scalable implementation with advanced reporting, integration management, system monitoring, and enhanced authentication capabilities.
