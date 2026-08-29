/**
 * Thin AJAX wrapper around fetch(), used by every page instead of raw
 * XMLHttpRequest/fetch calls. Centralizes the API base URL, auth header,
 * and JSON error handling so cart/order updates can hit the backend
 * asynchronously without full page reloads.
 */
const API_BASE = "http://localhost:8080/api";

const Session = {
    get token() { return localStorage.getItem("fo_token"); },
    get userId() { return localStorage.getItem("fo_userId"); },
    get name() { return localStorage.getItem("fo_name"); },
    get role() { return localStorage.getItem("fo_role"); },

    save(auth) {
        localStorage.setItem("fo_token", auth.token);
        localStorage.setItem("fo_userId", auth.userId);
        localStorage.setItem("fo_name", auth.name);
        localStorage.setItem("fo_role", auth.role);
    },
    clear() {
        ["fo_token", "fo_userId", "fo_name", "fo_role"].forEach(k => localStorage.removeItem(k));
    },
    isLoggedIn() { return !!this.token; },
    requireLogin() {
        if (!this.isLoggedIn()) window.location.href = "index.html";
    }
};

/**
 * Core AJAX call. Every cart/order/menu interaction in the app routes
 * through here, so a single place handles auth headers and error surfacing.
 */
async function apiCall(path, { method = "GET", body = null, auth = true } = {}) {
    const headers = { "Content-Type": "application/json" };
    if (auth && Session.token) headers["Authorization"] = Session.token;

    const res = await fetch(`${API_BASE}${path}`, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined
    });

    let data = null;
    try { data = await res.json(); } catch (e) { /* no body */ }

    if (!res.ok) {
        const message = (data && data.error) ? data.error : `Request failed (${res.status})`;
        throw new Error(message);
    }
    return data;
}

function showError(message, elId = "error-banner") {
    const el = document.getElementById(elId);
    if (!el) { alert(message); return; }
    el.textContent = message;
    el.style.display = "block";
    setTimeout(() => { el.style.display = "none"; }, 5000);
}

function money(amount) {
    return "$" + Number(amount).toFixed(2);
}
