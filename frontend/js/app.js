// State management
let currentCategory = "All";
let currentSearch = "";
let currentEvents = [];
let searchDebounceTimer = null;

// DOM Elements
const eventsGrid = document.getElementById("events-grid");
const loadingState = document.getElementById("loading-state");
const errorState = document.getElementById("error-state");
const errorMessage = document.getElementById("error-message");
const retryBtn = document.getElementById("retry-btn");
const emptyState = document.getElementById("empty-state");
const resetFiltersBtn = document.getElementById("reset-filters-btn");
const searchInput = document.getElementById("search-input");
const clearSearchBtn = document.getElementById("clear-search-btn");
const eventsCount = document.getElementById("events-count");
const categoryFilters = document.getElementById("category-filters");

// Modal Elements
const eventModal = document.getElementById("event-modal");
const modalCard = document.getElementById("modal-card");
const closeModalBtn = document.getElementById("close-modal-btn");
const modalIcon = document.getElementById("modal-icon");
const modalTitle = document.getElementById("modal-title");
const modalCategory = document.getElementById("modal-category");
const modalFee = document.getElementById("modal-fee");
const modalDate = document.getElementById("modal-date");
const modalTime = document.getElementById("modal-time");
const modalVenue = document.getElementById("modal-venue");
const modalDescription = document.getElementById("modal-description");
const modalSeatsBadge = document.getElementById("modal-seats-badge");
const registrationForm = document.getElementById("registration-form");
const formEventId = document.getElementById("form-event-id");
const submitRegisterBtn = document.getElementById("submit-register-btn");
const formAlert = document.getElementById("form-alert");

// Client Form Error Elements
const errStudentName = document.getElementById("err-student-name");
const errRollNumber = document.getElementById("err-roll-number");
const errEmail = document.getElementById("err-email");

// -------------------------------------------------------------
// Category Badge Helper
// -------------------------------------------------------------
function getCategoryBadgeClass(category) {
  switch (category?.toLowerCase()) {
    case "technical":
      return "bg-cyan-950 text-cyan-300 border-cyan-800/60";
    case "cultural":
      return "bg-purple-950 text-purple-300 border-purple-800/60";
    case "sports":
      return "bg-emerald-950 text-emerald-300 border-emerald-800/60";
    case "gaming":
      return "bg-rose-950 text-rose-300 border-rose-800/60";
    case "workshop":
      return "bg-amber-950 text-amber-300 border-amber-800/60";
    default:
      return "bg-slate-800 text-slate-300 border-slate-700";
  }
}

// -------------------------------------------------------------
// Fetch and Render Events
// -------------------------------------------------------------
async function fetchEvents() {
  // Show Loading state
  loadingState.classList.remove("hidden");
  errorState.classList.add("hidden");
  emptyState.classList.add("hidden");
  eventsGrid.classList.add("hidden");

  try {
    const params = new URLSearchParams();
    if (currentCategory && currentCategory !== "All") {
      params.append("category", currentCategory);
    }
    if (currentSearch && currentSearch.trim() !== "") {
      params.append("search", currentSearch.trim());
    }

    const url = `/api/events${params.toString() ? "?" + params.toString() : ""}`;
    const res = await fetch(url);

    if (!res.ok) {
      throw new Error(`Server returned status ${res.status}`);
    }

    const events = await res.json();
    currentEvents = events;
    loadingState.classList.add("hidden");

    eventsCount.textContent = events.length;

    if (events.length === 0) {
      emptyState.classList.remove("hidden");
      return;
    }

    renderEventsGrid(events);
    eventsGrid.classList.remove("hidden");
  } catch (err) {
    console.error("Failed to fetch events:", err);
    loadingState.classList.add("hidden");
    errorMessage.textContent = err.message || "Failed to connect to backend server. Make sure the API is running.";
    errorState.classList.remove("hidden");
  }
}

function renderEventsGrid(events) {
  eventsGrid.innerHTML = "";

  events.forEach((event, index) => {
    const card = document.createElement("div");
    card.className =
      "card-fade bg-slate-800/70 backdrop-blur border border-slate-700/80 rounded-2xl p-5 flex flex-col justify-between hover:border-purple-500/60 hover:shadow-xl hover:shadow-purple-900/20 transition-all duration-200 group";
    card.style.animationDelay = `${index * 40}ms`;

    const isFull = event.available_seats <= 0;
    const feeText = event.fee > 0 ? `₹${event.fee}` : "Free";
    const categoryBadgeClass = getCategoryBadgeClass(event.category);

    card.innerHTML = `
      <div>
        <div class="flex items-start justify-between gap-3 mb-3">
          <div class="flex items-center gap-2.5">
            <span class="text-3xl p-2 rounded-xl bg-slate-750 group-hover:scale-110 transition-transform select-none">
              ${event.icon || "✨"}
            </span>
            <div>
              <span class="inline-block text-[11px] font-bold px-2 py-0.5 rounded-full border ${categoryBadgeClass}">
                ${event.category}
              </span>
              <h3 class="text-lg font-bold text-white group-hover:text-purple-300 transition-colors line-clamp-1 mt-0.5">
                ${event.title}
              </h3>
            </div>
          </div>
          <div>
            ${
              isFull
                ? `<span class="px-2 py-0.5 text-xs font-black uppercase rounded bg-rose-950 text-rose-300 border border-rose-800 animate-pulse">FULL</span>`
                : `<span class="px-2 py-0.5 text-xs font-semibold rounded bg-slate-900 text-slate-300 border border-slate-700">${feeText}</span>`
            }
          </div>
        </div>

        <p class="text-xs text-slate-300 line-clamp-2 mb-4 leading-relaxed">
          ${event.description || "No description provided."}
        </p>

        <div class="space-y-1.5 text-xs text-slate-300 pt-3 border-t border-slate-700/60 mb-5">
          <div class="flex items-center gap-2">
            <span class="text-slate-400">📅</span>
            <span>${event.event_date} • ${event.event_time}</span>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-slate-400">📍</span>
            <span class="truncate">${event.venue}</span>
          </div>
          <div class="flex items-center justify-between pt-1 text-slate-300 font-medium">
            <span>Seats:</span>
            <span class="${isFull ? 'text-rose-400 font-bold' : 'text-emerald-400 font-bold'}">
              ${event.available_seats} / ${event.capacity} left
            </span>
          </div>
        </div>
      </div>

      <button
        onclick="openEventDetails(${event.id})"
        class="w-full py-2 px-3 text-xs sm:text-sm font-semibold rounded-xl bg-slate-750 hover:bg-purple-600 active:bg-purple-700 text-white border border-slate-700 hover:border-purple-500 transition-all duration-150 flex items-center justify-center gap-1.5 cursor-pointer shadow-sm"
      >
        <span>View Details & Register</span>
        <span class="text-xs">→</span>
      </button>
    `;

    eventsGrid.appendChild(card);
  });
}

// -------------------------------------------------------------
// Event Details & Registration Modal
// -------------------------------------------------------------
async function openEventDetails(eventId) {
  try {
    const res = await fetch(`/api/events/${eventId}`);
    if (!res.ok) throw new Error("Could not load event details");
    const event = await res.json();

    formEventId.value = event.id;
    modalIcon.textContent = event.icon || "✨";
    modalTitle.textContent = event.title;
    modalCategory.textContent = event.category;
    modalCategory.className = `text-xs font-semibold px-2.5 py-0.5 rounded-full border ${getCategoryBadgeClass(event.category)}`;
    modalFee.textContent = event.fee > 0 ? `₹${event.fee}` : "Free";
    modalDate.textContent = event.event_date;
    modalTime.textContent = event.event_time;
    modalVenue.textContent = event.venue;
    modalDescription.textContent = event.description || "Join us for this exciting event at Zenith '26!";

    // Seat availability info
    const isFull = event.available_seats <= 0;
    if (isFull) {
      modalSeatsBadge.textContent = "0 seats available • FULL";
      modalSeatsBadge.className = "font-bold text-xs px-2.5 py-0.5 rounded-full bg-rose-950 text-rose-300 border border-rose-800";
      submitRegisterBtn.disabled = true;
      submitRegisterBtn.textContent = "Registration Closed (Full)";
    } else {
      modalSeatsBadge.textContent = `${event.available_seats} / ${event.capacity} seats remaining`;
      modalSeatsBadge.className = "font-bold text-xs px-2.5 py-0.5 rounded-full bg-emerald-950 text-emerald-300 border border-emerald-800";
      submitRegisterBtn.disabled = false;
      submitRegisterBtn.textContent = "Register for Event";
    }

    // Reset Form
    registrationForm.reset();
    clearFormErrors();
    hideFormAlert();

    // Show modal with animation
    eventModal.classList.remove("hidden");
    requestAnimationFrame(() => {
      eventModal.classList.remove("opacity-0");
      modalCard.classList.remove("scale-95");
      modalCard.classList.add("scale-100");
    });
  } catch (err) {
    alert("Error fetching event details: " + err.message);
  }
}

function closeModal() {
  eventModal.classList.add("opacity-0");
  modalCard.classList.remove("scale-100");
  modalCard.classList.add("scale-95");
  setTimeout(() => {
    eventModal.classList.add("hidden");
    clearFormErrors();
    hideFormAlert();
  }, 200);
}

// -------------------------------------------------------------
// Form Validation & Registration Handling
// -------------------------------------------------------------
function clearFormErrors() {
  errStudentName.classList.add("hidden");
  errRollNumber.classList.add("hidden");
  errEmail.classList.add("hidden");
}

function showFormAlert(type, message) {
  formAlert.className = `mb-4 p-3 rounded-xl text-sm font-medium ${
    type === "success"
      ? "bg-emerald-950/80 text-emerald-200 border border-emerald-800"
      : "bg-rose-950/80 text-rose-200 border border-rose-800"
  }`;
  formAlert.textContent = message;
  formAlert.classList.remove("hidden");
}

function hideFormAlert() {
  formAlert.classList.add("hidden");
  formAlert.textContent = "";
}

registrationForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  clearFormErrors();
  hideFormAlert();

  const eventId = formEventId.value;
  const studentName = document.getElementById("student-name").value.trim();
  const rollNumber = document.getElementById("roll-number").value.trim();
  const email = document.getElementById("email").value.trim();
  const phone = document.getElementById("phone").value.trim();

  // Client-side validation checks
  let hasError = false;

  if (!studentName) {
    errStudentName.textContent = "Please enter your full name.";
    errStudentName.classList.remove("hidden");
    hasError = true;
  }

  if (!rollNumber) {
    errRollNumber.textContent = "Please enter your college roll number.";
    errRollNumber.classList.remove("hidden");
    hasError = true;
  }

  if (!email) {
    errEmail.textContent = "Please enter your email address.";
    errEmail.classList.remove("hidden");
    hasError = true;
  } else if (!email.includes("@")) {
    errEmail.textContent = "Email must contain '@'.";
    errEmail.classList.remove("hidden");
    hasError = true;
  }

  if (hasError) return;

  // Submit to server
  submitRegisterBtn.disabled = true;
  const originalBtnText = submitRegisterBtn.textContent;
  submitRegisterBtn.textContent = "Submitting registration...";

  try {
    const res = await fetch(`/api/events/${eventId}/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        student_name: studentName,
        roll_number: rollNumber,
        email: email,
        phone: phone || null,
      }),
    });

    const data = await res.json();

    if (res.status === 201) {
      showFormAlert(
        "success",
        `🎉 Registration Confirmed! ${data.student_name} is registered for ${modalTitle.textContent}.`
      );
      // Re-fetch events list to update seat count live on cards
      fetchEvents();

      // Automatically close modal after brief moment so user can see success
      setTimeout(() => {
        closeModal();
      }, 2000);
    } else {
      // Show exact server error message (e.g. "Event is full", "Already registered")
      const errMsg = data.detail || "Registration failed. Please check your details.";
      showFormAlert("error", errMsg);
      submitRegisterBtn.disabled = false;
      submitRegisterBtn.textContent = originalBtnText;
    }
  } catch (err) {
    console.error("Network error during registration:", err);
    showFormAlert("error", "Network error: Unable to contact server. Please retry.");
    submitRegisterBtn.disabled = false;
    submitRegisterBtn.textContent = originalBtnText;
  }
});

// -------------------------------------------------------------
// Search & Filter Listeners
// -------------------------------------------------------------
searchInput.addEventListener("input", (e) => {
  currentSearch = e.target.value;
  if (currentSearch.length > 0) {
    clearSearchBtn.classList.remove("hidden");
  } else {
    clearSearchBtn.classList.add("hidden");
  }

  clearTimeout(searchDebounceTimer);
  searchDebounceTimer = setTimeout(() => {
    fetchEvents();
  }, 250);
});

clearSearchBtn.addEventListener("click", () => {
  searchInput.value = "";
  currentSearch = "";
  clearSearchBtn.classList.add("hidden");
  fetchEvents();
});

categoryFilters.addEventListener("click", (e) => {
  const btn = e.target.closest(".filter-btn");
  if (!btn) return;

  const category = btn.getAttribute("data-category");
  currentCategory = category;

  // Update button active state
  document.querySelectorAll(".filter-btn").forEach((b) => {
    b.classList.remove("bg-purple-600", "text-white", "shadow-md", "shadow-purple-600/30");
    b.classList.add("bg-slate-800", "text-slate-300", "border", "border-slate-700");
  });

  btn.classList.add("bg-purple-600", "text-white", "shadow-md", "shadow-purple-600/30");
  btn.classList.remove("bg-slate-800", "text-slate-300", "border", "border-slate-700");

  fetchEvents();
});

resetFiltersBtn.addEventListener("click", () => {
  currentSearch = "";
  searchInput.value = "";
  clearSearchBtn.classList.add("hidden");
  currentCategory = "All";

  document.querySelectorAll(".filter-btn").forEach((b) => {
    if (b.getAttribute("data-category") === "All") {
      b.classList.add("bg-purple-600", "text-white", "shadow-md", "shadow-purple-600/30");
      b.classList.remove("bg-slate-800", "text-slate-300");
    } else {
      b.classList.remove("bg-purple-600", "text-white", "shadow-md", "shadow-purple-600/30");
      b.classList.add("bg-slate-800", "text-slate-300");
    }
  });

  fetchEvents();
});

retryBtn.addEventListener("click", fetchEvents);
closeModalBtn.addEventListener("click", closeModal);

// Close modal when clicking outside
eventModal.addEventListener("click", (e) => {
  if (e.target === eventModal) {
    closeModal();
  }
});

// ESC key to close modal
document.addEventListener("keydown", (e) => {
  if (e.key === "Escape" && !eventModal.classList.contains("hidden")) {
    closeModal();
  }
});

// Initial Load
document.addEventListener("DOMContentLoaded", fetchEvents);
