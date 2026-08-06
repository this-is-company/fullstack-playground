# enum-sqlalchemy-python-app

FastAPI + SQLAlchemy 2.x port of the Kotlin QueryDSL enum demo (`enum-jpa-querydsl-kotlin-app`).

## Stack

- FastAPI, SQLAlchemy 2.x, Pydantic v2, psycopg, uvicorn
- Default port **8097**
- Tables: `pqdsl_demo_orders`, `pqdsl_demo_order_items`

## Setup

```bash
cd backend/enum-sqlalchemy-python-app
python -m venv .venv
source .venv/bin/activate
pip install -e ".[dev]"
```

## Run (PostgreSQL)

Ensure Postgres is up (`demo` / `demo123` @ `localhost:5432/demodb`), then:

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8097
# or
python -m app.main
```

OpenAPI: http://localhost:8097/docs

## Config

| Env | Default |
|-----|---------|
| `DATABASE_URL` | `postgresql+psycopg://demo:demo123@localhost:5432/demodb` |
| `PORT` | `8097` |

For SQLite (tests):

```bash
export DATABASE_URL=sqlite+pysqlite:///:memory:
```

## API sketch

- `POST /api/orders` — create (201)
- `PUT /api/orders/{id}` — update
- `GET /api/orders/{id}` — get
- `POST /api/orders/search` — flexible search (empty body → all)
- `POST /api/orders/search/by-date` | `by-date-between` | `by-datetime-between`
- `PUT /api/resource/orders` — create (201)
- `PATCH /api/resource/orders/{id}` — update
- `POST /api/resource/orders` — list
- `POST /api/resource/orders/{id}/cancel`
- `POST /api/resource/orders/update` | `/delete` — bulk

All responses use `{ "status", "error", "result" }`. Enum fields use **code** strings (`P`, `CARD`, `G`, …) plus `*Description` on responses.
