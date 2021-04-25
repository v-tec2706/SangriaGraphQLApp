aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 841957747737.dkr.ecr.us-east-1.amazonaws.com
docker build -t wsoczek .
docker tag wsoczek:latest 841957747737.dkr.ecr.us-east-1.amazonaws.com/wsoczek:latest
docker push 841957747737.dkr.ecr.us-east-1.amazonaws.com/wsoczek:latest