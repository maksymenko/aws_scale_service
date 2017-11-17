# Guide to setup AWS auto scaleble server based on Dockered Spring Boot REST API server. 

> ## In progress

## Create application

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

## AWS setup 
In order to address different load we want our solution be scalable. Server has to scale out when load is being increased. And scale in when load decreased and no need to pay for unused respurces.

### Create security group `services>EC2>Security Group`
Create two groups one to controll loab balancer traffic and another to controll web tire traffic.
* Create group for **Load Balancer**
  * Click `Create Security Group` and set name for load balancer group
  * `Add Rule` for inbound traffic
  * Click `Create`.
* Create group for **Web Tire**
  * Click Click `Create Security Group` and set name for web-tire 
  * `Add Rule` for inbound traffic (http)
  * Input load balance security `Group Name` in field `Source` to restrict traffic for balancer only

### Create key pair
Private public key pair is used to access AWS linux based instances. And created under `Services > EC2 > Key Pairs` and click "Create Key Pair". 

### Create EC2 instance
Navigate to `Services > EC2 > Instances` and click `Launch Instance`. Select server. Here we will use `Ubuntu Server` and instabce type `t2.micko`.
* Click `Configure Instance Details`
* Select explicitely availablility zone (e.g. zone a)
* Define starter bash script in `Advanced Details` section.

start section
```
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
sudo apt-get update
apt-cache policy docker-ce
sudo apt-get install -y docker-ce
//sudo systemctl status docker


```



### Reources
* http://docs.aws.amazon.com/AmazonECS/latest/developerguide/private-auth.html
* https://docs.docker.com/engine/reference/commandline/login/#login-to-a-self-hosted-registry