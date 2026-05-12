const API_BASE = window.SMARTHIRE_API || `${window.location.origin}/api`;
const PAGE_SIZE = 8;
const TABLE_SIZE = 10;

let authToken = localStorage.getItem("sh_token");
let currentUser = JSON.parse(localStorage.getItem("sh_user") || "null");
let currentFilter = "";
let currentQuery = "";
let dashboardPage = 0;
let tablePage = 0;
let currentEmployeeId = null;
let currentView = "dashboard";
let searchDebounceId = null;

document.addEventListener("DOMContentLoaded", () => {
  bindAuthForms();
  bindNavigation();
  bindFilters();
  bindSearch();
  bindEmployeeForm();
  bindModal();

  if (authToken && currentUser) {
    enterApp();
  } else {
    showAuth();
  }
});

function bindAuthForms() {
  document.getElementById("loginForm")?.addEventListener("submit", handleLogin);
  document.getElementById("registerForm")?.addEventListener("submit", handleRegister);
}

function bindNavigation() {
  document.querySelectorAll(".nav-item[data-view]").forEach((item) => {
    item.addEventListener("click", (event) => {
      event.preventDefault();
      switchView(item.dataset.view);
    });
  });

  document.getElementById("logoutBtn")?.addEventListener("click", logout);
}

function bindFilters() {
  document.getElementById("filterTabs")?.addEventListener("click", (event) => {
    const button = event.target.closest(".filter-btn");
    if (!button) {
      return;
    }

    document.querySelectorAll(".filter-btn").forEach((item) => item.classList.remove("active"));
    button.classList.add("active");
    currentFilter = button.dataset.filter || "";
    dashboardPage = 0;
    if (currentView === "dashboard") {
      loadDashboard();
    }
  });
}

function bindSearch() {
  document.getElementById("searchInput")?.addEventListener("input", (event) => {
    clearTimeout(searchDebounceId);
    searchDebounceId = window.setTimeout(() => {
      currentQuery = event.target.value.trim();
      dashboardPage = 0;
      tablePage = 0;
      refreshCurrentView();
    }, 250);
  });
}

function bindEmployeeForm() {
  document.getElementById("addForm")?.addEventListener("submit", async (event) => {
    event.preventDefault();

    const submitButton = event.target.querySelector("button[type='submit']");
    const message = document.getElementById("formMsg");
    const departmentId = document.getElementById("fDept").value;
    const payload = {
      fullName: document.getElementById("fName").value.trim(),
      email: document.getElementById("fEmail").value.trim(),
      role: document.getElementById("fRole").value.trim(),
      startDate: document.getElementById("fDate").value || null,
      status: document.getElementById("fStatus").value,
      department: departmentId ? { id: Number(departmentId) } : null
    };

    submitButton.disabled = true;
    setFormMessage(message, "", "");

    try {
      const response = await apiFetch("/employees", {
        method: "POST",
        body: JSON.stringify(payload)
      });

      if (!response) {
        return;
      }

      if (!response.ok) {
        const error = await safeJson(response);
        setFormMessage(message, error?.message || error?.error || "Could not add employee.", "error");
        return;
      }

      event.target.reset();
      document.getElementById("fStatus").value = "PENDING";
      setFormMessage(message, "Employee added successfully.", "success");
      dashboardPage = 0;
      tablePage = 0;
      await Promise.all([loadDepartments(), fetchStats()]);
      await refreshCurrentView();
    } catch (error) {
      setFormMessage(message, "Network error. Start the backend and try again.", "error");
      console.error(error);
    } finally {
      submitButton.disabled = false;
    }
  });
}

function bindModal() {
  document.getElementById("modalClose")?.addEventListener("click", closeModal);
  document.getElementById("modalOverlay")?.addEventListener("click", (event) => {
    if (event.target.id === "modalOverlay") {
      closeModal();
    }
  });
}

async function handleLogin(event) {
  event.preventDefault();
  await authenticate({
    endpoint: "/auth/login",
    payload: {
      username: document.getElementById("loginUser").value.trim(),
      password: document.getElementById("loginPass").value
    },
    buttonId: "loginBtn",
    errorId: "loginError"
  });
}

async function handleRegister(event) {
  event.preventDefault();
  await authenticate({
    endpoint: "/auth/register",
    payload: {
      username: document.getElementById("regUser").value.trim(),
      email: document.getElementById("regEmail").value.trim(),
      password: document.getElementById("regPass").value
    },
    buttonId: "registerBtn",
    errorId: "registerError"
  });
}

async function authenticate({ endpoint, payload, buttonId, errorId }) {
  const button = document.getElementById(buttonId);
  const errorEl = document.getElementById(errorId);

  toggleLoadingButton(button, true);
  setInlineError(errorEl, "");

  try {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });
    const data = await safeJson(response);

    if (!response.ok) {
      setInlineError(errorEl, data?.error || "Authentication failed.");
      return;
    }

    authToken = data.token;
    currentUser = { username: data.username, email: data.email, role: data.role };
    localStorage.setItem("sh_token", authToken);
    localStorage.setItem("sh_user", JSON.stringify(currentUser));
    enterApp();
  } catch (error) {
    setInlineError(errorEl, "Cannot reach the backend. Start the server and try again.");
    console.error(error);
  } finally {
    toggleLoadingButton(button, false);
  }
}

function enterApp() {
  document.getElementById("authScreen").classList.add("hidden");
  document.getElementById("mainApp").classList.remove("hidden");
  document.getElementById("sidebar").classList.remove("hidden");
  document.getElementById("welcomeUser").textContent = currentUser?.username || "Admin";
  switchView(currentView);
  loadDepartments();
}

function showAuth() {
  document.getElementById("authScreen").classList.remove("hidden");
  document.getElementById("mainApp").classList.add("hidden");
  document.getElementById("sidebar").classList.add("hidden");
}

function logout() {
  authToken = null;
  currentUser = null;
  localStorage.removeItem("sh_token");
  localStorage.removeItem("sh_user");
  closeModal();
  showAuth();
}

function switchAuthTab(tab) {
  document.getElementById("loginForm").classList.toggle("hidden", tab !== "login");
  document.getElementById("registerForm").classList.toggle("hidden", tab !== "register");
  document.getElementById("tabLogin").classList.toggle("active", tab === "login");
  document.getElementById("tabRegister").classList.toggle("active", tab === "register");
}

function togglePw(inputId, trigger) {
  const input = document.getElementById(inputId);
  const isPassword = input.type === "password";
  input.type = isPassword ? "text" : "password";
  trigger.textContent = isPassword ? "Hide" : "Show";
}

function switchView(view) {
  currentView = view;
  document.querySelectorAll(".view").forEach((item) => item.classList.remove("active"));
  document.getElementById(`view-${view}`)?.classList.add("active");

  document.querySelectorAll(".nav-item").forEach((item) => item.classList.remove("active"));
  document.querySelector(`.nav-item[data-view="${view}"]`)?.classList.add("active");

  const titles = {
    dashboard: "Dashboard",
    employees: "Employees",
    add: "Add Employee"
  };
  document.getElementById("pageTitle").textContent = titles[view] || "SmartHire";

  refreshCurrentView();
}

async function refreshCurrentView() {
  if (!authToken) {
    return;
  }

  if (currentView === "dashboard") {
    await loadDashboard();
    return;
  }

  if (currentView === "employees") {
    await loadTablePage();
  }
}

async function loadDashboard() {
  await Promise.all([fetchStats(), loadDashboardPage()]);
}

async function fetchStats() {
  try {
    const response = await apiFetch("/stats");
    if (!response) {
      return;
    }

    const data = await response.json();
    document.getElementById("statTotal").textContent = data.total ?? "0";
    document.getElementById("statPending").textContent = data.pending ?? "0";
    document.getElementById("statActive").textContent = data.active ?? "0";
    document.getElementById("statCompleted").textContent = data.completed ?? "0";
  } catch (error) {
    console.error(error);
  }
}

async function loadDashboardPage() {
  const grid = document.getElementById("employeeGrid");
  grid.innerHTML = '<div class="loading-state">Loading employees...</div>';

  try {
    const data = await fetchEmployeeCollection({ page: dashboardPage, size: PAGE_SIZE });
    renderCards(data.items);
    renderPagination("paginationDash", dashboardPage, data.totalPages, (page) => {
      dashboardPage = page;
      loadDashboardPage();
    });
  } catch (error) {
    grid.innerHTML = '<div class="empty-state">Could not load employees. Check whether the backend is running.</div>';
    console.error(error);
  }
}

async function loadTablePage() {
  const tbody = document.getElementById("empTableBody");
  tbody.innerHTML = '<tr><td colspan="7" class="loading-state">Loading employees...</td></tr>';

  try {
    const data = await fetchEmployeeCollection({ page: tablePage, size: TABLE_SIZE });
    renderTableRows(data.items);
    renderPagination("paginationTable", tablePage, data.totalPages, (page) => {
      tablePage = page;
      loadTablePage();
    });
  } catch (error) {
    tbody.innerHTML = '<tr><td colspan="7" class="empty-state">Could not load employees.</td></tr>';
    console.error(error);
  }
}

async function fetchEmployeeCollection({ page, size }) {
  if (currentQuery) {
    const response = await apiFetch(`/employees?search=${encodeURIComponent(currentQuery)}`);
    if (!response) {
      return { items: [], totalPages: 1 };
    }
    const data = await response.json();
    return paginateClientSide(data, page, size);
  }

  if (currentFilter) {
    const response = await apiFetch(`/employees?status=${encodeURIComponent(currentFilter)}&page=${page}&size=${size}&sort=id`);
    if (!response) {
      return { items: [], totalPages: 1 };
    }
    const data = await response.json();
    return { items: data.content || [], totalPages: data.totalPages || 1 };
  }

  const response = await apiFetch(`/employees?page=${page}&size=${size}&sort=id`);
  if (!response) {
    return { items: [], totalPages: 1 };
  }
  const data = await response.json();
  return { items: data.content || [], totalPages: data.totalPages || 1 };
}

function paginateClientSide(items, page, size) {
  const filtered = currentFilter ? items.filter((item) => item.status === currentFilter) : items;
  const totalPages = Math.max(1, Math.ceil(filtered.length / size));
  const start = page * size;
  return {
    items: filtered.slice(start, start + size),
    totalPages
  };
}

function renderCards(employees) {
  const grid = document.getElementById("employeeGrid");
  if (!employees.length) {
    grid.innerHTML = '<div class="empty-state">No employees found.</div>';
    return;
  }

  grid.innerHTML = employees.map((employee, index) => {
    const progress = progressFromStatus(employee.status);
    return `
      <article class="emp-card" data-id="${employee.id}">
        <div class="emp-card-top">
          <div class="avatar av-${index % 5}">${esc(employee.avatarInitials || initials(employee.fullName))}</div>
          <span class="badge badge-${employee.status}">${employee.status}</span>
        </div>
        <div class="emp-name">${esc(employee.fullName)}</div>
        <div class="emp-role">${esc(employee.role || "Role not set")}</div>
        <div class="emp-dept">${esc(employee.department?.name || "No department")}</div>
        <div class="mini-progress">
          <div class="mini-bar-track"><div class="mini-bar-fill" style="width:${progress}%"></div></div>
          <div class="mini-label">${progress}% onboarding progress</div>
        </div>
      </article>
    `;
  }).join("");

  document.querySelectorAll(".emp-card").forEach((card) => {
    card.addEventListener("click", () => openEmployeeModal(Number(card.dataset.id)));
  });
}

function renderTableRows(employees) {
  const tbody = document.getElementById("empTableBody");
  if (!employees.length) {
    tbody.innerHTML = '<tr><td colspan="7" class="empty-state">No employees found.</td></tr>';
    return;
  }

  tbody.innerHTML = employees.map((employee, index) => `
    <tr>
      <td>
        <div class="table-avatar">
          <div class="avatar av-${index % 5}">${esc(employee.avatarInitials || initials(employee.fullName))}</div>
          <div>
            <div class="table-name">${esc(employee.fullName)}</div>
            <div class="table-email">${esc(employee.email)}</div>
          </div>
        </div>
      </td>
      <td>${esc(employee.role || "-")}</td>
      <td>${esc(employee.department?.name || "-")}</td>
      <td>${esc(employee.startDate || "-")}</td>
      <td><span class="badge badge-${employee.status}">${employee.status}</span></td>
      <td>${progressFromStatus(employee.status)}%</td>
      <td><button class="btn-view" data-id="${employee.id}" type="button">View</button></td>
    </tr>
  `).join("");

  tbody.querySelectorAll(".btn-view").forEach((button) => {
    button.addEventListener("click", () => openEmployeeModal(Number(button.dataset.id)));
  });
}

function renderPagination(containerId, currentPageIndex, totalPages, onPageChange) {
  const container = document.getElementById(containerId);
  if (!container) {
    return;
  }

  if (totalPages <= 1) {
    container.innerHTML = "";
    return;
  }

  let html = "";
  html += `<button class="page-btn" type="button" data-page="${currentPageIndex - 1}" ${currentPageIndex === 0 ? "disabled" : ""}>Prev</button>`;
  for (let page = 0; page < totalPages; page += 1) {
    html += `<button class="page-btn${page === currentPageIndex ? " active" : ""}" type="button" data-page="${page}">${page + 1}</button>`;
  }
  html += `<button class="page-btn" type="button" data-page="${currentPageIndex + 1}" ${currentPageIndex >= totalPages - 1 ? "disabled" : ""}>Next</button>`;
  container.innerHTML = html;

  container.querySelectorAll("button[data-page]").forEach((button) => {
    button.addEventListener("click", () => onPageChange(Number(button.dataset.page)));
  });
}

async function loadDepartments() {
  try {
    const response = await apiFetch("/departments");
    if (!response) {
      return;
    }

    const departments = await response.json();
    const select = document.getElementById("fDept");
    select.innerHTML = '<option value="">Select department</option>';
    departments.forEach((department) => {
      const option = document.createElement("option");
      option.value = department.id;
      option.textContent = department.name;
      select.appendChild(option);
    });
  } catch (error) {
    console.error(error);
  }
}

async function openEmployeeModal(employeeId) {
  currentEmployeeId = employeeId;
  const overlay = document.getElementById("modalOverlay");
  overlay.classList.remove("hidden");
  overlay.classList.add("open");

  try {
    const [employeeResponse, tasksResponse] = await Promise.all([
      apiFetch(`/employees/${employeeId}`),
      apiFetch(`/employees/${employeeId}/tasks`)
    ]);

    if (!employeeResponse || !tasksResponse) {
      return;
    }

    const employee = await employeeResponse.json();
    const tasks = await tasksResponse.json();
    const doneCount = tasks.filter((task) => task.done).length;
    const progress = tasks.length ? Math.round((doneCount / tasks.length) * 100) : progressFromStatus(employee.status);

    document.getElementById("modalAvatar").textContent = employee.avatarInitials || initials(employee.fullName);
    document.getElementById("modalName").textContent = employee.fullName;
    document.getElementById("modalRole").textContent = employee.role || "Role not set";
    document.getElementById("modalStatus").className = `badge badge-${employee.status}`;
    document.getElementById("modalStatus").textContent = employee.status;
    document.getElementById("modalMeta").innerHTML = `
      <span class="meta-pill">${esc(employee.email)}</span>
      <span class="meta-pill">${esc(employee.department?.name || "No department")}</span>
      <span class="meta-pill">Start: ${esc(employee.startDate || "Not scheduled")}</span>
      <span class="meta-pill">Employee #${employee.id}</span>
    `;
    document.getElementById("modalProgress").style.width = `${progress}%`;
    document.getElementById("modalProgressLabel").textContent = `${doneCount}/${tasks.length} tasks completed`;
    renderTasks(tasks);
    document.getElementById("deleteBtn").onclick = () => deleteEmployee(employeeId);
  } catch (error) {
    console.error(error);
  }
}

function renderTasks(tasks) {
  const list = document.getElementById("taskList");
  if (!tasks.length) {
    list.innerHTML = '<li class="task-item">No onboarding tasks yet.</li>';
    return;
  }

  list.innerHTML = tasks.map((task) => `
    <li class="task-item ${task.done ? "done" : ""}">
      <button class="task-checkbox ${task.done ? "checked" : ""}" type="button" data-id="${task.id}" data-done="${task.done}">
        ${task.done ? "✓" : ""}
      </button>
      <span class="task-name">${esc(task.taskName)}</span>
      <span class="task-cat">${esc(task.category || "")}</span>
    </li>
  `).join("");

  list.querySelectorAll(".task-checkbox").forEach((button) => {
    button.addEventListener("click", async () => {
      await toggleTask(Number(button.dataset.id), button.dataset.done !== "true");
    });
  });
}

async function toggleTask(taskId, done) {
  try {
    const response = await apiFetch(`/tasks/${taskId}`, {
      method: "PATCH",
      body: JSON.stringify({ done })
    });

    if (!response) {
      return;
    }

    await Promise.all([fetchStats(), openEmployeeModal(currentEmployeeId)]);
    if (currentView === "dashboard") {
      await loadDashboardPage();
    }
    if (currentView === "employees") {
      await loadTablePage();
    }
  } catch (error) {
    console.error(error);
  }
}

async function deleteEmployee(employeeId) {
  if (!window.confirm("Delete this employee?")) {
    return;
  }

  try {
    const response = await apiFetch(`/employees/${employeeId}`, { method: "DELETE" });
    if (!response) {
      return;
    }

    closeModal();
    await Promise.all([fetchStats(), loadDashboardPage(), loadTablePage()]);
  } catch (error) {
    console.error(error);
  }
}

function closeModal() {
  currentEmployeeId = null;
  const overlay = document.getElementById("modalOverlay");
  overlay.classList.add("hidden");
  overlay.classList.remove("open");
}

async function apiFetch(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(authToken ? { Authorization: `Bearer ${authToken}` } : {}),
      ...(options.headers || {})
    }
  });

  if (response.status === 401 || response.status === 403) {
    logout();
    return null;
  }

  return response;
}

function toggleLoadingButton(button, loading) {
  button.querySelector(".btn-text")?.classList.toggle("hidden", loading);
  button.querySelector(".btn-loader")?.classList.toggle("hidden", !loading);
}

function setInlineError(element, message) {
  element.textContent = message;
  element.classList.toggle("hidden", !message);
}

function setFormMessage(element, message, type) {
  element.textContent = message;
  element.className = "form-msg";
  if (type) {
    element.classList.add(type);
  }
}

function progressFromStatus(status) {
  if (status === "COMPLETED") {
    return 100;
  }
  if (status === "ACTIVE") {
    return 60;
  }
  return 0;
}

function initials(fullName) {
  if (!fullName) {
    return "?";
  }

  const parts = fullName.trim().split(/\s+/);
  if (parts.length === 1) {
    return parts[0].slice(0, 2).toUpperCase();
  }

  return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
}

function esc(value) {
  return String(value || "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

async function safeJson(response) {
  try {
    return await response.json();
  } catch {
    return null;
  }
}
