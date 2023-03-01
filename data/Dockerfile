FROM postgres:15.2

ARG STARTUP_SCRIPTS_DIR="."

COPY $STARTUP_SCRIPTS_DIR/schema.sql /docker-entrypoint-initdb.d/a_schema.sql

EXPOSE 5432