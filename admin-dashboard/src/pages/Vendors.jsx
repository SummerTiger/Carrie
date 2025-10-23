import { useState, useEffect } from 'react';
import { vendorsAPI } from '../services/api';
import './Vendors.css';

function Vendors() {
  const [vendors, setVendors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterActive, setFilterActive] = useState('all');
  const [showModal, setShowModal] = useState(false);
  const [editingVendor, setEditingVendor] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    companyName: '',
    contactPerson: '',
    email: '',
    phoneNumber: '',
    mobileNumber: '',
    faxNumber: '',
    website: '',
    addressLine1: '',
    addressLine2: '',
    city: 'Toronto',
    province: 'ON',
    postalCode: '',
    country: 'Canada',
    customerIdWithVendor: '',
    businessNumber: '',
    taxId: '',
    discountRate: 0,
    description: '',
    notes: '',
    active: true,
    preferred: false,
    rating: 0,
    orderDeliver: false,
    curbsidePickup: false,
    inPersonOnly: false,
  });

  useEffect(() => {
    fetchVendors();
  }, []);

  const fetchVendors = async () => {
    try {
      setLoading(true);
      const response = await vendorsAPI.getAll();
      setVendors(response.data);
      setError(null);
    } catch (err) {
      setError('Failed to fetch vendors');
      console.error('Error fetching vendors:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchQuery.trim()) {
      fetchVendors();
      return;
    }
    try {
      const response = await vendorsAPI.search(searchQuery);
      setVendors(response.data);
    } catch (err) {
      console.error('Error searching vendors:', err);
    }
  };

  const handleCreate = () => {
    setEditingVendor(null);
    setFormData({
      name: '',
      companyName: '',
      contactPerson: '',
      email: '',
      phoneNumber: '',
      mobileNumber: '',
      faxNumber: '',
      website: '',
      addressLine1: '',
      addressLine2: '',
      city: 'Toronto',
      province: 'ON',
      postalCode: '',
      country: 'Canada',
      customerIdWithVendor: '',
      businessNumber: '',
      taxId: '',
      discountRate: 0,
      description: '',
      notes: '',
      active: true,
      preferred: false,
      rating: 0,
      orderDeliver: false,
      curbsidePickup: false,
      inPersonOnly: false,
    });
    setShowModal(true);
  };

  const handleEdit = (vendor) => {
    setEditingVendor(vendor);
    setFormData({
      name: vendor.name || '',
      companyName: vendor.companyName || '',
      contactPerson: vendor.contactPerson || '',
      email: vendor.email || '',
      phoneNumber: vendor.phoneNumber || '',
      mobileNumber: vendor.mobileNumber || '',
      faxNumber: vendor.faxNumber || '',
      website: vendor.website || '',
      addressLine1: vendor.addressLine1 || '',
      addressLine2: vendor.addressLine2 || '',
      city: vendor.city || '',
      province: vendor.province || '',
      postalCode: vendor.postalCode || '',
      country: vendor.country || 'Canada',
      customerIdWithVendor: vendor.customerIdWithVendor || '',
      businessNumber: vendor.businessNumber || '',
      taxId: vendor.taxId || '',
      discountRate: vendor.discountRate || 0,
      description: vendor.description || '',
      notes: vendor.notes || '',
      active: vendor.active !== undefined ? vendor.active : true,
      preferred: vendor.preferred || false,
      rating: vendor.rating || 0,
      orderDeliver: vendor.orderDeliver || false,
      curbsidePickup: vendor.curbsidePickup || false,
      inPersonOnly: vendor.inPersonOnly || false,
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this vendor?')) {
      try {
        await vendorsAPI.delete(id);
        fetchVendors();
      } catch (err) {
        alert('Failed to delete vendor');
        console.error('Error deleting vendor:', err);
      }
    }
  };

  const handleToggleStatus = async (id) => {
    try {
      await vendorsAPI.toggleStatus(id);
      fetchVendors();
    } catch (err) {
      alert('Failed to toggle vendor status');
      console.error('Error toggling status:', err);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingVendor) {
        await vendorsAPI.update(editingVendor.id, formData);
      } else {
        await vendorsAPI.create(formData);
      }
      setShowModal(false);
      fetchVendors();
    } catch (err) {
      alert('Failed to save vendor');
      console.error('Error saving vendor:', err);
    }
  };

  // Format phone number to (XXX) XXX-XXXX
  const formatPhoneNumber = (value) => {
    if (!value) return value;
    const phoneNumber = value.replace(/[^\d]/g, '');
    const phoneNumberLength = phoneNumber.length;

    if (phoneNumberLength < 4) return phoneNumber;
    if (phoneNumberLength < 7) {
      return `(${phoneNumber.slice(0, 3)}) ${phoneNumber.slice(3)}`;
    }
    return `(${phoneNumber.slice(0, 3)}) ${phoneNumber.slice(3, 6)}-${phoneNumber.slice(6, 10)}`;
  };

  // Validate Canadian postal code format (A1A 1A1)
  const validatePostalCode = (value) => {
    if (!value) return true;
    const postalCodeRegex = /^[A-Za-z]\d[A-Za-z][ -]?\d[A-Za-z]\d$/;
    return postalCodeRegex.test(value);
  };

  // Format postal code to A1A 1A1
  const formatPostalCode = (value) => {
    if (!value) return value;
    const cleaned = value.replace(/[^A-Za-z0-9]/g, '').toUpperCase();
    if (cleaned.length <= 3) return cleaned;
    return `${cleaned.slice(0, 3)} ${cleaned.slice(3, 6)}`;
  };

  // Validate HST ID: at least 9 digits and ends with RT or RT001
  const validateHstId = (value) => {
    if (!value) return true;
    const digitCount = (value.match(/\d/g) || []).length;
    if (digitCount < 9) return false;
    return value.endsWith('RT') || value.endsWith('RT001');
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    let newValue = type === 'checkbox' ? checked : value;

    // Format phone numbers
    if (name === 'phoneNumber' || name === 'mobileNumber' || name === 'faxNumber') {
      newValue = formatPhoneNumber(value);
    }

    // Format postal code
    if (name === 'postalCode') {
      newValue = formatPostalCode(value);
    }

    setFormData({
      ...formData,
      [name]: newValue,
    });
  };

  const filteredVendors = vendors.filter((vendor) => {
    if (filterActive === 'active') return vendor.active;
    if (filterActive === 'inactive') return !vendor.active;
    if (filterActive === 'preferred') return vendor.preferred;
    return true;
  });

  if (loading) {
    return <div className="loading">Loading vendors...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  return (
    <div className="vendors-page">
      <div className="page-header">
        <h1>Vendor of Records</h1>
        <button className="btn-primary" onClick={handleCreate}>
          Add New Vendor
        </button>
      </div>

      <div className="filters-section">
        <div className="search-bar">
          <input
            type="text"
            placeholder="Search vendors..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
          />
          <button onClick={handleSearch}>Search</button>
          <button onClick={fetchVendors}>Clear</button>
        </div>

        <div className="filter-buttons">
          <button
            className={filterActive === 'all' ? 'active' : ''}
            onClick={() => setFilterActive('all')}
          >
            All
          </button>
          <button
            className={filterActive === 'active' ? 'active' : ''}
            onClick={() => setFilterActive('active')}
          >
            Active
          </button>
          <button
            className={filterActive === 'inactive' ? 'active' : ''}
            onClick={() => setFilterActive('inactive')}
          >
            Inactive
          </button>
          <button
            className={filterActive === 'preferred' ? 'active' : ''}
            onClick={() => setFilterActive('preferred')}
          >
            Preferred
          </button>
        </div>
      </div>

      <div className="vendors-table-container">
        <table className="vendors-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Phone</th>
              <th>City</th>
              <th>Customer ID</th>
              <th>Total Purchases</th>
              <th>Total Spent</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredVendors.map((vendor) => (
              <tr key={vendor.id}>
                <td>
                  {vendor.name}
                  {vendor.preferred && <span className="badge-preferred">★ Preferred</span>}
                </td>
                <td>{vendor.phoneNumber}</td>
                <td>{vendor.city}</td>
                <td>{vendor.customerIdWithVendor}</td>
                <td>{vendor.totalPurchases || 0}</td>
                <td>${vendor.totalSpent ? vendor.totalSpent.toFixed(2) : '0.00'}</td>
                <td>
                  <span className={`status-badge ${vendor.active ? 'active' : 'inactive'}`}>
                    {vendor.active ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td className="actions-cell">
                  <button className="btn-small" onClick={() => handleEdit(vendor)}>
                    Edit
                  </button>
                  <button
                    className="btn-small btn-toggle"
                    onClick={() => handleToggleStatus(vendor.id)}
                  >
                    {vendor.active ? 'Deactivate' : 'Activate'}
                  </button>
                  <button
                    className="btn-small btn-danger"
                    onClick={() => handleDelete(vendor.id)}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editingVendor ? 'Edit Vendor' : 'Create New Vendor'}</h2>
              <button className="close-btn" onClick={() => setShowModal(false)}>
                ×
              </button>
            </div>
            <form onSubmit={handleSubmit} className="vendor-form">
              <div className="form-sections">
                {/* Basic Information */}
                <div className="form-section">
                  <h3>Basic Information</h3>
                  <div className="form-grid">
                    <div className="form-group full-width">
                      <label>Name *</label>
                      <input
                        type="text"
                        name="name"
                        value={formData.name}
                        onChange={handleInputChange}
                        required
                      />
                    </div>
                    <div className="form-group full-width">
                      <label>Company Name</label>
                      <input
                        type="text"
                        name="companyName"
                        value={formData.companyName}
                        onChange={handleInputChange}
                      />
                    </div>
                    <div className="form-group">
                      <label>Contact Person</label>
                      <input
                        type="text"
                        name="contactPerson"
                        value={formData.contactPerson}
                        onChange={handleInputChange}
                      />
                    </div>
                    <div className="form-group">
                      <label>Customer ID with Vendor</label>
                      <input
                        type="text"
                        name="customerIdWithVendor"
                        value={formData.customerIdWithVendor}
                        onChange={handleInputChange}
                      />
                    </div>
                  </div>
                </div>

                {/* Contact Information */}
                <div className="form-section">
                  <h3>Contact Information</h3>
                  <div className="form-grid">
                    <div className="form-group">
                      <label>Email</label>
                      <input
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleInputChange}
                      />
                    </div>
                    <div className="form-group">
                      <label>Phone Number</label>
                      <input
                        type="text"
                        name="phoneNumber"
                        value={formData.phoneNumber}
                        onChange={handleInputChange}
                      />
                    </div>
                    <div className="form-group">
                      <label>Mobile Number</label>
                      <input
                        type="text"
                        name="mobileNumber"
                        value={formData.mobileNumber}
                        onChange={handleInputChange}
                      />
                    </div>
                    <div className="form-group">
                      <label>Fax Number</label>
                      <input
                        type="text"
                        name="faxNumber"
                        value={formData.faxNumber}
                        onChange={handleInputChange}
                      />
                    </div>
                    <div className="form-group full-width">
                      <label>Website</label>
                      <input
                        type="text"
                        name="website"
                        value={formData.website}
                        onChange={handleInputChange}
                      />
                    </div>
                  </div>
                </div>

                {/* Address */}
                <div className="form-section">
                  <h3>Address</h3>
                  <div className="form-grid">
                    <div className="form-group full-width">
                      <label>Address Line 1</label>
                      <input
                        type="text"
                        name="addressLine1"
                        value={formData.addressLine1}
                        onChange={handleInputChange}
                      />
                    </div>
                    <div className="form-group full-width">
                      <label>Address Line 2</label>
                      <input
                        type="text"
                        name="addressLine2"
                        value={formData.addressLine2}
                        onChange={handleInputChange}
                      />
                    </div>
                    <div className="form-group">
                      <label>City</label>
                      <input
                        type="text"
                        name="city"
                        value={formData.city}
                        onChange={handleInputChange}
                      />
                    </div>
                    <div className="form-group">
                      <label>Province</label>
                      <select
                        name="province"
                        value={formData.province}
                        onChange={handleInputChange}
                      >
                        <option value="">Select Province</option>
                        <option value="AB">Alberta</option>
                        <option value="BC">British Columbia</option>
                        <option value="MB">Manitoba</option>
                        <option value="NB">New Brunswick</option>
                        <option value="NL">Newfoundland and Labrador</option>
                        <option value="NS">Nova Scotia</option>
                        <option value="NT">Northwest Territories</option>
                        <option value="NU">Nunavut</option>
                        <option value="ON">Ontario</option>
                        <option value="PE">Prince Edward Island</option>
                        <option value="QC">Quebec</option>
                        <option value="SK">Saskatchewan</option>
                        <option value="YT">Yukon</option>
                      </select>
                    </div>
                    <div className="form-group">
                      <label>Postal Code</label>
                      <input
                        type="text"
                        name="postalCode"
                        value={formData.postalCode}
                        onChange={handleInputChange}
                        placeholder="A1A 1A1"
                        maxLength="7"
                        style={{ borderColor: formData.postalCode && !validatePostalCode(formData.postalCode) ? 'red' : '' }}
                      />
                      {formData.postalCode && !validatePostalCode(formData.postalCode) && (
                        <small style={{ color: 'red' }}>Invalid postal code format (A1A 1A1)</small>
                      )}
                    </div>
                    <div className="form-group">
                      <label>Country</label>
                      <input
                        type="text"
                        name="country"
                        value={formData.country}
                        onChange={handleInputChange}
                      />
                    </div>
                  </div>
                </div>

                {/* Business Details */}
                <div className="form-section">
                  <h3>Business Details</h3>
                  <div className="form-grid">
                    <div className="form-group">
                      <label>Business Number</label>
                      <input
                        type="text"
                        name="businessNumber"
                        value={formData.businessNumber}
                        onChange={handleInputChange}
                      />
                    </div>
                    <div className="form-group">
                      <label>HST ID</label>
                      <input
                        type="text"
                        name="taxId"
                        value={formData.taxId}
                        onChange={handleInputChange}
                        style={{ borderColor: formData.taxId && !validateHstId(formData.taxId) ? 'red' : '' }}
                      />
                      {formData.taxId && !validateHstId(formData.taxId) && (
                        <small style={{ color: 'red' }}>HST ID must have at least 9 digits and end with RT or RT001</small>
                      )}
                    </div>
                    <div className="form-group">
                      <label>Discount Rate (%)</label>
                      <input
                        type="number"
                        step="0.01"
                        name="discountRate"
                        value={formData.discountRate}
                        onChange={handleInputChange}
                      />
                    </div>
                    <div className="form-group">
                      <label>Rating (0-5)</label>
                      <input
                        type="number"
                        min="0"
                        max="5"
                        name="rating"
                        value={formData.rating}
                        onChange={handleInputChange}
                      />
                    </div>
                  </div>
                </div>

                {/* Delivery Types */}
                <div className="form-section">
                  <h3>Delivery Types</h3>
                  <div className="form-checkboxes">
                    <label className="checkbox-label">
                      <input
                        type="checkbox"
                        name="orderDeliver"
                        checked={formData.orderDeliver}
                        onChange={handleInputChange}
                      />
                      Order Deliver
                    </label>
                    <label className="checkbox-label">
                      <input
                        type="checkbox"
                        name="curbsidePickup"
                        checked={formData.curbsidePickup}
                        onChange={handleInputChange}
                      />
                      Curbside Pickup
                    </label>
                    <label className="checkbox-label">
                      <input
                        type="checkbox"
                        name="inPersonOnly"
                        checked={formData.inPersonOnly}
                        onChange={handleInputChange}
                      />
                      In-Person Only
                    </label>
                  </div>
                </div>

                {/* Additional Information */}
                <div className="form-section">
                  <h3>Additional Information</h3>
                  <div className="form-grid">
                    <div className="form-group full-width">
                      <label>Description</label>
                      <textarea
                        name="description"
                        value={formData.description}
                        onChange={handleInputChange}
                        rows="3"
                      />
                    </div>
                    <div className="form-group full-width">
                      <label>Notes</label>
                      <textarea
                        name="notes"
                        value={formData.notes}
                        onChange={handleInputChange}
                        rows="3"
                      />
                    </div>
                  </div>
                </div>

                {/* Status */}
                <div className="form-section">
                  <h3>Status</h3>
                  <div className="form-checkboxes">
                    <label className="checkbox-label">
                      <input
                        type="checkbox"
                        name="active"
                        checked={formData.active}
                        onChange={handleInputChange}
                      />
                      Active
                    </label>
                    <label className="checkbox-label">
                      <input
                        type="checkbox"
                        name="preferred"
                        checked={formData.preferred}
                        onChange={handleInputChange}
                      />
                      Preferred Vendor
                    </label>
                  </div>
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  {editingVendor ? 'Update' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Vendors;
