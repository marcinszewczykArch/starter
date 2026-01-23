# S3 Bucket for user files
resource "aws_s3_bucket" "user_files" {
  bucket = "${var.app_name}-files-${var.environment}"

  tags = {
    Name        = "${var.app_name}-files"
    Environment = var.environment
  }
}

# Block public access (security - files are private)
resource "aws_s3_bucket_public_access_block" "user_files" {
  bucket = aws_s3_bucket.user_files.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Versioning (for recovery)
resource "aws_s3_bucket_versioning" "user_files" {
  bucket = aws_s3_bucket.user_files.id

  versioning_configuration {
    status = "Enabled"
  }
}

# CORS configuration (for presigned URLs from frontend)
resource "aws_s3_bucket_cors_configuration" "user_files" {
  bucket = aws_s3_bucket.user_files.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "PUT", "POST", "DELETE", "HEAD"]
    allowed_origins = split(",", var.cors_allowed_origins)
    expose_headers  = ["ETag"]
    max_age_seconds = 3600
  }
}

# Lifecycle policy (optional - move old files to cheaper storage)
resource "aws_s3_bucket_lifecycle_configuration" "user_files" {
  bucket = aws_s3_bucket.user_files.id

  rule {
    id     = "delete_old_versions"
    status = "Enabled"

    filter {}

    noncurrent_version_expiration {
      noncurrent_days = 30
    }
  }
}
