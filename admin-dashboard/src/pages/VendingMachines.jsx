import React, { useState, useEffect } from 'react';
import { machinesAPI } from '../services/api';

function VendingMachines() {
  const [machines, setMachines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editingMachine, setEditingMachine] = useState(null);
  const [formData, setFormData] = useState({
    brand: '',
    model: '',
    hasCashBillReader: false,
    hasCashlessPos: false,
    hasCoinChanger: false,
    active: true,
    location: {
      name: '',
      address: '',
      city: '',
      province: '',
      postalCode: '',
    },
  });

  useEffect(() => {
    fetchMachines();
  }, []);

  const fetchMachines = async () => {
    try {
      const response = await machinesAPI.getAll();
      setMachines(Array.isArray(response.data) ? response.data : []);
      setLoading(false);
    } catch (err) {
      setError('Failed to fetch vending machines');
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;

    if (name.startsWith('location.')) {
      const locationField = name.split('.')[1];
      setFormData({
        ...formData,
        location: {
          ...formData.location,
          [locationField]: value,
        },
      });
    } else {
      setFormData({
        ...formData,
        [name]: type === 'checkbox' ? checked : value,
      });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingMachine) {
        await machinesAPI.update(editingMachine.id, formData);
      } else {
        await machinesAPI.create(formData);
      }
      setShowForm(false);
      setEditingMachine(null);
      resetForm();
      fetchMachines();
    } catch (err) {
      setError('Failed to save vending machine');
    }
  };

  const handleEdit = (machine) => {
    setEditingMachine(machine);
    setFormData({
      brand: machine.brand || '',
      model: machine.model || '',
      hasCashBillReader: machine.hasCashBillReader || false,
      hasCashlessPos: machine.hasCashlessPos || false,
      hasCoinChanger: machine.hasCoinChanger || false,
      active: machine.active !== undefined ? machine.active : true,
      location: {
        name: machine.location?.name || '',
        address: machine.location?.address || '',
        city: machine.location?.city || '',
        province: machine.location?.province || '',
        postalCode: machine.location?.postalCode || '',
      },
    });
    setShowForm(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this vending machine?')) {
      try {
        await machinesAPI.delete(id);
        fetchMachines();
      } catch (err) {
        setError('Failed to delete vending machine');
      }
    }
  };

  const resetForm = () => {
    setFormData({
      brand: '',
      model: '',
      hasCashBillReader: false,
      hasCashlessPos: false,
      hasCoinChanger: false,
      active: true,
      location: {
        name: '',
        address: '',
        city: '',
        province: '',
        postalCode: '',
      },
    });
  };

  if (loading) return <div className="loading">Loading vending machines...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>Vending Machines</h1>
        <p>Manage your vending machine fleet</p>
      </div>

      {error && <div className="error">{error}</div>}

      <div className="content-card">
        <div className="card-header">
          <h2>Machine List</h2>
          <button
            className="btn btn-primary"
            onClick={() => {
              setShowForm(!showForm);
              setEditingMachine(null);
              resetForm();
            }}
          >
            {showForm ? 'Cancel' : 'Add Machine'}
          </button>
        </div>

        {showForm && (
          <form onSubmit={handleSubmit} className="machine-form">
            <h3>Machine Details</h3>
            <div className="form-row">
              <div className="form-group">
                <label>Brand *</label>
                <input
                  type="text"
                  name="brand"
                  value={formData.brand}
                  onChange={handleInputChange}
                  required
                />
              </div>
              <div className="form-group">
                <label>Model *</label>
                <input
                  type="text"
                  name="model"
                  value={formData.model}
                  onChange={handleInputChange}
                  required
                />
              </div>
            </div>

            <h3>Features</h3>
            <div className="form-row">
              <div className="form-group">
                <label>
                  <input
                    type="checkbox"
                    name="hasCashBillReader"
                    checked={formData.hasCashBillReader}
                    onChange={handleInputChange}
                  />
                  Cash Bill Reader
                </label>
              </div>
              <div className="form-group">
                <label>
                  <input
                    type="checkbox"
                    name="hasCashlessPos"
                    checked={formData.hasCashlessPos}
                    onChange={handleInputChange}
                  />
                  Cashless POS
                </label>
              </div>
              <div className="form-group">
                <label>
                  <input
                    type="checkbox"
                    name="hasCoinChanger"
                    checked={formData.hasCoinChanger}
                    onChange={handleInputChange}
                  />
                  Coin Changer
                </label>
              </div>
              <div className="form-group">
                <label>
                  <input
                    type="checkbox"
                    name="active"
                    checked={formData.active}
                    onChange={handleInputChange}
                  />
                  Active
                </label>
              </div>
            </div>

            <h3>Location</h3>
            <div className="form-group">
              <label>Location Name</label>
              <input
                type="text"
                name="location.name"
                value={formData.location.name}
                onChange={handleInputChange}
              />
            </div>

            <div className="form-group">
              <label>Address</label>
              <input
                type="text"
                name="location.address"
                value={formData.location.address}
                onChange={handleInputChange}
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>City</label>
                <input
                  type="text"
                  name="location.city"
                  value={formData.location.city}
                  onChange={handleInputChange}
                />
              </div>
              <div className="form-group">
                <label>Province</label>
                <input
                  type="text"
                  name="location.province"
                  value={formData.location.province}
                  onChange={handleInputChange}
                />
              </div>
              <div className="form-group">
                <label>Postal Code</label>
                <input
                  type="text"
                  name="location.postalCode"
                  value={formData.location.postalCode}
                  onChange={handleInputChange}
                />
              </div>
            </div>

            <button type="submit" className="btn btn-success">
              {editingMachine ? 'Update Machine' : 'Create Machine'}
            </button>
          </form>
        )}

        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Brand</th>
                <th>Model</th>
                <th>Location</th>
                <th>Features</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {machines.map((machine) => (
                <tr key={machine.id}>
                  <td>{machine.brand}</td>
                  <td>{machine.model}</td>
                  <td>
                    {machine.location?.name || 'No location'}
                    <br />
                    <small>{machine.location?.city}, {machine.location?.province}</small>
                  </td>
                  <td>
                    {machine.hasCashBillReader && <span className="badge">Bill Reader</span>}
                    {machine.hasCashlessPos && <span className="badge">POS</span>}
                    {machine.hasCoinChanger && <span className="badge">Coin Changer</span>}
                  </td>
                  <td>
                    <span className={machine.active ? 'badge-success' : 'badge-danger'}>
                      {machine.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="action-buttons">
                    <button
                      className="btn btn-secondary"
                      onClick={() => handleEdit(machine)}
                    >
                      Edit
                    </button>
                    <button
                      className="btn btn-danger"
                      onClick={() => handleDelete(machine.id)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {machines.length === 0 && (
            <div className="empty-state">
              <h3>No vending machines found</h3>
              <p>Click "Add Machine" to create your first vending machine.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default VendingMachines;
