from collections.abc import Generator

from sqlalchemy import create_engine, event
from sqlalchemy.orm import DeclarativeBase, Session, sessionmaker
from sqlalchemy.pool import StaticPool

from app.config import settings


def _create_engine():
    url = settings.database_url
    if url.startswith("sqlite"):
        connect_args = {"check_same_thread": False}
        # :memory: needs StaticPool so schema + sessions share one DB
        if ":memory:" in url:
            return create_engine(
                url,
                echo=False,
                connect_args=connect_args,
                poolclass=StaticPool,
            )
        return create_engine(url, echo=False, connect_args=connect_args)
    return create_engine(url, echo=False)


engine = _create_engine()
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False, expire_on_commit=False)


class Base(DeclarativeBase):
    pass


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
        db.commit()
    except Exception:
        db.rollback()
        raise
    finally:
        db.close()


@event.listens_for(engine, "connect")
def _sqlite_fk(dbapi_connection, connection_record) -> None:  # noqa: ARG001
    if settings.is_sqlite:
        cursor = dbapi_connection.cursor()
        cursor.execute("PRAGMA foreign_keys=ON")
        cursor.close()
