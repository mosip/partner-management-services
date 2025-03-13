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

# Build-time arguments for configuration
ARG spring_config_label
ARG active_profile
ARG spring_config_url
ARG is_glowroot
ARG artifactory_url
# Runtime environment variables
ENV active_profile_env=${active_profile}
ENV spring_config_label_env=${spring_config_label}
ENV spring_config_url_env=${spring_config_url}
ENV is_glowroot_env=${is_glowroot}
ENV artifactory_url_env=${artifactory_url}
# Container user configurations
ARG container_user=mosip
ARG container_user_group=mosip
ARG container_user_uid=1001
ARG container_user_gid=1001

# Install required packages and create user
RUN apk -q update \
&& apk add -q unzip wget \
&& addgroup -g ${container_user_gid} ${container_user_group} \
&& adduser -s /bin/sh -u ${container_user_uid} -G ${container_user_group} -h /home/${container_user} --disabled-password ${container_user}

# Set working directory
WORKDIR /home/${container_user}
ENV work_dir=/home/${container_user}

# Create additional JARs directory
ARG loader_path=${work_dir}/additional_jars/
RUN mkdir -p ${loader_path}
ENV loader_path_env=${loader_path}

# Define volume for logs and Glowroot storage
VOLUME /home/logs /home/Glowroot

# Copy JAR file
COPY ./target/partner-management-batch-job-*.jar partner-management-batch-job.jar

# Set permissions for user
RUN chown -R ${container_user}:${container_user} /home/${container_user}

# Run container as the created user
USER ${container_user_uid}:${container_user_gid}

# Expose port
EXPOSE 9110

# Entry point command
CMD if [ "$is_glowroot_env" = "present" ]; then \
    wget -q --show-progress "${artifactory_url_env}"/artifactory/libs-release-local/io/mosip/testing/glowroot.zip ; \
    unzip glowroot.zip ; \
    rm -rf glowroot.zip ; \
    sed -i 's/<service_name>/partner-management-batch-job/g' glowroot/glowroot.properties ; \
    java -jar -javaagent:glowroot/glowroot.jar -Dloader.path="${loader_path_env}" -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.profiles.active="${active_profile_env}" -Dspring.cloud.config.uri="${spring_config_url_env}" partner-management-batch-job.jar ; \
    else \
    java -jar -Dloader.path="${loader_path_env}" -Dspring.cloud.config.label="${spring_config_label_env}" -Dspring.profiles.active="${active_profile_env}" -Dspring.cloud.config.uri="${spring_config_url_env}" partner-management-batch-job.jar ; \
    fi