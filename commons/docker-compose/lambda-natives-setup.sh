#!/bin/bash

echo ""
echo "WAITING FOR LAMBDA RESOURCES FROM BUILDER CONTAINER..."
DATA_FUNCTION_PATH="/var/tmp/device-data-assembly.zip"
UPDATE_FUNCTION_PATH="/var/tmp/device-update-assembly.zip"
while [ ! -f "$DATA_FUNCTION_PATH" ] && [ ! -f "$UPDATE_FUNCTION_PATH" ]; do
  sleep 1
done

echo ""
echo "CREATING LAMBDA ROLE..."
awslocal iam create-role                \
    --role-name 'lambda-role'           \
    --assume-role-policy-document '{"Version": "2012-10-17","Statement": [{ "Effect": "Allow", "Principal": {"Service": "lambda.amazonaws.com"}, "Action": "sts:AssumeRole"}]}'

echo ""
echo "ALLOWING LAMBDA ROLE TO ACCESS DYNAMODB..."
awslocal iam put-role-policy            \
    --role-name 'lambda-role'           \
    --policy-name DynamoDBPolicy        \
    --policy-document '{"Version": "2012-10-17", "Statement": [{"Effect": "Allow", "Action": "dynamodb:GetItem, dynamodb:PutItem", "Resource": "arn:aws:dynamodb:us-east-1:000000000000:table/Devices"}]}'

echo ""
echo "CREATING DEVICES DATA FUNCTION..."
awslocal lambda create-function                                                                 \
    --function-name 'city-devices-data-function'                                                \
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
    --queue-name 'city-devices-update-function-dlq'

echo ""
echo "CREATING DEVICES UPDATE FUNCTION..."
awslocal lambda create-function                                                                             \
    --function-name 'city-devices-update-function'                                                          \
    --runtime 'provided.al2023'                                                                             \
    --architectures 'arm64'                                                                                 \
    --zip-file fileb://"$UPDATE_FUNCTION_PATH"                                                              \
    --handler 'org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest'               \
    --dead-letter-config 'TargetArn=arn:aws:sqs:us-east-1:000000000000:city-devices-update-function-dlq'    \
    --timeout 20                                                                                            \
    --memory-size 512                                                                                       \
    --role 'arn:aws:iam::000000000000:role/lambda-role'                                                     \
    --environment 'Variables={SPRING_CLOUD_AWS_ENDPOINT=http://host.docker.internal:4566}'

echo ""
echo "CREATING EVENTBRIDGE RULE FOR UPDATE FUNCTION..."
awslocal events put-rule                                        \
    --name 'city-devices-update-function-rule'                  \
    --event-pattern "{\"source\":[\"hiperium.city.tasks.api\"],\"detail-type\":[\"TaskExecutedEvent\"]}"

echo ""
echo "ADDING LAMBDA FUNCTION AS EVENTBRIDGE TARGET..."
awslocal events put-targets                                     \
    --rule 'city-devices-update-function-rule'                  \
    --targets 'Id=1,Arn=arn:aws:lambda:us-east-1:000000000000:function:city-devices-update-function'

echo ""
echo "ALLOWING EVENTBRIDGE TO INVOKE UPDATE LAMBDA FUNCTION..."
awslocal lambda add-permission                                  \
    --function-name 'city-devices-update-function'              \
    --statement-id  'eventbridge-lambda-permission'             \
    --action        'lambda:InvokeFunction'                     \
    --principal     'events.amazonaws.com'                      \
    --source-arn    'arn:aws:events:us-east-1:000000000000:rule/city-devices-update-function-rule'

echo ""
echo "DONE!"
