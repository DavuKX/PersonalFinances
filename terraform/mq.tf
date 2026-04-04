# ─── Amazon MQ (RabbitMQ) ──────────────────────────────────────────────────────

resource "random_password" "mq_password" {
  length  = 32
  special = false
}

resource "aws_security_group" "mq" {
  name_prefix = "${var.project_name}-mq-"
  description = "Allow RabbitMQ access from EKS worker nodes"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description     = "AMQP from EKS"
    from_port       = 5671
    to_port         = 5671
    protocol        = "tcp"
    security_groups = [module.eks.node_security_group_id]
  }

  ingress {
    description     = "RabbitMQ Management from EKS"
    from_port       = 443
    to_port         = 443
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
    Name = "${var.project_name}-mq-sg"
  }
}

resource "aws_mq_broker" "rabbitmq" {
  broker_name = "${var.project_name}-mq"

  engine_type                = "RabbitMQ"
  engine_version             = var.mq_engine_version
  host_instance_type         = var.mq_instance_type
  deployment_mode            = "SINGLE_INSTANCE"
  auto_minor_version_upgrade = true

  publicly_accessible = false
  subnet_ids          = [module.vpc.database_subnets[0]]
  security_groups     = [aws_security_group.mq.id]

  user {
    username = var.mq_username
    password = random_password.mq_password.result
  }

  tags = {
    Name = "${var.project_name}-mq"
  }
}

