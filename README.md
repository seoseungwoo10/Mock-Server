# Mock Server

## 개요

이 프로젝트는 외부 서비스 API를 모킹하기 위한 Spring Boot 기반의 Mock Server입니다.
다양한 시나리오에 따라 동적으로 응답을 생성하거나, 실제 외부 API로 요청을 전달(Relay)하는 기능을 제공합니다.
또한, 비동기 응답을 위한 Webhook 알림 기능과 현실적인 Mock 데이터 생성을 위한 JavaFaker 라이브러리를 활용합니다.

## 주요 기능

*   **규칙 기반 Mock 응답**: 특정 요청 조건에 따라 미리 정의된 Mock 응답을 반환합니다.
*   **요청 Relay**: 특정 경로로 들어오는 요청을 실제 외부 API로 전달하고 그 응답을 반환합니다.
*   **비동기 Webhook 알림**: 한도 조회와 같은 작업에 대해 비동기적으로 Webhook URL을 통해 결과를 통지할 수 있습니다.
*   **Mock 규칙 관리 API**: Mock 응답 규칙을 동적으로 추가하고 조회할 수 있는 API를 제공합니다.
*   **현실적인 Mock 데이터 생성**: JavaFaker를 사용하여 이름, 주민등록번호 등 현실적인 가짜 데이터를 생성합니다.

## 기술 스택

*   Java 17
*   Spring Boot 3.5.0
*   Spring Web
*   Lombok
*   JavaFaker 1.0.2
*   Maven

## 시작하기

### 요구 사항

*   Java 17 이상
*   Maven

### 설치 및 실행

1.  프로젝트를 클론하거나 다운로드합니다.
    ```bash
    git clone <repository-url>
    cd mock-server
    ```
2.  Maven을 사용하여 프로젝트를 빌드합니다.
    ```bash
    mvn clean install
    ```
3.  애플리케이션을 실행합니다.
    ```bash
    mvn spring-boot:run
    ```
    또는 IDE에서 `MockServerApplication.java` 파일을 직접 실행합니다.

애플리케이션은 기본적으로 `8080` 포트에서 실행됩니다.

## API 엔드포인트

### Loan API (`/api/v1/loans`)

*   **한도 조회**: `POST /api/v1/loans/limits`
    *   요청 본문 (`LoanDtos.LimitRequest`):
        ```json
        {
          "userName": "홍길동",
          "userRegistrationNumber": "880101-1234567",
          "webhookUrl": "https://your-webhook-url.com/callback" // 선택 사항
        }
        ```
    *   응답 본문 (`LoanDtos.LimitResponse`):
        *   동기 응답 예시:
            ```json
            {
              "applicationId": "app-12345",
              "limitAmount": 50000000,
              "interestRate": 3.5,
              "customHeaders": {
                  "X-Custom-Header": "Value"
              }
            }
            ```
        *   비동기 처리 시작 시 응답 예시:
            ```json
            {
              "applicationId": "app-12345",
              "status": "PROCESSING"
            }
            ```
    *   설명: 사용자 정보와 선택적으로 Webhook URL을 받아 대출 한도를 조회합니다. `webhookUrl`이 제공되면, 서버는 초기 응답 후 해당 URL로 최종 결과를 비동기적으로 전송할 수 있습니다.

*   **대출 실행**: `POST /api/v1/loans/executions`
    *   요청 본문 (`LoanDtos.ExecutionRequest`):
        ```json
        {
          "applicationId": "app-12345",
          "loanAmount": 30000000
        }
        ```
    *   응답 본문 (`LoanDtos.ExecutionResponse`):
        ```json
        {
          "status": "SUCCESS",
          "message": "대출이 성공적으로 실행되었습니다."
        }
        ```
    *   설명: 한도 조회 후 발급된 `applicationId`와 실제 대출 희망 금액을 받아 대출을 실행합니다. (현재 구현은 `HttpStatus.NOT_IMPLEMENTED`를 반환합니다.)

*   **요청 Relay**: `ANY /api/v1/loans/relay/**`
    *   설명: `/api/v1/loans/relay/` 하위 경로로 들어오는 모든 HTTP 요청(GET, POST, PUT, DELETE 등)을 설정된 외부 대상 URL로 전달합니다. 요청 헤더와 본문이 그대로 전달되며, 외부 API의 응답이 클라이언트에게 반환됩니다.
    *   예시: 클라이언트가 `GET /api/v1/loans/relay/external/users/1`로 요청하면, Mock 서버는 이 요청을 `targetBaseUrl + /external/users/1`로 전달합니다.

### Mock Config API (`/api/v1/mock-configs`)

*   **Mock 규칙 추가**: `POST /api/v1/mock-configs`
    *   요청 본문 (`MockRule`):
        ```json
        {
          "ruleName": "HighIncomeUserLimit",
          "conditions": {
            "pathPattern": "/api/v1/loans/limits",
            "httpMethod": "POST",
            "requestBodyFields": {
              "userRegistrationNumber": "^[0-9]{6}-[12][0-9]{6}$" // 정규식 예시
            }
          },
          "response": {
            "statusCode": 200,
            "body": {
              "limitAmount": 100000000,
              "interestRate": 2.5
            },
            "headers": {
              "X-Mock-Rule": "HighIncomeUserLimit"
            }
          },
          "priority": 1,
          "delayMs": 0,
          "isRelay": false
        }
        ```
    *   응답: `ResponseEntity<String>` (예: "규칙이 성공적으로 추가되었습니다: HighIncomeUserLimit")
    *   설명: 새로운 Mock 응답 규칙을 시스템에 추가합니다.

*   **Mock 규칙 조회**: `GET /api/v1/mock-configs`
    *   응답 본문: `ResponseEntity<List<MockRule>>` (현재 설정된 모든 Mock 규칙 목록)
    *   설명: 현재 시스템에 설정된 모든 Mock 규칙의 목록을 조회합니다.

### 에러 응답

잘못된 요청이나 서버 오류 발생 시 `LoanDtos.ErrorResponse` 형식으로 응답합니다.
```json
{
  "errorCode": "INVALID_INPUT",
  "errorMessage": "사용자 이름을 입력해주세요."
}