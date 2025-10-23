import React, { useState, useEffect } from 'react';
import { productsAPI, productCategoriesAPI, productBrandsAPI } from '../services/api';

function Products() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [viewMode, setViewMode] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    categoryId: '',
    brandId: '',
    unitSize: '',
    basePrice: '',
    currentStock: '',
    minimumStock: '',
    hstExempt: false,
    active: true,
    description: '',
    barcode: '',
    sku: '',
  });

  useEffect(() => {
    fetchProducts();
    fetchCategories();
    fetchBrands();
  }, []);

  const fetchProducts = async () => {
    try {
      const response = await productsAPI.getAll();
      setProducts(Array.isArray(response.data) ? response.data : []);
      setLoading(false);
    } catch (err) {
      setError('Failed to fetch products');
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await productCategoriesAPI.getActive();
      setCategories(response.data);
    } catch (err) {
      console.error('Failed to fetch categories:', err);
    }
  };

  const fetchBrands = async () => {
    try {
      const response = await productBrandsAPI.getActive();
      setBrands(response.data);
    } catch (err) {
      console.error('Failed to fetch brands:', err);
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
      if (editMode && editingProduct) {
        await productsAPI.update(editingProduct.id, formData);
        setEditMode(false);
        setEditingProduct(null);
      } else if (editingProduct) {
        await productsAPI.update(editingProduct.id, formData);
      } else {
        await productsAPI.create(formData);
      }
      setShowForm(false);
      setEditingProduct(null);
      resetForm();
      fetchProducts();
    } catch (err) {
      setError('Failed to save product');
    }
  };

  const handleView = (product) => {
    setEditingProduct(product);
    setFormData({
      name: product.name || '',
      categoryId: product.category?.id || '',
      brandId: product.brand?.id || '',
      unitSize: product.unitSize || '',
      basePrice: product.basePrice || '',
      currentStock: product.currentStock || '',
      minimumStock: product.minimumStock || '',
      hstExempt: product.hstExempt || false,
      active: product.active !== undefined ? product.active : true,
      description: product.description || '',
      barcode: product.barcode || '',
      sku: product.sku || '',
    });
    setViewMode(true);
    setEditMode(false);
    setShowForm(false);
  };

  const handleEditClick = () => {
    setViewMode(false);
    setEditMode(true);
  };

  const handleEdit = (product) => {
    setEditingProduct(product);
    setFormData({
      name: product.name || '',
      categoryId: product.category?.id || '',
      brandId: product.brand?.id || '',
      unitSize: product.unitSize || '',
      basePrice: product.basePrice || '',
      currentStock: product.currentStock || '',
      minimumStock: product.minimumStock || '',
      hstExempt: product.hstExempt || false,
      active: product.active !== undefined ? product.active : true,
      description: product.description || '',
      barcode: product.barcode || '',
      sku: product.sku || '',
    });
    setShowForm(true);
  };

  const handleCancel = () => {
    setShowForm(false);
    setViewMode(false);
    setEditMode(false);
    setEditingProduct(null);
    resetForm();
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this product?')) {
      try {
        await productsAPI.delete(id);
        fetchProducts();
      } catch (err) {
        setError('Failed to delete product');
      }
    }
  };

  const resetForm = () => {
    setFormData({
      name: '',
      categoryId: '',
      brandId: '',
      unitSize: '',
      basePrice: '',
      currentStock: '',
      minimumStock: '',
      hstExempt: false,
      active: true,
      description: '',
      barcode: '',
      sku: '',
    });
  };

  if (loading) return <div className="loading">Loading products...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>Products</h1>
        <p>Manage your product inventory</p>
      </div>

      {error && <div className="error">{error}</div>}

      <div className="content-card">
        <div className="card-header">
          <h2>Product List</h2>
          {!viewMode && !editMode && (
            <button
              className="btn btn-primary"
              onClick={() => {
                setShowForm(!showForm);
                setEditingProduct(null);
                resetForm();
              }}
            >
              {showForm ? 'Cancel' : 'Add Product'}
            </button>
          )}
        </div>

        {(showForm || viewMode || editMode) && (
          <form onSubmit={handleSubmit} className="product-form">
            <h3 style={{ color: viewMode ? '#666' : editMode ? '#2563eb' : '#000' }}>
              {viewMode ? '👁️ View Product (Read-Only)' : editMode ? '✏️ Edit Product' : '➕ New Product'}
            </h3>
            <div className="form-row">
              <div className="form-group">
                <label>Name *</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  disabled={viewMode}
                  required
                />
              </div>
              <div className="form-group">
                <label>Category *</label>
                <select
                  name="categoryId"
                  value={formData.categoryId}
                  onChange={handleInputChange}
                  disabled={viewMode}
                  required
                >
                  <option value="">Select a category</option>
                  {categories.map((category) => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Brand</label>
                <select
                  name="brandId"
                  value={formData.brandId}
                  onChange={handleInputChange}
                  disabled={viewMode}
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
                <label>Unit Size</label>
                <input
                  type="text"
                  name="unitSize"
                  value={formData.unitSize}
                  onChange={handleInputChange}
                  disabled={viewMode}
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Base Price</label>
                <input
                  type="number"
                  step="0.01"
                  name="basePrice"
                  value={formData.basePrice}
                  onChange={handleInputChange}
                  disabled={viewMode}
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Current Stock</label>
                <input
                  type="number"
                  name="currentStock"
                  value={formData.currentStock}
                  onChange={handleInputChange}
                  disabled={viewMode}
                />
              </div>
              <div className="form-group">
                <label>Minimum Stock</label>
                <input
                  type="number"
                  name="minimumStock"
                  value={formData.minimumStock}
                  onChange={handleInputChange}
                  disabled={viewMode}
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Barcode</label>
                <input
                  type="text"
                  name="barcode"
                  value={formData.barcode}
                  onChange={handleInputChange}
                  disabled={viewMode}
                />
              </div>
              <div className="form-group">
                <label>SKU</label>
                <input
                  type="text"
                  name="sku"
                  value={formData.sku}
                  onChange={handleInputChange}
                  disabled={viewMode}
                />
              </div>
            </div>

            <div className="form-group">
              <label>Description</label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleInputChange}
                disabled={viewMode}
                rows="3"
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>
                  <input
                    type="checkbox"
                    name="hstExempt"
                    checked={formData.hstExempt}
                    onChange={handleInputChange}
                    disabled={viewMode}
                  />
                  HST Exempt
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
                    {editMode ? 'Update Product' : editingProduct ? 'Update Product' : 'Create Product'}
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
                  <th>Name</th>
                  <th>Category</th>
                  <th>Brand</th>
                  <th>Price</th>
                  <th>Stock</th>
                  <th>HST</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {products.map((product) => (
                  <tr
                    key={product.id}
                    onDoubleClick={() => handleView(product)}
                    style={{ cursor: 'pointer' }}
                  >
                    <td>{product.name}</td>
                  <td>{product.category?.name || '-'}</td>
                  <td>{product.brand?.name || '-'}</td>
                  <td>${product.basePrice?.toFixed(2)}</td>
                  <td>{product.currentStock || 0}</td>
                  <td>{product.hstExempt ? 'Exempt' : 'Taxable'}</td>
                  <td>
                    <span className={product.active ? 'badge-success' : 'badge-danger'}>
                      {product.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="action-buttons">
                    <button
                      className="btn btn-secondary"
                      onClick={() => handleEdit(product)}
                    >
                      Edit
                    </button>
                    <button
                      className="btn btn-danger"
                      onClick={() => handleDelete(product.id)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {products.length === 0 && (
            <div className="empty-state">
              <h3>No products found</h3>
              <p>Click "Add Product" to create your first product.</p>
            </div>
          )}
          </div>
        )}
      </div>
    </div>
  );
}

export default Products;
