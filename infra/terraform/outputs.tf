output "ec2_public_ip" {
  description = "Public IP of EC2 instance (Elastic IP)"
  value       = aws_eip.app.public_ip
}

output "ec2_instance_id" {
  description = "EC2 instance ID"
  value       = aws_instance.app.id
}

output "rds_endpoint" {
  description = "RDS endpoint (host:port)"
  value       = aws_db_instance.main.endpoint
}

output "rds_hostname" {
  description = "RDS hostname (without port, for DBeaver)"
  value       = aws_db_instance.main.address
}

output "rds_identifier" {
  description = "RDS instance identifier"
  value       = aws_db_instance.main.identifier
}

output "db_connection_string" {
  description = "JDBC connection string for Spring Boot"
  value       = "jdbc:postgresql://${aws_db_instance.main.endpoint}/${var.db_name}"
  sensitive   = true
}

output "ssh_command" {
  description = "SSH command to connect to EC2"
  value       = "ssh -i your-key.pem ec2-user@${aws_eip.app.public_ip}"
}

output "app_url" {
  description = "Application URL"
  value       = "http://${aws_eip.app.public_ip}"
}

