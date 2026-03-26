const state = {
    token: localStorage.getItem("fluxa_token") || "",
    userEmail: "",
    isAdmin: false,
    openApi: null,
    tasks: [],
    adminUsers: [],
    adminTasks: []
};

const ui = {
    docsStatus: document.getElementById("docs-status"),
    docsGroups: document.getElementById("docs-groups"),
    sessionBadge: document.getElementById("session-badge"),
    logoutButton: document.getElementById("logout-button"),
    authFeedback: document.getElementById("auth-feedback"),
    loginForm: document.getElementById("login-form"),
    registerForm: document.getElementById("register-form"),
    taskForm: document.getElementById("task-form"),
    taskCancel: document.getElementById("task-cancel"),
    tasksFeedback: document.getElementById("tasks-feedback"),
    tasksList: document.getElementById("tasks-list"),
    userEmail: document.getElementById("user-email"),
    tasksCount: document.getElementById("tasks-count"),
    adminState: document.getElementById("admin-state"),
    adminFeedback: document.getElementById("admin-feedback"),
    adminContent: document.getElementById("admin-content"),
    adminUsersBody: document.getElementById("admin-users-body"),
    adminTasksBody: document.getElementById("admin-tasks-body"),
    adminUsersCount: document.getElementById("admin-users-count"),
    adminTasksCount: document.getElementById("admin-tasks-count"),
    tokenPreview: document.getElementById("token-preview"),
    openApiPreview: document.getElementById("openapi-preview"),
    currentYear: document.getElementById("current-year")
};

function setFeedback(element, message, type = "muted") {
    element.textContent = message;
    element.className = `feedback-panel ${type}`;
}

function formatDate(value) {
    if (!value) {
        return "-";
    }

    return new Date(value).toLocaleString("pt-BR");
}

function truncateToken(token) {
    if (!token) {
        return "nenhum token armazenado";
    }

    if (token.length <= 54) {
        return token;
    }

    return `${token.slice(0, 28)}...${token.slice(-18)}`;
}

async function api(path, { method = "GET", body, auth = true } = {}) {
    const headers = {
        "Content-Type": "application/json"
    };

    if (auth && state.token) {
        headers.Authorization = `Bearer ${state.token}`;
    }

    const response = await fetch(path, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined
    });

    const contentType = response.headers.get("content-type") || "";
    const payload = contentType.includes("application/json")
            ? await response.json().catch(() => ({}))
            : await response.text();

    if (!response.ok) {
        const message = typeof payload === "string"
                ? payload
                : payload.mensagem || "Nao foi possivel concluir a requisicao.";
        const error = new Error(message);
        error.status = response.status;
        throw error;
    }

    return payload;
}

function renderDocs() {
    if (!state.openApi) {
        ui.docsStatus.textContent = "openapi indisponivel";
        ui.docsGroups.innerHTML = "";
        return;
    }

    const grouped = new Map();

    Object.entries(state.openApi.paths || {}).forEach(([path, methods]) => {
        Object.entries(methods).forEach(([method, config]) => {
            const tag = (config.tags && config.tags[0]) || "Geral";
            if (!grouped.has(tag)) {
                grouped.set(tag, []);
            }

            grouped.get(tag).push({
                method: method.toUpperCase(),
                path,
                summary: config.summary || config.operationId || "Endpoint"
            });
        });
    });

    ui.docsStatus.textContent = `${grouped.size} grupos sincronizados`;
    ui.openApiPreview.textContent = `${state.openApi.info.title} ${state.openApi.info.version}`;

    ui.docsGroups.innerHTML = Array.from(grouped.entries())
            .map(([tag, endpoints]) => `
                <section class="docs-group">
                    <h4>${tag}</h4>
                    ${endpoints.map((endpoint) => `
                        <article class="endpoint-card">
                            <div class="endpoint-meta">
                                <span class="method-badge method-${endpoint.method.toLowerCase()}">${endpoint.method}</span>
                                <code>${endpoint.path}</code>
                            </div>
                            <strong>${endpoint.summary}</strong>
                        </article>
                    `).join("")}
                </section>
            `)
            .join("");
}

function renderTasks() {
    ui.tasksCount.textContent = String(state.tasks.length);

    if (!state.token) {
        ui.tasksList.innerHTML = "";
        ui.userEmail.textContent = "aguardando autenticacao";
        setFeedback(ui.tasksFeedback, "Entre para carregar suas tarefas.", "muted");
        return;
    }

    if (state.tasks.length === 0) {
        ui.tasksList.innerHTML = "";
        setFeedback(ui.tasksFeedback, "Nenhuma tarefa encontrada. Crie a primeira pelo formulario acima.", "muted");
        return;
    }

    setFeedback(ui.tasksFeedback, `${state.tasks.length} tarefa(s) carregada(s).`, "success");

    ui.tasksList.innerHTML = state.tasks.map((task) => `
        <article class="task-card">
            <div class="task-card-head">
                <h4>${task.titulo}</h4>
                <div class="task-card-actions">
                    <button class="ghost-button" type="button" data-action="edit-task" data-id="${task.id}">Editar</button>
                    <button class="secondary-button" type="button" data-action="delete-task" data-id="${task.id}">Excluir</button>
                </div>
            </div>
            <p>${task.descricao}</p>
            <footer>
                <span class="task-tag">${task.status}</span>
                <small>${task.emailUsuario}</small>
            </footer>
        </article>
    `).join("");
}

function renderAdmin() {
    ui.adminState.textContent = state.isAdmin ? "habilitado" : "restrito";

    if (!state.token) {
        ui.adminContent.hidden = true;
        setFeedback(ui.adminFeedback, "Autentique-se para validar se o token possui acesso administrativo.", "muted");
        return;
    }

    if (!state.isAdmin) {
        ui.adminContent.hidden = true;
        setFeedback(ui.adminFeedback, "O usuario autenticado nao possui acesso ao painel administrativo.", "muted");
        return;
    }

    ui.adminContent.hidden = false;
    setFeedback(ui.adminFeedback, "Dados administrativos carregados com sucesso.", "success");

    ui.adminUsersCount.textContent = `${state.adminUsers.length} usuarios`;
    ui.adminTasksCount.textContent = `${state.adminTasks.length} tarefas`;

    ui.adminUsersBody.innerHTML = state.adminUsers.map((user) => `
        <tr>
            <td>${user.nome}</td>
            <td>${user.email}</td>
            <td>${user.perfil}</td>
            <td>${formatDate(user.criadoEm)}</td>
        </tr>
    `).join("");

    ui.adminTasksBody.innerHTML = state.adminTasks.map((task) => `
        <tr>
            <td>${task.titulo}</td>
            <td>${task.status}</td>
            <td>${task.emailUsuario}</td>
        </tr>
    `).join("");
}

function resetTaskForm() {
    ui.taskForm.reset();
    ui.taskForm.elements.id.value = "";
    ui.taskCancel.hidden = true;
    ui.taskForm.querySelector("button[type='submit']").textContent = "Salvar tarefa";
}

function applySessionUi() {
    ui.sessionBadge.textContent = state.token ? "autenticado" : "nao autenticado";
    ui.logoutButton.hidden = !state.token;
    ui.userEmail.textContent = state.userEmail || "aguardando autenticacao";
    ui.tokenPreview.textContent = truncateToken(state.token);
}

function persistToken() {
    if (state.token) {
        localStorage.setItem("fluxa_token", state.token);
    } else {
        localStorage.removeItem("fluxa_token");
    }
}

async function loadDocs() {
    try {
        state.openApi = await api("/v3/api-docs", { auth: false });
        renderDocs();
    } catch (error) {
        ui.docsStatus.textContent = error.message;
        ui.openApiPreview.textContent = "falha ao sincronizar openapi";
    }
}

async function loadProfileAndData() {
    if (!state.token) {
        state.userEmail = "";
        state.tasks = [];
        state.adminUsers = [];
        state.adminTasks = [];
        state.isAdmin = false;
        applySessionUi();
        renderTasks();
        renderAdmin();
        return;
    }

    try {
        state.userEmail = await api("/usuarios/eu");
        state.tasks = await api("/tarefas");
    } catch (error) {
        state.token = "";
        persistToken();
        applySessionUi();
        renderTasks();
        renderAdmin();
        setFeedback(ui.authFeedback, error.message, "error");
        return;
    }

    try {
        state.adminUsers = await api("/administracao/usuarios");
        state.adminTasks = await api("/administracao/tarefas");
        state.isAdmin = true;
    } catch (error) {
        state.adminUsers = [];
        state.adminTasks = [];
        state.isAdmin = false;
    }

    applySessionUi();
    renderTasks();
    renderAdmin();
}

async function handleLogin(event) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);

    try {
        const payload = await api("/autenticacao/login", {
            method: "POST",
            auth: false,
            body: {
                email: formData.get("email"),
                senha: formData.get("senha")
            }
        });

        state.token = payload.token;
        persistToken();
        setFeedback(ui.authFeedback, "Login realizado com sucesso.", "success");
        await loadProfileAndData();
    } catch (error) {
        setFeedback(ui.authFeedback, error.message, "error");
    }
}

async function handleRegister(event) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);

    try {
        const message = await api("/autenticacao/cadastro", {
            method: "POST",
            auth: false,
            body: {
                nome: formData.get("nome"),
                email: formData.get("email"),
                senha: formData.get("senha")
            }
        });

        setFeedback(ui.authFeedback, typeof message === "string" ? message : "Usuario cadastrado com sucesso.", "success");
        event.currentTarget.reset();
    } catch (error) {
        setFeedback(ui.authFeedback, error.message, "error");
    }
}

async function handleTaskSubmit(event) {
    event.preventDefault();

    if (!state.token) {
        setFeedback(ui.tasksFeedback, "Faca login antes de salvar tarefas.", "error");
        return;
    }

    const formData = new FormData(event.currentTarget);
    const id = formData.get("id");
    const body = {
        titulo: formData.get("titulo"),
        descricao: formData.get("descricao")
    };

    try {
        if (id) {
            await api(`/tarefas/${id}`, { method: "PUT", body });
            setFeedback(ui.tasksFeedback, "Tarefa atualizada com sucesso.", "success");
        } else {
            await api("/tarefas", { method: "POST", body });
            setFeedback(ui.tasksFeedback, "Tarefa criada com sucesso.", "success");
        }

        resetTaskForm();
        state.tasks = await api("/tarefas");
        renderTasks();
    } catch (error) {
        setFeedback(ui.tasksFeedback, error.message, "error");
    }
}

async function handleTaskListClick(event) {
    const target = event.target.closest("[data-action]");
    if (!target) {
        return;
    }

    const taskId = target.dataset.id;
    const task = state.tasks.find((item) => String(item.id) === taskId);

    if (!task) {
        return;
    }

    if (target.dataset.action === "edit-task") {
        ui.taskForm.elements.id.value = task.id;
        ui.taskForm.elements.titulo.value = task.titulo;
        ui.taskForm.elements.descricao.value = task.descricao;
        ui.taskCancel.hidden = false;
        ui.taskForm.querySelector("button[type='submit']").textContent = "Salvar alteracoes";
        ui.taskForm.scrollIntoView({ behavior: "smooth", block: "center" });
        return;
    }

    if (target.dataset.action === "delete-task") {
        try {
            await api(`/tarefas/${task.id}`, { method: "DELETE" });
            state.tasks = await api("/tarefas");
            renderTasks();
            setFeedback(ui.tasksFeedback, "Tarefa excluida com sucesso.", "success");
        } catch (error) {
            setFeedback(ui.tasksFeedback, error.message, "error");
        }
    }
}

function logout() {
    state.token = "";
    state.userEmail = "";
    state.tasks = [];
    state.adminUsers = [];
    state.adminTasks = [];
    state.isAdmin = false;
    persistToken();
    resetTaskForm();
    applySessionUi();
    renderTasks();
    renderAdmin();
    setFeedback(ui.authFeedback, "Sessao encerrada.", "muted");
}

function bindNavigation() {
    document.querySelectorAll("[data-scroll-target]").forEach((button) => {
        button.addEventListener("click", () => {
            const target = document.querySelector(button.dataset.scrollTarget);
            if (target) {
                target.scrollIntoView({ behavior: "smooth", block: "start" });
            }
        });
    });
}

function init() {
    ui.currentYear.textContent = new Date().getFullYear();
    ui.loginForm.addEventListener("submit", handleLogin);
    ui.registerForm.addEventListener("submit", handleRegister);
    ui.taskForm.addEventListener("submit", handleTaskSubmit);
    ui.tasksList.addEventListener("click", handleTaskListClick);
    ui.taskCancel.addEventListener("click", resetTaskForm);
    ui.logoutButton.addEventListener("click", logout);
    bindNavigation();
    applySessionUi();
    renderTasks();
    renderAdmin();
    loadDocs();
    loadProfileAndData();
}

init();
