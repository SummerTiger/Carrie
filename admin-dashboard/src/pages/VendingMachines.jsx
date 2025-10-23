import React, { useState, useEffect } from 'react';
import { machinesAPI, machineBrandsAPI, machineModelsAPI } from '../services/api';

function VendingMachines() {
  const [machines, setMachines] = useState([]);
  const [brands, setBrands] = useState([]);
  const [models, setModels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [viewMode, setViewMode] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [editingMachine, setEditingMachine] = useState(null);
  const [formData, setFormData] = useState({
    machineId: '',
    machineName: '',
    brandId: '',
    modelId: '',
    modelNumber: '',
    serialNumber: '',
    datePurchased: '',
    purchasedPrice: '',
    condition: '',
    deployed: false,
    status: 'ACTIVE',
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
    fetchBrands();
    fetchModels();
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

  const fetchBrands = async () => {
    try {
      const response = await machineBrandsAPI.getActive();
      setBrands(response.data);
    } catch (err) {
      console.error('Failed to fetch machine brands:', err);
    }
  };

  const fetchModels = async () => {
    try {
      const response = await machineModelsAPI.getActive();
      setModels(response.data);
    } catch (err) {
      console.error('Failed to fetch machine models:', err);
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
      if (editMode && editingMachine) {
        await machinesAPI.update(editingMachine.id, formData);
        setEditMode(false);
        setEditingMachine(null);
      } else if (editingMachine) {
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

  const handleView = (machine) => {
    setEditingMachine(machine);
    setFormData({
      machineId: machine.machineId || '',
      machineName: machine.machineName || '',
      brandId: machine.machineBrand?.id || '',
      modelId: machine.machineModel?.id || '',
      modelNumber: machine.modelNumber || '',
      serialNumber: machine.serialNumber || '',
      datePurchased: machine.datePurchased || '',
      purchasedPrice: machine.purchasedPrice || '',
      condition: machine.condition || '',
      deployed: machine.deployed || false,
      status: machine.status || 'ACTIVE',
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
    setViewMode(true);
    setEditMode(false);
    setShowForm(false);
  };

  const handleEditClick = () => {
    setViewMode(false);
    setEditMode(true);
  };

  const handleEdit = (machine) => {
    setEditingMachine(machine);
    setFormData({
      machineId: machine.machineId || '',
      machineName: machine.machineName || '',
      brandId: machine.machineBrand?.id || '',
      modelId: machine.machineModel?.id || '',
      modelNumber: machine.modelNumber || '',
      serialNumber: machine.serialNumber || '',
      datePurchased: machine.datePurchased || '',
      purchasedPrice: machine.purchasedPrice || '',
      condition: machine.condition || '',
      deployed: machine.deployed || false,
      status: machine.status || 'ACTIVE',
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

  const handleCancel = () => {
    setShowForm(false);
    setViewMode(false);
    setEditMode(false);
    setEditingMachine(null);
    resetForm();
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
      machineId: '',
      machineName: '',
      brandId: '',
      modelId: '',
      modelNumber: '',
      serialNumber: '',
      datePurchased: '',
      purchasedPrice: '',
      condition: '',
      deployed: false,
      status: 'ACTIVE',
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
          {!viewMode && !editMode && (
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
          )}
        </div>

        {(showForm || viewMode || editMode) && (
          <form onSubmit={handleSubmit} className="machine-form">
            <h3 style={{ color: viewMode ? '#666' : editMode ? '#2563eb' : '#000' }}>
              {viewMode ? '👁️ View Machine (Read-Only)' : editMode ? '✏️ Edit Machine' : '➕ New Machine'}
            </h3>
            <h3>Machine Details</h3>
            <div className="form-row">
              <div className="form-group">
                <label>Machine ID *</label>
                <input
                  type="text"
                  name="machineId"
                  value={formData.machineId}
                  onChange={handleInputChange}
                  disabled={viewMode}
                  required
                />
              </div>
              <div className="form-group">
                <label>Machine Name</label>
                <input
                  type="text"
                  name="machineName"
                  value={formData.machineName}
                  onChange={handleInputChange}
                  disabled={viewMode}
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Brand *</label>
                <select
                  name="brandId"
                  value={formData.brandId}
                  onChange={handleInputChange}
                  disabled={viewMode}
                  required
                >
                  <option value="">Select a brand</option>
                  {brands.map((brand) => (
                    <option key={brand.id} value={brand.id}>
                      {brand.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Model *</label>
                <select
                  name="modelId"
                  value={formData.modelId}
                  onChange={handleInputChange}
                  disabled={viewMode}
                  required
                >
                  <option value="">Select a model</option>
                  {models.map((model) => (
                    <option key={model.id} value={model.id}>
                      {model.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Model Number</label>
                <input
                  type="text"
                  name="modelNumber"
                  value={formData.modelNumber}
                  onChange={handleInputChange}
                  disabled={viewMode}
                />
              </div>
              <div className="form-group">
                <label>Serial Number</label>
                <input
                  type="text"
                  name="serialNumber"
                  value={formData.serialNumber}
                  onChange={handleInputChange}
                  disabled={viewMode}
                />
              </div>
            </div>

            <h3>Purchase Information</h3>
            <div className="form-row">
              <div className="form-group">
                <label>Date Purchased</label>
                <input
                  type="date"
                  name="datePurchased"
                  value={formData.datePurchased}
                  onChange={handleInputChange}
                  disabled={viewMode}
                />
              </div>
              <div className="form-group">
                <label>Purchased Price</label>
                <input
                  type="number"
                  step="0.01"
                  name="purchasedPrice"
                  value={formData.purchasedPrice}
                  onChange={handleInputChange}
                  disabled={viewMode}
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Condition</label>
                <select
                  name="condition"
                  value={formData.condition}
                  onChange={handleInputChange}
                  disabled={viewMode}
                >
                  <option value="">Select Condition</option>
                  <option value="NEW">New</option>
                  <option value="USED">Used</option>
                  <option value="REFURBISHED">Refurbished</option>
                </select>
              </div>
              <div className="form-group">
                <label>Status</label>
                <select
                  name="status"
                  value={formData.status}
                  onChange={handleInputChange}
                  disabled={viewMode}
                >
                  <option value="ACTIVE">Active</option>
                  <option value="BROKEN">Broken</option>
                  <option value="INACTIVE">Inactive</option>
                  <option value="SOLD">Sold</option>
                </select>
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
                    disabled={viewMode}
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
                    disabled={viewMode}
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
                    disabled={viewMode}
                  />
                  Coin Changer
                </label>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>
                  <input
                    type="checkbox"
                    name="deployed"
                    checked={formData.deployed}
                    onChange={handleInputChange}
                    disabled={viewMode}
                  />
                  Deployed
                </label>
              </div>
              <div className="form-group">
                <label>
                  <input
                    type="checkbox"
                    name="active"
                    checked={formData.active}
                    onChange={handleInputChange}
                    disabled={viewMode}
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
                disabled={viewMode}
              />
            </div>

            <div className="form-group">
              <label>Address</label>
              <input
                type="text"
                name="location.address"
                value={formData.location.address}
                onChange={handleInputChange}
                disabled={viewMode}
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
                  disabled={viewMode}
                />
              </div>
              <div className="form-group">
                <label>Province</label>
                <input
                  type="text"
                  name="location.province"
                  value={formData.location.province}
                  onChange={handleInputChange}
                  disabled={viewMode}
                />
              </div>
              <div className="form-group">
                <label>Postal Code</label>
                <input
                  type="text"
                  name="location.postalCode"
                  value={formData.location.postalCode}
                  onChange={handleInputChange}
                  disabled={viewMode}
                />
              </div>
            </div>

            <div style={{ marginTop: '20px' }}>
              {viewMode ? (
                <>
                  <button type="button" className="btn btn-primary" onClick={handleEditClick}>
                    Edit
                  </button>
                  <button type="button" className="btn btn-secondary" onClick={handleCancel} style={{ marginLeft: '10px' }}>
                    Cancel
                  </button>
                </>
              ) : (
                <>
                  <button type="submit" className="btn btn-success">
                    {editMode ? 'Update Machine' : editingMachine ? 'Update Machine' : 'Create Machine'}
                  </button>
                  <button type="button" className="btn btn-secondary" onClick={handleCancel} style={{ marginLeft: '10px' }}>
                    Cancel
                  </button>
                </>
              )}
            </div>
          </form>
        )}

        {!viewMode && !editMode && !showForm && (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Machine ID</th>
                  <th>Brand / Model</th>
                  <th>Serial #</th>
                  <th>Location</th>
                  <th>Condition</th>
                  <th>Status</th>
                  <th>Deployed</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {machines.map((machine) => (
                  <tr
                    key={machine.id}
                    onDoubleClick={() => handleView(machine)}
                    style={{ cursor: 'pointer' }}
                  >
                  <td>
                    <strong>{machine.machineId}</strong>
                    {machine.machineName && (
                      <>
                        <br />
                        <small>{machine.machineName}</small>
                      </>
                    )}
                  </td>
                  <td>
                    {machine.machineBrand?.name || '-'}
                    <br />
                    <small>{machine.machineModel?.name || '-'}</small>
                  </td>
                  <td>
                    <small>{machine.serialNumber || '-'}</small>
                  </td>
                  <td>
                    {machine.location?.name || machine.location?.city || 'No location'}
                    <br />
                    <small>{machine.location?.city}, {machine.location?.province}</small>
                  </td>
                  <td>
                    {machine.condition ? (
                      <span className="badge">{machine.condition}</span>
                    ) : '-'}
                  </td>
                  <td>
                    <span className={
                      machine.status === 'ACTIVE' ? 'badge-success' :
                      machine.status === 'BROKEN' ? 'badge-danger' :
                      machine.status === 'SOLD' ? 'badge-warning' :
                      'badge-secondary'
                    }>
                      {machine.status}
                    </span>
                  </td>
                  <td>
                    <span className={machine.deployed ? 'badge-success' : 'badge-secondary'}>
                      {machine.deployed ? 'Yes' : 'No'}
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
        )}
      </div>
    </div>
  );
}

export default VendingMachines;
