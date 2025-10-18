import React, { useState, useEffect } from 'react';
import { usersAPI } from '../services/api';

function Users() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phoneNumber: '',
    roles: [],
    enabled: true,
    accountNonLocked: true,
  });

  const availableRoles = ['ADMIN', 'MANAGER', 'OPERATOR', 'VIEWER'];

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      const response = await usersAPI.getAll();
      setUsers(Array.isArray(response.data) ? response.data : []);
      setLoading(false);
      setError('');
    } catch (err) {
      setError('Failed to fetch users');
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });
  };

  const handleRoleChange = (role) => {
    const updatedRoles = formData.roles.includes(role)
      ? formData.roles.filter((r) => r !== role)
      : [...formData.roles, role];
    setFormData({ ...formData, roles: updatedRoles });
  };

  const validateForm = () => {
    if (!editMode) {
      // Create mode validation
      if (!formData.username || formData.username.length < 3) {
        setError('Username must be at least 3 characters');
        return false;
      }
      if (!formData.password || formData.password.length < 8) {
        setError('Password must be at least 8 characters');
        return false;
      }
    }

    if (!formData.email || !formData.email.includes('@')) {
      setError('Valid email is required');
      return false;
    }

    if (formData.roles.length === 0) {
      setError('At least one role must be selected');
      return false;
    }

    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      if (editMode && editingUser) {
        // For edit mode, don't send username and password
        const { username, password, ...updateData } = formData;
        await usersAPI.update(editingUser.id, updateData);
        setEditMode(false);
        setEditingUser(null);
      } else {
        // For create mode, send all data
        await usersAPI.create(formData);
      }
      setShowForm(false);
      setEditingUser(null);
      resetForm();
      fetchUsers();
      setError('');
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Failed to save user';
      setError(errorMessage);
    }
  };

  const handleEdit = (user) => {
    setEditingUser(user);
    setFormData({
      username: user.username || '',
      email: user.email || '',
      password: '', // Don't populate password for security
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      phoneNumber: user.phoneNumber || '',
      roles: user.roles || [],
      enabled: user.enabled !== undefined ? user.enabled : true,
      accountNonLocked: user.accountNonLocked !== undefined ? user.accountNonLocked : true,
    });
    setEditMode(true);
    setShowForm(true);
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditMode(false);
    setEditingUser(null);
    resetForm();
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
      try {
        await usersAPI.delete(id);
        fetchUsers();
        setError('');
      } catch (err) {
        const errorMessage = err.response?.data?.message || 'Failed to delete user';
        setError(errorMessage);
      }
    }
  };

  const handleUnlockAccount = async (userId) => {
    try {
      await usersAPI.update(userId, { accountNonLocked: true });
      fetchUsers();
      setError('');
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Failed to unlock account';
      setError(errorMessage);
    }
  };

  const resetForm = () => {
    setFormData({
      username: '',
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      phoneNumber: '',
      roles: [],
      enabled: true,
      accountNonLocked: true,
    });
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'Never';
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getRoleBadgeColor = (role) => {
    const colors = {
      ADMIN: '#e74c3c',
      MANAGER: '#3498db',
      OPERATOR: '#2ecc71',
      VIEWER: '#95a5a6',
    };
    return colors[role] || '#95a5a6';
  };

  const filteredUsers = users.filter((user) => {
    if (!searchTerm) return true;
    const search = searchTerm.toLowerCase();
    return (
      user.username?.toLowerCase().includes(search) ||
      user.email?.toLowerCase().includes(search) ||
      user.firstName?.toLowerCase().includes(search) ||
      user.lastName?.toLowerCase().includes(search) ||
      user.roles?.some(role => role.toLowerCase().includes(search))
    );
  });

  if (loading) return <div className="loading">Loading users...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>Users</h1>
        <p>Manage user accounts and permissions</p>
      </div>

      {error && <div className="error">{error}</div>}

      <div className="content-card">
        <div className="card-header">
          <h2>User List</h2>
          {!editMode && !showForm && (
            <button
              className="btn btn-primary"
              onClick={() => {
                setShowForm(true);
                setEditingUser(null);
                resetForm();
              }}
            >
              Add User
            </button>
          )}
        </div>

        {showForm && (
          <form onSubmit={handleSubmit} className="product-form">
            <h3 style={{ color: editMode ? '#2563eb' : '#000' }}>
              {editMode ? 'Edit User' : 'New User'}
            </h3>

            {!editMode && (
              <div className="form-row">
                <div className="form-group">
                  <label>Username *</label>
                  <input
                    type="text"
                    name="username"
                    value={formData.username}
                    onChange={handleInputChange}
                    minLength="3"
                    required
                    placeholder="Minimum 3 characters"
                  />
                </div>
                <div className="form-group">
                  <label>Password *</label>
                  <input
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleInputChange}
                    minLength="8"
                    required
                    placeholder="Minimum 8 characters"
                  />
                </div>
              </div>
            )}

            {editMode && (
              <div className="form-group" style={{ marginBottom: '15px' }}>
                <label style={{ color: '#666', fontSize: '13px' }}>
                  Username: <strong>{formData.username}</strong> (cannot be changed)
                </label>
              </div>
            )}

            <div className="form-row">
              <div className="form-group">
                <label>Email *</label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  required
                  placeholder="user@example.com"
                />
              </div>
              <div className="form-group">
                <label>Phone Number</label>
                <input
                  type="text"
                  name="phoneNumber"
                  value={formData.phoneNumber}
                  onChange={handleInputChange}
                  placeholder="Optional"
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>First Name</label>
                <input
                  type="text"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleInputChange}
                  placeholder="Optional"
                />
              </div>
              <div className="form-group">
                <label>Last Name</label>
                <input
                  type="text"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleInputChange}
                  placeholder="Optional"
                />
              </div>
            </div>

            <div className="form-group" style={{ marginBottom: '20px' }}>
              <label style={{ marginBottom: '10px' }}>Roles * (select at least one)</label>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '15px' }}>
                {availableRoles.map((role) => (
                  <label
                    key={role}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      cursor: 'pointer',
                      padding: '8px 12px',
                      background: formData.roles.includes(role) ? '#e3f2fd' : '#f5f5f5',
                      borderRadius: '5px',
                      border: formData.roles.includes(role) ? '2px solid #3498db' : '2px solid transparent',
                    }}
                  >
                    <input
                      type="checkbox"
                      checked={formData.roles.includes(role)}
                      onChange={() => handleRoleChange(role)}
                      style={{ marginRight: '8px' }}
                    />
                    <span style={{ fontWeight: formData.roles.includes(role) ? 'bold' : 'normal' }}>
                      {role}
                    </span>
                  </label>
                ))}
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>
                  <input
                    type="checkbox"
                    name="enabled"
                    checked={formData.enabled}
                    onChange={handleInputChange}
                  />
                  Account Enabled
                </label>
              </div>
              {editMode && (
                <div className="form-group">
                  <label>
                    <input
                      type="checkbox"
                      name="accountNonLocked"
                      checked={formData.accountNonLocked}
                      onChange={handleInputChange}
                    />
                    Account Not Locked
                  </label>
                </div>
              )}
            </div>

            <div style={{ marginTop: '20px' }}>
              <button type="submit" className="btn btn-success">
                {editMode ? 'Update User' : 'Create User'}
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={handleCancel}
                style={{ marginLeft: '10px' }}
              >
                Cancel
              </button>
            </div>
          </form>
        )}

        {!showForm && (
          <>
            <div style={{ marginBottom: '20px' }}>
              <input
                type="text"
                placeholder="Search by username, email, name, or role..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px 15px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px',
                }}
              />
            </div>

            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Username</th>
                    <th>Email</th>
                    <th>Full Name</th>
                    <th>Roles</th>
                    <th>Status</th>
                    <th>Last Login</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredUsers.map((user) => (
                    <tr key={user.id}>
                      <td style={{ fontWeight: '600' }}>{user.username}</td>
                      <td>{user.email}</td>
                      <td>
                        {user.firstName || user.lastName
                          ? `${user.firstName || ''} ${user.lastName || ''}`.trim()
                          : '-'}
                      </td>
                      <td>
                        {user.roles && user.roles.length > 0 ? (
                          user.roles.map((role) => (
                            <span
                              key={role}
                              className="badge"
                              style={{
                                background: getRoleBadgeColor(role),
                                marginRight: '4px',
                              }}
                            >
                              {role}
                            </span>
                          ))
                        ) : (
                          <span style={{ color: '#999' }}>No roles</span>
                        )}
                      </td>
                      <td>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                          <span className={user.enabled ? 'badge-success' : 'badge-danger'}>
                            {user.enabled ? 'Enabled' : 'Disabled'}
                          </span>
                          {!user.accountNonLocked && (
                            <span className="badge-danger">Locked</span>
                          )}
                        </div>
                      </td>
                      <td>{formatDate(user.lastLogin)}</td>
                      <td className="action-buttons">
                        <button
                          className="btn btn-secondary"
                          onClick={() => handleEdit(user)}
                        >
                          Edit
                        </button>
                        {!user.accountNonLocked && (
                          <button
                            className="btn btn-success"
                            onClick={() => handleUnlockAccount(user.id)}
                            title="Unlock account"
                          >
                            Unlock
                          </button>
                        )}
                        <button
                          className="btn btn-danger"
                          onClick={() => handleDelete(user.id)}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {filteredUsers.length === 0 && (
                <div className="empty-state">
                  <h3>No users found</h3>
                  <p>
                    {searchTerm
                      ? 'Try adjusting your search criteria.'
                      : 'Click "Add User" to create your first user.'}
                  </p>
                </div>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export default Users;
