# Application base microservices (suite2)
(NEW) in this project, we replace gateway Api **Spring-Cloud-Gateway** by **Kong Api Gateway**  
(NEW) we configure in the Kong Gateway: **routing**, **rate-limiting**, **authentication**, **logging**, **etc.**  
(NEW) we add Konga-dashboard which is Kong UI to manage Kong objects

- application base microservices that manage addresses, employees, companies and projects. 
- each business microservices of the application is implemented into **clean architecture**. 
- each writing event is published and distributed using **kafka infrastructure**.
- **avro** and **schema-registry** are used to serialize kafka events
- kong-api-gateway to be the bridge between http users and backend services
- konga-dashboard to manager Kong objects

## business microservices
- Address-microservice 
- Employee-microservice 
- company-microservice
- Project-microservice

# utility tools

## 1. utility microservices
- microservices-config-service: to centralize and distribute all microservices configurations
- kong-API-gateway as unique entry point to backend microservices. 
- In declarative mode, **kong.yaml** file of all configurations and docker-compose file for kong deployment are defined under **Kong-API-Gateway-DBLess** folder:
    - routing: all microservices routes,
    - rate-limiting: common plugin for all microservices that limit traffics to 5 requests each minute,
    - authentication: http user have to authenticate before accessing the appication,
    - logging
- when using UI for managing kong objects, we deploy **konga-dashboard**: 
  - all configuratons created declarative mode, are done using konga UI
  - under **Kong-API-Gateway-Postgres-Konga** folder is docker-compose file for deploying kong infrastructure:
    - **postgress db**, **kong-database-migrations**, **kong-api-gateway**, **konga-dashboard**
- Under **Logs** folder is a logs file of all hppt request torwards backend microservices

## 2. kafka infrastructure
- a kafka infrastructure to publish and distribute events.
- each writing event in database (POST, DELETE, UPDATE) is distributed into kafka topics.
- **schema registry** to difine schema for all events and **avro** to serialiaze events sent to topics
- **kafdrop** is used as UI for managing and exploring kafka events, kafka servers,...
- kafka infrastructure:
  - **zookeeper**: to manage kafka brokers
  - **kafka-server(3 brokers)**, **kafka-broker-1**, **kafka-broker-2**, **kafka-broker-3**: publish events into topics and disbribute events to consumers
  - **schema registry**: defines and register a common schema of the all events
  - **avro schema**:to serialize kafka events
  - **kafdrop**: a kafka UI

## 3. databases
- **mysql db** docker image for peristing data from business microservices
- **postgreSQL db** docker image for persisting all kong configurations made with Konga-UI

# unit tests and deploment
- each code unit of business microservices is tested with **JUnit5** and **AssertJ**.
- **Mockito** is used to mock external unit dependency.
- **KafkaContainer** is used  to mock Kafka  containers producer/consumer.
      - [Testcontainers for Java](https://java.testcontainers.org/modules/kafka/).
- each service (business and utility microservice) is deployed in docker image.
- a docker-compose template is used to deploy all images of the application.
- Jenkins is used for continuous integrataion and deployment (CI/CD). After each git commit, Jenkins launch automatically following jobs:
  - a build of each microservice.
  - unit tests of all business microservices and published a report of passed, skipped and fail tests.
  - package each microservice and publish a related jar file.
  - build a docker image of each microservice refering to a dockerfile defined inside microservice.
  - publish docker images in docker registry.
  - run docker images of microservices of application in docker containers.
 
    
 ### deployed microservices in docker images
 - kafka infrastructure:
  - zookeeper (one instance) for managing kafka brokers
  - kafka server (three instances)
  - kafdrop (one instance) for web UI for monitoring kafka brokers, topics and events produced
- utility microservices:
  - a config service for managing and externalize services configuration
  - a K8s discovery service dependency for microservices registration and discovery
  - kong api infrastructure: postgreSQL, kong-db-prepare, kong-api-gateway, konga-db-prepare, konga-konga-dashboard

- business microservices:
  - k8s-kafka-avro-kong-bs-ms-address
  - k8s-kafka-avro-kong-bs-ms-employee
  - k8s-kafka-avro-kong-bs-ms-company
  - k8s-kafka-avro-kong-bs-ms-project
 
### summary (CI-DC)
![my-ci-cd-flow](https://github.com/placidenduwayo1/k8s-kafka-avro-kong-back/assets/124048212/e7e0898b-79d7-4786-a1c5-3197e0438b39)
    
## architecture kafka inside business microservice
- a model is a java bean that is sent as payload using a REST api, 
- a spring service build a kafka message with java model,
- a spring service uses kafka producer to send the kafka message to kafka topic,
- a spring service uses kafka consumer to subscribe to the kafka topic and consumes events that it sends to another spring service,
- the final spring service can handle received event as it wants, either to persist it in db or do anything with it.


  ### kafka infra summary
![kafka-infrastructure](https://github.com/placidenduwayo1/k8s-kafka-avro-aepc-back/assets/124048212/4cb3738e-718a-466c-9b59-41d4773a1a0b)

- a schema registry defines a schema for all events to publish into kafka topics,
- avro-schema uses that defined and registered schema to serialize avents,
- after events serialized, kafka producer send them into kafka topics.

# global architecture of the project
![k8s-kafka-avro-kong-clean-archi](https://github.com/placidenduwayo1/k8s-kafka-avro-kong-back/assets/124048212/7fed9f8a-e7c3-4d22-8331-ca736ec4d338)

- To access to backend business microservices, the client goes through a ***kong-api-gateway***

## Kong object creation using Konga-dashbboard

- to manager Kong objects using Konga-dashoboard, we connect to Konga-dashboard: **http://localhost:8686**.
- we create connection to Kong Admin API: **http://kong-api-gateway:8001**.
- via the kong-dashboard we can configure: create services, routes, plugins, consumers, etc.

## expoded endpoints by microservices

After creating Kong objects ing Kong-dashboard, we access to endepoints via kong api gateway

kong-api-gateway url: **http://localhost:8002/**

  ## address-microservice

  list of **endpoints** exposed by k8s-kafka-avro-kong-bs-ms-address:
    - [GET] ```http://localhost:8002/address-api/```
    - [POST]```http://localhost:8002/addresses```
    ***payload***:
      ```
      {
        num:int value
        street: string value
        poBox: int value
        city: string value
        coutry: string value
      }
      ```
    - [GET]```http://http://localhost:8002/address-api/addresses```
    - [GET]```http://http://localhost:8002/address-api/addresses/id/{value of address id}```
    - [DELETE]```http://http://localhost:8002/address-api/addresses/id/{value of address id}```
    **Note**: address cannot be removed when it is aleardy assigned  employee or company 
    - [PUT]```http://http://localhost:8002/address-api/addresses/id/{value of address id}```
      ***payload***:
        
        ```
        {
          num:int value
          street: string value
          poBox: int value
          city: string value
          coutry: string value
        }
        ```

## company-microservice

list of **endpoints** exposed by k8s-kafka-avro-kong-bs-ms-company pod:
  - [GET] ```http://http://localhost:8002/company-api/```
  - [POST]```http://localhost:8002/company-api/companies```
      
      ***payload***:
      ```
      {
        name: string value
        agency: int value
        type: string value
        address-id: string value
      }
      ```
  **Note**: cannot create company on address that already holds another company
  - [GET] ```http://localhost:8002/company-api/companies```
  - [GET] ```http://localhost:8002/company-api/addresses/id/{address-id}```
  - [GET] ```http://localhost:8002/company-api/addresses/city/{city}```
  - [DELETE] ```http://localhost:8002/company-api/companies/id/{company-id}```
  - [PUT] ```http://localhost:8002/company-api/companies/id/{company-id}```
  **Note**: cannot update company on address that already holds another company
  - [GET] ```http://localhost:8002/company-api/companies/name/{company-name}```
  - [GET] ```http://localhost:8002/company-api/companies/agency/{company-agency}```
      
    ***payload***:
    ```
    {
      name: string value
      agency: int value
      type: string value
      address-id: string value
    }
    ```

## employee-microservice

list of **endpoints** exposed by k8s-kafka-avro-kong-bs-ms-employee pod:
  - [GET] ```http://localhost:8002/employee-api/```
  - [POST]```http://localhost:8002/employee-api/employees```
      
  ***payload***:
  ```
  {
    firstname: string value
    lastname: string value
    state: string value
    type: string value
    address-id: string value
  }
  ```
  - [GET] ```http://localhost:8002/employee-api/employees```
  - [GET] ```http://localhost:8002/employee-api/employees/id/{employee-id}```
  - [GET] ```http://localhost:8002/employee-api/employees/lastname/{lastname}```
  - [DELETE] ```http://localhost:8002/employee-api/employees/id/{employee-id}```
  **Note**: cannot delete employee that already assigned project(s)
  - [PUT] ```http://localhost:8002/employee-api/employees/id/{employee-id}``` 
  ***payload:***
  ```
  {
    firstname: string value
    lastname: string value
    state: string value
    type: string value
    address-id: string value
  }
  ```
  - [GET] ```http://localhost:8002/employee-api/employees/addresses/id/{address-id}```: employees living at given address
  - [GET] ```http://localhost:8002/employee-api/employees/addresses/city/{address-id}```: employees living at given address city


for [POST] and [PUT], ***address-id***, employee ms sends request to address ms to ask address related to address-id. a resilience is managed if address ms is down

## project-microservice

list of **endpoints** exposed by k8s-kafka-avro-kong-bs-ms-project pod:
  - [GET] ```http://localhost:8002/project-api/``
  - [POST]```http://localhost:8002/project-api/projects```
      
  ***payload:***
  ```
  {
    name: string value
    description: string value
    priority: string value
    state: string value
    employee-id: string value
    company-id: string value
  }
  ```
  **Note**: cannot create project for archived employee
  - [GET] ```http://localhost:8002/project-api/projects```
  - [GET] ```http://localhost:8002/project-api/projects/{project-id}```
  - [DELETE] ```localhost:8002/project-api/projects/{value of project id}```
  **Note**: cannot delete project aleardy assigned to employee or company
  - [PUT] ```http://localhost:8002/project-api/projects/{value of project id}```
  **Note**: cannot update project on archived employee
    
  ***payload:***
  ```
  {
    name: string value
    description: string value
    priority: string value
    state: string value
    employee-id: string value
    company-id: string value
  }
  ```
  - [GET] ```http://localhost:8002/project-api/projects/employees/id/{employee-id}```: list of projects assigned to a given unique employee
  - [GET] ```http://localhost:8002/project-api/projects/employees/lastname/{employee-lastname}```: list of projects assigned to given employees lastname
  - [GET] ```http://localhost:8002/project-api/projects/companies/id/{company-id}```: list of projects assigned to a given unque company
  - [GET] ```http://localhost:8002/project-api/projects/companies/name/{company-name}```: list of projects assigned to a given company name with all agencies
  - [GET] ```http://localhost:8002/project-api/projects/companies/agency/{company-agency}```: list of projects assigned to a given company's agency

   for [POST] and [PUT], ***employee-id, company-id***, project ms sends request to eployee and company ms to ask employee and company related to employee-id and company-id. a resilience is managed if theses ms are down
