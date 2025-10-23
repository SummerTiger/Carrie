import { useState, useEffect } from 'react';
import { productCategoriesAPI } from '../services/api';
import './ProductCategories.css';

function ProductCategories() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [editingCategory, setEditingCategory] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    icon: '',
    active: true,
    displayOrder: 0,
  });

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      setLoading(true);
      const response = await productCategoriesAPI.getAll();
      setCategories(response.data);
      setError('');
    } catch (err) {
      setError('Failed to fetch categories');
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
      if (editMode && editingCategory) {
        await productCategoriesAPI.update(editingCategory.id, formData);
      } else {
        await productCategoriesAPI.create(formData);
      }
      fetchCategories();
      closeModal();
    } catch (err) {
      setError('Failed to save category');
      console.error(err);
    }
  };

  const handleEdit = (category) => {
    setEditMode(true);
    setEditingCategory(category);
    setFormData({
      name: category.name || '',
      description: category.description || '',
      icon: category.icon || '',
      active: category.active !== undefined ? category.active : true,
      displayOrder: category.displayOrder || 0,
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this category?')) {
      try {
        await productCategoriesAPI.delete(id);
        fetchCategories();
      } catch (err) {
        setError('Failed to delete category');
        console.error(err);
      }
    }
  };

  const openModal = () => {
    setEditMode(false);
    setEditingCategory(null);
    setFormData({
      name: '',
      description: '',
      icon: '',
      active: true,
      displayOrder: 0,
    });
    setShowModal(true);
  };

  const closeModal = () => {
    setShowModal(false);
    setEditMode(false);
    setEditingCategory(null);
    setFormData({
      name: '',
      description: '',
      icon: '',
      active: true,
      displayOrder: 0,
    });
  };

  if (loading) {
    return <div className="loading">Loading categories...</div>;
  }

  return (
    <div className="product-categories">
      <div className="page-header">
        <h1>Product Categories</h1>
        <button onClick={openModal} className="btn-primary">
          Add Category
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="categories-table-container">
        <table className="categories-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Description</th>
              <th>Icon</th>
              <th>Display Order</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {categories.map((category) => (
              <tr key={category.id}>
                <td>{category.name}</td>
                <td>{category.description || '-'}</td>
                <td>{category.icon || '-'}</td>
                <td>{category.displayOrder}</td>
                <td>
                  <span className={`status-badge ${category.active ? 'active' : 'inactive'}`}>
                    {category.active ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td>
                  <div className="action-buttons">
                    <button onClick={() => handleEdit(category)} className="btn-edit">
                      Edit
                    </button>
                    <button onClick={() => handleDelete(category.id)} className="btn-delete">
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
              <h2>{editMode ? 'Edit Category' : 'Add Category'}</h2>
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
                <label htmlFor="icon">Icon</label>
                <input
                  type="text"
                  id="icon"
                  name="icon"
                  value={formData.icon}
                  onChange={handleInputChange}
                  placeholder="e.g., 🍫, 🥤, 🍪"
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

export default ProductCategories;
