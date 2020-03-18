FROM amazoncorretto:11
LABEL maintainer="k8sbook"

RUN yum install -y glibc-langpack-ja
ENV LANG ja_JP.UTF8
ENV LC_ALL ja_JP.UTF8
RUN ln -sf /usr/share/zoneinfo/Japan /etc/localtime

VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", \
 "-verbose:gc", \
 "-Xlog:gc*:stdout:time,uptime,level,tags", \
 "-Djava.security.egd=file:/dev/./urandom", \
 "-jar", \
 "/app.jar"]
