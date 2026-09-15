/**
 * Common JavaScript utilities for Cinema Web Application
 */
const CinemaApp = {
    // Format currency to VND
    formatCurrency: function(amount) {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    },

    // Show toast message
    showToast: function(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast-msg toast-${type}`;
        toast.innerText = message;
        document.body.appendChild(toast);
        setTimeout(() => toast.remove(), 3500);
    },

    // Standard AJAX fetch wrapper
    fetchApi: async function(url, options = {}) {
        try {
            const response = await fetch(url, options);
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }
};
