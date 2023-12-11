# Application base microservices (suite2)
(NEW) in this project, we replace gateway Api **Spring-Cloud-Gateway** by **Kong Api Gateway**  
(NEW) we configure in the Kong Gateway: **routing**, **rate-limiting**, **authentication**, **logging**, **etc.**  
(NEW) we add Konga-dashboard which is kong ui to manage Kong objects

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

# utility services

## 1. configuration server
**microservices-config-service**: to centralize and distribute all microservices configurations into a git repository

## 2. kafka infrastructure
- a kafka infrastructure to publish and distribute events.
- each writing event in database (POST, DELETE, UPDATE) is distributed into kafka topics.
- **schema registry** to difine schema for all events and **avro** to serialiaze events sent to topics
- **kafdrop** is used as ui for managing and exploring kafka events, kafka servers,...
- kafka infrastructure:
  - **zookeeper**: to manage kafka brokers
  - **kafka-servers (3 brokers)**, **kafka-broker-1**, **kafka-broker-2**, **kafka-broker-3**: publish events into topics and disbribute events to consumers
  - **schema registry**: defines and register a common schema of the all events
  - **avro schema**: serializes kafka events
  - **kafdrop**: a kafka ui to manage kafka topics and events

## 3. kong-API-Gateway
- **kong-API-Gateway** is a unique entry point (proxy) to backend microservices. 
- in declarative mode, kong-API-gateway is deployed using docker and docker-compose file. the compose file is defined under **Kong-Gateway-DBLess-Docker** folder and **kong.yaml** file of all Kong objects is defined under **Kong-Gateway-Config-DBLess** folder: **routing**, **rate-limiting**, **authentication** (basic-auth, jwt), **logging** plugins. 
- using ui for managing kong objects, we deploy **konga-dashboard**: 
  - all configuratons created in declarative mode, are done using konga-dashboard
  - under **Kong-Gateway-Postgres-Konga-Docker** folder is docker-compose file for deploying kong infrastructure: **postgress db**, **kong-db-prepare**, **kong-api-gateway**, **konga-db-prepare**, **konga-dashboard**
- Under **Logs** folder is a logs file **logs-file.log** that logs all hppt request torwards backend microservices

## 4. databases
- **mysql db** docker image for peristing data from business microservices
- **postgreSQL db** docker image for persisting all kong configurations made with konga-dashboard

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
 
    
 # Docker images deploy
 All the services of the application are deployed into docker images: 
- kafka infrastructure: zookeeper, kafka-server, schema-regisrry, kafdrop-ui
- microservices config server: kong-microservices-config-service
- kong api infrastructure: postgreSQL, kong-db-prepare, kong-api-gateway, konga-db-prepare, konga-konga-dashboard
- business microservices:
  - k8s discovery service dependency for microservices registration and discovery
  - k8s-kafka-avro-kong-bs-ms-address
  - k8s-kafka-avro-kong-bs-ms-employee
  - k8s-kafka-avro-kong-bs-ms-company
  - k8s-kafka-avro-kong-bs-ms-project

# K8s docker container deploy

all the docker containers of the application are deployed into a **K8s minikube cluster**.
- the folder **Kubernetes-Container-Orch** contains k8s deployments of all containers of the application.
- in the first time, **kong-api-geteway** is deployed in declarative mode:**kong-api-gateway-declarative-mode.yaml**
- all k8s deployment (pods) are exposed by **k8s-services**
 
# summary (CI-DC)
![my-ci-cd-flow](https://github.com/placidenduwayo1/k8s-kafka-avro-kong-back/assets/124048212/dcf3f67e-4330-4563-91d5-947703e92ade)

    
# architecture kafka inside business microservice
- a model is a java bean that is sent as payload using a REST api, 
- a spring service build a kafka message with java model,
- a spring service uses kafka producer to send the kafka message to kafka topic,
- a spring service uses kafka consumer to subscribe to the kafka topic and consumes events that it sends to another spring service,
- the final spring service can handle received event as it wants, either to persist it in db or do anything with it.


# kafka infra summary
![kafka-infrastructure](https://github.com/placidenduwayo1/k8s-kafka-avro-aepc-back/assets/124048212/4cb3738e-718a-466c-9b59-41d4773a1a0b)

- a schema registry defines a schema for all events to publish into kafka topics,
- avro-schema uses that defined and registered schema to serialize avents,
- after events serialized, kafka producer send them into kafka topics.

# global architecture of the project
![k8s-kafka-avro-kong-clean-archi](https://github.com/placidenduwayo1/k8s-kafka-avro-kong-back/assets/124048212/4107499e-7894-4240-998c-b2c8e144751d)


- To access to backend business microservices, the client goes through a ***kong-api-gateway***

# Kong object creation using Konga-dashbboard

- to manager kong objects using konga-dashoboard, we connect to konga-dashboard: **http://localhost:8686**.
- we create connection to Kong Admin API: **http://kong-api-gateway:8001**.
- via the kong-dashboard we can configure: create services, routes, plugins, consumers, etc.

## expoded endpoints by microservices

After creating kong objects ing Kong-dashboard, we access to endepoints via kong api gateway

kong-api-gateway url: **http://192.168.49.2:30800/**

  ## address-microservice

list of **endpoints** exposed by k8s-kafka-avro-kong-bs-ms-address:  
  - [GET]```http://192.168.49.2:30800/address-api/```  
  - [POST]```http://192.168.49.2:30800/addresses```    
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
    - [GET]```http://192.168.49.2:30800/address-api/addresses```  
    - [GET]```http://192.168.49.2:30800/address-api/addresses/id/{value of address id}```  
    - [DELETE]```http://192.168.49.2:30800/address-api/addresses/id/{value of address id}```  
    **Note**: address cannot be removed when it is aleardy assigned  employee or company   
    - [PUT]```http://192.168.49.2:30800/address-api/addresses/id/{value of address id}```  
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
  - [GET] ```http://http://192.168.49.2:30800/company-api/```  
  - [POST]```http://192.168.49.2:30800/company-api/companies```  
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
  - [GET] ```http://192.168.49.2:30800/company-api/companies```  
  - [GET] ```http://192.168.49.2:30800/company-api/addresses/id/{address-id}```  
  - [GET] ```http://192.168.49.2:30800/company-api/addresses/city/{city}```  
  - [DELETE] ```http://192.168.49.2:30800/company-api/companies/id/{company-id}```   
  **Note**: cannot delete company holds project(s)  
  - [GET] ```http://192.168.49.2:30800/company-api/companies/name/{company-name}```
  - [GET] ```http://192.168.49.2:30800/company-api/companies/agency/{company-agency}```
  - [PUT] ```http://192.168.49.2:30800/company-api/companies/id/{company-id}```  
    ***payload***:
    ```
    {
      name: string value
      agency: int value
      type: string value
      address-id: string value
    }
    ```  
  **Note**: cannot update company on address holding another company  
## employee-microservice 
list of **endpoints** exposed by k8s-kafka-avro-kong-bs-ms-employee pod:  
  - [GET] ```http://192.168.49.2:30800/employee-api/```  
  - [POST]```http://192.168.49.2:30800/employee-api/employees```  
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
  - [GET] ```http://192.168.49.2:30800/employee-api/employees```  
  - [GET] ```http://192.168.49.2:30800/employee-api/employees/id/{employee-id}```  
  - [GET] ```http://192.168.49.2:30800/employee-api/employees/lastname/{lastname}```  
  - [DELETE] ```http://192.168.49.2:30800/employee-api/employees/id/{employee-id}```  
  **Note**: cannot delete employee already assigned project(s)  
  - [PUT] ```http://192.168.49.2:30800/employee-api/employees/id/{employee-id}```   
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
  - [GET] ```http://192.168.49.2:30800/employee-api/employees/addresses/id/{address-id}```: employees living at given address  
  - [GET] ```http://192.168.49.2:30800/employee-api/employees/addresses/city/{address-id}```: employees living at given address city  

**Note:** for [POST] and [PUT], ***address-id***, employee ms sends request to address ms to ask address related to address-id. a resilience is managed if address ms is down
## project-microservice
list of **endpoints** exposed by k8s-kafka-avro-kong-bs-ms-project pod:  
  - [GET] ```http://192.168.49.2:30800/project-api/``  
  - [POST]```http://192.168.49.2:30800/project-api/projects```    
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
  - [GET] ```http://192.168.49.2:30800/project-api/projects```  
  - [GET] ```http://192.168.49.2:30800/project-api/projects/{project-id}```  
  - [DELETE] ```192.168.49.2:30800/project-api/projects/{value of project id}```  
  **Note**: cannot delete project aleardy assigned to employee or company  
  - [PUT] ```http://192.168.49.2:30800/project-api/projects/{value of project id}```  
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
  - [GET] ```http://192.168.49.2:30800/project-api/projects/employees/id/{employee-id}```: list of projects assigned to a given unique employee  
  - [GET] ```http://192.168.49.2:30800/project-api/projects/employees/lastname/{employee-lastname}```: list of projects assigned to given employees lastname  
  - [GET] ```http://192.168.49.2:30800/project-api/projects/companies/id/{company-id}```: list of projects assigned to a given unque company  
  - [GET] ```http://192.168.49.2:30800/project-api/projects/companies/name/{company-name}```: list of projects assigned to a given company name with all agencies  
  - [GET] ```http://192.168.49.2:30800/project-api/projects/companies/agency/{company-agency}```: list of projects assigned to a given company's agency  

**Note:** for [POST] and [PUT], ***employee-id, company-id***, project ms sends request to eployee and company ms to ask employee and company related to employee-id and company-id. a resilience is managed if theses ms are down
