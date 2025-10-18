import React, { useState, useEffect } from 'react';
import { procurementAPI, productsAPI } from '../services/api';

// Security: Use environment variable for API URL
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

function Procurement() {
  const [batches, setBatches] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [viewMode, setViewMode] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [selectedBatch, setSelectedBatch] = useState(null);
  const [formData, setFormData] = useState({
    purchaseDate: new Date().toISOString().slice(0, 16), // YYYY-MM-DDTHH:MM format
    supplier: '',
    supplierContact: '',
    invoiceNumber: '',
    notes: '',
    items: [],
    receiptImages: [],
  });
  const [uploadingImage, setUploadingImage] = useState(false);

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
          packQuantity: 1,
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

  // Calculate unit price: pack cost / units per pack
  const calculateUnitPrice = (item) => {
    const unitCost = parseFloat(item.unitCost) || 0;
    const packQuantity = parseInt(item.packQuantity) || 1;
    return unitCost / packQuantity;
  };

  // Calculate item subtotal: pack cost * number of packs
  const calculateItemSubtotal = (item) => {
    const unitCost = parseFloat(item.unitCost) || 0;
    const quantity = parseInt(item.quantity) || 0;
    return unitCost * quantity;
  };

  // Calculate HST for an item
  const calculateItemHST = (item) => {
    if (item.hstExempt) return 0;
    const subtotal = calculateItemSubtotal(item);
    return subtotal * 0.13;
  };

  // Calculate item total with HST
  const calculateItemTotal = (item) => {
    return calculateItemSubtotal(item) + calculateItemHST(item);
  };

  // Calculate order totals
  const calculateOrderTotals = () => {
    const subtotal = formData.items.reduce((sum, item) => sum + calculateItemSubtotal(item), 0);
    const totalHST = formData.items.reduce((sum, item) => sum + calculateItemHST(item), 0);
    const total = subtotal + totalHST;
    return { subtotal, totalHST, total };
  };

  const handleView = async (batch) => {
    setSelectedBatch(batch);
    setFormData({
      purchaseDate: batch.purchaseDate.slice(0, 16),
      supplier: batch.supplier,
      supplierContact: batch.supplierContact || '',
      invoiceNumber: batch.invoiceNumber || '',
      notes: batch.notes || '',
      items: batch.items.map(item => ({
        productId: item.productId,
        productName: item.productName,
        quantity: item.quantity,
        packQuantity: item.packQuantity,
        unitCost: item.unitCost,
        hstExempt: item.hstExempt,
      })),
      receiptImages: batch.receiptImages || [],
    });
    setViewMode(true);
    setEditMode(false);
    setShowForm(false);
  };

  const handleEdit = () => {
    console.log('handleEdit called - viewMode:', viewMode, 'editMode:', editMode);
    setViewMode(false);
    setEditMode(true);
    console.log('After state update - viewMode should be false, editMode should be true');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editMode && selectedBatch) {
        await procurementAPI.update(selectedBatch.id, formData);
        setEditMode(false);
        setSelectedBatch(null);
      } else {
        await procurementAPI.create(formData);
        setShowForm(false);
      }
      resetForm();
      fetchBatches();
    } catch (err) {
      setError(editMode ? 'Failed to update procurement batch' : 'Failed to create procurement batch');
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

  const handleImageUpload = async (event) => {
    const files = Array.from(event.target.files);
    if (formData.receiptImages.length + files.length > 4) {
      setError('Maximum 4 receipt images allowed');
      return;
    }

    setUploadingImage(true);
    try {
      const uploadPromises = files.map(async (file, index) => {
        const fileFormData = new FormData();
        fileFormData.append('file', file);
        fileFormData.append('category', 'receipt');

        const response = await fetch(`${API_BASE_URL}/files/upload`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
          },
          body: fileFormData,
        });

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(`Upload failed: ${errorText}`);
        }

        const data = await response.json();
        return {
          imageUrl: data.fileUrl,
          imageName: data.fileName,
          contentType: data.contentType,
          fileSize: parseInt(data.size),
          imageOrder: formData.receiptImages.length + index
        };
      });

      const uploadedImages = await Promise.all(uploadPromises);
      setFormData(prev => ({
        ...prev,
        receiptImages: [...prev.receiptImages, ...uploadedImages]
      }));
      setError('');
    } catch (err) {
      console.error('Image upload error:', err);
      setError('Failed to upload images: ' + err.message);
    } finally {
      setUploadingImage(false);
    }
  };

  const removeImage = async (index) => {
    const image = formData.receiptImages[index];
    try {
      // Delete from server
      await fetch(`${API_BASE_URL.replace('/api', '')}${image.imageUrl}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      });

      // Remove from state
      const newImages = formData.receiptImages.filter((_, i) => i !== index);
      setFormData({
        ...formData,
        receiptImages: newImages.map((img, i) => ({ ...img, imageOrder: i }))
      });
    } catch (err) {
      setError('Failed to delete image');
    }
  };

  const handleCancel = () => {
    setShowForm(false);
    setViewMode(false);
    setEditMode(false);
    setSelectedBatch(null);
    resetForm();
  };

  const resetForm = () => {
    setFormData({
      purchaseDate: new Date().toISOString().slice(0, 16),
      supplier: '',
      supplierContact: '',
      invoiceNumber: '',
      notes: '',
      items: [],
      receiptImages: [],
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
          {!viewMode && !editMode && (
            <button
              className="btn btn-primary"
              onClick={() => {
                setShowForm(!showForm);
                resetForm();
              }}
            >
              {showForm ? 'Cancel' : 'New Purchase'}
            </button>
          )}
        </div>

        {(showForm || viewMode || editMode) && (
          <form onSubmit={handleSubmit} className="machine-form">
            <h3 style={{ color: viewMode ? '#666' : editMode ? '#2563eb' : '#000' }}>
              {viewMode ? '👁️ View Purchase Order (Read-Only)' : editMode ? '✏️ Edit Purchase Order' : '➕ New Purchase Order'}
            </h3>
            <div className="form-row">
              <div className="form-group">
                <label>Purchase Date *</label>
                <input
                  type="datetime-local"
                  name="purchaseDate"
                  value={formData.purchaseDate}
                  onChange={handleInputChange}
                  disabled={viewMode}
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
                  disabled={viewMode}
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
                  disabled={viewMode}
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
                  disabled={viewMode}
                />
              </div>
            </div>

            <div className="form-group">
              <label>Notes</label>
              <textarea
                name="notes"
                value={formData.notes}
                onChange={handleInputChange}
                disabled={viewMode}
                rows="3"
              />
            </div>

            <h3>Items</h3>
            {formData.items.map((item, index) => (
              <div key={index} style={{ borderBottom: '1px solid #eee', paddingBottom: '15px', marginBottom: '15px' }}>
                <div className="form-row" style={{ alignItems: 'flex-end' }}>
                  <div className="form-group">
                    <label>Product *</label>
                    {viewMode ? (
                      <input
                        type="text"
                        value={item.productName || ''}
                        disabled
                      />
                    ) : (
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
                    )}
                  </div>
                  <div className="form-group">
                    <label>Packs *</label>
                    <input
                      type="number"
                      value={item.quantity}
                      onChange={(e) => updateItem(index, 'quantity', parseInt(e.target.value))}
                      disabled={viewMode}
                      min="1"
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Units/Pack *</label>
                    <input
                      type="number"
                      value={item.packQuantity}
                      onChange={(e) => updateItem(index, 'packQuantity', parseInt(e.target.value))}
                      disabled={viewMode}
                      min="1"
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Pack Cost *</label>
                    <input
                      type="number"
                      step="0.01"
                      value={item.unitCost}
                      onChange={(e) => updateItem(index, 'unitCost', parseFloat(e.target.value))}
                      disabled={viewMode}
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
                        disabled={viewMode}
                      />
                      HST Exempt
                    </label>
                  </div>
                  {!viewMode && (
                    <button
                      type="button"
                      className="btn btn-danger"
                      onClick={() => removeItem(index)}
                    >
                      Remove
                    </button>
                  )}
                </div>
                {(item.unitCost && item.quantity && item.packQuantity) && (
                  <div style={{ marginTop: '10px', padding: '10px', backgroundColor: '#f5f5f5', borderRadius: '4px', fontSize: '14px' }}>
                    <strong>Calculations:</strong>
                    <span style={{ marginLeft: '15px' }}>Unit Price: ${calculateUnitPrice(item).toFixed(4)}</span>
                    <span style={{ marginLeft: '15px' }}>Subtotal: ${calculateItemSubtotal(item).toFixed(2)}</span>
                    <span style={{ marginLeft: '15px' }}>HST (13%): ${calculateItemHST(item).toFixed(2)}</span>
                    <span style={{ marginLeft: '15px' }}><strong>Total: ${calculateItemTotal(item).toFixed(2)}</strong></span>
                  </div>
                )}
              </div>
            ))}

            {!viewMode && (
              <button type="button" className="btn btn-secondary" onClick={addItem}>
                Add Item
              </button>
            )}

            {formData.items.length > 0 && (
              <div style={{ marginTop: '20px', padding: '15px', backgroundColor: '#e8f5e9', borderRadius: '4px', border: '1px solid #4caf50' }}>
                <h4 style={{ marginTop: 0, marginBottom: '10px' }}>Order Totals</h4>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                  <span>Subtotal:</span>
                  <strong>${calculateOrderTotals().subtotal.toFixed(2)}</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
                  <span>HST (13%):</span>
                  <strong>${calculateOrderTotals().totalHST.toFixed(2)}</strong>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '2px solid #4caf50', paddingTop: '10px', marginTop: '10px', fontSize: '18px' }}>
                  <span><strong>Total:</strong></span>
                  <strong style={{ color: '#2e7d32' }}>${calculateOrderTotals().total.toFixed(2)}</strong>
                </div>
              </div>
            )}

            <div style={{ marginTop: '20px', padding: '15px', backgroundColor: '#f5f5f5', borderRadius: '4px' }}>
              <h3 style={{ marginTop: 0 }}>Receipt Images {!viewMode && '(Max 4)'}</h3>

              {formData.receiptImages.length > 0 && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(150px, 1fr))', gap: '15px', marginBottom: '15px' }}>
                  {formData.receiptImages.map((image, index) => (
                    <div key={index} style={{ position: 'relative', border: '1px solid #ddd', borderRadius: '4px', overflow: 'hidden' }}>
                      <img
                        src={`${API_BASE_URL.replace('/api', '')}${image.imageUrl}`}
                        alt={`Receipt ${index + 1}`}
                        style={{ width: '100%', height: '150px', objectFit: 'cover' }}
                      />
                      {!viewMode && (
                        <button
                          type="button"
                          onClick={() => removeImage(index)}
                          style={{
                            position: 'absolute',
                            top: '5px',
                            right: '5px',
                            backgroundColor: '#f44336',
                            color: 'white',
                            border: 'none',
                            borderRadius: '50%',
                            width: '25px',
                            height: '25px',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: '16px'
                          }}
                        >
                          ×
                        </button>
                      )}
                    </div>
                  ))}
                </div>
              )}

              {!viewMode && formData.receiptImages.length < 4 && (
                <div>
                  <input
                    type="file"
                    accept="image/*"
                    multiple
                    onChange={handleImageUpload}
                    disabled={uploadingImage}
                    style={{ display: 'none' }}
                    id="receipt-upload"
                  />
                  <label htmlFor="receipt-upload">
                    <button
                      type="button"
                      className="btn btn-secondary"
                      onClick={() => document.getElementById('receipt-upload').click()}
                      disabled={uploadingImage}
                    >
                      {uploadingImage ? 'Uploading...' : '📷 Upload Receipt Images'}
                    </button>
                  </label>
                  <small style={{ display: 'block', marginTop: '8px', color: '#666' }}>
                    {formData.receiptImages.length}/4 images uploaded
                  </small>
                </div>
              )}

              {viewMode && formData.receiptImages.length === 0 && (
                <p style={{ color: '#999', fontStyle: 'italic' }}>No receipt images uploaded</p>
              )}
            </div>

            <div style={{ marginTop: '20px' }}>
              {viewMode ? (
                <>
                  <button type="button" className="btn btn-primary" onClick={handleEdit}>
                    Edit
                  </button>
                  <button type="button" className="btn btn-secondary" onClick={handleCancel} style={{ marginLeft: '10px' }}>
                    Cancel
                  </button>
                </>
              ) : (
                <>
                  <button type="submit" className="btn btn-success">
                    {editMode ? 'Update Purchase Order' : 'Create Purchase Order'}
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
                  <tr
                    key={batch.id}
                    onDoubleClick={() => handleView(batch)}
                    style={{ cursor: 'pointer' }}
                  >
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
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete(batch.id);
                        }}
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
        )}
      </div>
    </div>
  );
}

export default Procurement;
