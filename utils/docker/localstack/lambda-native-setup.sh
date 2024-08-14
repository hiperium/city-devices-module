#!/bin/bash

echo ""
echo "WAITING FOR RESOURCES FROM BUILDER CONTAINER..."
DATA_FUNCTION_PATH="/var/tmp/devices-data/data-function-assembly.zip"
UPDATE_FUNCTION_PATH="/var/tmp/devices-update/update-function-assembly.zip"
while [ ! -f "$DATA_FUNCTION_PATH" ] || [ ! -f "$UPDATE_FUNCTION_PATH" ]; do
    sleep 3
done
echo "DONE!"

echo ""
echo "CREATING LAMBDA ROLE..."
awslocal iam create-role                    \
    --role-name 'lambda-role'               \
    --assume-role-policy-document '{
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Principal": {
              "Service": "lambda.amazonaws.com"
            },
            "Action": "sts:AssumeRole"
          }
        ]
      }'

echo ""
echo "ALLOWING LAMBDA TO ACCESS LOGS..."
awslocal iam put-role-policy                \
    --role-name 'lambda-role'               \
    --policy-name 'CloudWatchLogsPolicy'    \
    --policy-document '{
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Action": [
              "logs:CreateLogGroup",
              "logs:CreateLogStream",
              "logs:PutLogEvents"
            ],
            "Resource": "arn:aws:logs:*:*:*"
          }
        ]
      }'

echo ""
echo "ALLOWING LAMBDA TO ACCESS DYNAMODB..."
awslocal iam put-role-policy                \
    --role-name 'lambda-role'               \
    --policy-name 'DynamoDBPolicy'          \
    --policy-document '{
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Action": [
              "dynamodb:GetItem",
              "dynamodb:PutItem",
              "dynamodb:UpdateItem",
              "dynamodb:Scan",
              "dynamodb:Query"
            ],
            "Resource": "arn:aws:dynamodb:us-east-1:000000000000:table/Devices"
          }
        ]
      }'

echo ""
echo "CREATING DEVICE DATA FUNCTION..."
awslocal lambda create-function                                                                 \
    --function-name 'device-read-function'                                                      \
    --runtime 'provided.al2023'                                                                 \
    --architectures 'arm64'                                                                     \
    --zip-file fileb://"$DATA_FUNCTION_PATH"                                                    \
    --handler 'org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest'   \
    --timeout 20                                                                                \
    --memory-size 512                                                                           \
    --role 'arn:aws:iam::000000000000:role/lambda-role'                                         \
    --environment 'Variables={SPRING_CLOUD_AWS_ENDPOINT=http://host.docker.internal:4566}'

echo ""
echo "CREATING SQS-DLQ FOR DEVICE UPDATE FUNCTION..."
awslocal sqs create-queue  \
    --queue-name 'device-update-function-dlq'

echo ""
echo "CREATING DEVICE UPDATE FUNCTION..."
awslocal lambda create-function                                                                       \
    --function-name 'device-update-function'                                                          \
    --runtime 'provided.al2023'                                                                       \
    --architectures 'arm64'                                                                           \
    --zip-file fileb://"$UPDATE_FUNCTION_PATH"                                                        \
    --handler 'org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest'         \
    --dead-letter-config 'TargetArn=arn:aws:sqs:us-east-1:000000000000:device-update-function-dlq'    \
    --timeout 20                                                                                      \
    --memory-size 512                                                                                 \
    --role 'arn:aws:iam::000000000000:role/lambda-role'                                               \
    --environment 'Variables={SPRING_CLOUD_AWS_ENDPOINT=http://host.docker.internal:4566}'

echo "CREATING EVENTBRIDGE RULE FOR UPDATE FUNCTION..."
awslocal events put-rule                                        \
    --name 'device-update-function-rule'                        \
    --event-pattern '{
      "source": ["hiperium.city.tasks.api"],
      "detail-type": ["ExecutedTaskEvent"]
    }'

echo ""
echo "ALLOWING EVENTBRIDGE TO INVOKE UPDATE FUNCTION..."
awslocal lambda add-permission                                                  \
    --function-name 'device-update-function'                                    \
    --statement-id  'eventbridge-invoke-device-update-function-permission'      \
    --action        'lambda:InvokeFunction'                                     \
    --principal     'events.amazonaws.com'                                      \
    --source-arn    'arn:aws:events:us-east-1:000000000000:rule/device-update-function-rule'

echo ""
echo "SETTING UPDATE FUNCTION AS EVENTBRIDGE TARGET..."
awslocal events put-targets                                     \
    --rule 'device-update-function-rule'                        \
    --targets 'Id=1,Arn=arn:aws:lambda:us-east-1:000000000000:function:device-update-function'

echo ""
echo "DONE!"
