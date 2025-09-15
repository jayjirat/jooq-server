# JOOQ Server

A proof-of-concept server that allows clients to upload JAR files containing JOOQ query methods and execute them dynamically through REST APIs.

## Overview

This project provides a flexible way to execute JOOQ queries by uploading compiled JAR files to the server. Clients can upload their custom query methods packaged as JAR files, and then execute these methods remotely with dynamic conditions.

**⚠️ Note: This is a POC (Proof of Concept) project and does not include any security measures. Do not use in production environments.**

## Features

- Upload JAR files containing JOOQ query methods
- List available methods from uploaded JARs
- Execute methods dynamically with complex conditions
- Support for nested conditions with logical operators
- JSON response format

## API Endpoints

### 1. Upload JAR File

```
POST /api/jar/upload
```

Upload a JAR file containing JOOQ query methods.

**Request:**

- Content-Type: `multipart/form-data`
- Form field: `file` (the JAR file)

### 2. List Methods

```
GET /api/jar/methods
```

Retrieve all available methods from uploaded JAR files.

### 3. Execute Method

```
POST /api/jar/execute
```

Execute a specific method with provided conditions.

**Request Body:**

```json
{
  "jarName": "string",
  "methodName": "string",
  "conditions": {
    "field": "string",
    "operator": "EQUALS|NOT_EQUALS|GREATER_THAN|LESS_THAN|BETWEEN|IN|LIKE",
    "logicalOperator": "AND|OR|NOT",
    "values": ["array of values"],
    "nestedConditions": [...]
  }
}
```

## Client Setup

### Maven Configuration

Before building your JAR file, configure your `pom.xml` to disable fat JAR creation:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
      <executions>
        <execution>
          <id>repackage</id>
          <phase>none</phase> <!-- Disable fat JAR -->
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

### Query Method Structure

Your query methods must follow this exact signature:

```java
public class MyQuery {
    public static Result<?> getUser(DSLContext dsl, Condition condition) {
        return dsl.selectFrom("users")
                .where(condition)
                .fetch();
    }

    public static Result<?> getOrder(DSLContext dsl, Condition condition) {
        return dsl.selectFrom("orders")
                .where(condition)
                .fetch();
    }
}
```

**Requirements:**

- Methods must be `public static`
- Return type must be `Result<?>`
- Must accept exactly two parameters: `DSLContext dsl` and `Condition condition`
- Use the provided `condition` parameter in your WHERE clause

### Build and Upload

1. Compile your project: `mvn compile`
2. Package as JAR: `mvn package`
3. Upload the generated JAR file using the upload API

## Example Usage

### Upload JAR

```bash
curl -X POST \
  -F "file=@myclient.jar" \
  http://localhost:8080/api/jar/upload
```

### List Methods

```bash
curl -X GET http://localhost:8080/api/jar/methods
```

### Execute Method

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "jarName": "myclient2",
    "methodName": "MyQuery:getOrder",
    "conditions": {
      "field": "status",
      "operator": "EQUALS",
      "logicalOperator": "OR",
      "values": ["PENDING"],
      "nestedConditions": [
        {
          "field": "status",
          "operator": "EQUALS",
          "logicalOperator": "OR",
          "values": ["COMPLETED"],
          "nestedConditions": []
        },
        {
          "field": "user_id",
          "operator": "EQUALS",
          "logicalOperator": "NOT",
          "values": [3],
          "nestedConditions": []
        }
      ]
    }
  }' \
  http://localhost:8080/api/jar/execute
```

### Example Response

```json
[
  {
    "id": 1,
    "user_id": 1,
    "order_date": "2025-09-11T20:17:34.000+00:00",
    "status": "COMPLETED"
  },
  {
    "id": 2,
    "user_id": 2,
    "order_date": "2025-09-11T20:17:34.000+00:00",
    "status": "PENDING"
  },
  {
    "id": 3,
    "user_id": 1,
    "order_date": "2025-09-11T20:17:34.000+00:00",
    "status": "CANCELLED"
  }
]
```

## Supported Operators

### Comparison Operators

- `EQUALS` - Equal to
- `NOT_EQUALS` - Not equal to
- `GREATER_THAN` - Greater than
- `LESS_THAN` - Less than
- `BETWEEN` - Between two values
- `IN` - In a list of values
- `LIKE` - Pattern matching

### Logical Operators

- `AND` - Logical AND
- `OR` - Logical OR
- `NOT` - Logical NOT

## Condition Structure

Conditions support nested structures for complex queries:

- `field`: Database column name
- `operator`: Comparison operator
- `logicalOperator`: How to combine with other conditions
- `values`: Array of values for the comparison
- `nestedConditions`: Array of sub-conditions for complex logic

## Limitations

- **No Security**: This is a POC with no authentication or authorization
- **No Validation**: Limited input validation
- **No Error Handling**: Minimal error handling for edge cases
- **Single Database**: Assumes single database connection
- **Memory Usage**: Loaded JARs remain in memory

## CI Pipeline Test with Trivy

### Build CI Image

```bash
docker build -f Dockerfile.ci -t ci-pipeline:latest .
```

### Run CI Pipeline

```bash
docker run --rm \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v ./:/app \
  -w /app \
  ci-pipeline:latest \
  bash ci.sh
```

## Development Notes

This project demonstrates dynamic JAR loading and method execution using reflection. In a production environment, you would need to add:

- Authentication and authorization
- Input validation and sanitization
- Proper error handling
- Security scanning of uploaded JARs
- Resource management and cleanup
- Logging and monitoring
- Database connection pooling
- Method signature validation

## Dependencies

- Spring Boot
- JOOQ
- Database driver (PostgreSQL/MySQL/etc.)
- Maven/Gradle for build management
