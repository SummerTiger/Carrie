import React, { useState, useEffect } from 'react';
import { procurementAPI, productsAPI } from '../services/api';

function Procurement() {
  const [batches, setBatches] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    purchaseDate: new Date().toISOString().split('T')[0],
    supplier: '',
    supplierContact: '',
    invoiceNumber: '',
    notes: '',
    items: [],
  });

  useEffect(() => {
    fetchBatches();
    fetchProducts();
  }, []);

  const fetchBatches = async () => {
    try {
      const response = await procurementAPI.getAll();
      setBatches(Array.isArray(response.data) ? response.data : []);
      setLoading(false);
    } catch (err) {
      setError('Failed to fetch procurement batches');
      setLoading(false);
    }
  };

  const fetchProducts = async () => {
    try {
      const response = await productsAPI.getAll();
      setProducts(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      console.error('Failed to fetch products:', err);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const addItem = () => {
    setFormData({
      ...formData,
      items: [
        ...formData.items,
        {
          productId: '',
          quantity: 1,
          unitCost: 0,
          hstExempt: false,
        },
      ],
    });
  };

  const removeItem = (index) => {
    const newItems = [...formData.items];
    newItems.splice(index, 1);
    setFormData({
      ...formData,
      items: newItems,
    });
  };

  const updateItem = (index, field, value) => {
    const newItems = [...formData.items];
    newItems[index][field] = value;
    setFormData({
      ...formData,
      items: newItems,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await procurementAPI.create(formData);
      setShowForm(false);
      resetForm();
      fetchBatches();
    } catch (err) {
      setError('Failed to create procurement batch');
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this procurement batch?')) {
      try {
        await procurementAPI.delete(id);
        fetchBatches();
      } catch (err) {
        setError('Failed to delete procurement batch');
      }
    }
  };

  const resetForm = () => {
    setFormData({
      purchaseDate: new Date().toISOString().split('T')[0],
      supplier: '',
      supplierContact: '',
      invoiceNumber: '',
      notes: '',
      items: [],
    });
  };

  if (loading) return <div className="loading">Loading procurement batches...</div>;

  return (
    <div>
      <div className="page-header">
        <h1>Procurement</h1>
        <p>Track product purchases and supplier invoices</p>
      </div>

      {error && <div className="error">{error}</div>}

      <div className="content-card">
        <div className="card-header">
          <h2>Purchase Orders</h2>
          <button
            className="btn btn-primary"
            onClick={() => {
              setShowForm(!showForm);
              resetForm();
            }}
          >
            {showForm ? 'Cancel' : 'New Purchase'}
          </button>
        </div>

        {showForm && (
          <form onSubmit={handleSubmit} className="machine-form">
            <h3>Purchase Details</h3>
            <div className="form-row">
              <div className="form-group">
                <label>Purchase Date *</label>
                <input
                  type="date"
                  name="purchaseDate"
                  value={formData.purchaseDate}
                  onChange={handleInputChange}
                  required
                />
              </div>
              <div className="form-group">
                <label>Invoice Number</label>
                <input
                  type="text"
                  name="invoiceNumber"
                  value={formData.invoiceNumber}
                  onChange={handleInputChange}
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Supplier *</label>
                <input
                  type="text"
                  name="supplier"
                  value={formData.supplier}
                  onChange={handleInputChange}
                  required
                />
              </div>
              <div className="form-group">
                <label>Supplier Contact</label>
                <input
                  type="text"
                  name="supplierContact"
                  value={formData.supplierContact}
                  onChange={handleInputChange}
                />
              </div>
            </div>

            <div className="form-group">
              <label>Notes</label>
              <textarea
                name="notes"
                value={formData.notes}
                onChange={handleInputChange}
                rows="3"
              />
            </div>

            <h3>Items</h3>
            {formData.items.map((item, index) => (
              <div key={index} className="form-row" style={{ alignItems: 'flex-end', borderBottom: '1px solid #eee', paddingBottom: '10px', marginBottom: '10px' }}>
                <div className="form-group">
                  <label>Product *</label>
                  <select
                    value={item.productId}
                    onChange={(e) => updateItem(index, 'productId', e.target.value)}
                    required
                  >
                    <option value="">Select Product</option>
                    {products.map((p) => (
                      <option key={p.id} value={p.id}>{p.name}</option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label>Quantity *</label>
                  <input
                    type="number"
                    value={item.quantity}
                    onChange={(e) => updateItem(index, 'quantity', parseInt(e.target.value))}
                    min="1"
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Unit Cost *</label>
                  <input
                    type="number"
                    step="0.01"
                    value={item.unitCost}
                    onChange={(e) => updateItem(index, 'unitCost', parseFloat(e.target.value))}
                    min="0.01"
                    required
                  />
                </div>
                <div className="form-group">
                  <label>
                    <input
                      type="checkbox"
                      checked={item.hstExempt}
                      onChange={(e) => updateItem(index, 'hstExempt', e.target.checked)}
                    />
                    HST Exempt
                  </label>
                </div>
                <button
                  type="button"
                  className="btn btn-danger"
                  onClick={() => removeItem(index)}
                >
                  Remove
                </button>
              </div>
            ))}

            <button type="button" className="btn btn-secondary" onClick={addItem}>
              Add Item
            </button>

            <button type="submit" className="btn btn-success" style={{ marginLeft: '10px' }}>
              Create Purchase Order
            </button>
          </form>
        )}

        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Supplier</th>
                <th>Invoice #</th>
                <th>Items</th>
                <th>Subtotal</th>
                <th>HST</th>
                <th>Total</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {batches.map((batch) => (
                <tr key={batch.id}>
                  <td>{new Date(batch.purchaseDate).toLocaleDateString()}</td>
                  <td>
                    <strong>{batch.supplier}</strong>
                    {batch.supplierContact && (
                      <>
                        <br />
                        <small>{batch.supplierContact}</small>
                      </>
                    )}
                  </td>
                  <td>{batch.invoiceNumber || '-'}</td>
                  <td>{batch.totalItemsCount || 0} items</td>
                  <td>${batch.subtotal?.toFixed(2) || '0.00'}</td>
                  <td>${batch.totalHst?.toFixed(2) || '0.00'}</td>
                  <td><strong>${batch.totalAmount?.toFixed(2) || '0.00'}</strong></td>
                  <td className="action-buttons">
                    <button
                      className="btn btn-danger"
                      onClick={() => handleDelete(batch.id)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {batches.length === 0 && (
            <div className="empty-state">
              <h3>No purchase orders found</h3>
              <p>Click "New Purchase" to create your first purchase order.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default Procurement;
