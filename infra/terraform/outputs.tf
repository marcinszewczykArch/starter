output "ec2_public_ip" {
  description = "Public IP of EC2 instance (Elastic IP)"
  value       = aws_eip.app.public_ip
}

output "ec2_instance_id" {
  description = "EC2 instance ID"
  value       = aws_instance.app.id
}

output "ssh_command" {
  description = "SSH command to connect to EC2"
  value       = "ssh -i your-key.pem ec2-user@${aws_eip.app.public_ip}"
}

output "app_url" {
  description = "Application URL"
  value       = "http://${aws_eip.app.public_ip}"
}

output "db_connection_info" {
  description = "Database runs in Docker on EC2. Connect via SSH tunnel."
  value       = "SSH tunnel: ssh -L 5432:localhost:5432 ec2-user@${aws_eip.app.public_ip}"
}

output "s3_bucket_name" {
  description = "S3 bucket name for user files"
  value       = aws_s3_bucket.user_files.id
}

