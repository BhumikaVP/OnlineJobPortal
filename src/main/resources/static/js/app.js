// Utility to parse JSON safely
const parseJSON = async (response) => {
    try {
        const text = await response.text();
        return text ? JSON.parse(text) : {};
    } catch (e) {
        return { message: "An error occurred" };
    }
};

// --- Auth Functions ---
async function handleRegister(e) {
    e.preventDefault();
    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const role = document.getElementById('role').value;
    const errorDiv = document.getElementById('registerError');

    const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, email, password, role })
    });

    if (response.ok) {
        window.location.href = 'login.html';
    } else {
        const data = await parseJSON(response);
        errorDiv.textContent = data.message || "Registration failed";
        errorDiv.classList.remove('d-none');
    }
}

async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const errorDiv = document.getElementById('loginError');

    const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    });

    if (response.ok) {
        const user = await response.json();
        redirectUserBasedOnRole(user.role);
    } else {
        const data = await parseJSON(response);
        errorDiv.textContent = data.message || "Invalid credentials";
        errorDiv.classList.remove('d-none');
    }
}

async function logout() {
    await fetch('/api/auth/logout', { method: 'POST' });
    window.location.href = 'index.html';
}

function redirectUserBasedOnRole(role) {
    if (role === 'Admin') window.location.href = 'dashboard-admin.html';
    else if (role === 'Recruiter') window.location.href = 'dashboard-recruiter.html';
    else window.location.href = 'dashboard-seeker.html';
}

async function checkAuthAndUpdateNav() {
    const response = await fetch('/api/auth/me');
    const navLinks = document.getElementById('navLinks');
    if (!navLinks) return;
    
    if (response.ok) {
        const user = await response.json();
        let dashboardLink = '';
        if (user.role === 'Admin') dashboardLink = 'dashboard-admin.html';
        else if (user.role === 'Recruiter') dashboardLink = 'dashboard-recruiter.html';
        else dashboardLink = 'dashboard-seeker.html';

        navLinks.innerHTML = `
            <li class="nav-item">
                <a class="nav-link fw-bold text-primary" href="${dashboardLink}">Dashboard</a>
            </li>
            <li class="nav-item">
                <button onclick="logout()" class="btn btn-outline-danger ms-2">Logout</button>
            </li>
        `;
    }
}

// --- Common ---
async function loadRecentJobs() {
    const response = await fetch('/api/jobs');
    const containerId = document.getElementById('recentJobsList') ? 'recentJobsList' : 'seekerJobsList';
    const container = document.getElementById(containerId);
    if (!container) return;

    if (response.ok) {
        const jobs = await response.json();
        container.innerHTML = '';
        if (jobs.length === 0) {
            container.innerHTML = '<div class="col-12 text-center text-muted">No jobs available at the moment.</div>';
            return;
        }

        jobs.forEach(job => {
            container.innerHTML += `
                <div class="col-md-6 col-lg-4">
                    <div class="card job-card h-100 p-3">
                        <div class="card-body d-flex flex-column">
                            <h5 class="card-title mb-1">${job.title}</h5>
                            <h6 class="text-muted mb-3">🏢 ${job.company} | 📍 ${job.location}</h6>
                            <p class="card-text text-truncate flex-grow-1">${job.description}</p>
                            ${containerId === 'seekerJobsList' ? 
                                `<button onclick="applyForJob(${job.id})" class="btn btn-primary mt-3 w-100">Apply Now</button>` : 
                                `<a href="login.html" class="btn btn-outline-primary mt-3 w-100">Login to Apply</a>`
                            }
                        </div>
                    </div>
                </div>
            `;
        });
    }
}

// --- Job Seeker Logic ---
async function initSeekerDashboard() {
    const meResponse = await fetch('/api/auth/me');
    if (!meResponse.ok) { window.location.href = 'login.html'; return; }
    const user = await meResponse.json();
    document.getElementById('userNameDisplay').textContent = `Welcome, ${user.name}`;

    loadRecentJobs();
    loadMyApplications();
    loadMyResume(user.id);

    const resumeForm = document.getElementById('resumeForm');
    if (resumeForm) {
        resumeForm.addEventListener('submit', handleResumeUpload);
    }
}

async function applyForJob(jobId) {
    const response = await fetch(`/api/applications/${jobId}`, { method: 'POST' });
    if (response.ok) {
        alert("Applied successfully!");
        loadMyApplications();
    } else {
        const error = await response.text();
        alert(error || "Failed to apply");
    }
}

async function loadMyApplications() {
    const response = await fetch('/api/applications/my-applications');
    const tbody = document.getElementById('myApplicationsList');
    if (!tbody) return;

    if (response.ok) {
        const apps = await response.json();
        tbody.innerHTML = '';
        if (apps.length === 0) {
            tbody.innerHTML = '<tr><td colspan="2" class="text-center text-muted">No applications found.</td></tr>';
            return;
        }
        apps.forEach(app => {
            const statusClass = app.status === 'Accepted' ? 'status-accepted' : 
                                app.status === 'Rejected' ? 'status-rejected' : 'status-pending';
            tbody.innerHTML += `
                <tr>
                    <td>#${app.jobId}</td>
                    <td><span class="status-badge ${statusClass}">${app.status}</span></td>
                </tr>
            `;
        });
    }
}

async function handleResumeUpload(e) {
    e.preventDefault();
    const file = document.getElementById('resumeFile').files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    const msg = document.getElementById('resumeMessage');
    msg.innerHTML = '<span class="text-info">Uploading...</span>';

    const response = await fetch('/api/resume/upload', {
        method: 'POST',
        body: formData
    });

    if (response.ok) {
        msg.innerHTML = '<span class="text-success fw-bold">Upload successful!</span>';
        const user = await (await fetch('/api/auth/me')).json();
        loadMyResume(user.id);
    } else {
        const err = await response.text();
        msg.innerHTML = `<span class="text-danger">Error: ${err}</span>`;
    }
}

async function loadMyResume(userId) {
    const response = await fetch(`/api/resume/${userId}`);
    if (response.ok) {
        const resume = await response.json();
        document.getElementById('currentResume').classList.remove('d-none');
        document.getElementById('resumeLink').href = `/${resume.filePath}`;
    }
}

// --- Recruiter Logic ---
async function initRecruiterDashboard() {
    const meResponse = await fetch('/api/auth/me');
    if (!meResponse.ok) { window.location.href = 'login.html'; return; }
    const user = await meResponse.json();
    document.getElementById('userNameDisplay').textContent = `Welcome, ${user.name}`;

    loadRecruiterJobs();

    const postJobForm = document.getElementById('postJobForm');
    if (postJobForm) {
        postJobForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const job = {
                title: document.getElementById('jobTitle').value,
                company: document.getElementById('jobCompany').value,
                location: document.getElementById('jobLocation').value,
                description: document.getElementById('jobDescription').value,
            };
            const res = await fetch('/api/jobs', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(job)
            });
            if (res.ok) {
                bootstrap.Modal.getInstance(document.getElementById('postJobModal')).hide();
                postJobForm.reset();
                loadRecruiterJobs();
            }
        });
    }
}

async function loadRecruiterJobs() {
    const response = await fetch('/api/jobs');
    const container = document.getElementById('recruiterJobsList');
    if (!container) return;
    const meResponse = await fetch('/api/auth/me');
    const user = await meResponse.json();

    if (response.ok) {
        const allJobs = await response.json();
        const myJobs = allJobs.filter(j => j.recruiterId === user.id);
        
        container.innerHTML = '';
        if (myJobs.length === 0) {
            container.innerHTML = '<div class="col-12 text-center text-muted">You have not posted any jobs yet.</div>';
            return;
        }

        myJobs.forEach(job => {
            container.innerHTML += `
                <div class="col-md-6 col-lg-4">
                    <div class="card job-card h-100 p-3">
                        <div class="card-body d-flex flex-column">
                            <h5 class="card-title mb-1">${job.title}</h5>
                            <h6 class="text-muted mb-3">📍 ${job.location}</h6>
                            <p class="card-text text-truncate flex-grow-1">${job.description}</p>
                            <button onclick="viewApplicants(${job.id})" class="btn btn-secondary mt-3 w-100">View Applicants</button>
                        </div>
                    </div>
                </div>
            `;
        });
    }
}

async function viewApplicants(jobId) {
    const response = await fetch(`/api/applications/job/${jobId}`);
    const tbody = document.getElementById('applicantsList');
    if (response.ok) {
        const apps = await response.json();
        tbody.innerHTML = '';
        if (apps.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No applicants yet.</td></tr>';
        } else {
            apps.forEach(app => {
                const statusClass = app.status === 'Accepted' ? 'status-accepted' : 
                                    app.status === 'Rejected' ? 'status-rejected' : 'status-pending';
                tbody.innerHTML += `
                    <tr>
                        <td>#${app.id}</td>
                        <td><a href="#" onclick="viewResume(${app.userId})">User ${app.userId} Resume</a></td>
                        <td><span class="status-badge ${statusClass}">${app.status}</span></td>
                        <td>
                            <select class="form-select form-select-sm" onchange="updateAppStatus(${app.id}, this.value)">
                                <option value="">Action</option>
                                <option value="Accepted">Accept</option>
                                <option value="Rejected">Reject</option>
                            </select>
                        </td>
                    </tr>
                `;
            });
        }
        new bootstrap.Modal(document.getElementById('applicantsModal')).show();
    }
}

async function viewResume(userId) {
    const res = await fetch(`/api/resume/${userId}`);
    if (res.ok) {
        const resume = await res.json();
        window.open(`/${resume.filePath}`, '_blank');
    } else {
        alert("Resume not found for this user.");
    }
}

async function updateAppStatus(appId, status) {
    if(!status) return;
    const res = await fetch(`/api/applications/${appId}/status`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status })
    });
    if (res.ok) {
        alert("Status updated");
    } else {
        alert("Failed to update status");
    }
}

// --- Admin Logic ---
async function initAdminDashboard() {
    const meResponse = await fetch('/api/auth/me');
    if (!meResponse.ok) { window.location.href = 'login.html'; return; }
    const user = await meResponse.json();
    document.getElementById('userNameDisplay').textContent = `Admin: ${user.name}`;

    loadAdminStats();
    loadAdminJobs();
}

async function loadAdminStats() {
    const res = await fetch('/api/admin/stats');
    if (res.ok) {
        const stats = await res.json();
        document.getElementById('totalUsersCount').textContent = stats.totalUsers;
        document.getElementById('totalJobsCount').textContent = stats.totalJobs;
    }
}

async function loadAdminJobs() {
    const res = await fetch('/api/jobs');
    const tbody = document.getElementById('adminJobsList');
    if (res.ok) {
        const jobs = await res.json();
        tbody.innerHTML = '';
        jobs.forEach(job => {
            tbody.innerHTML += `
                <tr>
                    <td>${job.id}</td>
                    <td class="fw-bold">${job.title}</td>
                    <td>${job.company}</td>
                    <td>${job.recruiterId}</td>
                    <td><button onclick="adminDeleteJob(${job.id})" class="btn btn-danger btn-sm">Delete</button></td>
                </tr>
            `;
        });
    }
}

async function adminDeleteJob(jobId) {
    if (confirm("Are you sure you want to delete this job?")) {
        const res = await fetch(`/api/admin/jobs/${jobId}`, { method: 'DELETE' });
        if (res.ok) {
            loadAdminStats();
            loadAdminJobs();
        } else {
            alert("Failed to delete job");
        }
    }
}
