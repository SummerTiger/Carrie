import React, { useState, useEffect } from 'react';
import { auditLogsAPI } from '../services/api';
import '../styles/Dashboard.css';

function AuditLogs() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({
    username: '',
    action: '',
    resourceType: '',
    startDate: '',
    endDate: ''
  });
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [viewMode, setViewMode] = useState(false);
  const [selectedLog, setSelectedLog] = useState(null);

  useEffect(() => {
    fetchLogs();
  }, [page]);

  const fetchLogs = async () => {
    try {
      setLoading(true);
      setError('');

      let response;
      if (filters.startDate && filters.endDate) {
        response = await auditLogsAPI.getByDateRange(filters.startDate, filters.endDate, { page, size: 20 });
      } else if (filters.username) {
        response = await auditLogsAPI.getByUsername(filters.username, { page, size: 20 });
      } else if (filters.action) {
        response = await auditLogsAPI.getByAction(filters.action, { page, size: 20 });
      } else if (filters.resourceType) {
        response = await auditLogsAPI.getByResource(filters.resourceType, { page, size: 20 });
      } else {
        response = await auditLogsAPI.getAll({ page, size: 20 });
      }

      setLogs(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
    } catch (err) {
      setError('Failed to fetch audit logs');
      console.error('Error fetching audit logs:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (e) => {
    setFilters({
      ...filters,
      [e.target.name]: e.target.value
    });
  };

  const applyFilters = () => {
    setPage(0);
    fetchLogs();
  };

  const clearFilters = () => {
    setFilters({
      username: '',
      action: '',
      resourceType: '',
      startDate: '',
      endDate: ''
    });
    setPage(0);
    setTimeout(fetchLogs, 0);
  };

  const exportToCSV = () => {
    const headers = ['Timestamp', 'Username', 'Action', 'Resource Type', 'Resource ID', 'IP Address', 'Status', 'Details'];
    const rows = logs.map(log => [
      formatTimestamp(log.timestamp),
      log.username,
      log.action,
      log.resourceType || '',
      log.resourceId || '',
      log.ipAddress || '',
      log.status,
      log.details || ''
    ]);

    const csvContent = [
      headers.join(','),
      ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `audit_logs_${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const formatTimestamp = (timestamp) => {
    return new Date(timestamp).toLocaleString();
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'SUCCESS':
        return 'status-success';
      case 'FAILURE':
        return 'status-failure';
      case 'WARNING':
        return 'status-warning';
      default:
        return '';
    }
  };

  const handleView = (log) => {
    setSelectedLog(log);
    setViewMode(true);
  };

  const handleCancel = () => {
    setViewMode(false);
    setSelectedLog(null);
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>Audit Logs</h1>
        <div>
          <button className="btn-primary" onClick={exportToCSV} disabled={logs.length === 0} style={{ marginRight: '10px' }}>
            Export to CSV
          </button>
          <button className="btn-primary" onClick={fetchLogs}>
            Refresh
          </button>
        </div>
      </div>

      {/* View Mode - Detail View */}
      {viewMode && selectedLog ? (
        <div className="detail-view">
          <div className="detail-header">
            <h2>Audit Log Details</h2>
            <button className="btn-secondary" onClick={handleCancel}>
              Cancel
            </button>
          </div>
          <div className="detail-form">
            <div className="form-row">
              <div className="form-group">
                <label>ID:</label>
                <div className="readonly-field">{selectedLog.id}</div>
              </div>
              <div className="form-group">
                <label>Timestamp:</label>
                <div className="readonly-field">{formatTimestamp(selectedLog.timestamp)}</div>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Username:</label>
                <div className="readonly-field">{selectedLog.username}</div>
              </div>
              <div className="form-group">
                <label>Action:</label>
                <div className="readonly-field">
                  <span className="badge">{selectedLog.action}</span>
                </div>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Resource Type:</label>
                <div className="readonly-field">{selectedLog.resourceType || '-'}</div>
              </div>
              <div className="form-group">
                <label>Resource ID:</label>
                <div className="readonly-field">{selectedLog.resourceId || '-'}</div>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>IP Address:</label>
                <div className="readonly-field">{selectedLog.ipAddress || '-'}</div>
              </div>
              <div className="form-group">
                <label>Status:</label>
                <div className="readonly-field">
                  <span className={`badge ${getStatusColor(selectedLog.status)}`}>
                    {selectedLog.status}
                  </span>
                </div>
              </div>
            </div>

            <div className="form-group">
              <label>Details:</label>
              <div className="readonly-field details-field">{selectedLog.details || '-'}</div>
            </div>
          </div>
        </div>
      ) : (
        <>
          {/* Filters */}
          <div className="filters-container">
        <div className="filter-group">
          <label htmlFor="username">Username:</label>
          <input
            type="text"
            id="username"
            name="username"
            value={filters.username}
            onChange={handleFilterChange}
            placeholder="Filter by username"
          />
        </div>

        <div className="filter-group">
          <label htmlFor="action">Action:</label>
          <select
            id="action"
            name="action"
            value={filters.action}
            onChange={handleFilterChange}
          >
            <option value="">All Actions</option>
            <option value="LOGIN">Login</option>
            <option value="LOGOUT">Logout</option>
            <option value="LOGIN_FAILED">Login Failed</option>
            <option value="PASSWORD_CHANGED">Password Changed</option>
            <option value="CREATE">Create</option>
            <option value="UPDATE">Update</option>
            <option value="DELETE">Delete</option>
            <option value="VIEW">View</option>
            <option value="EXPORT">Export</option>
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="resourceType">Resource:</label>
          <select
            id="resourceType"
            name="resourceType"
            value={filters.resourceType}
            onChange={handleFilterChange}
          >
            <option value="">All Resources</option>
            <option value="PRODUCT">Product</option>
            <option value="VENDING_MACHINE">Vending Machine</option>
            <option value="USER">User</option>
            <option value="BATCH">Batch</option>
            <option value="RESTOCK">Restock</option>
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="startDate">Start Date:</label>
          <input
            type="date"
            id="startDate"
            name="startDate"
            value={filters.startDate}
            onChange={handleFilterChange}
          />
        </div>

        <div className="filter-group">
          <label htmlFor="endDate">End Date:</label>
          <input
            type="date"
            id="endDate"
            name="endDate"
            value={filters.endDate}
            onChange={handleFilterChange}
          />
        </div>

        <div className="filter-actions">
          <button className="btn-primary" onClick={applyFilters}>
            Apply Filters
          </button>
          <button className="btn-secondary" onClick={clearFilters}>
            Clear
          </button>
        </div>
      </div>

          {error && <div className="error-message">{error}</div>}

          {loading ? (
            <div className="loading">Loading audit logs...</div>
          ) : (
            <>
              <div className="table-container">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Timestamp</th>
                      <th>Username</th>
                      <th>Action</th>
                      <th>Resource Type</th>
                      <th>Resource ID</th>
                      <th>IP Address</th>
                      <th>Status</th>
                      <th>Details</th>
                    </tr>
                  </thead>
                  <tbody>
                    {logs.length === 0 ? (
                      <tr>
                        <td colSpan="8" className="text-center">No audit logs found</td>
                      </tr>
                    ) : (
                      logs.map((log) => (
                        <tr
                          key={log.id}
                          onDoubleClick={() => handleView(log)}
                          style={{ cursor: 'pointer' }}
                        >
                          <td>{formatTimestamp(log.timestamp)}</td>
                          <td>{log.username}</td>
                          <td><span className="badge">{log.action}</span></td>
                          <td>{log.resourceType || '-'}</td>
                          <td className="text-truncate">{log.resourceId || '-'}</td>
                          <td>{log.ipAddress || '-'}</td>
                          <td>
                            <span className={`badge ${getStatusColor(log.status)}`}>
                              {log.status}
                            </span>
                          </td>
                          <td className="text-truncate" title={log.details}>
                            {log.details || '-'}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="pagination">
                  <button
                    className="btn-secondary"
                    onClick={() => setPage(Math.max(0, page - 1))}
                    disabled={page === 0}
                  >
                    Previous
                  </button>
                  <span className="page-info">
                    Page {page + 1} of {totalPages}
                  </span>
                  <button
                    className="btn-secondary"
                    onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                    disabled={page >= totalPages - 1}
                  >
                    Next
                  </button>
                </div>
              )}
            </>
          )}
        </>
      )}
    </div>
  );
}

export default AuditLogs;
