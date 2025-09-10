# Disaster Recovery Plan for Integrixs Flow Bridge

## Table of Contents
1. [Overview](#overview)
2. [Recovery Objectives](#recovery-objectives)
3. [System Architecture](#system-architecture)
4. [Backup Strategies](#backup-strategies)
5. [Recovery Procedures](#recovery-procedures)
6. [Testing and Maintenance](#testing-and-maintenance)
7. [Emergency Contacts](#emergency-contacts)

## Overview

This document outlines the disaster recovery (DR) procedures for the Integrixs Flow Bridge platform. It covers backup strategies, recovery procedures, and operational guidelines to ensure business continuity in case of system failures, data loss, or catastrophic events.

### Scope
- Application services (Backend, Frontend, API Gateway)
- Databases (PostgreSQL)
- Message queues (RabbitMQ, Kafka)
- Cache systems (Redis, Hazelcast)
- File storage
- Configuration and secrets

### Disaster Scenarios
1. **Hardware Failure**: Server, storage, or network component failure
2. **Software Failure**: Application crashes, corruption, or bugs
3. **Data Corruption**: Database corruption or accidental deletion
4. **Security Breach**: Ransomware, data theft, or system compromise
5. **Natural Disaster**: Fire, flood, earthquake affecting data centers
6. **Human Error**: Accidental deletion or misconfiguration

## Recovery Objectives

### RTO (Recovery Time Objective)
- **Critical Services**: 4 hours
- **Non-critical Services**: 24 hours
- **Complete System**: 48 hours

### RPO (Recovery Point Objective)
- **Database**: 15 minutes
- **File Storage**: 1 hour
- **Configuration**: Real-time (Git)
- **Audit Logs**: 1 hour

### Service Priority Levels

#### Priority 1 (Critical)
- PostgreSQL Database
- API Gateway
- Authentication Service
- Core Backend Services

#### Priority 2 (High)
- Message Queues
- Redis Cache
- File Storage Service
- Monitoring Services

#### Priority 3 (Medium)
- Reporting Services
- Batch Processing
- Development/Test Environments

## System Architecture

### Primary Site Configuration
```
┌─────────────────────────────────────────────────────────────┐
│                     Load Balancer (HA)                      │
└─────────────────┬───────────────────────────┬──────────────┘
                  │                           │
         ┌────────▼────────┐         ┌───────▼────────┐
         │  API Gateway    │         │  API Gateway   │
         │  (Primary)      │         │  (Secondary)   │
         └────────┬────────┘         └───────┬────────┘
                  │                           │
         ┌────────▼───────────────────────────▼────────┐
         │          Application Cluster                 │
         │  ┌─────────┐ ┌─────────┐ ┌─────────┐      │
         │  │Backend 1│ │Backend 2│ │Backend 3│      │
         │  └────┬────┘ └────┬────┘ └────┬────┘      │
         └───────┼───────────┼───────────┼────────────┘
                 │           │           │
         ┌───────▼───────────▼───────────▼────────────┐
         │         Database Cluster (Primary)          │
         │  PostgreSQL Master + Read Replicas          │
         └───────────────┬─────────────────────────────┘
                         │ Streaming Replication
         ┌───────────────▼─────────────────────────────┐
         │         Database Cluster (Standby)          │
         │  PostgreSQL Standby + Read Replicas         │
         └─────────────────────────────────────────────┘
```

### Backup Infrastructure
```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Local Backups  │────▶│ Remote Backups  │────▶│ Archive Storage │
│   (Fast Recovery)     │   (Primary DR)   │     │  (Long-term)    │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

## Backup Strategies

### 1. Database Backups

#### PostgreSQL Continuous Archiving
```bash
# WAL archiving configuration in postgresql.conf
archive_mode = on
archive_command = 'rsync -a %p backup-server:/pgbackup/wal/%f'
wal_level = replica
max_wal_senders = 10
```

#### Backup Schedule
- **Full Backup**: Daily at 02:00 UTC
- **Incremental**: Every 15 minutes via WAL archiving
- **Retention**: 30 days local, 90 days remote, 1 year archive

#### Backup Script
```bash
#!/bin/bash
# /opt/integrixs/scripts/backup-database.sh

BACKUP_DIR="/backup/postgresql"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_NAME="integrixs_full_${TIMESTAMP}"

# Perform backup
pg_basebackup -h localhost -D ${BACKUP_DIR}/${BACKUP_NAME} -Ft -z -P

# Verify backup
pg_verifybackup ${BACKUP_DIR}/${BACKUP_NAME}

# Sync to remote
rsync -avz ${BACKUP_DIR}/${BACKUP_NAME} backup-server:/remote/postgresql/

# Clean old backups
find ${BACKUP_DIR} -name "integrixs_full_*" -mtime +30 -delete
```

### 2. Application Data Backups

#### File Storage
```bash
#!/bin/bash
# /opt/integrixs/scripts/backup-files.sh

SOURCE_DIR="/data/integrixs/uploads"
BACKUP_DIR="/backup/files"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# Create incremental backup
rsync -avz --backup --backup-dir=${BACKUP_DIR}/incremental/${TIMESTAMP} \
      ${SOURCE_DIR}/ ${BACKUP_DIR}/current/

# Sync to remote
rsync -avz ${BACKUP_DIR}/ backup-server:/remote/files/
```

### 3. Configuration Backups

#### GitOps for Configuration
```yaml
# .gitlab-ci.yml for configuration backup
backup-config:
  stage: backup
  script:
    - git bundle create config-backup.bundle --all
    - aws s3 cp config-backup.bundle s3://integrixs-backups/config/
  only:
    - schedules
```

### 4. Redis Backups

#### Redis Persistence Configuration
```conf
# redis.conf
save 900 1
save 300 10
save 60 10000
dbfilename dump.rdb
dir /data/redis/

# AOF for better durability
appendonly yes
appendfsync everysec
```

### 5. Message Queue Backups

#### RabbitMQ Backup
```bash
#!/bin/bash
# /opt/integrixs/scripts/backup-rabbitmq.sh

# Export definitions
rabbitmqctl export_definitions /backup/rabbitmq/definitions.json

# Backup Mnesia database
systemctl stop rabbitmq-server
tar -czf /backup/rabbitmq/mnesia-$(date +%Y%m%d).tar.gz /var/lib/rabbitmq/mnesia/
systemctl start rabbitmq-server
```

## Recovery Procedures

### 1. Database Recovery

#### Point-in-Time Recovery (PITR)
```bash
#!/bin/bash
# /opt/integrixs/scripts/restore-database-pitr.sh

RECOVERY_TARGET_TIME="2024-01-15 14:30:00"
RESTORE_DIR="/data/postgresql/restore"
BACKUP_BASE="/backup/postgresql/latest"

# Stop PostgreSQL
systemctl stop postgresql

# Restore base backup
rm -rf ${RESTORE_DIR}/*
tar -xzf ${BACKUP_BASE}/base.tar.gz -C ${RESTORE_DIR}

# Configure recovery
cat > ${RESTORE_DIR}/recovery.conf << EOF
restore_command = 'cp /backup/postgresql/wal/%f %p'
recovery_target_time = '${RECOVERY_TARGET_TIME}'
recovery_target_action = 'promote'
EOF

# Start PostgreSQL
systemctl start postgresql

# Verify recovery
psql -c "SELECT pg_last_wal_replay_lsn();"
```

#### Failover to Standby
```bash
#!/bin/bash
# /opt/integrixs/scripts/failover-database.sh

# Promote standby
ssh standby-db "sudo -u postgres pg_ctl promote -D /data/postgresql"

# Update application configuration
sed -i 's/primary-db/standby-db/g' /etc/integrixs/database.conf

# Restart services
systemctl restart integrixs-backend

# Notify team
/opt/integrixs/scripts/send-alert.sh "Database failover completed to standby-db"
```

### 2. Application Recovery

#### Service Recovery Checklist
```bash
#!/bin/bash
# /opt/integrixs/scripts/recover-services.sh

# 1. Verify infrastructure
echo "Checking infrastructure..."
for host in api-1 api-2 backend-1 backend-2 backend-3; do
    ping -c 1 $host || echo "WARNING: $host unreachable"
done

# 2. Start core services in order
echo "Starting database..."
systemctl start postgresql
sleep 10

echo "Starting Redis..."
systemctl start redis
sleep 5

echo "Starting message queues..."
systemctl start rabbitmq-server
systemctl start kafka
sleep 10

echo "Starting application services..."
systemctl start integrixs-backend
systemctl start integrixs-api-gateway
sleep 10

# 3. Verify services
echo "Verifying services..."
curl -f http://localhost:8080/health || exit 1
curl -f http://localhost:8090/health || exit 1

echo "Recovery completed successfully"
```

### 3. Data Corruption Recovery

#### Detecting and Fixing Corruption
```sql
-- Check for corruption
SELECT schemaname, tablename, 
       pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables 
WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Verify data integrity
VACUUM FULL ANALYZE;
REINDEX DATABASE integrixs;

-- Check for orphaned records
SELECT f.* FROM integration_flows f
LEFT JOIN users u ON f.created_by = u.id
WHERE u.id IS NULL;
```

### 4. Security Breach Recovery

#### Incident Response Procedure
```bash
#!/bin/bash
# /opt/integrixs/scripts/security-breach-response.sh

# 1. Isolate affected systems
echo "Isolating affected systems..."
iptables -A INPUT -j DROP
iptables -A OUTPUT -j DROP
iptables -A INPUT -s 10.0.0.0/8 -j ACCEPT  # Allow internal only

# 2. Preserve evidence
echo "Collecting forensic data..."
mkdir -p /forensics/$(date +%Y%m%d_%H%M%S)
cp -r /var/log /forensics/
netstat -an > /forensics/network_connections.txt
ps aux > /forensics/processes.txt
last -100 > /forensics/logins.txt

# 3. Reset credentials
echo "Resetting all credentials..."
/opt/integrixs/scripts/rotate-all-credentials.sh

# 4. Restore from clean backup
echo "Restoring from last known good backup..."
/opt/integrixs/scripts/restore-full-system.sh

# 5. Apply security patches
echo "Applying security updates..."
apt-get update && apt-get upgrade -y

# 6. Re-enable network with monitoring
echo "Re-enabling network with enhanced monitoring..."
iptables -F
systemctl restart fail2ban
systemctl restart integrixs-ids
```

## Testing and Maintenance

### DR Testing Schedule

#### Monthly Tests
- Backup verification
- Single service recovery
- Configuration restore

#### Quarterly Tests
- Database failover
- Partial system recovery
- Network failure simulation

#### Annual Tests
- Full DR drill
- Site failover
- Complete system recovery from scratch

### Test Scenarios

#### Scenario 1: Database Failure
```bash
#!/bin/bash
# DR Test: Database failure and recovery

echo "Starting DR Test: Database Failure"
date

# 1. Create test data
psql integrixs -c "CREATE TABLE dr_test (id serial, data text, created_at timestamp);"
psql integrixs -c "INSERT INTO dr_test (data, created_at) VALUES ('DR Test Data', NOW());"

# 2. Note current state
psql integrixs -c "SELECT COUNT(*) FROM dr_test;" > /tmp/dr_test_before.txt

# 3. Simulate failure
systemctl stop postgresql

# 4. Execute recovery
/opt/integrixs/scripts/failover-database.sh

# 5. Verify recovery
psql integrixs -c "SELECT COUNT(*) FROM dr_test;" > /tmp/dr_test_after.txt

# 6. Compare results
diff /tmp/dr_test_before.txt /tmp/dr_test_after.txt || echo "WARNING: Data mismatch!"

# 7. Cleanup
psql integrixs -c "DROP TABLE dr_test;"

echo "DR Test completed"
```

### Maintenance Tasks

#### Daily
- Verify backup completion
- Check replication lag
- Review backup logs

#### Weekly
- Test restore random backup
- Verify backup integrity
- Update DR documentation

#### Monthly
- Review and update contact lists
- Test notification systems
- Update recovery scripts

## Automation Scripts

### Health Check Monitoring
```bash
#!/bin/bash
# /opt/integrixs/scripts/dr-health-check.sh

ALERT_EMAIL="dr-team@integrixs.com"
LOG_FILE="/var/log/integrixs/dr-health.log"

check_service() {
    service=$1
    if systemctl is-active --quiet $service; then
        echo "$(date): $service is running" >> $LOG_FILE
    else
        echo "$(date): WARNING - $service is not running!" >> $LOG_FILE
        echo "$service is down on $(hostname)" | mail -s "DR Alert: Service Down" $ALERT_EMAIL
    fi
}

# Check critical services
check_service postgresql
check_service redis
check_service rabbitmq-server
check_service integrixs-backend
check_service integrixs-api-gateway

# Check backup status
if [ -f /backup/postgresql/last_backup_time ]; then
    last_backup=$(cat /backup/postgresql/last_backup_time)
    current_time=$(date +%s)
    backup_age=$((current_time - last_backup))
    
    if [ $backup_age -gt 86400 ]; then  # More than 24 hours
        echo "WARNING: Last backup is older than 24 hours!" | mail -s "DR Alert: Backup Overdue" $ALERT_EMAIL
    fi
fi

# Check replication lag
lag=$(psql -h standby-db -c "SELECT EXTRACT(EPOCH FROM (NOW() - pg_last_xact_replay_timestamp())) AS lag;" -t)
if (( $(echo "$lag > 300" | bc -l) )); then  # More than 5 minutes
    echo "WARNING: Replication lag is $lag seconds!" | mail -s "DR Alert: Replication Lag" $ALERT_EMAIL
fi
```

### Automated Failover Script
```bash
#!/bin/bash
# /opt/integrixs/scripts/auto-failover.sh

PRIMARY_DB="primary-db"
STANDBY_DB="standby-db"
MAX_ATTEMPTS=3
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if pg_isready -h $PRIMARY_DB -t 5; then
        echo "Primary database is responding"
        exit 0
    fi
    
    ATTEMPT=$((ATTEMPT + 1))
    echo "Attempt $ATTEMPT: Primary database not responding"
    sleep 10
done

# Primary is down, initiate failover
echo "Primary database failed, initiating failover..."

# Promote standby
ssh $STANDBY_DB "sudo -u postgres pg_ctl promote -D /data/postgresql"

# Update DNS or load balancer
aws route53 change-resource-record-sets --hosted-zone-id Z123456 \
    --change-batch '{"Changes":[{"Action":"UPSERT","ResourceRecordSet":{"Name":"db.integrixs.com","Type":"A","TTL":60,"ResourceRecords":[{"Value":"'$(dig +short $STANDBY_DB)'"}]}}]}'

# Update application configuration
ansible all -m lineinfile -a "path=/etc/integrixs/database.conf regexp='^host=' line='host=$STANDBY_DB'"

# Restart applications
ansible all -m service -a "name=integrixs-backend state=restarted"

# Send notifications
/opt/integrixs/scripts/send-alert.sh "CRITICAL: Database failover completed. Primary: $PRIMARY_DB -> Standby: $STANDBY_DB"
```

## Emergency Contacts

### Primary Contacts
| Role | Name | Phone | Email | Availability |
|------|------|-------|-------|--------------|
| DR Coordinator | John Smith | +1-555-0100 | john.smith@integrixs.com | 24/7 |
| Database Admin | Jane Doe | +1-555-0101 | jane.doe@integrixs.com | 24/7 |
| System Admin | Bob Wilson | +1-555-0102 | bob.wilson@integrixs.com | Business hours |
| Security Lead | Alice Brown | +1-555-0103 | alice.brown@integrixs.com | 24/7 |

### Vendor Contacts
| Service | Company | Support Number | Account # |
|---------|---------|----------------|-----------|
| Cloud Provider | AWS | +1-800-555-0200 | 123456789 |
| Database Support | PostgreSQL Inc | +1-800-555-0201 | PSG-98765 |
| Network Provider | ISP Corp | +1-800-555-0202 | NET-54321 |

### Escalation Path
1. On-call Engineer (PagerDuty)
2. Team Lead
3. DR Coordinator
4. CTO
5. CEO (Critical incidents only)

## Appendices

### A. Recovery Checklist Template
```
□ Incident detected and logged
□ DR team notified
□ Impact assessment completed
□ Recovery strategy selected
□ Stakeholders notified
□ Recovery initiated
□ Services restored
□ Data integrity verified
□ Performance validated
□ Post-mortem scheduled
□ Documentation updated
```

### B. Communication Templates

#### Initial Incident Notification
```
Subject: [INCIDENT] Integrixs System Issue Detected

Severity: [Critical/High/Medium]
Affected Systems: [List systems]
Impact: [User impact description]
Status: Investigation in progress
Next Update: [Time]

DR Team has been activated and is working on resolution.
```

#### Recovery Complete Notification
```
Subject: [RESOLVED] Integrixs System Recovery Complete

Incident Duration: [Start time] - [End time]
Root Cause: [Brief description]
Resolution: [What was done]
Data Loss: [None/Minimal/Description]

Services have been fully restored. A post-mortem will be scheduled.
```

### C. Lessons Learned Template
```markdown
# Post-Mortem: [Incident Date]

## Summary
- **Duration**: 
- **Impact**: 
- **Root Cause**: 

## Timeline
- [Time]: Event description

## What Went Well
- 

## What Went Wrong
- 

## Action Items
- [ ] Action item with owner and due date

## Prevention Measures
- 
```

---

**Document Version**: 1.0  
**Last Updated**: [Current Date]  
**Next Review**: [Date + 3 months]  
**Owner**: DR Team