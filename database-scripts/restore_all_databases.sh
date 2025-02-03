#!/bin/bash
databases=("SecurityServer" "PaymentMicroservice" "ProductMicroservice" "NotificationMicroservice" "CompanyMicroservice") # Список баз
backup_dir=~/backups
mkdir -p $backup_dir

for db in "${databases[@]}"; do
    docker exec -t postgres-container pg_dump -U xparadize -d $db > "$backup_dir/$db.sql"
done
