#!/bin/bash

# Get the current working directory
current_dir=$(pwd)

# Get the path to the directory containing the files and folders to zip
scripts_dir="$current_dir/../pipeline/infra/scripts"

# Create a zip file of the files and folders in the scripts directory
zip -r scripts.zip "$scripts_dir"

# Get the name of the S3 bucket to upload the zip file to
bucket_name="my-bucket"

# Upload the zip file to the S3 bucket
aws s3 cp scripts.zip s3://$bucket_name