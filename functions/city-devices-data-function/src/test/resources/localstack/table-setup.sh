#!/bin/bash

echo ""
echo "CREATING DEVICES TABLE..."
awslocal dynamodb create-table              \
  --table-name 'Devices'                    \
  --attribute-definitions                   \
    AttributeName=id,AttributeType=S        \
    AttributeName=cityId,AttributeType=S    \
  --key-schema                              \
    AttributeName=id,KeyType=HASH           \
    AttributeName=cityId,KeyType=RANGE      \
  --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5

echo ""
echo "WRITING DEVICE ITEMS..."
awslocal dynamodb batch-write-item  \
    --request-items file:///var/lib/localstack/table-data.json
