import os
import re
import sqlite3
from pathlib import Path
from typing import Any, List, Optional, Tuple, Union

try:
    import psycopg2
    import psycopg2.extras
    PSYCOPG2_AVAILABLE = True
except ImportError:
    PSYCOPG2_AVAILABLE = False

# Database file location in the project root (for SQLite fallback)
BASE_DIR = Path(__file__).resolve().parent.parent
DB_PATH = BASE_DIR / "fest.db"


def get_database_url() -> Optional[str]:
    """Retrieve database URL from standard environment variables."""
    url = (
        os.getenv("DATABASE_URL")
        or os.getenv("POSTGRES_URL")
        or os.getenv("POSTGRES_PRISMA_URL")
        or os.getenv("POSTGRES_URL_NON_POOLING")
        or os.getenv("SUPABASE_DB_URL")
    )
    if url:
        url = url.strip()
        # Normalize postgres:// to postgresql:// for compatibility if needed
        if url.startswith("postgres://"):
            url = "postgresql://" + url[len("postgres://"):]
        # Ensure sslmode=require for cloud-hosted databases if not localhost
        if ("@localhost" not in url and "@127.0.0.1" not in url) and "sslmode=" not in url:
            separator = "&" if "?" in url else "?"
            url = f"{url}{separator}sslmode=require"
        return url
    return None


def is_postgres() -> bool:
    """Return True if a PostgreSQL connection string is configured."""
    url = get_database_url()
    return bool(url and (url.startswith("postgres://") or url.startswith("postgresql://")))


class PostgresCursorWrapper:
    """
    Wraps a psycopg2 DictCursor to provide seamless SQLite-compatible behavior:
      - Converts '?' parameter placeholders to '%s'
      - Sets cursor.lastrowid on INSERT using RETURNING id
      - Exposes fetchone, fetchall, and dict-like column access
    """

    def __init__(self, real_cursor):
        self._cursor = real_cursor
        self.lastrowid = None

    def execute(self, query: str, params: Any = None):
        # Convert SQLite ? placeholders to %s
        converted_query = query.replace("?", "%s")

        # Automatically append RETURNING id for INSERT queries if not already present
        stripped = converted_query.strip()
        is_insert = stripped.upper().startswith("INSERT INTO")
        has_returning = "RETURNING" in stripped.upper()

        if is_insert and not has_returning:
            converted_query += " RETURNING id"
            if params is not None:
                self._cursor.execute(converted_query, params)
            else:
                self._cursor.execute(converted_query)

            try:
                row = self._cursor.fetchone()
                if row and "id" in row:
                    self.lastrowid = row["id"]
            except Exception:
                self.lastrowid = None
        else:
            if params is not None:
                self._cursor.execute(converted_query, params)
            else:
                self._cursor.execute(converted_query)

        return self

    def fetchone(self):
        return self._cursor.fetchone()

    def fetchall(self):
        return self._cursor.fetchall()

    def fetchmany(self, size=None):
        return self._cursor.fetchmany(size) if size else self._cursor.fetchmany()

    @property
    def rowcount(self):
        return self._cursor.rowcount

    def close(self):
        self._cursor.close()

    def __iter__(self):
        return iter(self._cursor)


class PostgresConnectionWrapper:
    """
    Wraps a psycopg2 connection to mimic sqlite3.Connection methods.
    """

    def __init__(self, real_conn):
        self._conn = real_conn

    def cursor(self):
        return PostgresCursorWrapper(
            self._conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor)
        )

    def execute(self, query: str, params: Any = None):
        c = self.cursor()
        c.execute(query, params)
        return c

    def commit(self):
        self._conn.commit()

    def rollback(self):
        self._conn.rollback()

    def close(self):
        self._conn.close()


def get_db_connection():
    """
    Connect to PostgreSQL if DATABASE_URL or POSTGRES_URL is configured,
    otherwise connect to the local SQLite database.
    """
    if is_postgres():
        if not PSYCOPG2_AVAILABLE:
            raise RuntimeError(
                "PostgreSQL connection configured but psycopg2 is not installed. "
                "Run `pip install psycopg2-binary` to install it."
            )
        url = get_database_url()
        conn = psycopg2.connect(url)
        return PostgresConnectionWrapper(conn)
    else:
        conn = sqlite3.connect(str(DB_PATH))
        conn.row_factory = sqlite3.Row
        conn.execute("PRAGMA foreign_keys = ON")
        return conn


def sync_postgres_sequences(conn=None):
    """
    Synchronize PostgreSQL SERIAL sequences with current max(id)
    so future auto-increment IDs do not conflict with seeded IDs.
    """
    if not is_postgres():
        return

    close_at_end = False
    if conn is None:
        conn = get_db_connection()
        close_at_end = True

    try:
        cursor = conn.cursor()
        # Events sequence
        cursor.execute(
            """
            SELECT setval(
                pg_get_serial_sequence('events', 'id'),
                COALESCE((SELECT MAX(id) FROM events), 1),
                (SELECT MAX(id) IS NOT NULL FROM events)
            )
            """
        )
        # Registrations sequence
        cursor.execute(
            """
            SELECT setval(
                pg_get_serial_sequence('registrations', 'id'),
                COALESCE((SELECT MAX(id) FROM registrations), 1),
                (SELECT MAX(id) IS NOT NULL FROM registrations)
            )
            """
        )
        conn.commit()
    except Exception as e:
        print(f"Notice: Sequence sync skipped or non-applicable: {e}")
    finally:
        if close_at_end:
            conn.close()


def init_db():
    """Create the events and registrations tables if they do not exist."""
    conn = get_db_connection()
    cursor = conn.cursor()

    if is_postgres():
        cursor.execute(
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
            )
            """
        )

        cursor.execute(
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
            )
            """
        )
        conn.commit()
        sync_postgres_sequences(conn)
    else:
        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                category TEXT NOT NULL,
                description TEXT,
                event_date TEXT NOT NULL,
                event_time TEXT NOT NULL,
                venue TEXT NOT NULL,
                capacity INTEGER NOT NULL,
                fee INTEGER NOT NULL DEFAULT 0,
                icon TEXT
            )
            """
        )

        cursor.execute(
            """
            CREATE TABLE IF NOT EXISTS registrations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event_id INTEGER NOT NULL REFERENCES events(id),
                student_name TEXT NOT NULL,
                roll_number TEXT NOT NULL,
                email TEXT NOT NULL,
                phone TEXT,
                registered_at TEXT NOT NULL,
                UNIQUE(event_id, email)
            )
            """
        )
        conn.commit()

    conn.close()


if __name__ == "__main__":
    init_db()
    db_type = "PostgreSQL" if is_postgres() else "SQLite"
    print(f"Database ({db_type}) initialized successfully.")
