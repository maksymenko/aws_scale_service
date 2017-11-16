## Dockered Spring Boot rest api server. 

### Build server
```
 gradle build
```

### Build docker image
```
docker build -t  sample_api_img . 
```

### Run docker container
```
docker run --rm -p 8080:8080 sample_api_img
```

### Push to docker-hub
```
docker login
docker tag sample_api_img $DOCKER_USER/sample_api:dev
docker push $DOCKER_USER/sample_api
```

### Run docker from docker-hub
```
docker run --rm -p 8080:8080 $DOCKER_USER/sample_api:dev
```