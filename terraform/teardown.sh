#!/usr/bin/env bash
# ─── teardown.sh ───────────────────────────────────────────────────────────────
# Tears down the entire AWS infrastructure provisioned by Terraform.
#
# Usage:
#   ./terraform/teardown.sh              # interactive (asks for confirmation)
#   ./terraform/teardown.sh --auto       # non-interactive (auto-approve)
# ───────────────────────────────────────────────────────────────────────────────

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

AUTO_APPROVE=""
if [[ "${1:-}" == "--auto" ]]; then
  AUTO_APPROVE="-auto-approve"
fi

echo "══════════════════════════════════════════════════════════"
echo "  PersonalFinance — Infrastructure Teardown"
echo "══════════════════════════════════════════════════════════"
echo ""

if [[ -z "${AUTO_APPROVE}" ]]; then
  echo "⚠️  This will DESTROY all AWS resources managed by Terraform:"
  echo "    - EKS cluster & worker nodes"
  echo "    - RDS PostgreSQL database (ALL DATA LOST)"
  echo "    - ElastiCache Redis"
  echo "    - Amazon MQ broker"
  echo "    - ECR repositories & images"
  echo "    - S3 frontend bucket & CloudFront distribution"
  echo "    - AWS Secrets Manager secrets"
  echo "    - VPC & networking"
  echo ""
  read -rp "  Type 'destroy' to confirm: " CONFIRM
  if [[ "${CONFIRM}" != "destroy" ]]; then
    echo "  Aborted."
    exit 1
  fi
fi

echo ""
echo "▶ Removing Helm releases first (to avoid dependency issues)..."
# Remove helm releases that Terraform manages (ignore errors if cluster is gone)
aws eks update-kubeconfig --name personalfinance-eks-cluster --region "${AWS_REGION:-us-east-1}" 2>/dev/null || true
helm uninstall aws-load-balancer-controller -n kube-system 2>/dev/null || true
helm uninstall external-secrets -n external-secrets 2>/dev/null || true

echo ""
echo "▶ Running terraform destroy..."
terraform init -input=false
terraform destroy ${AUTO_APPROVE}

echo ""
echo "══════════════════════════════════════════════════════════"
echo "  ✅  All infrastructure destroyed."
echo ""
echo "  The Terraform state bucket and DynamoDB lock table"
echo "  were NOT deleted (they hold the state). Delete manually"
echo "  if you no longer need them:"
echo ""
echo "    aws s3 rb s3://personalfinance-terraform-state --force"
echo "    aws dynamodb delete-table --table-name personalfinance-terraform-lock"
echo "══════════════════════════════════════════════════════════"

