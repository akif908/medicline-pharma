/**
 * Medicline Pharma — Frontend Application Logic
 * Handles API communication, auth state, and global interactions.
 */

const API_BASE = '/api';

// ---- API Helper ----
const api = {
    async get(endpoint) {
        try {
            const res = await fetch(`${API_BASE}${endpoint}`);
            if (!res.ok) throw new Error(`GET ${endpoint} failed: ${res.status}`);
            return await res.json();
        } catch (err) {
            console.error('[API GET Error]', err);
            return null;
        }
    },

    async post(endpoint, data) {
        try {
            const res = await fetch(`${API_BASE}${endpoint}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            if (!res.ok) throw new Error(`POST ${endpoint} failed: ${res.status}`);
            return await res.json();
        } catch (err) {
            console.error('[API POST Error]', err);
            return null;
        }
    }
};

// ---- Auth State ----
function getCurrentUser() {
    const stored = localStorage.getItem('user');
    return stored ? JSON.parse(stored) : null;
}

function logout() {
    localStorage.removeItem('user');
    window.location.href = 'login.html';
}

// ---- Page Init ----
document.addEventListener('DOMContentLoaded', () => {
    updateNavForAuth();
});

function updateNavForAuth() {
    const user = getCurrentUser();
    const navRight = document.querySelector('.nav-right');
    if (!navRight) return;

    if (user) {
        // If logged in, show user info instead of login button
        const loginBtn = navRight.querySelector('a[href="login.html"]');
        if (loginBtn) {
            loginBtn.textContent = user.name || 'Dashboard';
            loginBtn.href = 'dashboard.html';
            loginBtn.classList.remove('btn-outline');
            loginBtn.classList.add('btn-primary');
        }
    }
}

console.log('%c Medicline Pharma Initialized ', 'background:#14b8a6;color:#003731;font-weight:bold;padding:4px 8px;border-radius:4px;');
