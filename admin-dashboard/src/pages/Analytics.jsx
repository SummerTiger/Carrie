import { useState, useEffect } from 'react';
import { analyticsAPI } from '../services/api';
import {
  LineChart, Line, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';
import './Analytics.css';

const COLORS = ['#4CAF50', '#2196F3', '#FF9800', '#F44336', '#9C27B0', '#00BCD4', '#FFEB3B', '#795548'];

const Analytics = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [dateRange, setDateRange] = useState({
    startDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    endDate: new Date().toISOString().split('T')[0]
  });

  const [summary, setSummary] = useState(null);
  const [revenueData, setRevenueData] = useState([]);
  const [inventoryTrends, setInventoryTrends] = useState([]);
  const [machinePerformance, setMachinePerformance] = useState([]);
  const [productAnalytics, setProductAnalytics] = useState([]);
  const [categoryBreakdown, setCategoryBreakdown] = useState([]);

  useEffect(() => {
    fetchAnalytics();
  }, [dateRange]);

  const fetchAnalytics = async () => {
    setLoading(true);
    setError(null);
    try {
      const startDateTime = `${dateRange.startDate}T00:00:00`;
      const endDateTime = `${dateRange.endDate}T23:59:59`;

      const [summaryRes, revenueRes, inventoryRes, machineRes, productRes, categoryRes] = await Promise.all([
        analyticsAPI.getSummary(startDateTime, endDateTime),
        analyticsAPI.getRevenueData(startDateTime, endDateTime),
        analyticsAPI.getInventoryTrends(startDateTime, endDateTime),
        analyticsAPI.getMachinePerformance(startDateTime, endDateTime),
        analyticsAPI.getProductAnalytics(startDateTime, endDateTime),
        analyticsAPI.getCategoryBreakdown(startDateTime, endDateTime)
      ]);

      setSummary(summaryRes.data);
      setRevenueData(revenueRes.data);
      setInventoryTrends(inventoryRes.data);
      setMachinePerformance(machineRes.data);
      setProductAnalytics(productRes.data.slice(0, 10)); // Top 10 products
      setCategoryBreakdown(categoryRes.data);
    } catch (err) {
      console.error('Error fetching analytics:', err);
      setError(err.response?.data?.message || 'Failed to load analytics');
    } finally {
      setLoading(false);
    }
  };

  const handleDateChange = (e) => {
    setDateRange({ ...dateRange, [e.target.name]: e.target.value });
  };

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(value || 0);
  };

  const formatPercent = (value) => {
    return `${(value || 0).toFixed(2)}%`;
  };

  if (loading) {
    return <div className="analytics-container"><div className="loading">Loading analytics...</div></div>;
  }

  if (error) {
    return <div className="analytics-container"><div className="error">{error}</div></div>;
  }

  return (
    <div className="analytics-container">
      <div className="analytics-header">
        <h1>Analytics & Reports</h1>
        <div className="date-range-filter">
          <label>
            From:
            <input
              type="date"
              name="startDate"
              value={dateRange.startDate}
              onChange={handleDateChange}
              max={dateRange.endDate}
            />
          </label>
          <label>
            To:
            <input
              type="date"
              name="endDate"
              value={dateRange.endDate}
              onChange={handleDateChange}
              min={dateRange.startDate}
              max={new Date().toISOString().split('T')[0]}
            />
          </label>
          <button className="btn-primary" onClick={fetchAnalytics}>Apply</button>
        </div>
      </div>

      {/* Summary Cards */}
      {summary && (
        <div className="summary-cards">
          <div className="summary-card revenue">
            <h3>Total Revenue</h3>
            <div className="value">{formatCurrency(summary.totalCashCollected)}</div>
            <div className="label">Cash Collected</div>
          </div>
          <div className="summary-card cost">
            <h3>Total Cost</h3>
            <div className="value">{formatCurrency(summary.totalProcurementCost)}</div>
            <div className="label">Procurement</div>
          </div>
          <div className="summary-card profit">
            <h3>Profit Margin</h3>
            <div className="value">{formatPercent(summary.profitMargin)}</div>
            <div className="label">Gross Margin</div>
          </div>
          <div className="summary-card products">
            <h3>Products</h3>
            <div className="value">{summary.totalProducts}</div>
            <div className="sub-stats">
              <span className="low-stock">{summary.lowStockProducts} Low Stock</span>
              <span className="out-stock">{summary.outOfStockProducts} Out of Stock</span>
            </div>
          </div>
          <div className="summary-card machines">
            <h3>Machines</h3>
            <div className="value">{summary.totalMachines}</div>
            <div className="label">Active Machines</div>
          </div>
          <div className="summary-card restocks">
            <h3>Restocking Sessions</h3>
            <div className="value">{summary.totalRestockingSessions}</div>
            <div className="label">Total Sessions</div>
          </div>
        </div>
      )}

      {/* Revenue Chart */}
      <div className="chart-section">
        <h2>Revenue & Profit Analysis</h2>
        <div className="chart-container">
          <ResponsiveContainer width="100%" height={350}>
            <LineChart data={revenueData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis />
              <Tooltip formatter={(value) => formatCurrency(value)} />
              <Legend />
              <Line type="monotone" dataKey="cashCollected" stroke="#4CAF50" name="Cash Collected" strokeWidth={2} />
              <Line type="monotone" dataKey="procurementCost" stroke="#FF9800" name="Procurement Cost" strokeWidth={2} />
              <Line type="monotone" dataKey="profit" stroke="#2196F3" name="Profit" strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Inventory Trends Chart */}
      <div className="chart-section">
        <h2>Inventory Trends</h2>
        <div className="chart-container">
          <ResponsiveContainer width="100%" height={350}>
            <LineChart data={inventoryTrends}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" />
              <YAxis />
              <Tooltip />
              <Legend />
              <Line type="monotone" dataKey="totalStock" stroke="#2196F3" name="Total Stock" strokeWidth={2} />
              <Line type="monotone" dataKey="lowStockCount" stroke="#FF9800" name="Low Stock Items" strokeWidth={2} />
              <Line type="monotone" dataKey="outOfStockCount" stroke="#F44336" name="Out of Stock" strokeWidth={2} />
              <Line type="monotone" dataKey="restockedItemsCount" stroke="#4CAF50" name="Restocked Items" strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Machine Performance */}
      <div className="chart-section">
        <h2>Machine Performance (Top 10 by Revenue)</h2>
        <div className="chart-container">
          <ResponsiveContainer width="100%" height={400}>
            <BarChart data={machinePerformance.slice(0, 10)}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="machineBrand" />
              <YAxis />
              <Tooltip formatter={(value, name) => name === 'totalCashCollected' || name === 'averageCashPerRestock' ? formatCurrency(value) : value} />
              <Legend />
              <Bar dataKey="totalCashCollected" fill="#4CAF50" name="Total Cash Collected" />
              <Bar dataKey="restockCount" fill="#2196F3" name="Restock Count" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="machine-performance-table">
          <table>
            <thead>
              <tr>
                <th>Machine</th>
                <th>Location</th>
                <th>Restocks</th>
                <th>Total Cash</th>
                <th>Avg/Restock</th>
                <th>Items Restocked</th>
                <th>Maintenance</th>
              </tr>
            </thead>
            <tbody>
              {machinePerformance.slice(0, 10).map((machine) => (
                <tr key={machine.machineId}>
                  <td>{machine.machineBrand} {machine.machineModel}</td>
                  <td>{machine.locationAddress}</td>
                  <td>{machine.restockCount}</td>
                  <td>{formatCurrency(machine.totalCashCollected)}</td>
                  <td>{formatCurrency(machine.averageCashPerRestock)}</td>
                  <td>{machine.totalItemsRestocked}</td>
                  <td>{machine.maintenanceCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Top Products */}
      <div className="chart-section">
        <h2>Top 10 Products by Restock Volume</h2>
        <div className="chart-container">
          <ResponsiveContainer width="100%" height={350}>
            <BarChart data={productAnalytics} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis type="number" />
              <YAxis dataKey="productName" type="category" width={150} />
              <Tooltip />
              <Legend />
              <Bar dataKey="totalRestocked" fill="#4CAF50" name="Total Restocked" />
              <Bar dataKey="currentStock" fill="#2196F3" name="Current Stock" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Category Breakdown */}
      <div className="chart-section">
        <h2>Category Breakdown</h2>
        <div className="charts-row">
          <div className="chart-container half">
            <h3>By Stock Volume</h3>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={categoryBreakdown}
                  dataKey="totalStock"
                  nameKey="category"
                  cx="50%"
                  cy="50%"
                  outerRadius={100}
                  label={(entry) => `${entry.category}: ${entry.totalStock}`}
                >
                  {categoryBreakdown.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="chart-container half">
            <h3>By Total Value</h3>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={categoryBreakdown}
                  dataKey="totalValue"
                  nameKey="category"
                  cx="50%"
                  cy="50%"
                  outerRadius={100}
                  label={(entry) => `${entry.category}: ${formatCurrency(entry.totalValue)}`}
                >
                  {categoryBreakdown.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(value) => formatCurrency(value)} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="category-breakdown-table">
          <table>
            <thead>
              <tr>
                <th>Category</th>
                <th>Products</th>
                <th>Total Stock</th>
                <th>Total Value</th>
                <th>Total Restocked</th>
                <th>Procurement Cost</th>
              </tr>
            </thead>
            <tbody>
              {categoryBreakdown.map((category) => (
                <tr key={category.category}>
                  <td><strong>{category.category}</strong></td>
                  <td>{category.productCount}</td>
                  <td>{category.totalStock}</td>
                  <td>{formatCurrency(category.totalValue)}</td>
                  <td>{category.totalRestocked}</td>
                  <td>{formatCurrency(category.procurementCost)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Analytics;
