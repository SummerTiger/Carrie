import React, { useState, useEffect } from 'react';
import { productsAPI, machinesAPI, healthAPI } from '../services/api';

function Dashboard() {
  const [stats, setStats] = useState({
    totalProducts: 0,
    activeProducts: 0,
    totalMachines: 0,
    activeMachines: 0,
  });
  const [systemStatus, setSystemStatus] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      const [productsRes, machinesRes, healthRes] = await Promise.all([
        productsAPI.getAll(),
        machinesAPI.getAll(),
        healthAPI.check(),
      ]);

      const products = Array.isArray(productsRes.data) ? productsRes.data : [];
      const machines = Array.isArray(machinesRes.data) ? machinesRes.data : [];

      setStats({
        totalProducts: products.length,
        activeProducts: products.filter(p => p.active).length,
        totalMachines: machines.length,
        activeMachines: machines.filter(m => m.active).length,
      });

      setSystemStatus(healthRes.data);
      setLoading(false);
    } catch (err) {
      console.error('Failed to fetch dashboard data', err);
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">Loading dashboard...</div>;
  }

  return (
    <div>
      <div className="page-header">
        <h1>Dashboard</h1>
        <p>Welcome to your Vending Inventory Management System</p>
      </div>

      {systemStatus && (
        <div className="content-card" style={{ marginBottom: '20px' }}>
          <h3>System Status</h3>
          <p><strong>Status:</strong> {systemStatus.status}</p>
          <p><strong>Version:</strong> {systemStatus.version}</p>
          <p><strong>Message:</strong> {systemStatus.message}</p>
        </div>
      )}

      <div className="stats-grid">
        <div className="stat-card">
          <h3>Total Products</h3>
          <div className="stat-value">{stats.totalProducts}</div>
        </div>

        <div className="stat-card">
          <h3>Active Products</h3>
          <div className="stat-value">{stats.activeProducts}</div>
        </div>

        <div className="stat-card">
          <h3>Total Machines</h3>
          <div className="stat-value">{stats.totalMachines}</div>
        </div>

        <div className="stat-card">
          <h3>Active Machines</h3>
          <div className="stat-value">{stats.activeMachines}</div>
        </div>
      </div>

      <div className="content-card">
        <h2>Quick Actions</h2>
        <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
          <button className="btn btn-primary" onClick={() => window.location.href = '/dashboard/products'}>
            Manage Products
          </button>
          <button className="btn btn-primary" onClick={() => window.location.href = '/dashboard/machines'}>
            Manage Machines
          </button>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
