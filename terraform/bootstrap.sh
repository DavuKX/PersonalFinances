#!/usr/bin/env bash
# ─── bootstrap.sh ──────────────────────────────────────────────────────────────
# One-time setup: creates the S3 bucket + DynamoDB table for Terraform remote
# state. Run this BEFORE your first `terraform init`.
#
# Usage:
#   chmod +x terraform/bootstrap.sh
#   ./terraform/bootstrap.sh
# ───────────────────────────────────────────────────────────────────────────────

set -euo pipefail

AWS_REGION="${AWS_REGION:-us-east-1}"
BUCKET_NAME="personalfinance-terraform-state"
DYNAMO_TABLE="personalfinance-terraform-lock"

echo "══════════════════════════════════════════════════════════"
echo "  PersonalFinance — Terraform Backend Bootstrap"
echo "  Region : ${AWS_REGION}"
echo "  Bucket : ${BUCKET_NAME}"
echo "  Lock   : ${DYNAMO_TABLE}"
echo "══════════════════════════════════════════════════════════"

# ── S3 Bucket ────────────────────────────────────────────────────────────────
echo ""
echo "▶ Creating S3 bucket for Terraform state..."
if aws s3api head-bucket --bucket "${BUCKET_NAME}" 2>/dev/null; then
  echo "  ✔ Bucket already exists."
else
  if [ "${AWS_REGION}" = "us-east-1" ]; then
    aws s3api create-bucket \
      --bucket "${BUCKET_NAME}" \
      --region "${AWS_REGION}"
  else
    aws s3api create-bucket \
      --bucket "${BUCKET_NAME}" \
      --region "${AWS_REGION}" \
      --create-bucket-configuration LocationConstraint="${AWS_REGION}"
  fi
  echo "  ✔ Bucket created."
fi

echo "▶ Enabling versioning..."
aws s3api put-bucket-versioning \
  --bucket "${BUCKET_NAME}" \
  --versioning-configuration Status=Enabled

echo "▶ Enabling server-side encryption..."
aws s3api put-bucket-encryption \
  --bucket "${BUCKET_NAME}" \
  --server-side-encryption-configuration '{
    "Rules": [{
      "ApplyServerSideEncryptionByDefault": {
        "SSEAlgorithm": "aws:kms"
      },
      "BucketKeyEnabled": true
    }]
  }'

echo "▶ Blocking public access..."
aws s3api put-public-access-block \
  --bucket "${BUCKET_NAME}" \
  --public-access-block-configuration \
    BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true

# ── DynamoDB Lock Table ──────────────────────────────────────────────────────
echo ""
echo "▶ Creating DynamoDB table for state locking..."
if aws dynamodb describe-table --table-name "${DYNAMO_TABLE}" --region "${AWS_REGION}" &>/dev/null; then
  echo "  ✔ Table already exists."
else
  aws dynamodb create-table \
    --table-name "${DYNAMO_TABLE}" \
    --attribute-definitions AttributeName=LockID,AttributeType=S \
    --key-schema AttributeName=LockID,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --region "${AWS_REGION}"
  echo "  ✔ Table created."
fi

echo ""
echo "══════════════════════════════════════════════════════════"
echo "  ✅  Bootstrap complete!"
echo ""
echo "  Next steps:"
echo "    cd terraform"
echo "    cp terraform.tfvars.example terraform.tfvars"
echo "    # edit terraform.tfvars with your values"
echo "    terraform init"
echo "    terraform plan"
echo "    terraform apply"
echo "══════════════════════════════════════════════════════════"

