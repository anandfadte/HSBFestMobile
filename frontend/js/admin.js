// State
let allEvents = [];
let allRegistrations = [];
let currentFilterEventId = "";
let tableSearchQuery = "";

// Elements
const statTotalEvents = document.getElementById("stat-total-events");
const statTotalRegistrations = document.getElementById("stat-total-registrations");
const statUtilization = document.getElementById("stat-utilization");
const statCapacityDetails = document.getElementById("stat-capacity-details");
const statUpcomingEvents = document.getElementById("stat-upcoming-events");
const categoryDistribution = document.getElementById("category-distribution");

const eventsTableBody = document.getElementById("events-table-body");
const tableSearchInput = document.getElementById("table-search-input");

const registrationsTableBody = document.getElementById("registrations-table-body");
const regEventFilter = document.getElementById("reg-event-filter");

const tabEventsBtn = document.getElementById("tab-events-btn");
const tabRegistrationsBtn = document.getElementById("tab-registrations-btn");
const sectionEventsTable = document.getElementById("section-events-table");
const sectionRegistrationsTable = document.getElementById("section-registrations-table");

const refreshBtn = document.getElementById("refresh-btn");

// Add Event Modal
const addEventModal = document.getElementById("add-event-modal");
const addEventCard = document.getElementById("add-event-card");
const openAddEventBtn = document.getElementById("open-add-event-btn");
const closeAddEventBtn = document.getElementById("close-add-event-btn");
const addEventForm = document.getElementById("add-event-form");
const addEventAlert = document.getElementById("add-event-alert");

// -------------------------------------------------------------
// Category styling
// -------------------------------------------------------------
function getCategoryPill(category) {
  const map = {
    technical: "bg-cyan-950 text-cyan-300 border-cyan-800",
    cultural: "bg-purple-950 text-purple-300 border-purple-800",
    sports: "bg-emerald-950 text-emerald-300 border-emerald-800",
    gaming: "bg-rose-950 text-rose-300 border-rose-800",
    workshop: "bg-amber-950 text-amber-300 border-amber-800",
  };
  const cls = map[category?.toLowerCase()] || "bg-slate-800 text-slate-300 border-slate-700";
  return `<span class="inline-block text-[11px] font-semibold px-2 py-0.5 rounded-full border ${cls}">${category}</span>`;
}

// -------------------------------------------------------------
// 1. Fetch Live Stats
// -------------------------------------------------------------
async function fetchStats() {
  try {
    const res = await fetch("/api/stats");
    if (!res.ok) throw new Error("Failed to fetch dashboard stats");
    const data = await res.json();

    statTotalEvents.textContent = data.total_events;
    statTotalRegistrations.textContent = data.total_registrations;
    statUtilization.textContent = `${data.overall_utilization_pct}%`;
    statCapacityDetails.textContent = `Of ${data.total_capacity} total seats filled`;
    statUpcomingEvents.textContent = data.upcoming_events_count;

    // Category breakdown
    categoryDistribution.innerHTML = "";
    const categories = Object.entries(data.events_by_category || {});
    if (categories.length === 0) {
      categoryDistribution.innerHTML = '<span class="text-xs text-slate-400">No categories found.</span>';
    } else {
      categories.forEach(([cat, count]) => {
        const pct = data.total_events > 0 ? Math.round((count / data.total_events) * 100) : 0;
        const pill = document.createElement("div");
        pill.className = "flex items-center gap-2 px-3 py-1.5 rounded-xl bg-slate-850 border border-slate-700/80 text-xs";
        pill.innerHTML = `
          ${getCategoryPill(cat)}
          <span class="font-bold text-white">${count} events</span>
          <span class="text-slate-400 text-[11px]">(${pct}%)</span>
        `;
        categoryDistribution.appendChild(pill);
      });
    }
  } catch (err) {
    console.error("Error fetching stats:", err);
    statTotalEvents.textContent = "!";
    statTotalRegistrations.textContent = "!";
    statUtilization.textContent = "Err";
  }
}

// -------------------------------------------------------------
// 2. Fetch Events & Render Utilization Table
// -------------------------------------------------------------
async function fetchEventsTable() {
  eventsTableBody.innerHTML = `
    <tr>
      <td colspan="7" class="py-6 text-center text-slate-400">Loading events...</td>
    </tr>
  `;

  try {
    const res = await fetch("/api/events");
    if (!res.ok) throw new Error("Failed to load events");
    const events = await res.json();
    allEvents = events;

    // Populate dropdown filter on registrations tab
    populateEventFilterDropdown(events);

    renderEventsTable();
  } catch (err) {
    eventsTableBody.innerHTML = `
      <tr>
        <td colspan="7" class="py-6 text-center text-rose-400 font-medium">
          Error loading events: ${err.message}
        </td>
      </tr>
    `;
  }
}

function renderEventsTable() {
  // Compute utilization & sort descending
  let processed = allEvents.map((e) => {
    const registered = e.capacity - e.available_seats;
    const utilizationPct = e.capacity > 0 ? Number(((registered / e.capacity) * 100).toFixed(1)) : 0;
    return {
      ...e,
      registered,
      utilizationPct,
    };
  });

  // Filter by search query if any
  if (tableSearchQuery.trim()) {
    const q = tableSearchQuery.toLowerCase();
    processed = processed.filter(
      (e) =>
        e.title.toLowerCase().includes(q) ||
        e.category.toLowerCase().includes(q) ||
        e.venue.toLowerCase().includes(q)
    );
  }

  // Sort by utilization percentage descending
  processed.sort((a, b) => b.utilizationPct - a.utilizationPct);

  if (processed.length === 0) {
    eventsTableBody.innerHTML = `
      <tr>
        <td colspan="7" class="py-8 text-center text-slate-400">
          No events match your search query.
        </td>
      </tr>
    `;
    return;
  }

  eventsTableBody.innerHTML = "";

  processed.forEach((e) => {
    const isWarning = e.utilizationPct >= 90;
    const isFull = e.available_seats === 0;

    const row = document.createElement("tr");
    // Highlight in warning color if >= 90% capacity
    row.className = isWarning
      ? "bg-rose-950/25 border-l-4 border-l-rose-500 hover:bg-rose-950/40 transition"
      : "hover:bg-slate-750/50 transition";

    row.innerHTML = `
      <td class="py-3 px-4">
        <div class="flex items-center gap-2">
          <span class="text-xl select-none">${e.icon || "📅"}</span>
          <span class="font-bold text-white">${e.title}</span>
        </div>
      </td>
      <td class="py-3 px-3">
        ${getCategoryPill(e.category)}
      </td>
      <td class="py-3 px-3 text-slate-300">
        <div>${e.event_date}</div>
        <div class="text-[11px] text-slate-400">${e.event_time}</div>
      </td>
      <td class="py-3 px-3 text-slate-300">
        ${e.venue}
      </td>
      <td class="py-3 px-3 text-right font-medium">
        <span class="${isFull ? 'text-rose-400 font-bold' : 'text-slate-200'}">${e.registered}</span>
        <span class="text-slate-400">/ ${e.capacity}</span>
      </td>
      <td class="py-3 px-4 text-right">
        <div class="flex items-center justify-end gap-2">
          <div class="w-16 bg-slate-700 rounded-full h-1.5 hidden sm:block overflow-hidden">
            <div class="${isWarning ? 'bg-rose-500' : 'bg-emerald-400'} h-1.5 rounded-full" style="width: ${Math.min(100, e.utilizationPct)}%"></div>
          </div>
          <span class="font-bold ${isWarning ? 'text-rose-400' : 'text-emerald-400'}">
            ${e.utilizationPct}%
          </span>
        </div>
      </td>
      <td class="py-3 px-3 text-center">
        ${
          isFull
            ? `<span class="px-2 py-0.5 rounded text-[10px] font-black bg-rose-950 text-rose-300 border border-rose-800">FULL</span>`
            : isWarning
            ? `<span class="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-950 text-amber-300 border border-amber-800">HIGH DEMAND</span>`
            : `<span class="px-2 py-0.5 rounded text-[10px] font-medium bg-emerald-950 text-emerald-300 border border-emerald-800">OPEN</span>`
        }
      </td>
    `;
    eventsTableBody.appendChild(row);
  });
}

// -------------------------------------------------------------
// 3. Fetch Registrations
// -------------------------------------------------------------
function populateEventFilterDropdown(events) {
  const currentVal = regEventFilter.value;
  regEventFilter.innerHTML = '<option value="">All Events</option>';
  events.forEach((ev) => {
    const opt = document.createElement("option");
    opt.value = ev.id;
    opt.textContent = `${ev.title} (${ev.category})`;
    if (String(ev.id) === String(currentVal)) {
      opt.selected = true;
    }
    regEventFilter.appendChild(opt);
  });
}

async function fetchRegistrations() {
  registrationsTableBody.innerHTML = `
    <tr>
      <td colspan="8" class="py-6 text-center text-slate-400">Loading registrations...</td>
    </tr>
  `;

  try {
    const url = currentFilterEventId
      ? `/api/registrations?event_id=${currentFilterEventId}`
      : "/api/registrations";
    const res = await fetch(url);
    if (!res.ok) throw new Error("Failed to load registrations");
    const data = await res.json();
    allRegistrations = data;
    renderRegistrationsTable(data);
  } catch (err) {
    registrationsTableBody.innerHTML = `
      <tr>
        <td colspan="8" class="py-6 text-center text-rose-400 font-medium">
          Error loading registrations: ${err.message}
        </td>
      </tr>
    `;
  }
}

function renderRegistrationsTable(registrations) {
  if (registrations.length === 0) {
    registrationsTableBody.innerHTML = `
      <tr>
        <td colspan="8" class="py-8 text-center text-slate-400">
          No registrations recorded yet.
        </td>
      </tr>
    `;
    return;
  }

  registrationsTableBody.innerHTML = "";

  registrations.forEach((reg) => {
    const row = document.createElement("tr");
    row.className = "hover:bg-slate-750/50 transition";

    const formattedTime = new Date(reg.registered_at).toLocaleString(undefined, {
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });

    row.innerHTML = `
      <td class="py-3 px-4 font-mono text-xs text-slate-400">#${reg.id}</td>
      <td class="py-3 px-3 font-bold text-white">${reg.student_name}</td>
      <td class="py-3 px-3 font-mono text-slate-300">${reg.roll_number}</td>
      <td class="py-3 px-3 text-slate-300 font-mono text-xs">${reg.email}</td>
      <td class="py-3 px-3 text-slate-400">${reg.phone || "-"}</td>
      <td class="py-3 px-4 font-semibold text-purple-300">${reg.event_title}</td>
      <td class="py-3 px-3 text-slate-400 text-xs">${formattedTime}</td>
      <td class="py-3 px-3 text-center">
        <button
          onclick="handleCancelRegistration(${reg.id}, '${reg.student_name.replace(/'/g, "\\'")}', '${reg.event_title.replace(/'/g, "\\'")}')"
          class="px-2.5 py-1 text-xs rounded-lg bg-rose-950 hover:bg-rose-900 text-rose-300 border border-rose-800 transition"
          title="Cancel this registration"
        >
          Cancel
        </button>
      </td>
    `;
    registrationsTableBody.appendChild(row);
  });
}

// -------------------------------------------------------------
// Stretch Goal 2: Cancel Registration
// -------------------------------------------------------------
async function handleCancelRegistration(regId, studentName, eventTitle) {
  const confirmed = confirm(
    `Are you sure you want to cancel the registration for "${studentName}" in "${eventTitle}"?`
  );
  if (!confirmed) return;

  try {
    const res = await fetch(`/api/registrations/${regId}`, {
      method: "DELETE",
    });
    const data = await res.json();
    if (!res.ok) {
      alert("Error: " + (data.detail || "Could not cancel registration"));
      return;
    }

    // Refresh data immediately
    await refreshAllData();
  } catch (err) {
    alert("Network error: " + err.message);
  }
}

// -------------------------------------------------------------
// Stretch Goal 1: Add New Event
// -------------------------------------------------------------
function openAddEventModal() {
  addEventAlert.classList.add("hidden");
  addEventForm.reset();
  addEventModal.classList.remove("hidden");
  requestAnimationFrame(() => {
    addEventModal.classList.remove("opacity-0");
    addEventCard.classList.remove("scale-95");
    addEventCard.classList.add("scale-100");
  });
}

function closeAddEventModal() {
  addEventModal.classList.add("opacity-0");
  addEventCard.classList.remove("scale-100");
  addEventCard.classList.add("scale-95");
  setTimeout(() => {
    addEventModal.classList.add("hidden");
  }, 200);
}

addEventForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  addEventAlert.classList.add("hidden");

  const title = document.getElementById("new-title").value.trim();
  const category = document.getElementById("new-category").value;
  const icon = document.getElementById("new-icon").value.trim() || "📅";
  const description = document.getElementById("new-description").value.trim();
  const date = document.getElementById("new-date").value;
  const time = document.getElementById("new-time").value.trim();
  const venue = document.getElementById("new-venue").value.trim();
  const capacity = parseInt(document.getElementById("new-capacity").value, 10);
  const fee = parseInt(document.getElementById("new-fee").value, 10) || 0;

  if (!title || !category || !date || !time || !venue || isNaN(capacity) || capacity <= 0) {
    addEventAlert.className = "p-3 rounded-xl text-xs font-semibold bg-rose-950 text-rose-300 border border-rose-800";
    addEventAlert.textContent = "Please provide valid event information. Capacity must be > 0.";
    addEventAlert.classList.remove("hidden");
    return;
  }

  try {
    const res = await fetch("/api/events", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        title,
        category,
        icon,
        description,
        event_date: date,
        event_time: time,
        venue,
        capacity,
        fee,
      }),
    });

    const data = await res.json();
    if (res.status === 201) {
      addEventAlert.className = "p-3 rounded-xl text-xs font-semibold bg-emerald-950 text-emerald-300 border border-emerald-800";
      addEventAlert.textContent = `Event "${data.title}" added successfully!`;
      addEventAlert.classList.remove("hidden");
      await refreshAllData();
      setTimeout(closeAddEventModal, 1500);
    } else {
      addEventAlert.className = "p-3 rounded-xl text-xs font-semibold bg-rose-950 text-rose-300 border border-rose-800";
      addEventAlert.textContent = data.detail || "Failed to create event";
      addEventAlert.classList.remove("hidden");
    }
  } catch (err) {
    addEventAlert.className = "p-3 rounded-xl text-xs font-semibold bg-rose-950 text-rose-300 border border-rose-800";
    addEventAlert.textContent = "Network error: " + err.message;
    addEventAlert.classList.remove("hidden");
  }
});

// -------------------------------------------------------------
// Tab Switching
// -------------------------------------------------------------
tabEventsBtn.addEventListener("click", () => {
  tabEventsBtn.classList.add("border-purple-500", "text-white");
  tabEventsBtn.classList.remove("border-transparent", "text-slate-400");
  tabRegistrationsBtn.classList.remove("border-purple-500", "text-white");
  tabRegistrationsBtn.classList.add("border-transparent", "text-slate-400");

  sectionEventsTable.classList.remove("hidden");
  sectionRegistrationsTable.classList.add("hidden");
});

tabRegistrationsBtn.addEventListener("click", () => {
  tabRegistrationsBtn.classList.add("border-purple-500", "text-white");
  tabRegistrationsBtn.classList.remove("border-transparent", "text-slate-400");
  tabEventsBtn.classList.remove("border-purple-500", "text-white");
  tabEventsBtn.classList.add("border-transparent", "text-slate-400");

  sectionRegistrationsTable.classList.remove("hidden");
  sectionEventsTable.classList.add("hidden");
  fetchRegistrations();
});

// -------------------------------------------------------------
// Event listeners
// -------------------------------------------------------------
tableSearchInput.addEventListener("input", (e) => {
  tableSearchQuery = e.target.value;
  renderEventsTable();
});

regEventFilter.addEventListener("change", (e) => {
  currentFilterEventId = e.target.value;
  fetchRegistrations();
});

async function refreshAllData() {
  await Promise.all([fetchStats(), fetchEventsTable(), fetchRegistrations()]);
}

refreshBtn.addEventListener("click", refreshAllData);
openAddEventBtn.addEventListener("click", openAddEventModal);
closeAddEventBtn.addEventListener("click", closeAddEventModal);
addEventModal.addEventListener("click", (e) => {
  if (e.target === addEventModal) closeAddEventModal();
});

// Initial boot
document.addEventListener("DOMContentLoaded", () => {
  fetchStats();
  fetchEventsTable();
});
