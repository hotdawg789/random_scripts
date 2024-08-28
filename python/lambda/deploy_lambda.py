import boto3
import json
import io
import zipfile

def create_lambda_function():
    lambda_client = boto3.client('lambda')
    
    # Define the Lambda function
    function_name = 'NightlyTask'
    handler = 'lambda_function.lambda_handler'
    role_arn = 'arn:aws:iam::YOUR_ACCOUNT_ID:role/YOUR_LAMBDA_EXECUTION_ROLE'
    
    # Create a zip file in memory
    zip_output = io.BytesIO()
    with zipfile.ZipFile(zip_output, 'w') as zip_file:
        zip_file.write('lambda_function.py')
    
    zip_output.seek(0)
    
    # Create the Lambda function
    response = lambda_client.create_function(
        FunctionName=function_name,
        Runtime='python3.11',
        Role=role_arn,
        Handler=handler,
        Code={'ZipFile': zip_output.read()},
        Timeout=30,
        MemorySize=128
    )
    
    print(f"Lambda function created: {response['FunctionArn']}")
    return response['FunctionArn']

def create_cloudwatch_rule(function_arn):
    events_client = boto3.client('events')
    
    # Create the CloudWatch Events rule
    rule_name = 'NightlyTaskSchedule'
    schedule_expression = 'cron(0 22 * * ? *)'  # Run at 10 PM (UTC) every day
    
    response = events_client.put_rule(
        Name=rule_name,
        ScheduleExpression=schedule_expression,
        State='ENABLED'
    )
    
    print(f"CloudWatch Events rule created: {response['RuleArn']}")
    
    # Add permission to Lambda function to allow CloudWatch Events to invoke it
    lambda_client = boto3.client('lambda')
    lambda_client.add_permission(
        FunctionName=function_arn,
        StatementId='AllowCloudWatchEventsInvoke',
        Action='lambda:InvokeFunction',
        Principal='events.amazonaws.com',
        SourceArn=response['RuleArn']
    )
    
    # Create target for the rule
    events_client.put_targets(
        Rule=rule_name,
        Targets=[
            {
                'Id': '1',
                'Arn': function_arn
            }
        ]
    )
    
    print("CloudWatch Events target added to the rule")

if __name__ == '__main__':
    function_arn = create_lambda_function()
    create_cloudwatch_rule(function_arn)