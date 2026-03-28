# AWS Deployment Guide — PersonalFinance

## Prerequisites

- AWS CLI configured (`aws configure`)
- `kubectl` installed
- `eksctl` installed
- Docker installed

## 1. Create ECR Repositories

```bash
export AWS_REGION=us-east-1
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

for svc in api-gateway user-service transaction-service wallet-service analytics-service; do
  aws ecr create-repository \
    --repository-name personalfinance/$svc \
    --region $AWS_REGION
done
```

## 2. Build & Push Docker Images

```bash
aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

for svc in api-gateway user-service transaction-service wallet-service analytics-service; do
  docker build -t personalfinance/$svc ./$svc
  docker tag personalfinance/$svc:latest \
    $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/personalfinance/$svc:latest
  docker push \
    $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/personalfinance/$svc:latest
done
```

## 3. Create EKS Cluster

```bash
eksctl create cluster \
  --name personalfinance \
  --region $AWS_REGION \
  --version 1.31 \
  --nodegroup-name workers \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 1 \
  --nodes-max 4 \
  --managed
```

## 4. Provision AWS Managed Services

### RDS PostgreSQL

```bash
aws rds create-db-instance \
  --db-instance-identifier personalfinance-db \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --engine-version 17 \
  --master-username postgres \
  --master-user-password YOUR_STRONG_PASSWORD \
  --allocated-storage 20 \
  --vpc-security-group-ids sg-xxxxx \
  --db-subnet-group-name your-subnet-group \
  --no-publicly-accessible
```

After creation, connect and create databases:

```sql
CREATE DATABASE userdb;
CREATE DATABASE transactiondb;
CREATE DATABASE walletdb;
CREATE DATABASE analyticsdb;
```

Update all K8s ConfigMaps `DB_URL` to point to the RDS endpoint:
```
jdbc:postgresql://<RDS_ENDPOINT>:5432/<dbname>
```

### ElastiCache Redis

```bash
aws elasticache create-cache-cluster \
  --cache-cluster-id personalfinance-redis \
  --cache-node-type cache.t3.micro \
  --engine redis \
  --num-cache-nodes 1
```

Update `REDIS_HOST` in ConfigMaps to the ElastiCache endpoint.

### Amazon MQ (RabbitMQ)

```bash
aws mq create-broker \
  --broker-name personalfinance-mq \
  --engine-type RABBITMQ \
  --engine-version 3.13 \
  --deployment-mode SINGLE_INSTANCE \
  --host-instance-type mq.t3.micro \
  --users Username=user,Password=YOUR_STRONG_PASSWORD
```

Update `RABBITMQ_HOST` and `RABBITMQ_PORT` in ConfigMaps to the Amazon MQ endpoint.

## 5. Install AWS Load Balancer Controller

```bash
helm repo add eks https://aws.github.io/eks-charts
helm repo update

helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=personalfinance \
  --set serviceAccount.create=true \
  --set serviceAccount.name=aws-load-balancer-controller
```

> Ensure the IAM OIDC provider is associated and the controller IAM policy is attached.
> See: https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html

## 6. Update K8s Manifests

Replace `<AWS_ACCOUNT_ID>` and `<REGION>` placeholders in all deployment YAML files:

```bash
find k8s/deployments -name "*.yml" -exec sed -i \
  "s/<AWS_ACCOUNT_ID>/$AWS_ACCOUNT_ID/g; s/<REGION>/$AWS_REGION/g" {} \;
```

Update secrets with real production values:

```bash
kubectl create secret generic db-secrets \
  --namespace personalfinance \
  --from-literal=DB_USERNAME=postgres \
  --from-literal=DB_PASSWORD=YOUR_STRONG_PASSWORD

kubectl create secret generic rabbitmq-secrets \
  --namespace personalfinance \
  --from-literal=RABBITMQ_USERNAME=user \
  --from-literal=RABBITMQ_PASSWORD=YOUR_STRONG_PASSWORD

kubectl create secret generic jwt-secret \
  --namespace personalfinance \
  --from-literal=JWT_SECRET=YOUR_PRODUCTION_256_BIT_SECRET
```

## 7. Deploy to EKS

```bash
kubectl apply -f k8s/namespace.yml

# If you created secrets via kubectl (step 6), skip the secrets folder:
# kubectl apply -f k8s/secrets/

kubectl apply -f k8s/configmaps/
kubectl apply -f k8s/deployments/
kubectl apply -f k8s/services/
kubectl apply -f k8s/ingress/
```

Verify:

```bash
kubectl get pods -n personalfinance
kubectl get svc -n personalfinance
kubectl get ingress -n personalfinance
```

## 8. Production Recommendations

### Secrets Management
Use **AWS Secrets Manager + External Secrets Operator** instead of plain K8s secrets:
```bash
helm install external-secrets external-secrets/external-secrets -n external-secrets --create-namespace
```

### Database Migrations
Replace `spring.jpa.hibernate.ddl-auto=update` with **Flyway** or **Liquibase** for production. Set `JPA_DDL_AUTO=validate` in production ConfigMaps.

### Monitoring
- Enable **CloudWatch Container Insights** on the EKS cluster
- All services already expose `/actuator/health` and `/actuator/info`
- Consider adding Prometheus + Grafana via Helm for metrics dashboards

### CI/CD
Consider adding a GitHub Actions workflow:
1. Run tests (`./mvnw test`)
2. Build Docker images
3. Push to ECR
4. Update K8s deployments (`kubectl rollout restart`)

### Scaling
- Set up **Horizontal Pod Autoscaler** (HPA) per service based on CPU/memory
- RDS: enable Multi-AZ for high availability
- ElastiCache: use Redis cluster mode for failover

