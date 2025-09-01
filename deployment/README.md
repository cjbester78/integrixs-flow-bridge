# Deployment Guide for Integrix Flow Bridge

## Overview

This guide provides instructions for deploying the Integrix Flow Bridge application using Docker Compose or Kubernetes.

## Prerequisites

- Docker and Docker Compose (for Docker deployment)
- Kubernetes cluster (for K8s deployment)
- kubectl configured (for K8s deployment)
- Minimum 8GB RAM and 4 CPU cores
- 20GB available disk space

## Docker Compose Deployment

### Quick Start

1. **Clone the repository**
```bash
git clone <repository-url>
cd integrix-flow-bridge-backend/deployment
```

2. **Create environment file**
```bash
cp .env.example .env
# Edit .env with your configuration
```

3. **Start services**
```bash
docker-compose up -d
```

4. **Check status**
```bash
docker-compose ps
docker-compose logs -f integrix-app
```

5. **Access the application**
- Application: http://localhost:8080
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3001

### Configuration

Edit the `.env` file to configure:
- `DB_PASSWORD`: PostgreSQL password
- `REDIS_PASSWORD`: Redis password
- `JWT_SECRET`: JWT signing secret
- `SPRING_PROFILE`: Spring profile (dev/test/prod)

### Scaling

Scale the application:
```bash
docker-compose up -d --scale integrix-app=3
```

### Monitoring

1. **Prometheus**: Metrics collection at http://localhost:9090
2. **Grafana**: Dashboards at http://localhost:3001 (admin/admin)
3. **Application logs**: `docker-compose logs -f integrix-app`

## Kubernetes Deployment

### Prerequisites

1. **Create namespace**
```bash
kubectl apply -f kubernetes/namespace.yaml
```

2. **Create secrets**
```bash
kubectl create secret generic integrix-secrets \
  --from-literal=jwt-secret='your-secret-key' \
  --from-literal=db-password='your-db-password' \
  --from-literal=redis-password='your-redis-password' \
  -n integrix
```

3. **Create persistent volumes**
```bash
kubectl apply -f kubernetes/pv.yaml
kubectl apply -f kubernetes/pvc.yaml
```

### Deploy Application

1. **Apply configurations**
```bash
kubectl apply -f kubernetes/configmap.yaml
kubectl apply -f kubernetes/rbac.yaml
```

2. **Deploy services**
```bash
kubectl apply -f kubernetes/service.yaml
```

3. **Deploy applications**
```bash
kubectl apply -f kubernetes/deployment.yaml
```

4. **Deploy ingress**
```bash
kubectl apply -f kubernetes/ingress.yaml
```

### Verify Deployment

```bash
# Check pods
kubectl get pods -n integrix

# Check services
kubectl get svc -n integrix

# Check logs
kubectl logs -n integrix -l app=integrix-app

# Check health
kubectl exec -n integrix deployment/integrix-app -- curl localhost:8080/actuator/health
```

### Scaling

```bash
# Scale application
kubectl scale deployment integrix-app --replicas=5 -n integrix

# Enable autoscaling
kubectl autoscale deployment integrix-app \
  --min=3 --max=10 --cpu-percent=70 -n integrix
```

## Production Considerations

### Security

1. **Use strong passwords** for all services
2. **Enable TLS/SSL** for all external endpoints
3. **Configure network policies** in Kubernetes
4. **Use secrets management** (HashiCorp Vault, AWS Secrets Manager)
5. **Enable audit logging**

### Performance

1. **Database optimization**
   - Configure connection pooling
   - Add read replicas for scaling
   - Regular vacuum and analyze

2. **Caching strategy**
   - Use Redis for session storage
   - Cache frequently accessed data
   - Configure cache TTL appropriately

3. **Message queue tuning**
   - Configure Kafka partitions based on load
   - Set appropriate retention policies
   - Monitor consumer lag

### Backup and Recovery

1. **Database backups**
```bash
# PostgreSQL backup
pg_dump -h postgres-service -U integrix integrixflowbridge > backup.sql

# Restore
psql -h postgres-service -U integrix integrixflowbridge < backup.sql
```

2. **Application state**
- Use persistent volumes for uploads
- Regular snapshots of persistent volumes
- Document recovery procedures

### Monitoring and Alerting

1. **Metrics to monitor**
   - Application health and uptime
   - Response times and error rates
   - Database connections and query performance
   - Message queue lag and throughput
   - Resource utilization (CPU, memory, disk)

2. **Set up alerts for**
   - Service downtime
   - High error rates (>1%)
   - Slow response times (>1s)
   - Resource exhaustion
   - Failed background jobs

### Maintenance

1. **Rolling updates**
```bash
# Update image
kubectl set image deployment/integrix-app \
  integrix-app=integrix/flow-bridge:v2.0 -n integrix

# Check rollout status
kubectl rollout status deployment/integrix-app -n integrix
```

2. **Database migrations**
```bash
# Run migrations
kubectl exec -n integrix deployment/integrix-app -- \
  java -jar app.jar db migrate
```

3. **Log rotation**
- Configure log rotation in containers
- Use centralized logging (ELK stack)
- Set retention policies

## Troubleshooting

### Common Issues

1. **Application won't start**
   - Check database connectivity
   - Verify environment variables
   - Check for port conflicts
   - Review application logs

2. **Performance issues**
   - Check resource limits
   - Monitor database queries
   - Review connection pool settings
   - Check for memory leaks

3. **Connection errors**
   - Verify service discovery
   - Check network policies
   - Validate credentials
   - Test connectivity between pods

### Debug Commands

```bash
# Get pod details
kubectl describe pod <pod-name> -n integrix

# Execute commands in pod
kubectl exec -it <pod-name> -n integrix -- /bin/sh

# Port forward for debugging
kubectl port-forward -n integrix deployment/integrix-app 8080:8080

# View events
kubectl get events -n integrix --sort-by=.lastTimestamp
```

## Rollback Procedures

### Docker Compose
```bash
# Stop current version
docker-compose down

# Checkout previous version
git checkout <previous-tag>

# Start previous version
docker-compose up -d
```

### Kubernetes
```bash
# View rollout history
kubectl rollout history deployment/integrix-app -n integrix

# Rollback to previous version
kubectl rollout undo deployment/integrix-app -n integrix

# Rollback to specific revision
kubectl rollout undo deployment/integrix-app --to-revision=2 -n integrix
```

## Support

For issues and support:
1. Check application logs
2. Review monitoring dashboards
3. Consult documentation
4. Contact support team