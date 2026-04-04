# ─── RDS PostgreSQL ────────────────────────────────────────────────────────────

resource "random_password" "rds_password" {
  length  = 32
  special = false # avoid shell-escaping issues
}

resource "aws_security_group" "rds" {
  name_prefix = "${var.project_name}-rds-"
  description = "Allow PostgreSQL access from EKS worker nodes"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description     = "PostgreSQL from EKS"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [module.eks.node_security_group_id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-rds-sg"
  }
}

resource "aws_db_instance" "main" {
  identifier     = "${var.project_name}-db"
  engine         = "postgres"
  engine_version = var.rds_engine_version
  instance_class = var.rds_instance_class

  allocated_storage     = var.rds_allocated_storage
  max_allocated_storage = 0 # disable autoscaling for free tier
  storage_encrypted     = false # free tier does not support encryption on db.t3.micro

  db_name  = "userdb" # default database; we create others via provisioner
  username = var.rds_master_username
  password = random_password.rds_password.result

  db_subnet_group_name   = module.vpc.database_subnet_group_name
  vpc_security_group_ids = [aws_security_group.rds.id]

  multi_az            = false # set to true for HA in production
  publicly_accessible = false
  skip_final_snapshot = true
  deletion_protection = false # set to true for real production

  backup_retention_period = 1 # free tier only allows up to 1 day
  backup_window           = "03:00-04:00"
  maintenance_window      = "Mon:04:00-Mon:05:00"

  tags = {
    Name = "${var.project_name}-db"
  }
}

# ─── Create additional databases via null_resource ────────────────────────────
# NOTE: In production, prefer Flyway/Liquibase migrations in the apps themselves.
# This provisioner creates the extra databases that the services expect.

resource "null_resource" "create_databases" {
  depends_on = [aws_db_instance.main]

  provisioner "local-exec" {
    environment = {
      PGHOST     = aws_db_instance.main.address
      PGPORT     = tostring(aws_db_instance.main.port)
      PGUSER     = var.rds_master_username
      PGPASSWORD = random_password.rds_password.result
    }

    command = <<-EOT
      for db in transactiondb walletdb analyticsdb; do
        psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d userdb -tc \
          "SELECT 1 FROM pg_database WHERE datname='$db'" | grep -q 1 || \
          psql -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d userdb -c "CREATE DATABASE $db;"
      done
    EOT
  }
}

