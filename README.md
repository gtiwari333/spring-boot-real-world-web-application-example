### A Spring Boot Web Application Sample with tons of ready-to-use features. This can be used as starter for bigger projects.

### Variations
- Simpler version without KeyCloak and multi-modules is on separate project https://github.com/gtiwari333/spring-boot-blog-app
- Microservice example that uses Spring Cloud features(discovery, gateway, config server etc) is on separate project https://github.com/gtiwari333/spring-boot-microservice-example-java

<a href="https://www.buymeacoffee.com/gtiwari333" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/arial-blue.png" alt="Buy Me A Coffee" style="height: 42px !important;width: 180px !important;" height="42px" width="180px"></a>


### Service Dependency Map

```
main-webapp
├── mysql          (jdbc:mysql://mysql:3306/seedapp)
├── activemq       (activemq:61616)
├── keycloak       (http://keycloak:8080)
├── zipkin         (http://zipkin:9411)
└── email-service
├── emailhog   (emailhog:1025)
└── zipkin

content-checker-service
├── activemq
└── zipkin

trend-service
└── zipkin

report-service
├── mysql
├── keycloak
└── zipkin

```

### App Architecture:
[![Foo](https://lucid.app/publicSegments/view/8c2fa859-36bd-4559-80c7-12fb30997092/image.png)](https://lucid.app/documents/view/fa076c6e-86d3-412b-a9bc-1996dca86a1e)

### Included Features/Samples

MicroService:

[//]: # (- Spring micrometer based tracing with zipkin)
- Exposing and implementing Open Feign clients
- Spring Cloud Contract (WIP)

Spring MVC:
- MVC with thymeleaf templating
- Public and internal pages
- Live update of thymeleaf templates for local development
- HTML fragments, reusable pagination component using Thymeleaf parameterized fragments
- webjar - bootstrap4 + jquery
- Custom Error page
- Request logger filter
- Swagger API Docs with UI  ( http://localhost:8081/swagger-ui.html)
- @RestControllerAdvice, @ControllerAdvice demo
- CRUD UI + File upload/download
- favicon handler

Security:
- Account management with KeyCloak
- Spring Security 
- User/User_Authority entity and repository/services
    - login, logout, home pages based on user role
- Domain object Access security check on update/delete using custom PermissionEvaluator
- private pages based on user roles
- public home page -- view all notes by all 
- Limit max number of record in a paged request

Persistence/Search:
- Data JPA with User/Authority/Note/ReceivedFile entities, example of EntityGraph
- MySQL or any other SQL db can be configured for prod/docker etc profiles
- (in old code) H2 db for local, Console enabled for local ( http://localhost:8081/h2-console/, db url: jdbc:h2:mem:testdb, username: sa)
- jOOQ integration with code generation based on JPA entity 
- Liquibase database migration

Test:
- Unit/integration with JUnit 5, Mockito and Spring Test
- Tests with Spock Framework (Groovy 5, Spock 2.4)
- e2e with Selenide, fixtures. default data generated using Spring
- Load test with Gatling/Scala
- Architecture tests using ArchUnit
- file upload/download e2e test with Selenide
- TestContainers to perform realistic integration test
- Reset DB and Cache between test
- Assert expected query count during integration test

Misc:
- Code Generation: lombok,  mapstruct 
- Message Queue using ActiveMQ Artemis
- Approval/flagging api - message based
- Nested comment
- Cache implemented
- Zipkin tracing 
- Websocket implemented to show article/comment review status/notifications..
- Docker-compose deploy/kubernetes

Future: do more stuff
- CQRS with event store/streaming  
- Spring Cloud Contract integration (WIP)
- Visitors log - IP, browser, etc
- Centralized error reporting
- Geo-Spatial query for visitors
- Grafana Dashboard, @Timed and more ...
- logback LevelChangePropagator integration
- logback error email
- logback rolling policy
- Integrate Markdown editor for writing notes
- rate limit by IP on public API ( article api )
- Fetch user's avatar
- UI improvement
- S3 file upload, test with localstack TestContainers
- nested comment query/performance fix 
- Signup UI
- vendor neutral security with OIDC
- JfrUnit ( WIP )

### Requirements

- JDK 25+
- Lombok configured on IDE
    - http://ganeshtiwaridotcomdotnp.blogspot.com/2016/03/configuring-lombok-on-intellij.html
    - For eclipse, download the lombok jar, run it, and point to eclipse installation
- Maven
- Docker 
  - Make sure docker is started and running
  - Run `$ sudo chmod 666 /var/run/docker.sock` if you get error like this "Cannot connect to the Docker daemon at unix:///var/run/docker.sock. Is the docker daemon running? (Details: [13] Permission denied)"

# How to Run

## With docker - run all apps with dependencies (eg mysql, email server, keycloak etc):

### Build and create docker image

`sh build-docker-images.sh`

### Run all apps and dependencies using docker-compose 

- Run ```docker compose -f docker/docker-compose.yml up``` at root to run all apps and dependencies and open http://localhost:8081 to access main app



## How to Run apps individually without docker

It contains following applications:

- main-app
- email-service (optional)
- report-service (optional)
- trend-service (optional)
- content-checker (optional)

## Note you will need to create a database named 'seedapp' in your mysql server

Option 1 - run with manually started  KeyCloak, ActiveMQ and MySQL  servers
- Run ```mvn clean install``` at root 
- Run ```docker compose -f docker/docker-compose-dev.yml up``` at root to start docker containers
- Go to main-app folder and run ```mvn``` to start the application


Option 2 - run from IDE
- import into your IDE and compile the full project and run the Application.java on main-app module
- Update run configuration to run maven goal `wro4j:run` Before Launch. It should be after 'Build'

## Once the application starts, open  `http://localhost:8081` on your browser. 

The default username/passwords are listed on : gt.app.Application.initData, which are:

- system/pass
- user1/pass
- user2/pass


# Run Tests  

    ! Test uses TestContainers, which requires Docker to be installed locally.

##### Running full tests

`mvn clean verify`

##### Running unit tests only (it uses maven surefire plugin)

`mvn  compiler:testCompile resources:testResources  surefire:test`

##### Running integration tests only (it uses maven-failsafe-plugin)

`mvn  compiler:testCompile resources:testResources  failsafe:integration-test`

### Run Tests Faster by using parallel maven build
`mvn -T 5 clean package`


# Code Quality

##### The `error-prone` runs at compile time.

##### The `modernizer` `checkstyle` and `spotbugs` plugin are run as part of maven `test-compile` lifecycle phase. use `mvn spotbugs:gui' to

##### SonarQube scan

Run sonarqube server using docker
`docker run -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true -p 9000:9000 sonarqube:latest`

Perform scan:
`mvn sonar:sonar`
mvn sonar:sonar -Dsonar.login=admin -Dsonar.password=admin

View Reports in SonarQube web ui:

- visit http://localhost:9000
- default login and password are `admin`, you will be asked to change password after logging in with default
  username/password
- (optional) change sonarqube admin password without logging
  in: `curl -u admin:admin -X POST "http://localhost:9000/api/users/change_password?login=admin&previousPassword=admin&password=NEW_PASSWORD"`
- if you change the password, make sure the update `-Dsonar.password=admin` when you run sonarqube next time

### Dependency vulnerability scan

Owasp dependency check plugin is configured. Run `mvn dependency-check:check` to run scan and
open `dependency-check-report.html` from target to see the report.


### Dependency/plugin version checker
 - `mvn versions:display-dependency-updates`
 - `mvn versions:display-plugin-updates`



# Screenshots:

#### Public View
![](screenshots/public-page.png)

#### Read Article with nested comment/discussion
![](screenshots/read-article-with-nested-comment.png)

#### Logged in Feed View
![](screenshots/logged-in-home-page.png)

#### Logged in User's Article List View
![](screenshots/users-home-page.png)

#### Admin User's Review Page to approve/disapprove flagged posts
![](screenshots/admin-user-review-page.png)

#### Review Page
![](screenshots/review-flagged-content.png)

#### New Article
![](screenshots/new-article-page.png)
