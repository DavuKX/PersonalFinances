# -----------------------------------------------------------------------------
# General
# -----------------------------------------------------------------------------
variable "aws_region" {
  description = "AWS region for all resources"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Deployment environment (prod, staging)"
  type        = string
  default     = "prod"
}

variable "project_name" {
  description = "Project name used as prefix for resource naming"
  type        = string
  default     = "perfin"
}

# -----------------------------------------------------------------------------
# VPC / Networking
# -----------------------------------------------------------------------------
variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "List of AZs to use"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

# -----------------------------------------------------------------------------
# EKS
# -----------------------------------------------------------------------------
variable "eks_cluster_version" {
  description = "Kubernetes version for EKS"
  type        = string
  default     = "1.31"
}

variable "eks_node_instance_type" {
  description = "EC2 instance type for EKS worker nodes"
  type        = string
  default     = "t3.micro"
}

variable "eks_node_desired" {
  description = "Desired number of worker nodes"
  type        = number
  default     = 2
}

variable "eks_node_min" {
  description = "Minimum number of worker nodes"
  type        = number
  default     = 1
}

variable "eks_node_max" {
  description = "Maximum number of worker nodes"
  type        = number
  default     = 4
}

# -----------------------------------------------------------------------------
# RDS
# -----------------------------------------------------------------------------
variable "rds_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "rds_allocated_storage" {
  description = "Allocated storage in GB for RDS"
  type        = number
  default     = 20
}

variable "rds_engine_version" {
  description = "PostgreSQL engine version"
  type        = string
  default     = "17.2"
}

variable "rds_master_username" {
  description = "Master username for RDS"
  type        = string
  default     = "postgres"
  sensitive   = true
}

# -----------------------------------------------------------------------------
# ElastiCache
# -----------------------------------------------------------------------------
variable "redis_node_type" {
  description = "ElastiCache Redis node type"
  type        = string
  default     = "cache.t3.micro"
}

variable "redis_engine_version" {
  description = "Redis engine version"
  type        = string
  default     = "7.1"
}

# -----------------------------------------------------------------------------
# Amazon MQ
# -----------------------------------------------------------------------------
variable "mq_instance_type" {
  description = "Amazon MQ broker instance type"
  type        = string
  default     = "mq.t3.micro"
}

variable "mq_engine_version" {
  description = "RabbitMQ engine version"
  type        = string
  default     = "3.13"
}

variable "mq_username" {
  description = "Amazon MQ RabbitMQ admin username"
  type        = string
  default     = "admin"
  sensitive   = true
}

# -----------------------------------------------------------------------------
# Frontend
# -----------------------------------------------------------------------------
variable "frontend_domain" {
  description = "Custom domain for the frontend (optional). Leave empty to use CloudFront domain."
  type        = string
  default     = ""
}

variable "frontend_certificate_arn" {
  description = "ACM certificate ARN for the frontend domain (must be in us-east-1 for CloudFront)"
  type        = string
  default     = ""
}

