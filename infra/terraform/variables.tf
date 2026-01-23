variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "eu-central-1"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "prod"
}

variable "app_name" {
  description = "Application name"
  type        = string
  default     = "starter"
}

# EC2
variable "ec2_instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.small" # 2GB RAM, Spot pricing ~$4.50/month
}

variable "ec2_spot_max_price" {
  description = "Maximum hourly price for Spot instance (On-Demand t3.small = $0.0208)"
  type        = string
  default     = "0.015" # ~70% of On-Demand price
}

variable "ec2_key_name" {
  description = "Name of the SSH key pair (must exist in AWS)"
  type        = string
}

# Network
variable "allowed_ssh_cidr" {
  description = "CIDR block allowed to SSH (your IP)"
  type        = string
  default     = "0.0.0.0/0" # Restrict this to your IP in production!
}


# S3
variable "cors_allowed_origins" {
  description = "CORS allowed origins for S3 (comma-separated)"
  type        = string
  default     = "*"  # Change to your domain in production
}
