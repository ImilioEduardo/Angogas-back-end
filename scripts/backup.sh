#!/bin/bash
# Backup diário PostgreSQL — retenção 30 dias
# Cron: 0 2 * * * /opt/angogas/scripts/backup.sh

set -euo pipefail

DB_NAME="${DB_NAME:-angogas}"
DB_USER="${DB_USER:-angogas}"
BACKUP_DIR="/backups"
DATE=$(date +%Y%m%d_%H%M%S)
FILE="$BACKUP_DIR/angogas_$DATE.sql.gz"

mkdir -p "$BACKUP_DIR"

echo "[$(date)] A iniciar backup de $DB_NAME..."
pg_dump -U "$DB_USER" "$DB_NAME" | gzip > "$FILE"
echo "[$(date)] Backup guardado: $FILE ($(du -sh "$FILE" | cut -f1))"

# Apagar backups com mais de 30 dias
find "$BACKUP_DIR" -name "angogas_*.sql.gz" -mtime +30 -delete
echo "[$(date)] Backups antigos removidos."
