set -e
export AWS_ACCESS_KEY_ID=ASIA4ICEXVQMZJ3RAD5H
export AWS_SECRET_ACCESS_KEY=sWi7GKbfJd+Rsa8uSarhc0WnT5dNTd242syp+jgb
export AWS_SESSION_TOKEN=IQoJb3JpZ2luX2VjEIb//////////wEaCXVzLXdlc3QtMiJHMEUCIG40IvqW8Wgsh3n74HO22Xxs5bWD5Z+i1dNxjihkG0qiAiEA26cZoMm39Fi4ryWgyFF09qWYpuiaP1Fe4LDiIChtg/QqugII7///////////ARAAGgw4NDE5NTc3NDc3MzciDONna/0rJni9VLPk6SqOAlF6XkhQUdLecHLaMsCIo4alR0nTp/xWqfPAFGZvL/bg1QOhEuqEqY9YX1rCfPZA/yWRBU9mhPZrMJY4cCvDll1KPWNS66CyCFBeTXZH4RTntOIs+cYRbF5qZ+eo0RD4rbfdXfemn0nHVKJOnJ/2wrKBYOqGNW0I5+mzj7W+PjtR/yxap8Tos8QrYFNZ52YnUACKeX3fG8offvPJEPk45T8URu+uWyCYXg62hGfCm24l8TpdFFlTg90a7cOrLftV6VMlSpP/+SAs5pYb05n8TSRExpkZafiADcAbj0KTpF0Vw3DC3xScT2xXbYawe+Q4nZsQHjS6Nk0enYSuZ7xk9ptNklulEwL/GmIZhNHhyjCCzZCEBjqdAYYos5+h9aMga1/5eqsejZfNLZIdhe3QuPuGS5UxPtZ1UeIUogQ6rYLcvkUnGkXh3eG7XbYSDYBuIVCbDdwT9GrOHXa8QAz0tVtgOdIXHicTcTOPYpgU/yS4Kyn7FjWyScwpXNos2M+1tIVhb8DRTwrD2Mplm6rdu4w3Vhmuh+V1R2nXTjXYdEnahnyxYLNkTLZMlOd/KwF/O2bLwFY=

PROFILE_NAME=benchmark3
CLUSTER_NAME=benchmark-cluster3
REGION=us-east-1
LAUNCH_TYPE=EC2

ecs-cli configure profile --profile-name "$PROFILE_NAME" --access-key "$AWS_ACCESS_KEY_ID" --secret-key "$AWS_SECRET_ACCESS_KEY" --session-token "$AWS_SESSION_TOKEN"
ecs-cli configure --cluster "$CLUSTER_NAME" --default-launch-type "$LAUNCH_TYPE" --region "$REGION" --config-name "$PROFILE_NAME"

#aws ec2 create-key-pair --key-name benchmark-cluster3 --query 'KeyMaterial' --output text > ~/.ssh/benchamrk-cluster3.pem

ecs-cli up --keypair benchmark-cluster3 --instance-role aaa --size 2 --instance-type t3.medium --tags project=benchmark-cluster,owner=benchmark --cluster-config benchmark --ecs-profile benchmark3