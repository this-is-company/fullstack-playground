from __future__ import annotations

import os
import tempfile
from datetime import datetime
from pathlib import Path

# Must set before importing app modules (settings / engine are load-time).
os.environ["DATABASE_URL"] = "sqlite+pysqlite:///:memory:"
_UPLOAD_ROOT = tempfile.mkdtemp(prefix="enum-sqlalchemy-uploads-")
os.environ["UPLOAD_DIR"] = _UPLOAD_ROOT

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from app.db import Base, SessionLocal, engine, get_db
from app.domain.enums import OrderStatus, PayMethod, UserGrade
from app.domain.order_service import OrderService
from app.main import app
from app.web.schemas import OrderItemRequest, OrderRequest


@pytest.fixture(autouse=True)
def _reset_schema():
    Base.metadata.drop_all(bind=engine)
    Base.metadata.create_all(bind=engine)
    yield
    Base.metadata.drop_all(bind=engine)
    # clean upload leftovers between tests
    root = Path(_UPLOAD_ROOT)
    if root.exists():
        for child in root.iterdir():
            if child.is_file():
                child.unlink(missing_ok=True)


@pytest.fixture
def db() -> Session:
    session = SessionLocal()
    try:
        yield session
        session.commit()
    except Exception:
        session.rollback()
        raise
    finally:
        session.close()


@pytest.fixture
def client(db: Session):
    def override_get_db():
        try:
            yield db
            db.commit()
        except Exception:
            db.rollback()
            raise

    app.dependency_overrides[get_db] = override_get_db
    # Mirror MockMvc: assert on error JSON instead of re-raising server exceptions.
    with TestClient(app, raise_server_exceptions=False) as test_client:
        yield test_client
    app.dependency_overrides.clear()


def seed_order(
    db: Session,
    customer_name: str,
    status: OrderStatus,
    pay_method: PayMethod,
    quantity: int,
    created_at: datetime,
    user_grade: UserGrade = UserGrade.BASIC,
):
    """Seed via OrderService.create_with_created_at (mirrors Kotlin helpers)."""
    request = OrderRequest(
        customerName=customer_name,
        status=status,
        payMethod=pay_method,
        userGrade=user_grade,
        items=[
            OrderItemRequest(productName="Item", sku="SKU-1", quantity=quantity),
        ],
    )
    service = OrderService(db)
    response = service.create_with_created_at(request, created_at)
    db.flush()
    return response
