# EC2 Spot Instance - runs Docker containers pulled from GitHub Container Registry
# Spot instances are 60-70% cheaper than On-Demand
resource "aws_instance" "app" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = var.ec2_instance_type
  key_name               = var.ec2_key_name
  vpc_security_group_ids = [aws_security_group.ec2.id]

  # Spot instance configuration - ~70% cheaper than On-Demand
  instance_market_options {
    market_type = "spot"
    spot_options {
      max_price                      = var.ec2_spot_max_price
      spot_instance_type             = "persistent"
      instance_interruption_behavior = "stop"  # Stop (not terminate) on interruption - data preserved
    }
  }

  root_block_device {
    volume_size = 30
    volume_type = "gp3"
  }

  # Minimal setup - only Docker and Docker Compose needed
  # Images are pre-built and pulled from ghcr.io
  user_data = <<-EOF
    #!/bin/bash
    set -e

    # Update system
    yum update -y

    # Install Docker
    yum install -y docker
    systemctl start docker
    systemctl enable docker
    usermod -aG docker ec2-user

    # Install Docker Compose V2 as plugin
    mkdir -p /usr/local/lib/docker/cli-plugins
    curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64" -o /usr/local/lib/docker/cli-plugins/docker-compose
    chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

    # Create deployment directory
    mkdir -p /home/ec2-user/deploy
    chown ec2-user:ec2-user /home/ec2-user/deploy

    # Signal that setup is complete
    touch /home/ec2-user/.setup-complete
  EOF

  tags = {
    Name = "${var.app_name}-server"
  }

  # Prevent EC2 replacement when AMI updates
  lifecycle {
    ignore_changes = [ami, user_data]
  }
}

# Elastic IP for stable address
resource "aws_eip" "app" {
  instance = aws_instance.app.id
  domain   = "vpc"

  tags = {
    Name = "${var.app_name}-eip"
  }
}
