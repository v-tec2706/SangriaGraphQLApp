set -e
export AWS_ACCESS_KEY_ID=ASIA4ICEXVQMXB6IDT33
export AWS_SECRET_ACCESS_KEY=o0B0HobeT5l2Wy0Ls31ilCnoSN2Mv/tMo88UY2Qz
export AWS_SESSION_TOKEN=IQoJb3JpZ2luX2VjEHgaCXVzLXdlc3QtMiJIMEYCIQDJ1Xacvd64hOmVUegm7phhqeZC95mKRcmBMqWM5KJajQIhALCFWeMLZzT9yMccRpsS60x8ohSA0RAg8PSeCM1i1vQAKroCCPH//////////wEQABoMODQxOTU3NzQ3NzM3IgwsDqQXVTGSdLgasi8qjgJ3Jj2Dcg9TT8W2d2LPp88PdZGTGDDil4y3woyzsAneGe4KOkq/ZrSmeG/5tyHEJjnPsQU53YRF1Ak/S3Q2RjT0iiWuYWaH4olYOIId8CpiJ2GTKOBzbwrIWlEdMvOA7rEGMZPTTdfYi+LY/7poUNjm+YPJjHH+CKJgTGyefkhBfHZb4CQyGVCmgaorLs9fbqaXI+ctLRsnmDSmjknTl9WZbsotnPU/kFISn8N+liFUqrlPxCanGTtsMx2gFvhn5F0FoTO6gZKeD/3t7ZEcQ8dCiM38fXcKaTNqtzkr26nPWkPP8WHMmc6JdoS+np1kvr/m7zm1Z3ivAx6qkXnScK2SlJ3UxIHAt0lx896z7f4w6tXFhAY6nAEKgQkjYWkQx1kqikQ9tj6x24MLy5MpOzX8rvBhk/pfUkM4KtJ1XaQuiL2mAjnzph5bXoA4TNWtmNnCqJc7wfNueHHylO7wvaIlh+22h8ERTCl+MfrltRof3n47zrZFgYkGk37ZU+Q5+rN0XpqpBrwEZE8ajuvDi7cO/3lqyFnoypLFVgkK5iLknr9pG/d6JfK8KYM5aWe7Ih7O5rs=
export AWS_PAGER=""


# vpn & sg creation
VPC_ID=$(aws ec2 create-vpc --cidr-block 10.0.0.0/16 | grep VpcId | awk '{ print $4 }')
SUBNET_1=$(aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block 10.0.1.0/24 | grep SubnetId | awk '{ print $4 }')
SUBNET_2=$(aws ec2 create-subnet --vpc-id $VPC_ID --cidr-block 10.0.0.0/24 | grep SubnetId | awk '{ print $4 }')
IGW_ID=$(aws ec2 create-internet-gateway | grep igw | awk '{ print $2 }')
aws ec2 attach-internet-gateway --vpc-id $VPC_ID --internet-gateway-id $IGW_ID
RTB_ID=$(aws ec2 create-route-table --vpc-id $VPC_ID | grep rtb | awk '{ print $4 }')

aws ec2 create-route --route-table-id $RTB_ID --destination-cidr-block 0.0.0.0/0 --gateway-id $IGW_ID
aws ec2 associate-route-table  --subnet-id $SUBNET_1 --route-table-id $RTB_ID
aws ec2 modify-subnet-attribute --subnet-id $SUBNET_1 --map-public-ip-on-launch
SG_ID=$(aws ec2 create-security-group --group-name BenchmarkAccess --description "benchmark Security Group" --vpc-id $VPC_ID | grep GroupId | awk '{ print $4 }')
aws ec2 authorize-security-group-ingress --group-id $SG_ID --ip-permissions IpProtocol=tcp,FromPort=8080,ToPort=8090,IpRanges='[{CidrIp=0.0.0.0/0}]'

echo "--------------"
echo "Network setup "
echo "--------------"
echo "VPC_ID=${VPC_ID}"
echo "SUBNET_1=${SUBNET_1}"
echo "SUBNET_2=${SUBNET_2}"
echo "IGW_ID=${IGW_ID}"
echo "RTB_ID=${RTB_ID}"
echo "SG_ID=${SG_ID}"

# vpn & sg deletion

VPC_ID=vpc-061c867e06f52afaa
SUBNET_1=subnet-003295cc0e77567dd
SUBNET_2=subnet-052755a86b11c48a6
IGW_ID=igw-0616f72087944006a
RTB_ID=rtb-0b1ede1162fca8a89
SG_ID=sg-01d121c4414fc6a68

#aws ec2 delete-security-group --group-id ${SG_ID}
#aws ec2 delete-subnet --subnet-id ${SUBNET_1}
#aws ec2 delete-subnet --subnet-id ${SUBNET_2}
#aws ec2 delete-route-table --route-table-id ${RTB_ID}
#aws ec2 detach-internet-gateway --internet-gateway-id ${IGW_ID} --vpc-id ${VPC_ID}
#aws ec2 delete-internet-gateway --internet-gateway-id ${IGW_ID}
#aws ec2 delete-vpc --vpc-id ${VPC_ID}

# cluster creation
aws ecs create-cluster --cluster-name benchmark-cluster
REVISION=$(aws ecs register-task-definition --cli-input-json file://task-definition.json | grep revision | awk '{ print $4 }')
#echo s"Revision: ${REVISION}"
aws ecs create-service --cluster benchmark-cluster --service-name benchmark-service --task-definition benchmark-task:${REVISION} --desired-count 1 --launch-type "FARGATE" --network-configuration "awsvpcConfiguration={subnets=[${SUBNET_1}],securityGroups=[${SG_ID}],assignPublicIp=ENABLED}"
aws ecs list-services --cluster benchmark-cluster

# cluster deletion
#aws ecs delete-service --cluster benchmark-cluster --service benchmark-service --force
#aws ecs delete-cluster --cluster benchmark-cluster