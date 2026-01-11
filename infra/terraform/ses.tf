# =============================================================================
# AWS SES (Simple Email Service) Configuration
# =============================================================================
# Enables sending emails from the application (verification, password reset)
# EC2 instance uses IAM role to authenticate with SES (no hardcoded credentials)
# =============================================================================

# -----------------------------------------------------------------------------
# IAM Role for EC2 to access SES
# -----------------------------------------------------------------------------
resource "aws_iam_role" "ec2_role" {
  name = "${var.app_name}-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name = "${var.app_name}-ec2-role"
  }
}

# SES permissions - allow sending emails
resource "aws_iam_role_policy" "ses_policy" {
  name = "${var.app_name}-ses-policy"
  role = aws_iam_role.ec2_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ses:SendEmail",
          "ses:SendRawEmail"
        ]
        Resource = "*"
      }
    ]
  })
}

# Instance profile to attach role to EC2
resource "aws_iam_instance_profile" "ec2_profile" {
  name = "${var.app_name}-ec2-profile"
  role = aws_iam_role.ec2_role.name
}

# -----------------------------------------------------------------------------
# SES Email Identity (sender email verification)
# -----------------------------------------------------------------------------
# Note: SES starts in "sandbox mode" - you can only send to verified emails
# To send to anyone, request production access in AWS Console:
# SES → Account dashboard → Request production access

resource "aws_ses_email_identity" "sender" {
  email = var.ses_sender_email
}

# -----------------------------------------------------------------------------
# Outputs
# -----------------------------------------------------------------------------
output "ses_sender_email" {
  description = "Verified sender email for SES"
  value       = aws_ses_email_identity.sender.email
}

output "ses_verification_status" {
  description = "Check AWS Console to verify email (click link in email)"
  value       = "Verification email sent to ${var.ses_sender_email}"
}

