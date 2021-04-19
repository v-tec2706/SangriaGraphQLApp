FROM java:8

WORKDIR .

COPY target/scala-2.13/SangriaGraphQLApp-assembly-0.1.jar /
COPY run.sh /

ENTRYPOINT bin/bash run.sh