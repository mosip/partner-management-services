FROM eclipse-temurin:21-jre-alpine

# Build-time arguments
ARG SOURCE
ARG COMMIT_HASH
ARG COMMIT_ID
ARG BUILD_TIME
LABEL source=${SOURCE}
LABEL commit_hash=${COMMIT_HASH}
LABEL commit_id=${COMMIT_ID}
LABEL build_time=${BUILD_TIME}

# can be passed during Docker build as build time environment for github branch to pickup configuration from.

ARG spring_config_label

# can be passed during Docker build as build time environment for spring profiles active 

ARG active_profile

# can be passed during Docker build as build time environment for config server URL 

ARG spring_config_url

# environment variable to pass active profile such as DEV, QA etc at docker runtime

ENV active_profile_env=${active_profile}

# environment variable to pass github branch to pickup configuration from, at docker runtime

ENV spring_config_label_env=${spring_config_label}


# environment variable to pass spring configuration url, at docker runtime

ENV spring_config_url_env=${spring_config_url}

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user=mosip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_group=mosip

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_uid=1002

# can be passed during Docker build as build time environment for github branch to pickup configuration from.
ARG container_user_gid=1001

# Install required packages and create user
RUN apk -q update \
&& apk add -q unzip wget \
&& addgroup -g ${container_user_gid} ${container_user_group} \
&& adduser -s /bin/sh -u ${container_user_uid} -G ${container_user_group} -h /home/${container_user} --disabled-password ${container_user}

# Set working directory
WORKDIR /home/${container_user}
ENV work_dir=/home/${container_user}

# Copy JAR file
COPY ./target/partner-management-batch-job-*.jar partner-management-batch-job.jar

# Set permissions for user
RUN chown -R ${container_user}:${container_user} /home/${container_user}

# Run container as the created user
USER ${container_user_uid}:${container_user_gid}

# Expose port
EXPOSE 9110

CMD java -jar \
    -Dspring.cloud.config.label="${spring_config_label_env}" \
    -Dspring.profiles.active="${active_profile_env}" \
    -Dspring.cloud.config.uri="${spring_config_url_env}" \
    partner-management-batch-job.jar