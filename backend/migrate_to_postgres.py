"""
Database Migration Script: SQLite (fest.db) -> PostgreSQL
Migrates all events and registrations from local SQLite to PostgreSQL (Vercel Postgres, Neon, Supabase, etc.)
Preserves all IDs, timestamps, foreign keys, and resets sequence counters.
"""

import argparse
import os
import sqlite3
import sys
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent
if str(BASE_DIR) not in sys.path:
    sys.path.insert(0, str(BASE_DIR))

import psycopg2
import psycopg2.extras

SQLITE_PATH = BASE_DIR / "fest.db"


def migrate_to_postgres(postgres_url: str):
    print("=" * 60)
    print(" Zenith '26 Database Migration: SQLite -> PostgreSQL")
    print("=" * 60)

    if not SQLITE_PATH.exists():
        print(f"Error: SQLite database file not found at {SQLITE_PATH}")
        sys.exit(1)

    # Normalize URL if needed
    if postgres_url.startswith("postgres://"):
        postgres_url = "postgresql://" + postgres_url[len("postgres://"):]
    if ("@localhost" not in postgres_url and "@127.0.0.1" not in postgres_url) and "sslmode=" not in postgres_url:
        separator = "&" if "?" in postgres_url else "?"
        postgres_url = f"{postgres_url}{separator}sslmode=require"

    # Connect to SQLite
    print(f"\n[1/5] Connecting to SQLite source: {SQLITE_PATH.name}...")
    sqlite_conn = sqlite3.connect(str(SQLITE_PATH))
    sqlite_conn.row_factory = sqlite3.Row
    sqlite_cur = sqlite_conn.cursor()

    # Connect to PostgreSQL
    print("[2/5] Connecting to PostgreSQL destination...")
    try:
        pg_conn = psycopg2.connect(postgres_url)
        pg_cur = pg_conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor)
        print("  -> Connected successfully to PostgreSQL!")
    except Exception as e:
        print(f"  -> Failed to connect to PostgreSQL: {e}")
        sqlite_conn.close()
        sys.exit(1)

    # Create schema in PostgreSQL
    print("\n[3/5] Initializing PostgreSQL schema (tables & constraints)...")
    pg_cur.execute(
        """
        CREATE TABLE IF NOT EXISTS events (
            id SERIAL PRIMARY KEY,
            title TEXT NOT NULL,
            category TEXT NOT NULL,
            description TEXT,
            event_date TEXT NOT NULL,
            event_time TEXT NOT NULL,
            venue TEXT NOT NULL,
            capacity INTEGER NOT NULL,
            fee INTEGER NOT NULL DEFAULT 0,
            icon TEXT
        );
        """
    )
    pg_cur.execute(
        """
        CREATE TABLE IF NOT EXISTS registrations (
            id SERIAL PRIMARY KEY,
            event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE,
            student_name TEXT NOT NULL,
            roll_number TEXT NOT NULL,
            email TEXT NOT NULL,
            phone TEXT,
            registered_at TEXT NOT NULL,
            UNIQUE(event_id, email)
        );
        """
    )
    pg_conn.commit()
    print("  -> PostgreSQL tables verified.")

    # Migrate Events
    print("\n[4/5] Migrating events data...")
    sqlite_cur.execute("SELECT * FROM events ORDER BY id ASC")
    events = sqlite_cur.fetchall()
    migrated_events = 0
    for e in events:
        pg_cur.execute(
            """
            INSERT INTO events (id, title, category, description, event_date, event_time, venue, capacity, fee, icon)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (id) DO UPDATE SET
                title = EXCLUDED.title,
                category = EXCLUDED.category,
                description = EXCLUDED.description,
                event_date = EXCLUDED.event_date,
                event_time = EXCLUDED.event_time,
                venue = EXCLUDED.venue,
                capacity = EXCLUDED.capacity,
                fee = EXCLUDED.fee,
                icon = EXCLUDED.icon;
            """,
            (
                e["id"],
                e["title"],
                e["category"],
                e["description"],
                e["event_date"],
                e["event_time"],
                e["venue"],
                e["capacity"],
                e["fee"],
                e["icon"],
            ),
        )
        migrated_events += 1
    pg_conn.commit()
    print(f"  -> Migrated/updated {migrated_events} events.")

    # Migrate Registrations
    print("\n[5/5] Migrating registrations data...")
    sqlite_cur.execute("SELECT * FROM registrations ORDER BY id ASC")
    registrations = sqlite_cur.fetchall()
    migrated_regs = 0
    for r in registrations:
        pg_cur.execute(
            """
            INSERT INTO registrations (id, event_id, student_name, roll_number, email, phone, registered_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (id) DO UPDATE SET
                event_id = EXCLUDED.event_id,
                student_name = EXCLUDED.student_name,
                roll_number = EXCLUDED.roll_number,
                email = EXCLUDED.email,
                phone = EXCLUDED.phone,
                registered_at = EXCLUDED.registered_at;
            """,
            (
                r["id"],
                r["event_id"],
                r["student_name"],
                r["roll_number"],
                r["email"],
                r["phone"],
                r["registered_at"],
            ),
        )
        migrated_regs += 1
    pg_conn.commit()
    print(f"  -> Migrated/updated {migrated_regs} registrations.")

    # Reset serial sequences in PostgreSQL
    print("\nSynchronizing PostgreSQL sequences...")
    pg_cur.execute(
        """
        SELECT setval(
            pg_get_serial_sequence('events', 'id'),
            COALESCE((SELECT MAX(id) FROM events), 1),
            (SELECT MAX(id) IS NOT NULL FROM events)
        );
        SELECT setval(
            pg_get_serial_sequence('registrations', 'id'),
            COALESCE((SELECT MAX(id) FROM registrations), 1),
            (SELECT MAX(id) IS NOT NULL FROM registrations)
        );
        """
    )
    pg_conn.commit()

    # Verification
    pg_cur.execute("SELECT COUNT(*) AS count FROM events")
    pg_event_count = pg_cur.fetchone()["count"]
    pg_cur.execute("SELECT COUNT(*) AS count FROM registrations")
    pg_reg_count = pg_cur.fetchone()["count"]

    print("\n" + "=" * 60)
    print(" Migration Complete & Verified!")
    print("=" * 60)
    print(f"  SQLite source:       {len(events)} events, {len(registrations)} registrations")
    print(f"  PostgreSQL target:   {pg_event_count} events, {pg_reg_count} registrations")
    print("=" * 60 + "\n")

    sqlite_conn.close()
    pg_conn.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Migrate Zenith '26 database from SQLite to PostgreSQL")
    parser.add_argument(
        "--postgres-url",
        "-u",
        default=os.getenv("DATABASE_URL") or os.getenv("POSTGRES_URL"),
        help="PostgreSQL connection string (e.g. postgresql://user:pass@host:5432/dbname)",
    )
    args = parser.parse_args()

    if not args.postgres_url:
        print("Error: No PostgreSQL connection URL provided.")
        print("Please provide it via --postgres-url or set the DATABASE_URL / POSTGRES_URL environment variable.")
        print("\nUsage example:")
        print("  python -m backend.migrate_to_postgres --postgres-url 'postgresql://postgres:password@localhost:5432/zenithdb'")
        sys.exit(1)

    migrate_to_postgres(args.postgres_url)
