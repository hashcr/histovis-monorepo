#!/bin/bash
set -e

# Creates additional databases beyond the default POSTGRES_DB.
# The default DB (POSTGRES_DB) is already created by the postgres image.
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    SELECT 'CREATE DATABASE "${ANALYSIS_POSTGRES_DB}"'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${ANALYSIS_POSTGRES_DB}')
    \gexec
EOSQL
