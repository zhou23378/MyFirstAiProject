#!/bin/bash
# Salon Manager — Database Restore Script
# Usage: ./scripts/restore-db.sh <backup_file.sql.gz>

set -e

if [ -z "$1" ]; then
    echo "Usage: $0 <backup_file.sql.gz>"
    echo "Available backups:"
    ls -1tr "${BACKUP_DIR:-./backups}/" | grep '.sql.gz$' || echo "  (none)"
    exit 1
fi

BACKUP_FILE="$1"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-salon123}"
DB_NAME="${DB_NAME:-salon}"

if [ ! -f "${BACKUP_FILE}" ]; then
    echo "Error: file not found: ${BACKUP_FILE}"
    exit 1
fi

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Restoring ${DB_NAME} from ${BACKUP_FILE}..."

gunzip -c "${BACKUP_FILE}" | mysql \
  -h "${DB_HOST}" \
  -P "${DB_PORT}" \
  -u "${DB_USER}" \
  -p"${DB_PASSWORD}" \
  --default-character-set=utf8mb4 \
  "${DB_NAME}"

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Restore complete."
