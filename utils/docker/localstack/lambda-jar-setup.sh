#!/bin/bash
#################################################
#################### IMPORTANT ##################
#################################################
##
## THE COMMUNITY VERSION OF LOCALSTACK DOES NOT SUPPORT LAMBDA LAYERS AR RUNTIME.
## THIS SCRIPT IS INTENDED TO BE USED IF YOU HAVE A LOCALSTACK PRO VERSION, OR
## IF THE COMMUNITY VERSION WILL SUPPORTS LAMBDA LAYERS IN THE FUTURE.
##
echo ""
echo "WAITING FOR DEVICE DATA RESOURCES FROM BUILDER CONTAINER..."
DATA_FUNCTION_PATH="/var/tmp/devices-data/data-function.jar"
DATA_DEPENDENCIES_PATH="/var/tmp/devices-data/data-function-libs.zip"
while [ ! -f "$DATA_FUNCTION_PATH" ] || [ ! -f "$DATA_DEPENDENCIES_PATH" ]; do
    sleep 3
done
echo "DONE!"

echo ""
echo "WAITING FOR DEVICE UPDATE RESOURCES FROM BUILDER CONTAINER..."
UPDATE_FUNCTION_PATH="/var/tmp/devices-update/update-function.jar"
UPDATE_DEPENDENCIES_PATH="/var/tmp/devices-update/update-function-libs.zip"
while [ ! -f "$UPDATE_FUNCTION_PATH" ] || [ ! -f "$UPDATE_DEPENDENCIES_PATH" ]; do
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
echo "CREATING DEVICE DATA LAMBDA LAYER..."
awslocal lambda publish-layer-version               \
    --layer-name 'device-read-function-layer'       \
    --description "Device Data dependencies"        \
    --zip-file fileb://$DATA_DEPENDENCIES_PATH      \
    --compatible-runtimes 'java21'                  \
    --license-info 'MIT'

echo ""
echo "CREATING DEVICE UPDATE LAMBDA LAYER..."
awslocal lambda publish-layer-version               \
    --layer-name 'device-update-function-layer'     \
    --description "Device Update dependencies"      \
    --zip-file fileb://$UPDATE_DEPENDENCIES_PATH    \
    --compatible-runtimes 'java21'                  \
    --license-info 'MIT'

echo ""
echo "CREATING DEVICE DATA FUNCTION..."
awslocal lambda create-function                                                                 \
    --function-name 'device-read-function'                                                      \
    --runtime 'java21'                                                                          \
    --architectures 'arm64'                                                                     \
    --zip-file fileb://"$DATA_FUNCTION_PATH"                                                    \
    --handler 'org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest'   \
    --layers 'arn:aws:lambda:us-east-1:000000000000:layer:device-read-function-layer:1'         \
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
awslocal lambda create-function                                                                      \
    --function-name 'device-update-function'                                                         \
    --runtime 'java21'                                                                               \
    --architectures 'arm64'                                                                          \
    --zip-file fileb://"$UPDATE_FUNCTION_PATH"                                                       \
    --handler 'org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest'        \
    --layers 'arn:aws:lambda:us-east-1:000000000000:layer:device-update-function-layer:1'            \
    --dead-letter-config 'TargetArn=arn:aws:sqs:us-east-1:000000000000:device-update-function-dlq'   \
    --timeout 20                                                                                     \
    --memory-size 512                                                                                \
    --role 'arn:aws:iam::000000000000:role/lambda-role'                                              \
    --environment 'Variables={SPRING_CLOUD_AWS_ENDPOINT=http://host.docker.internal:4566}'

echo ""
echo "CREATING EVENTBRIDGE RULE FOR DEVICE UPDATE FUNCTION..."
awslocal events put-rule                                        \
    --name 'device-update-function-rule'                        \
    --event-pattern "{\"source\":[\"hiperium.city.tasks.api\"],\"detail-type\":[\"ExecutedTaskEvent\"]}"

echo ""
echo "ADDING DEVICE UPDATE FUNCTION AS EVENTBRIDGE TARGET..."
awslocal events put-targets                                     \
    --rule 'device-update-function-rule'                        \
    --targets 'Id=1,Arn=arn:aws:lambda:us-east-1:000000000000:function:device-update-function'

echo ""
echo "ALLOWING EVENTBRIDGE TO INVOKE DEVICE UPDATE FUNCTION..."
awslocal lambda add-permission                                                  \
    --function-name 'device-update-function'                                    \
    --statement-id  'eventbridge-invoke-device-update-function-permission'      \
    --action        'lambda:InvokeFunction'                                     \
    --principal     'events.amazonaws.com'                                      \
    --source-arn    'arn:aws:events:us-east-1:000000000000:rule/device-update-function-rule'

echo ""
echo "DONE!"
