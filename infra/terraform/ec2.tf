# EC2 Instance
resource "aws_instance" "app" {
  ami                    = data.aws_ami.amazon_linux.id
  instance_type          = var.ec2_instance_type
  key_name               = var.ec2_key_name
  vpc_security_group_ids = [aws_security_group.ec2.id]

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }

  user_data = <<-EOF
    #!/bin/bash
    set -e

    # Update system
    yum update -y

    # Install Docker
    yum install -y docker git
    systemctl start docker
    systemctl enable docker
    usermod -aG docker ec2-user

    # Install Docker Compose
    curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose

    # Create app directory
    mkdir -p /home/ec2-user/app
    chown ec2-user:ec2-user /home/ec2-user/app

    # Signal that setup is complete
    touch /home/ec2-user/.setup-complete
  EOF

  tags = {
    Name = "${var.app_name}-server"
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

