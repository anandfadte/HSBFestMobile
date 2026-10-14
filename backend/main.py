from datetime import datetime
from pathlib import Path
from typing import List, Optional

from fastapi import FastAPI, HTTPException, Query, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles

from backend.database import get_db_connection, init_db
from backend.models import (
    EventCreate,
    EventDetail,
    EventOut,
    RegistrationIn,
    RegistrationItemOut,
    RegistrationOut,
    StatsOut,
)
from backend.seed_data import seed_events

BASE_DIR = Path(__file__).resolve().parent.parent
FRONTEND_DIR = BASE_DIR / "frontend" if (BASE_DIR / "frontend").exists() else (BASE_DIR / "public")

app = FastAPI(
    title="Zenith '26 Fest Management System API",
    description="API for College Fest Events, Registrations, and Admin Dashboard",
    version="1.0.0",
)

# Enable CORS for local development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
def startup_event():
    """Initialize database schema and seed data on startup."""
    init_db()
    seed_events()


# ==========================================
# Event Endpoints
# ==========================================


@app.get("/api/events", response_model=List[EventOut])
def list_events(
    category: Optional[str] = Query(None, description="Filter by event category"),
    search: Optional[str] = Query(None, description="Search events by title (case-insensitive)"),
):
    """
    List all events with live available_seats count.
    Optional query params: category, search.
    """
    conn = get_db_connection()
    cursor = conn.cursor()

    query = """
        SELECT e.id, e.title, e.category, e.description, e.event_date,
               e.event_time, e.venue, e.capacity, e.fee, e.icon,
               (e.capacity - COUNT(r.id)) AS available_seats
        FROM events e
        LEFT JOIN registrations r ON e.id = r.event_id
        WHERE 1=1
    """
    params = []

    if category and category.strip() and category.lower() != "all":
        query += " AND LOWER(e.category) = LOWER(?)"
        params.append(category.strip())

    if search and search.strip():
        query += " AND LOWER(e.title) LIKE LOWER(?)"
        params.append(f"%{search.strip()}%")

    query += " GROUP BY e.id, e.title, e.category, e.description, e.event_date, e.event_time, e.venue, e.capacity, e.fee, e.icon ORDER BY e.id ASC"

    cursor.execute(query, params)
    rows = cursor.fetchall()
    conn.close()

    events = []
    for row in rows:
        events.append(
            EventOut(
                id=row["id"],
                title=row["title"],
                category=row["category"],
                description=row["description"],
                event_date=row["event_date"],
                event_time=row["event_time"],
                venue=row["venue"],
                capacity=row["capacity"],
                fee=row["fee"],
                icon=row["icon"],
                available_seats=max(0, row["available_seats"]),
            )
        )
    return events


@app.get("/api/events/{event_id}", response_model=EventDetail)
def get_event(event_id: int):
    """Retrieve details for a single event, including available seats."""
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute(
        """
        SELECT e.id, e.title, e.category, e.description, e.event_date,
               e.event_time, e.venue, e.capacity, e.fee, e.icon,
               (e.capacity - COUNT(r.id)) AS available_seats
        FROM events e
        LEFT JOIN registrations r ON e.id = r.event_id
        WHERE e.id = ?
        GROUP BY e.id, e.title, e.category, e.description, e.event_date,
                 e.event_time, e.venue, e.capacity, e.fee, e.icon
        """,
        (event_id,),
    )
    row = cursor.fetchone()
    conn.close()

    if not row:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Event with id {event_id} not found",
        )

    return EventDetail(
        id=row["id"],
        title=row["title"],
        category=row["category"],
        description=row["description"],
        event_date=row["event_date"],
        event_time=row["event_time"],
        venue=row["venue"],
        capacity=row["capacity"],
        fee=row["fee"],
        icon=row["icon"],
        available_seats=max(0, row["available_seats"]),
    )


@app.post("/api/events/{event_id}/register", response_model=RegistrationOut, status_code=status.HTTP_201_CREATED)
def register_for_event(event_id: int, payload: RegistrationIn):
    """
    Register a student for an event.
    Re-validates all rules server-side:
      - event exists (404)
      - required fields not empty (400)
      - email contains '@' (400)
      - event not at capacity (409 "Event is full")
      - email not already registered for this event (409 "Already registered")
    """
    # 1. Field validation
    student_name = payload.student_name.strip() if payload.student_name else ""
    roll_number = payload.roll_number.strip() if payload.roll_number else ""
    email = payload.email.strip().lower() if payload.email else ""
    phone = payload.phone.strip() if payload.phone else None

    if not student_name or not roll_number or not email:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Student name, roll number, and email are required and cannot be empty",
        )

    if "@" not in email:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid email address: must contain '@'",
        )

    conn = get_db_connection()
    cursor = conn.cursor()

    # 2. Check if event exists
    cursor.execute("SELECT id, capacity FROM events WHERE id = ?", (event_id,))
    event_row = cursor.fetchone()
    if not event_row:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Event with id {event_id} not found",
        )

    capacity = event_row["capacity"]

    # 3. Check current registration count vs capacity
    cursor.execute("SELECT COUNT(*) AS count FROM registrations WHERE event_id = ?", (event_id,))
    reg_count = cursor.fetchone()["count"]
    if reg_count >= capacity:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Event is full",
        )

    # 4. Check if student email is already registered for this event
    cursor.execute(
        "SELECT id FROM registrations WHERE event_id = ? AND LOWER(email) = LOWER(?)",
        (event_id, email),
    )
    existing_reg = cursor.fetchone()
    if existing_reg:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Already registered",
        )

    # 5. Insert registration
    registered_at = datetime.now().isoformat()
    cursor.execute(
        """
        INSERT INTO registrations (event_id, student_name, roll_number, email, phone, registered_at)
        VALUES (?, ?, ?, ?, ?, ?)
        """,
        (event_id, student_name, roll_number, email, phone, registered_at),
    )
    new_id = cursor.lastrowid
    conn.commit()
    conn.close()

    return RegistrationOut(
        id=new_id,
        event_id=event_id,
        student_name=student_name,
        registered_at=registered_at,
    )


# ==========================================
# Registration & Admin Endpoints
# ==========================================


@app.get("/api/registrations", response_model=List[RegistrationItemOut])
def list_registrations(event_id: Optional[int] = Query(None, description="Filter by event id")):
    """
    List registrations with event titles.
    Optional query param: event_id.
    """
    conn = get_db_connection()
    cursor = conn.cursor()

    query = """
        SELECT r.id, r.event_id, r.student_name, r.roll_number, r.email,
               r.phone, r.registered_at, e.title AS event_title
        FROM registrations r
        JOIN events e ON r.event_id = e.id
        WHERE 1=1
    """
    params = []
    if event_id is not None:
        query += " AND r.event_id = ?"
        params.append(event_id)

    query += " ORDER BY r.id DESC"

    cursor.execute(query, params)
    rows = cursor.fetchall()
    conn.close()

    return [
        RegistrationItemOut(
            id=row["id"],
            event_id=row["event_id"],
            student_name=row["student_name"],
            roll_number=row["roll_number"],
            email=row["email"],
            phone=row["phone"],
            registered_at=row["registered_at"],
            event_title=row["event_title"],
        )
        for row in rows
    ]


@app.get("/api/stats", response_model=StatsOut)
def get_stats():
    """
    Live dashboard stats computed directly from database on each call.
    """
    conn = get_db_connection()
    cursor = conn.cursor()

    # Total events and total capacity
    cursor.execute("SELECT COUNT(*) AS total_events, COALESCE(SUM(capacity), 0) AS total_capacity FROM events")
    event_stats = cursor.fetchone()
    total_events = event_stats["total_events"]
    total_capacity = event_stats["total_capacity"]

    # Total registrations
    cursor.execute("SELECT COUNT(*) AS total_registrations FROM registrations")
    total_registrations = cursor.fetchone()["total_registrations"]

    # Overall utilization percentage
    overall_utilization_pct = 0.0
    if total_capacity > 0:
        overall_utilization_pct = round((total_registrations / total_capacity) * 100, 1)

    # Upcoming events count (event_date today or later)
    today_str = datetime.now().strftime("%Y-%m-%d")
    cursor.execute("SELECT COUNT(*) AS count FROM events WHERE event_date >= ?", (today_str,))
    upcoming_events_count = cursor.fetchone()["count"]

    # Events by category
    cursor.execute("SELECT category, COUNT(*) AS count FROM events GROUP BY category ORDER BY category")
    category_rows = cursor.fetchall()
    events_by_category = {row["category"]: row["count"] for row in category_rows}

    conn.close()

    return StatsOut(
        total_events=total_events,
        total_registrations=total_registrations,
        total_capacity=total_capacity,
        overall_utilization_pct=overall_utilization_pct,
        upcoming_events_count=upcoming_events_count,
        events_by_category=events_by_category,
    )


# ==========================================
# Stretch Goals Endpoints
# ==========================================


@app.post("/api/events", response_model=EventOut, status_code=status.HTTP_201_CREATED)
def create_event(payload: EventCreate):
    """
    Stretch Goal: Add a new event with full server-side validation.
    """
    title = payload.title.strip() if payload.title else ""
    category = payload.category.strip() if payload.category else ""
    venue = payload.venue.strip() if payload.venue else ""
    event_date = payload.event_date.strip() if payload.event_date else ""
    event_time = payload.event_time.strip() if payload.event_time else ""

    if not title or not category or not venue or not event_date or not event_time:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Title, category, venue, event_date, and event_time are required",
        )

    if payload.capacity <= 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Capacity must be greater than 0",
        )

    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute(
        """
        INSERT INTO events (title, category, description, event_date, event_time, venue, capacity, fee, icon)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        (
            title,
            category,
            payload.description.strip() if payload.description else None,
            event_date,
            event_time,
            venue,
            payload.capacity,
            max(0, payload.fee),
            payload.icon or "📅",
        ),
    )
    new_id = cursor.lastrowid
    conn.commit()
    conn.close()

    return EventOut(
        id=new_id,
        title=title,
        category=category,
        description=payload.description,
        event_date=event_date,
        event_time=event_time,
        venue=venue,
        capacity=payload.capacity,
        fee=max(0, payload.fee),
        icon=payload.icon or "📅",
        available_seats=payload.capacity,
    )


@app.delete("/api/registrations/{registration_id}")
def cancel_registration(registration_id: int):
    """
    Stretch Goal: Cancel a registration by id.
    """
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT id, event_id FROM registrations WHERE id = ?", (registration_id,))
    reg = cursor.fetchone()
    if not reg:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Registration with id {registration_id} not found",
        )

    cursor.execute("DELETE FROM registrations WHERE id = ?", (registration_id,))
    conn.commit()
    conn.close()
    return {"message": f"Registration {registration_id} successfully cancelled"}


# ==========================================
# Frontend Static Files Serving (Step 4)
# ==========================================

@app.get("/", include_in_schema=False)
def serve_index():
    index_file = FRONTEND_DIR / "index.html"
    if index_file.exists():
        return FileResponse(index_file)
    return {"message": "Frontend index.html not found"}


@app.get("/admin.html", include_in_schema=False)
def serve_admin():
    admin_file = FRONTEND_DIR / "admin.html"
    if admin_file.exists():
        return FileResponse(admin_file)
    return {"message": "Frontend admin.html not found"}


# Mount the js folder
if (FRONTEND_DIR / "js").exists():
    app.mount("/js", StaticFiles(directory=FRONTEND_DIR / "js"), name="js")

# Mount any other frontend static assets
if FRONTEND_DIR.exists():
    app.mount("/static", StaticFiles(directory=FRONTEND_DIR), name="static")
