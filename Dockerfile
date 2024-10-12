FROM registry.access.redhat.com/ubi9/openjdk-17

LABEL org.opencontainers.image.authors="praveensonare007@gmail.com" \
      io.k8s.description="developer backend" \
      io.k8s.display-name="image service" \
      io.openshift.expose-services="8080:http" \
      io.openshift.tags="springboot"

EXPOSE 8080 9000
RUN mkdir -p /tmp/src/
ADD . /tmp/src/

RUN cd /tmp/src && sh gradlew build -x test --no-daemon

RUN ln -s /tmp/src/build/libs/airtable-service*.jar /deployments/prog-image.jar

