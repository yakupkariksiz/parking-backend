// ============================================================
// common.js — Shared utilities for Parking Scanner
// ============================================================

// --- JWT Auth Helpers ---
function getToken() { return localStorage.getItem('jwt_token'); }

function getAuthHeaders(extra) {
    const headers = { 'Authorization': 'Bearer ' + getToken() };
    if (extra) Object.assign(headers, extra);
    return headers;
}

function logout() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('jwt_username');
    localStorage.removeItem('jwt_role');
    window.location.href = '/login.html?logout';
}

function checkAuth(res) {
    if (res.status === 401 || res.status === 403) {
        logout();
        return false;
    }
    return true;
}

function requireAuth() {
    if (!getToken()) {
        window.location.href = '/login.html';
        return false;
    }
    return true;
}

function requireAdmin() {
    const storedRole = localStorage.getItem('jwt_role');
    if (storedRole !== 'ROLE_ADMIN') {
        window.location.href = '/dashboard.html';
        return false;
    }
    return true;
}

// --- Toast Notification System ---
let toastContainer = null;

function getToastContainer() {
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toast-container';
        toastContainer.className = 'fixed top-4 right-4 z-[999] flex flex-col gap-3 pointer-events-none';
        document.body.appendChild(toastContainer);
    }
    return toastContainer;
}

function showToast(message, type = 'info', duration = 3500) {
    const container = getToastContainer();

    const toast = document.createElement('div');
    toast.className = 'pointer-events-auto flex items-center gap-3 px-5 py-3.5 rounded-xl shadow-lg text-sm font-medium max-w-sm transform transition-all duration-300 translate-x-full opacity-0';

    const icons = {
        success: '<svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>',
        error: '<svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>',
        info: '<svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>',
        warning: '<svg class="w-5 h-5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z"/></svg>'
    };

    const colors = {
        success: 'bg-emerald-50 text-emerald-800 border border-emerald-200',
        error: 'bg-rose-50 text-rose-800 border border-rose-200',
        info: 'bg-indigo-50 text-indigo-800 border border-indigo-200',
        warning: 'bg-amber-50 text-amber-800 border border-amber-200'
    };

    toast.classList.add(...(colors[type] || colors.info).split(' '));
    toast.innerHTML = `${icons[type] || icons.info}<span>${message}</span>`;

    container.appendChild(toast);

    // Animate in
    requestAnimationFrame(() => {
        toast.classList.remove('translate-x-full', 'opacity-0');
        toast.classList.add('translate-x-0', 'opacity-100');
    });

    // Animate out and remove
    setTimeout(() => {
        toast.classList.remove('translate-x-0', 'opacity-100');
        toast.classList.add('translate-x-full', 'opacity-0');
        setTimeout(() => toast.remove(), 300);
    }, duration);
}

// --- Loading Spinner ---
function showSpinner(container, message = 'Loading...') {
    if (typeof container === 'string') container = document.getElementById(container);
    container.innerHTML = `
        <div class="flex items-center justify-center gap-3 py-12 text-slate-400">
            <svg class="animate-spin h-5 w-5" viewBox="0 0 24 24" fill="none">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
            <span class="text-sm">${message}</span>
        </div>
    `;
}

// Button loading state
function setBtnLoading(btn, loading, originalText) {
    if (loading) {
        btn.disabled = true;
        btn.dataset.originalText = btn.textContent;
        btn.innerHTML = `
            <svg class="animate-spin h-4 w-4 inline mr-2" viewBox="0 0 24 24" fill="none">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
            ${originalText || 'Loading...'}
        `;
    } else {
        btn.disabled = false;
        btn.textContent = btn.dataset.originalText || originalText || 'Submit';
    }
}

// --- Navigation Drawer ---
function renderNavDrawer(activePage) {
    const pages = [
        { href: '/dashboard.html', icon: 'dashboard', label: 'Dashboard', id: 'dashboard' },
        { href: '/index.html', icon: 'scan', label: 'Scan Plates', id: 'scan' },
        { divider: true },
        { href: '/residents.html', icon: 'residents', label: 'Residents', id: 'residents', admin: true },
        { href: '/stats.html', icon: 'stats', label: 'Session Stats', id: 'stats' },
        { href: '/occupancy.html', icon: 'chart', label: 'Occupancy Chart', id: 'occupancy' },
        { divider: true },
        { href: '/users.html', icon: 'users', label: 'User Management', id: 'users', admin: true },
        { href: '/audit.html', icon: 'audit', label: 'Audit Log', id: 'audit', admin: true },
    ];

    const svgIcons = {
        dashboard: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-4 0a1 1 0 01-1-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 01-1 1h-2z"/>',
        scan: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm12 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1zM5 20h2a1 1 0 001-1v-2a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z"/>',
        residents: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>',
        stats: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>',
        chart: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z"/>',
        users: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>',
        audit: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>',
        logout: '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>'
    };

    function makeSvg(iconName) {
        return `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">${svgIcons[iconName]}</svg>`;
    }

    // Build nav items HTML
    let navItemsHtml = '';
    pages.forEach(p => {
        if (p.divider) {
            navItemsHtml += '<div class="h-px bg-slate-100 my-2"></div>';
            return;
        }
        const isActive = p.id === activePage;
        const activeClass = isActive
            ? 'bg-indigo-50 text-indigo-700 font-semibold'
            : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900';
        const adminAttr = p.admin ? 'data-admin-only="true"' : '';
        const adminStyle = p.admin ? 'style="display:none;"' : '';
        const adminBadge = p.admin
            ? '<span class="ml-auto text-[0.65rem] font-semibold bg-amber-100 text-amber-700 px-2 py-0.5 rounded-full">Admin</span>'
            : '';
        navItemsHtml += `
            <a href="${p.href}" class="flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm transition-colors ${activeClass}" ${adminAttr} ${adminStyle}>
                ${makeSvg(p.icon)}
                <span>${p.label}</span>
                ${adminBadge}
            </a>
        `;
    });

    // Header bar HTML
    const headerHtml = `
    <header class="bg-slate-900 sticky top-0 z-50">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
            <a href="/dashboard.html" class="flex items-center gap-2.5 text-white font-semibold text-base hover:text-slate-300 transition-colors">
                <svg class="w-7 h-7 text-indigo-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"/>
                </svg>
                <span>Parking Scanner</span>
            </a>
            <div class="flex items-center gap-3">
                <!-- Menu button -->
                <button id="nav-menu-btn" class="flex items-center gap-2 bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white px-3.5 py-2 rounded-lg text-sm transition-colors cursor-pointer">
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
                    </svg>
                    <span class="hidden sm:inline">Menu</span>
                </button>
                <!-- User badge -->
                <div class="flex items-center gap-2.5">
                    <div class="text-right hidden sm:block">
                        <div class="text-sm font-medium text-white" id="user-name">...</div>
                        <div class="text-xs text-slate-400" id="user-role"></div>
                    </div>
                    <div class="w-9 h-9 rounded-full bg-slate-700 ring-2 ring-indigo-400/50 flex items-center justify-center">
                        <svg class="w-5 h-5 text-slate-300" fill="currentColor" viewBox="0 0 24 24">
                            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/>
                        </svg>
                    </div>
                </div>
            </div>
        </div>
    </header>

    <!-- Drawer backdrop -->
    <div id="nav-backdrop" class="fixed inset-0 bg-black/40 backdrop-blur-sm z-[60] opacity-0 pointer-events-none transition-opacity duration-300"></div>

    <!-- Slide-out drawer -->
    <div id="nav-drawer" class="fixed top-0 right-0 bottom-0 w-72 bg-white shadow-2xl z-[70] transform translate-x-full transition-transform duration-300 ease-out flex flex-col">
        <div class="flex items-center justify-between p-5 border-b border-slate-100">
            <span class="text-sm font-semibold text-slate-800">Navigation</span>
            <button id="nav-close-btn" class="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition-colors cursor-pointer">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
            </button>
        </div>
        <!-- User info inside drawer -->
        <div class="px-5 py-4 border-b border-slate-100 bg-slate-50/50">
            <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-gradient-to-br from-indigo-400 to-indigo-600 flex items-center justify-center text-white font-bold text-sm" id="drawer-avatar">?</div>
                <div>
                    <div class="text-sm font-semibold text-slate-800" id="drawer-user-name">...</div>
                    <div class="text-xs text-slate-500" id="drawer-user-role"></div>
                </div>
            </div>
        </div>
        <nav class="flex-1 overflow-y-auto p-3 space-y-0.5" id="nav-items">
            ${navItemsHtml}
        </nav>
        <div class="p-3 border-t border-slate-100">
            <a href="#" onclick="logout(); return false;" class="flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm text-slate-600 hover:bg-rose-50 hover:text-rose-700 transition-colors">
                ${makeSvg('logout')}
                <span>Logout</span>
            </a>
        </div>
    </div>
    `;

    // Insert into page
    document.body.insertAdjacentHTML('afterbegin', headerHtml);

    // Wire up drawer toggle
    const menuBtn = document.getElementById('nav-menu-btn');
    const backdrop = document.getElementById('nav-backdrop');
    const drawer = document.getElementById('nav-drawer');
    const closeBtn = document.getElementById('nav-close-btn');

    function openDrawer() {
        backdrop.classList.remove('opacity-0', 'pointer-events-none');
        backdrop.classList.add('opacity-100');
        drawer.classList.remove('translate-x-full');
        drawer.classList.add('translate-x-0');
    }

    function closeDrawer() {
        backdrop.classList.add('opacity-0', 'pointer-events-none');
        backdrop.classList.remove('opacity-100');
        drawer.classList.add('translate-x-full');
        drawer.classList.remove('translate-x-0');
    }

    menuBtn.addEventListener('click', openDrawer);
    backdrop.addEventListener('click', closeDrawer);
    closeBtn.addEventListener('click', closeDrawer);

    // Close on Escape
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') closeDrawer();
    });
}

// Load current user info and update header + admin links
async function loadCurrentUser(opts = {}) {
    const { onAdmin, onUser } = opts;
    try {
        const res = await fetch('/api/user/me', { headers: getAuthHeaders() });
        if (!checkAuth(res)) return null;
        if (!res.ok) return null;

        const user = await res.json();
        if (!user.authenticated) return null;

        // Update header
        const nameEl = document.getElementById('user-name');
        const roleEl = document.getElementById('user-role');
        const drawerNameEl = document.getElementById('drawer-user-name');
        const drawerRoleEl = document.getElementById('drawer-user-role');
        const drawerAvatarEl = document.getElementById('drawer-avatar');

        const displayName = user.name || 'User';
        const isAdmin = user.role === 'ROLE_ADMIN';
        const roleText = isAdmin ? 'Administrator' : 'User';

        if (nameEl) nameEl.textContent = displayName;
        if (roleEl) roleEl.textContent = roleText;
        if (drawerNameEl) drawerNameEl.textContent = displayName;
        if (drawerRoleEl) drawerRoleEl.textContent = roleText;
        if (drawerAvatarEl) drawerAvatarEl.textContent = displayName.charAt(0).toUpperCase();

        // Show admin-only nav items
        if (isAdmin) {
            document.querySelectorAll('[data-admin-only]').forEach(el => {
                el.style.display = '';
            });
            document.querySelectorAll('.admin-only').forEach(el => {
                el.style.display = '';
            });
            if (onAdmin) onAdmin(user);
        } else {
            if (onUser) onUser(user);
        }

        return user;
    } catch (err) {
        console.warn('Failed to load user info', err);
        return null;
    }
}
