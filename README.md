# How This Works!

- This `org.wso2.carbon.custom.jwt.handler` is a custom [gateway handler](https://docs.wso2.com/display/AM250/Writing+Custom+Handlers) for WSO2 API Manager 2.5.0.

- This `microService` is a simple micro service implementation to echo back the incoming request information(Headers/Body) and dumps the JWT claims if there any.

This repository contains a sample implementation for altering the JWT token issued by Key Manager(KM) (WSO2 IS-as-KM or Pre-Pack KM in APIM) in the Gateway node and injecting (Adding new) JWT claims which is/are extracted from the incoming API request HTTP headers.

## How to try!

1. Build `org.wso2.carbon.custom.jwt.handler` maven project
2. Put the jar file into <APIM_HOME>/repository/components/dropins/
3. Add the following handler element to API synapse file in `<APIM_HOME>/repository/deployment/server/synapse-configs/default/api/`
```xml
<handler class="org.wso2.carbon.custom.jwt.handler.AlterJWTHandler"/>
```
4. Start the WSO2 API Manager 2.5 server
5. Start the `microService` by running `./echo_microservice.py`
   * note: You need to have Python 3.5+
6. Try out the following `http` or `curl` command
```bash
http --verify=no "https://localhost:8243/sample/1.1.1/allep" "accept: application/json"  "Authorization: Bearer 7bf6fe85-b61f-30f2-85d7-de535785b96b" "x-myKey: gFqxSTuvRdIuhMr8pO57Vcz0OMAa"
```

7. `x-myKey` is the custom header given in the API request, and this header and it's value will be available in the JWT token receive to the Echo MicroService
8. Look for the JWT payload dump in the terminal

### Console log from Echo Micro Service
![echo_out](https://user-images.githubusercontent.com/3313885/45425477-1c9b2480-b6b7-11e8-90b6-ec186989634e.png)

### Response from Echo Micro Service
![echo_response](https://user-images.githubusercontent.com/3313885/45425478-1c9b2480-b6b7-11e8-8131-5b6047e018e6.png)

### WSO2 APIM Console log from AlterJWTHandler
![mediator_out](https://user-images.githubusercontent.com/3313885/45425479-1c9b2480-b6b7-11e8-907d-5496c621280a.png)


