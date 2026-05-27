#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE SCHEMA IF NOT EXISTS user_service;
    CREATE SCHEMA IF NOT EXISTS images_service;
    CREATE SCHEMA IF NOT EXISTS analysis_service;
EOSQL
