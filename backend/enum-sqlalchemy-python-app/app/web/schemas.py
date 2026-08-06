from __future__ import annotations

from datetime import datetime
from typing import Annotated, Any, Literal, Optional

from pydantic import (
    BaseModel,
    BeforeValidator,
    ConfigDict,
    Field,
    PlainSerializer,
    field_validator,
)

_CAMEL = ConfigDict(populate_by_name=True, serialize_by_alias=True)

from app.common.code_enum import CodeEnums
from app.domain.enums import OrderStatus, PayMethod, UserGrade
from app.web.validators import (
    local_date_string,
    local_datetime_string,
    no_special_chars,
    promo_code_pattern,
)


def _blank_to_none(v: Any) -> Any:
    if isinstance(v, str) and not v.strip():
        return None
    return v


def _code_enum_validator(enum_type: type):
    def _validate(v: Any) -> Any:
        v = _blank_to_none(v)
        if v is None:
            return None
        if isinstance(v, enum_type):
            return v
        if isinstance(v, str):
            return CodeEnums.from_code(enum_type, v)
        raise ValueError(f"Invalid value for {enum_type.__name__}")

    return _validate


def _code_serializer(v: Any) -> Optional[str]:
    if v is None:
        return None
    return v.code


OrderStatusCode = Annotated[
    Optional[OrderStatus],
    BeforeValidator(_code_enum_validator(OrderStatus)),
    PlainSerializer(_code_serializer, return_type=Optional[str]),
]
PayMethodCode = Annotated[
    Optional[PayMethod],
    BeforeValidator(_code_enum_validator(PayMethod)),
    PlainSerializer(_code_serializer, return_type=Optional[str]),
]
UserGradeCode = Annotated[
    Optional[UserGrade],
    BeforeValidator(_code_enum_validator(UserGrade)),
    PlainSerializer(_code_serializer, return_type=Optional[str]),
]


class OrderItemRequest(BaseModel):
    model_config = _CAMEL

    product_name: str | None = Field(default=None, alias="productName")
    sku: str | None = None
    quantity: int | None = None

    @field_validator("product_name", "sku", mode="before")
    @classmethod
    def _no_special(cls, v: Any) -> Any:
        if v is None:
            return v
        return no_special_chars(str(v) if not isinstance(v, str) else v)


class OrderRequest(BaseModel):
    model_config = _CAMEL

    id: int | None = None
    customer_name: str | None = Field(default=None, alias="customerName")
    status: OrderStatusCode = None
    pay_method: PayMethodCode = Field(default=None, alias="payMethod")
    user_grade: UserGradeCode = Field(default=None, alias="userGrade")
    promo_code: str | None = Field(default=None, alias="promoCode")
    items: list[OrderItemRequest] | None = None

    @field_validator("customer_name", mode="before")
    @classmethod
    def _customer_no_special(cls, v: Any) -> Any:
        if v is None:
            return v
        return no_special_chars(str(v) if not isinstance(v, str) else v)

    @field_validator("promo_code", mode="before")
    @classmethod
    def _promo(cls, v: Any) -> Any:
        return promo_code_pattern(v)

    def validate_for(self, mode: Literal["create", "update"]) -> None:
        errors: dict[str, str] = {}

        if mode == "create" and self.id is not None:
            errors["id"] = "id must be null on create"

        if self.customer_name is None or not str(self.customer_name).strip():
            errors["customerName"] = "must not be blank"

        if mode == "create":
            if self.status is None:
                errors["status"] = "status is required on create"
            if self.pay_method is None:
                errors["payMethod"] = "payMethod is required on create"

        if self.user_grade is None:
            errors["userGrade"] = "must not be null"

        if self.items is None:
            errors["items"] = "list must not be null"
        elif len(self.items) == 0:
            errors["items"] = "list must not be empty"
        else:
            for i, item in enumerate(self.items):
                if item.product_name is None or not str(item.product_name).strip():
                    errors[f"items[{i}].productName"] = (
                        f"list[{i}].productName must not be null or blank"
                    )
                if item.sku is None or not str(item.sku).strip():
                    errors[f"items[{i}].sku"] = f"list[{i}].sku must not be null or blank"
                if item.quantity is None:
                    errors[f"items[{i}].quantity"] = "must not be null"
                elif item.quantity <= 0:
                    errors[f"items[{i}].quantity"] = "must be greater than 0"

        if errors:
            raise ValidationFailed(errors)


class ValidationFailed(Exception):
    def __init__(self, errors: dict[str, str]) -> None:
        self.errors = errors
        super().__init__("Validation failed")


class OrderItemResponse(BaseModel):
    model_config = _CAMEL

    id: int | None = None
    product_name: str | None = Field(default=None, alias="productName")
    sku: str | None = None
    quantity: int | None = None


def _dt_serializer(v: Optional[datetime]) -> Optional[str]:
    if v is None:
        return None
    return v.strftime("%Y-%m-%dT%H:%M:%S")


class OrderResponse(BaseModel):
    model_config = _CAMEL

    id: Optional[int] = None
    customer_name: Optional[str] = Field(default=None, alias="customerName")
    status: OrderStatusCode = None
    status_description: Optional[str] = Field(default=None, alias="statusDescription")
    pay_method: PayMethodCode = Field(default=None, alias="payMethod")
    pay_method_description: Optional[str] = Field(default=None, alias="payMethodDescription")
    user_grade: UserGradeCode = Field(default=None, alias="userGrade")
    user_grade_description: Optional[str] = Field(default=None, alias="userGradeDescription")
    created_at: Annotated[
        Optional[datetime],
        PlainSerializer(_dt_serializer, return_type=Optional[str]),
    ] = Field(default=None, alias="createdAt")
    items: Optional[list[OrderItemResponse]] = None


class OrderDateSearchRequest(BaseModel):
    model_config = _CAMEL

    order_date: str | None = Field(default=None, alias="orderDate")
    from_date: str | None = Field(default=None, alias="fromDate")
    to_date: str | None = Field(default=None, alias="toDate")
    from_date_time: str | None = Field(default=None, alias="fromDateTime")
    to_date_time: str | None = Field(default=None, alias="toDateTime")
    min_quantity: int | None = Field(default=None, alias="minQuantity")

    @field_validator("order_date", "from_date", "to_date", mode="before")
    @classmethod
    def _dates(cls, v: Any) -> Any:
        return local_date_string(v)

    @field_validator("from_date_time", "to_date_time", mode="before")
    @classmethod
    def _datetimes(cls, v: Any) -> Any:
        return local_datetime_string(v)


class OrderFlexibleSearchRequest(BaseModel):
    model_config = _CAMEL

    customer_name: str | None = Field(default=None, alias="customerName")
    status: OrderStatusCode = None
    pay_method: PayMethodCode = Field(default=None, alias="payMethod")
    min_quantity: int | None = Field(default=None, alias="minQuantity")
    order_date: str | None = Field(default=None, alias="orderDate")
    from_date: str | None = Field(default=None, alias="fromDate")
    to_date: str | None = Field(default=None, alias="toDate")
    from_date_time: str | None = Field(default=None, alias="fromDateTime")
    to_date_time: str | None = Field(default=None, alias="toDateTime")

    @field_validator("customer_name", mode="before")
    @classmethod
    def _customer(cls, v: Any) -> Any:
        if v is None:
            return v
        return no_special_chars(str(v) if not isinstance(v, str) else v)

    @field_validator("order_date", "from_date", "to_date", mode="before")
    @classmethod
    def _dates(cls, v: Any) -> Any:
        return local_date_string(v)

    @field_validator("from_date_time", "to_date_time", mode="before")
    @classmethod
    def _datetimes(cls, v: Any) -> Any:
        return local_datetime_string(v)


class OrderIdsRequest(BaseModel):
    model_config = _CAMEL

    ids: list[int] = Field(min_length=1)
    status: OrderStatusCode = None


# --- GET validation demo ---


class GetValidationItemFilter(BaseModel):
    model_config = _CAMEL

    sku: str
    quantity: int

    @field_validator("sku", mode="before")
    @classmethod
    def _sku(cls, v: Any) -> Any:
        if v is None or (isinstance(v, str) and not v.strip()):
            raise ValueError("sku must not be blank")
        return no_special_chars(str(v))

    @field_validator("quantity", mode="before")
    @classmethod
    def _quantity(cls, v: Any) -> Any:
        if v is None:
            raise ValueError("quantity must not be null")
        return v

    @field_validator("quantity")
    @classmethod
    def _quantity_positive(cls, v: int) -> int:
        if v <= 0:
            raise ValueError("quantity must be positive")
        return v


class GetValidationItemView(BaseModel):
    model_config = _CAMEL

    sku: str
    quantity: int


def _date_serializer(v: Optional[Any]) -> Optional[str]:
    if v is None:
        return None
    if hasattr(v, "isoformat"):
        # date → yyyy-MM-dd; datetime → include time without micros if midnight? use strftime
        from datetime import date as date_cls
        from datetime import datetime as datetime_cls

        if isinstance(v, datetime_cls):
            return v.strftime("%Y-%m-%dT%H:%M:%S")
        if isinstance(v, date_cls):
            return v.strftime("%Y-%m-%d")
    return str(v)


class GetValidationParsedResponse(BaseModel):
    model_config = _CAMEL

    ids: list[int]
    items: list[GetValidationItemView]
    min_quantity: Optional[int] = Field(default=None, alias="minQuantity")
    page: Optional[int] = None
    order_date: Annotated[
        Optional[Any],
        PlainSerializer(_date_serializer, return_type=Optional[str]),
    ] = Field(default=None, alias="orderDate")
    from_date: Annotated[
        Optional[Any],
        PlainSerializer(_date_serializer, return_type=Optional[str]),
    ] = Field(default=None, alias="fromDate")
    to_date: Annotated[
        Optional[Any],
        PlainSerializer(_date_serializer, return_type=Optional[str]),
    ] = Field(default=None, alias="toDate")
    from_date_time: Annotated[
        Optional[Any],
        PlainSerializer(_date_serializer, return_type=Optional[str]),
    ] = Field(default=None, alias="fromDateTime")
    to_date_time: Annotated[
        Optional[Any],
        PlainSerializer(_date_serializer, return_type=Optional[str]),
    ] = Field(default=None, alias="toDateTime")
    note: Optional[str] = None


class FileAssetResponse(BaseModel):
    model_config = _CAMEL

    id: int
    original_name: str = Field(alias="originalName")
    content_type: Optional[str] = Field(default=None, alias="contentType")
    size_bytes: int = Field(alias="sizeBytes")
    status: str
    owner_ref: Optional[str] = Field(default=None, alias="ownerRef")
    created_at: Annotated[
        Optional[datetime],
        PlainSerializer(_dt_serializer, return_type=Optional[str]),
    ] = Field(default=None, alias="createdAt")
