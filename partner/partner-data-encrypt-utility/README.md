# Partner Data Encrypt Utility

## Overview
This utility provides a command-line tool for encrypting partner data in the MOSIP ecosystem.
## Prerequisites
- JDK 21.0.3 or higher
- Maven 3.9.6 or higher
- PostgreSQL database (for data access)

## Build & Run (for developers)

1. **Build and install:**
    ```sh
    cd partner
    mvn install -DskipTests=true -Dmaven.javadoc.skip=true -Dgpg.skip=true
    ```
2. **Build Docker image:**
    ```sh
    cd partner-data-encrypt-utility
    docker build -t partner-data-encrypt-utility .
    ```
3. **Run locally:**
    ```sh
    java -jar target/partner-data-encrypt-utility-*.jar
    ```
4. **Run with Docker:**
    ```sh
    docker run --rm \
      -e active_profile=dev \
      -e spring_config_label=master \
      -e spring_config_url=https://dev.mosip.net \
      partner-data-encrypt-utility
    ```

## Configuration
- Edit `src/main/resources/application.properties` and `bootstrap.properties` for database and service configuration.
- You can also pass configuration via environment variables or Docker build/run arguments as shown above.

## Notes
- The utility will automatically shut down after processing.
- For custom encryption logic, extend the service and utility classes as needed.

## License
This project is licensed under the terms of [Mozilla Public License 2.0](../../LICENSE) 