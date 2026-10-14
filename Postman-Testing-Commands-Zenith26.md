# Postman Testing Commands: Zenith '26 API

**Tool:** Postman — desktop app
**Companion to:** Vibe-Coding-Prompts-Zenith26.md (Step 10–11) and Zenith26-Fest-System-Assignment.docx (Section 11)
**What this file is:** every request to run in Postman against your finished backend, with the exact method, URL, body, and expected response — so "testing with Postman" isn't a vague instruction, it's a checklist you work through request by request.

---

## Before you start

1. Install the Postman **desktop app** (free, no account required for local testing — the desktop app's API Client works signed out).
2. Start your FastAPI server: `uvicorn backend.main:app --reload` from the project root.
3. Open `http://localhost:8000/docs` in a browser first, just to confirm the server is up and all endpoints are listed — that's FastAPI's own automatic docs page, free with zero setup. If something's wrong with the server, you'll see it here before you even open Postman.
4. In Postman, create a new **Collection** called `Zenith 26 API`.
5. Create a new **Environment** called `Zenith 26 Local`, with one variable:
   - `base_url` = `http://localhost:8000`

   Select this environment (top-right dropdown) before sending any request below. Every URL in this file uses `{{base_url}}` instead of a hardcoded address — the same "don't hardcode what might change" habit from the database design, applied to API testing. If you ever test against a deployed version instead of your laptop, you change one variable instead of every request.
6. Inside the collection, create four folders to keep things organized: **Events**, **Registration**, **Admin**, **Stretch Goals**. Save each request below into the matching folder as you go — this is also roughly what Step 10 of the prompts file will ask Antigravity to generate automatically, so building it by hand here first will help you sanity-check that generated collection against this one.

For every request: **read the status code first, then the body.** A 200 with the wrong data is still a bug. A 400 or 409 with a clear message is the app working correctly, not a failure.

---

## Folder: Events

### 1. List all events
- **Method / URL:** `GET {{base_url}}/api/events`
- **Body:** none
- **Expected:** `200`, a JSON array of 14 events, each including a computed `available_seats` field.
- **Check:** `available_seats` equals `capacity` for every event right now, since nothing is registered yet.

### 2. Filter by category
- **Method / URL:** `GET {{base_url}}/api/events?category=Technical`
- **Body:** none
- **Expected:** `200`, only the 4 Technical events (Code Sprint 2.0, Robo Race, AI Pitch Fest, Web Design Rumble).
- **Check:** no Cultural, Sports, Gaming, or Workshop events appear.

### 3. Search by title
- **Method / URL:** `GET {{base_url}}/api/events?search=robo`
- **Body:** none
- **Expected:** `200`, one result — Robo Race. Lowercase `robo` should still match, confirming the search is case-insensitive.

### 4. Get one event — success
- **Method / URL:** `GET {{base_url}}/api/events/1`
- **Body:** none
- **Expected:** `200`, the full detail for event id 1, Code Sprint 2.0.

### 5. Get one event — not found
- **Method / URL:** `GET {{base_url}}/api/events/999`
- **Body:** none
- **Expected:** `404`, with a clear error message (not a stack trace or an empty body).
- **Check:** this is your first deliberate failure case — confirm the API fails *cleanly*, the way a real client app would need it to.

---

## Folder: Registration

Use event id `2` (Robo Race, capacity 40) for these unless noted otherwise.

### 6. Register — success
- **Method / URL:** `POST {{base_url}}/api/events/2/register`
- **Headers:** `Content-Type: application/json`
- **Body (raw JSON):**
  ```json
  {
    "student_name": "Aarav Mehta",
    "roll_number": "CSE2024001",
    "email": "aarav.mehta@example.edu",
    "phone": "9876543210"
  }
  ```
- **Expected:** `201`, a `RegistrationOut` body with the new registration's `id`, `event_id`, `student_name`, and a server-generated `registered_at` timestamp.
- **Check:** re-run request 1 (list all events) — Robo Race's `available_seats` should now be 39, computed live, not a number you set yourself.

### 7. Register — duplicate email (same event)
- **Method / URL:** same as above, unchanged body, run again right after request 6.
- **Expected:** `409`, with message `"Already registered"`.
- **Check:** this is the "never trust the client" rule proving itself — the database catches a duplicate even if a sloppy frontend didn't.

### 8. Register — missing required field
- **Method / URL:** `POST {{base_url}}/api/events/2/register`
- **Body (raw JSON):**
  ```json
  {
    "student_name": "",
    "roll_number": "CSE2024002",
    "email": "priya.rao@example.edu"
  }
  ```
- **Expected:** `400`, rejecting the empty `student_name` before it ever reaches the database.

### 9. Register — invalid email
- **Method / URL:** `POST {{base_url}}/api/events/2/register`
- **Body (raw JSON):**
  ```json
  {
    "student_name": "Priya Rao",
    "roll_number": "CSE2024002",
    "email": "priya.rao.at.example.edu"
  }
  ```
- **Expected:** `400` — no `@` in the email, so it's rejected server-side even though it "looks like" text a careless frontend might have let through.

### 10. Register — event not found
- **Method / URL:** `POST {{base_url}}/api/events/999/register`
- **Body:** reuse the valid body from request 6.
- **Expected:** `404` — the event id is checked before anything else happens.

### 11. Register — event full
This is the one case you can't trigger with the seed data as-is (smallest event still holds 32 seats). Two ways to get there:

- **Easiest:** open `data/events_seed.xlsx`, change one event's `capacity` to `1` (Chess Championship, id 11, is a good candidate), re-run the seed step so the database picks it up, then send request 6's style of request against event id 11 twice with two different emails. The second attempt should return `409 "Event is full"`.
- **Alternative:** temporarily edit `capacity` directly in `fest.db` for one event using a SQLite browser, if you don't want to re-seed.
- **Check:** this is the same `available_seats` logic from request 6 working at its boundary — confirm the error message distinguishes "full" from "already registered" (they're different 409s with different messages).

---

## Folder: Admin

### 12. List all registrations
- **Method / URL:** `GET {{base_url}}/api/registrations`
- **Expected:** `200`, every registration made so far, each including the event's title (not just its id).

### 13. List registrations for one event
- **Method / URL:** `GET {{base_url}}/api/registrations?event_id=2`
- **Expected:** `200`, only Robo Race's registrations.

### 14. Dashboard stats
- **Method / URL:** `GET {{base_url}}/api/stats`
- **Expected:** `200`, a `StatsOut` object: `total_events`, `total_registrations`, `total_capacity`, `overall_utilization_pct`, `upcoming_events_count`, `events_by_category`.
- **Check:** register one more student (repeat request 6 with a new email), call this endpoint again, and confirm `total_registrations` and `overall_utilization_pct` both changed — proof the numbers are computed live, not cached from an earlier call.

---

## Folder: Stretch Goals *(only if you built the stretch endpoints)*

### 15. Add a new event — success
- **Method / URL:** `POST {{base_url}}/api/events`
- **Headers:** `Content-Type: application/json`
- **Body (raw JSON):**
  ```json
  {
    "title": "Photography Walk",
    "category": "Cultural",
    "description": "Guided campus photo walk, open to all skill levels",
    "event_date": "2026-11-15",
    "event_time": "07:00 AM",
    "venue": "Main Gate",
    "capacity": 25,
    "fee": 0,
    "icon": "📷"
  }
  ```
- **Expected:** `201`, the new event with its assigned `id`.
- **Check:** run request 1 again — the new event now appears, 15 total instead of 14.

### 16. Add a new event — invalid
- **Method / URL:** `POST {{base_url}}/api/events`
- **Body (raw JSON):**
  ```json
  {
    "title": "",
    "category": "Cultural",
    "capacity": -5
  }
  ```
- **Expected:** `400` — empty title and a negative capacity should both be rejected.

### 17. Cancel a registration — success
- **Method / URL:** `DELETE {{base_url}}/api/registrations/1`
- **Expected:** `200`, and the registration is gone.
- **Check:** list that event again (request 1 or 4) — its `available_seats` went back up by one, immediately, live.

### 18. Cancel a registration — not found
- **Method / URL:** `DELETE {{base_url}}/api/registrations/999`
- **Expected:** `404`.

---

## Screenshot checklist (for your submission)

The assignment's deliverables call for Postman evidence showing at least one success and one failure response. These four cover it with room to spare:

- [ ] Request 6 (register — success, `201`)
- [ ] Request 7 or 9 (register — failure, `400`/`409`)
- [ ] Request 14 (stats, `200`) — pick a moment where the numbers clearly reflect registrations you made during testing
- [ ] Request 5 (not found, `404`)

Export the finished collection (Collection menu → Export) alongside your screenshots — that export *is* the `zenith26-api.postman_collection.json` file Step 10 of the prompts file asks for.
