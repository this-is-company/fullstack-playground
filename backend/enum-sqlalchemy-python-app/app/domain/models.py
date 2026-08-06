from __future__ import annotations

from datetime import datetime
from enum import Enum
from typing import List, Optional

from sqlalchemy import BigInteger, DateTime, ForeignKey, Integer, String, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db import Base
from app.domain.enums import CodeEnumType, OrderStatus, PayMethod, UserGrade


class FileAssetStatus(str, Enum):
    TEMP = "TEMP"
    USED = "USED"


class Order(Base):
    __tablename__ = "pqdsl_demo_orders"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    customer_name: Mapped[str] = mapped_column(String(100), nullable=False)
    status: Mapped[Optional[OrderStatus]] = mapped_column(
        CodeEnumType(OrderStatus, 20), nullable=True
    )
    pay_method: Mapped[Optional[PayMethod]] = mapped_column(
        CodeEnumType(PayMethod, 20), nullable=True
    )
    user_grade: Mapped[UserGrade] = mapped_column(
        CodeEnumType(UserGrade, 20), nullable=False
    )
    created_at: Mapped[datetime] = mapped_column(
        DateTime, nullable=False, server_default=func.now()
    )

    items: Mapped[List[OrderItem]] = relationship(
        "OrderItem",
        back_populates="order",
        cascade="all, delete-orphan",
        lazy="selectin",
    )

    def replace_items(self, new_items: List[OrderItem]) -> None:
        self.items.clear()
        for item in new_items:
            self.add_item(item)

    def add_item(self, item: OrderItem) -> None:
        # append only — back_populates syncs item.order (avoid double-add)
        self.items.append(item)


class OrderItem(Base):
    __tablename__ = "pqdsl_demo_order_items"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    order_id: Mapped[int] = mapped_column(
        Integer, ForeignKey("pqdsl_demo_orders.id", ondelete="CASCADE"), nullable=False
    )
    product_name: Mapped[str] = mapped_column(String(100), nullable=False)
    sku: Mapped[str] = mapped_column(String(50), nullable=False)
    quantity: Mapped[int] = mapped_column(Integer, nullable=False)

    order: Mapped[Order] = relationship("Order", back_populates="items")


class FileAsset(Base):
    __tablename__ = "pqdsl_demo_files"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    original_name: Mapped[str] = mapped_column(String(255), nullable=False)
    stored_name: Mapped[str] = mapped_column(String(255), nullable=False)
    content_type: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    size_bytes: Mapped[int] = mapped_column(BigInteger, nullable=False)
    status: Mapped[str] = mapped_column(String(20), nullable=False)
    owner_ref: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime, nullable=False, server_default=func.now()
    )
