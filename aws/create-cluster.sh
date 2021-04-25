KEY_PAIR=benchmark-cluster
    ecs-cli up \
      --keypair $KEY_PAIR  \
      --capability-iam \
      --size 2 \
      --instance-type t3.medium \
      --tags project=benchmark-cluster,owner=benchmark \
      --cluster-config benchmark \
      --ecs-profile benchmark