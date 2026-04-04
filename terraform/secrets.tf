# ─── AWS Secrets Manager ───────────────────────────────────────────────────────
# Store all sensitive values; External Secrets Operator syncs them into K8s.

resource "random_password" "jwt_secret" {
  length  = 64
  special = false
}

# ─── Database secrets ─────────────────────────────────────────────────────────

resource "aws_secretsmanager_secret" "db_credentials" {
  name                    = "${var.project_name}/${var.environment}/db-credentials"
  description             = "RDS PostgreSQL credentials"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "db_credentials" {
  secret_id = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    DB_USERNAME = var.rds_master_username
    DB_PASSWORD = random_password.rds_password.result
    DB_HOST     = aws_db_instance.main.address
    DB_PORT     = tostring(aws_db_instance.main.port)
  })
}

# ─── RabbitMQ secrets ─────────────────────────────────────────────────────────

resource "aws_secretsmanager_secret" "rabbitmq_credentials" {
  name                    = "${var.project_name}/${var.environment}/rabbitmq-credentials"
  description             = "Amazon MQ RabbitMQ credentials"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "rabbitmq_credentials" {
  secret_id = aws_secretsmanager_secret.rabbitmq_credentials.id
  secret_string = jsonencode({
    RABBITMQ_USERNAME = var.mq_username
    RABBITMQ_PASSWORD = random_password.mq_password.result
    RABBITMQ_HOST     = replace(replace(aws_mq_broker.rabbitmq.instances[0].endpoints[0], "amqps://", ""), ":5671", "")
    RABBITMQ_PORT     = "5671"
  })
}

# ─── JWT secret ───────────────────────────────────────────────────────────────

resource "aws_secretsmanager_secret" "jwt_secret" {
  name                    = "${var.project_name}/${var.environment}/jwt-secret"
  description             = "JWT signing secret"
  recovery_window_in_days = 7
}

resource "aws_secretsmanager_secret_version" "jwt_secret" {
  secret_id = aws_secretsmanager_secret.jwt_secret.id
  secret_string = jsonencode({
    JWT_SECRET = random_password.jwt_secret.result
  })
}

# ─── IAM policy so External Secrets can read the secrets ──────────────────────

data "aws_iam_policy_document" "external_secrets_access" {
  statement {
    effect = "Allow"
    actions = [
      "secretsmanager:GetSecretValue",
      "secretsmanager:DescribeSecret",
    ]
    resources = [
      aws_secretsmanager_secret.db_credentials.arn,
      aws_secretsmanager_secret.rabbitmq_credentials.arn,
      aws_secretsmanager_secret.jwt_secret.arn,
    ]
  }
}

resource "aws_iam_policy" "external_secrets_access" {
  name   = "${var.project_name}-external-secrets-sm-access"
  policy = data.aws_iam_policy_document.external_secrets_access.json
}

resource "aws_iam_role_policy_attachment" "external_secrets_sm" {
  role       = module.external_secrets_irsa.iam_role_name
  policy_arn = aws_iam_policy.external_secrets_access.arn
}

