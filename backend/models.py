from typing import Optional, Dict
from pydantic import BaseModel, Field


class EventOut(BaseModel):
    id: int
    title: str
    category: str
    description: Optional[str] = None
    event_date: str
    event_time: str
    venue: str
    capacity: int
    fee: int = 0
    icon: Optional[str] = None
    available_seats: int


# EventDetail uses the same shape as EventOut for this project
class EventDetail(EventOut):
    pass


class RegistrationIn(BaseModel):
    student_name: str = Field(..., description="Full name of the student")
    roll_number: str = Field(..., description="College roll number")
    email: str = Field(..., description="Student email address")
    phone: Optional[str] = Field(None, description="Optional contact phone number")


class RegistrationOut(BaseModel):
    id: int
    event_id: int
    student_name: str
    registered_at: str


class RegistrationItemOut(BaseModel):
    id: int
    event_id: int
    student_name: str
    roll_number: str
    email: str
    phone: Optional[str] = None
    registered_at: str
    event_title: str


class StatsOut(BaseModel):
    total_events: int
    total_registrations: int
    total_capacity: int
    overall_utilization_pct: float
    upcoming_events_count: int
    events_by_category: Dict[str, int]


# Stretch Goal Model for creating a new event
class EventCreate(BaseModel):
    title: str = Field(..., min_length=1)
    category: str = Field(..., min_length=1)
    description: Optional[str] = None
    event_date: str = Field(..., min_length=1)
    event_time: str = Field(..., min_length=1)
    venue: str = Field(..., min_length=1)
    capacity: int = Field(..., gt=0)
    fee: int = 0
    icon: Optional[str] = "📅"
