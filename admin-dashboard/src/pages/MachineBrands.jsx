import { useState, useEffect } from 'react';
import { machineBrandsAPI } from '../services/api';
import './MachineBrands.css';

function MachineBrands() {
  const [brands, setBrands] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [editingBrand, setEditingBrand] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    logoUrl: '',
    website: '',
    active: true,
    displayOrder: 0,
  });

  useEffect(() => {
    fetchBrands();
  }, []);

  const fetchBrands = async () => {
    try {
      setLoading(true);
      const response = await machineBrandsAPI.getAll();
      setBrands(response.data);
      setError('');
    } catch (err) {
      setError('Failed to fetch machine brands');
      console.error(err);
    } finally {
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

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editMode && editingBrand) {
        await machineBrandsAPI.update(editingBrand.id, formData);
      } else {
        await machineBrandsAPI.create(formData);
      }
      fetchBrands();
      closeModal();
    } catch (err) {
      setError('Failed to save machine brand');
      console.error(err);
    }
  };

  const handleEdit = (brand) => {
    setEditMode(true);
    setEditingBrand(brand);
    setFormData({
      name: brand.name || '',
      description: brand.description || '',
      logoUrl: brand.logoUrl || '',
      website: brand.website || '',
      active: brand.active !== undefined ? brand.active : true,
      displayOrder: brand.displayOrder || 0,
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this machine brand?')) {
      try {
        await machineBrandsAPI.delete(id);
        fetchBrands();
      } catch (err) {
        setError('Failed to delete machine brand');
        console.error(err);
      }
    }
  };

  const openModal = () => {
    setEditMode(false);
    setEditingBrand(null);
    setFormData({
      name: '',
      description: '',
      logoUrl: '',
      website: '',
      active: true,
      displayOrder: 0,
    });
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setEditMode(false);
    setEditingBrand(null);
    setFormData({
      name: '',
      description: '',
      logoUrl: '',
      website: '',
      active: true,
      displayOrder: 0,
    });
  };

  if (loading) {
    return <div className="loading">Loading machine brands...</div>;
  }

  return (
    <div className="machine-brands">
      <div className="page-header">
        <h1>Machine Brands</h1>
        <button onClick={openModal} className="btn-primary">
          Add Machine Brand
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="machine-brands-table-container">
        <table className="machine-brands-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Description</th>
              <th>Website</th>
              <th>Display Order</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {brands.map((brand) => (
              <tr key={brand.id}>
                <td>{brand.name}</td>
                <td>{brand.description || '-'}</td>
                <td>{brand.website ? <a href={brand.website} target="_blank" rel="noopener noreferrer">{brand.website}</a> : '-'}</td>
                <td>{brand.displayOrder}</td>
                <td>
                  <span className={`status-badge ${brand.active ? 'active' : 'inactive'}`}>
                    {brand.active ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td>
                  <div className="action-buttons">
                    <button onClick={() => handleEdit(brand)} className="btn-edit">
                      Edit
                    </button>
                    <button onClick={() => handleDelete(brand.id)} className="btn-delete">
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
              <h2>{editMode ? 'Edit Machine Brand' : 'Add Machine Brand'}</h2>
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
                <label htmlFor="logoUrl">Logo URL</label>
                <input
                  type="url"
                  id="logoUrl"
                  name="logoUrl"
                  value={formData.logoUrl}
                  onChange={handleInputChange}
                  placeholder="https://example.com/logo.png"
                />
              </div>

              <div className="form-group">
                <label htmlFor="website">Website</label>
                <input
                  type="url"
                  id="website"
                  name="website"
                  value={formData.website}
                  onChange={handleInputChange}
                  placeholder="https://example.com"
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

export default MachineBrands;
