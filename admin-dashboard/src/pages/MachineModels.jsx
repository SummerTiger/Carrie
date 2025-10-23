import { useState, useEffect } from 'react';
import { machineModelsAPI, machineBrandsAPI } from '../services/api';
import './MachineModels.css';

function MachineModels() {
  const [models, setModels] = useState([]);
  const [brands, setBrands] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [editingModel, setEditingModel] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    brandId: '',
    capacity: '',
    dimensions: '',
    weightKg: '',
    powerRequirements: '',
    active: true,
    displayOrder: 0,
  });

  useEffect(() => {
    fetchModels();
    fetchBrands();
  }, []);

  const fetchModels = async () => {
    try {
      setLoading(true);
      const response = await machineModelsAPI.getAll();
      setModels(response.data);
      setError('');
    } catch (err) {
      setError('Failed to fetch machine models');
      console.error(err);
    } finally {
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

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      // Transform formData to match backend expectations
      const payload = {
        ...formData,
        machineBrand: formData.brandId ? { id: formData.brandId } : null,
      };
      // Remove brandId as backend expects machineBrand object
      delete payload.brandId;

      if (editMode && editingModel) {
        await machineModelsAPI.update(editingModel.id, payload);
      } else {
        await machineModelsAPI.create(payload);
      }
      fetchModels();
      closeModal();
    } catch (err) {
      setError('Failed to save machine model');
      console.error(err);
    }
  };

  const handleEdit = (model) => {
    setEditMode(true);
    setEditingModel(model);
    setFormData({
      name: model.name || '',
      description: model.description || '',
      brandId: model.machineBrand?.id || '',
      capacity: model.capacity || '',
      dimensions: model.dimensions || '',
      weightKg: model.weightKg || '',
      powerRequirements: model.powerRequirements || '',
      active: model.active !== undefined ? model.active : true,
      displayOrder: model.displayOrder || 0,
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this machine model?')) {
      try {
        await machineModelsAPI.delete(id);
        fetchModels();
      } catch (err) {
        setError('Failed to delete machine model');
        console.error(err);
      }
    }
  };

  const openModal = () => {
    setEditMode(false);
    setEditingModel(null);
    setFormData({
      name: '',
      description: '',
      brandId: '',
      capacity: '',
      dimensions: '',
      weightKg: '',
      powerRequirements: '',
      active: true,
      displayOrder: 0,
    });
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setEditMode(false);
    setEditingModel(null);
    setFormData({
      name: '',
      description: '',
      brandId: '',
      capacity: '',
      dimensions: '',
      weightKg: '',
      powerRequirements: '',
      active: true,
      displayOrder: 0,
    });
  };

  if (loading) {
    return <div className="loading">Loading machine models...</div>;
  }

  return (
    <div className="machine-models">
      <div className="page-header">
        <h1>Machine Models</h1>
        <button onClick={openModal} className="btn-primary">
          Add Machine Model
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="machine-models-table-container">
        <table className="machine-models-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Brand</th>
              <th>Description</th>
              <th>Capacity</th>
              <th>Display Order</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {models.map((model) => (
              <tr key={model.id}>
                <td>{model.name}</td>
                <td>{model.machineBrand?.name || '-'}</td>
                <td>{model.description || '-'}</td>
                <td>{model.capacity || '-'}</td>
                <td>{model.displayOrder}</td>
                <td>
                  <span className={`status-badge ${model.active ? 'active' : 'inactive'}`}>
                    {model.active ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td>
                  <div className="action-buttons">
                    <button onClick={() => handleEdit(model)} className="btn-edit">
                      Edit
                    </button>
                    <button onClick={() => handleDelete(model.id)} className="btn-delete">
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={closeModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editMode ? 'Edit Machine Model' : 'Add Machine Model'}</h2>
              <button onClick={closeModal} className="close-button">×</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label htmlFor="name">Name *</label>
                <input
                  type="text"
                  id="name"
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="brandId">Brand *</label>
                <select
                  id="brandId"
                  name="brandId"
                  value={formData.brandId}
                  onChange={handleInputChange}
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
                <label htmlFor="description">Description</label>
                <textarea
                  id="description"
                  name="description"
                  value={formData.description}
                  onChange={handleInputChange}
                  rows="3"
                />
              </div>

              <div className="form-group">
                <label htmlFor="capacity">Capacity</label>
                <input
                  type="number"
                  id="capacity"
                  name="capacity"
                  value={formData.capacity}
                  onChange={handleInputChange}
                  min="0"
                />
              </div>

              <div className="form-group">
                <label htmlFor="dimensions">Dimensions</label>
                <input
                  type="text"
                  id="dimensions"
                  name="dimensions"
                  value={formData.dimensions}
                  onChange={handleInputChange}
                  placeholder="e.g., 1800 x 900 x 850 mm"
                />
              </div>

              <div className="form-group">
                <label htmlFor="weightKg">Weight (kg)</label>
                <input
                  type="number"
                  step="0.01"
                  id="weightKg"
                  name="weightKg"
                  value={formData.weightKg}
                  onChange={handleInputChange}
                  min="0"
                />
              </div>

              <div className="form-group">
                <label htmlFor="powerRequirements">Power Requirements</label>
                <input
                  type="text"
                  id="powerRequirements"
                  name="powerRequirements"
                  value={formData.powerRequirements}
                  onChange={handleInputChange}
                  placeholder="e.g., 220V/50Hz"
                />
              </div>

              <div className="form-group">
                <label htmlFor="displayOrder">Display Order</label>
                <input
                  type="number"
                  id="displayOrder"
                  name="displayOrder"
                  value={formData.displayOrder}
                  onChange={handleInputChange}
                  min="0"
                />
              </div>

              <div className="form-group checkbox-group">
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

              <div className="form-actions">
                <button type="button" onClick={closeModal} className="btn-secondary">
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  {editMode ? 'Update' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default MachineModels;
