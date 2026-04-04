# ─── VPC ───────────────────────────────────────────────────────────────────────
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.8"

  name = "${var.project_name}-vpc"
  cidr = var.vpc_cidr

  azs             = var.availability_zones
  private_subnets = [cidrsubnet(var.vpc_cidr, 4, 0), cidrsubnet(var.vpc_cidr, 4, 1)]
  public_subnets  = [cidrsubnet(var.vpc_cidr, 4, 2), cidrsubnet(var.vpc_cidr, 4, 3)]

  # Database subnets (isolated – no NAT, no IGW route)
  database_subnets                   = [cidrsubnet(var.vpc_cidr, 4, 4), cidrsubnet(var.vpc_cidr, 4, 5)]
  create_database_subnet_group       = true
  database_subnet_group_name         = "${var.project_name}-db-subnet-group"
  create_database_subnet_route_table = true

  enable_nat_gateway   = true
  single_nat_gateway   = true          # cost-saving; use one_nat_gateway_per_az for HA
  enable_dns_hostnames = true
  enable_dns_support   = true

  # Tags required by EKS for auto-discovery
  public_subnet_tags = {
    "kubernetes.io/role/elb"                                = "1"
    "kubernetes.io/cluster/${var.project_name}-eks-cluster" = "shared"
  }

  private_subnet_tags = {
    "kubernetes.io/role/internal-elb"                       = "1"
    "kubernetes.io/cluster/${var.project_name}-eks-cluster" = "shared"
  }

  tags = {
    Name = "${var.project_name}-vpc"
  }
}

