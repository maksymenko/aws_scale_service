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
docker push $DOCKER_USER/sample_api:dev
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
  * `Add Rule` for ssh port 22
  * Input load balance security `Group Name` in field `Source` to restrict traffic for balancer only

### Create key pair
Private public key pair is used to access AWS linux based instances. And created under `Services > EC2 > Key Pairs` and click "Create Key Pair". 

### Create EC2 instance
First lets create instance which will be be used to create image. Then this image is used to created other instances.
Navigate to `Services > EC2 > Instances` and click `Launch Instance`.
* Select server. For this example we are going to use `Ubuntu Server` and instabce type `t2.micro` which is available in trial account.
* Click `Configure Instance Details`
* Select availability zone as subnet aattribute (e.g. zone a)
* Left all other parameters with default values 
* Clicke "Next" here we left default paramaters.
* Click "Next" and add tag to mark this instance (e.g. 'sample-ami')
* Click "Next" and choose sequrity group created earlier.
* Click `Review and Launch`
* Selecte creates earlier `key pair` and `Launch` instance
* ssh to created instance
```
$ sudo ssh -i "./sample-key.pem" ubuntu@instance_public_dns
```
* Install Docker  by commanda below or follw instraction on `https://www.docker.com/`
```
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
sudo apt-get update
apt-cache policy docker-ce
sudo apt-get install -y docker-ce

```
* Login to your docker account
```
docker login
export DOCKER_CONFIG=/home/ubuntu/.docker/
```
* After that docker file with credentials will be created `~/.docker/config.json`.
Just left this file here so that all instances will have this file shen new instance created to scake uot application.

### Create AMI
Navigate to `Services > EC2 > Instances` select instance created above. 
* Click `Action` and select `Image>Create Image` 
* Give name of image (e.g.: `sample-image`)
* Click `Create Image`
* Go yo `AMI` section to see that image creation is in progress, it take some time for new AMI to show up.

### Create EC2 instance based on created AMI image
* Go to `AMI` section \
* Choose `AMI` created above 
* Click `Launch`
* Click `Configure Instance Details`
* Select availability zone as subnet aattribute (e.g. zone a)
* Left all other parameters with default values
* Go to `Advanced Details` section and specify bash command which will be executes when instance created.
Here we are going to specify docked image which will be launched om this instance
```
#!/bin/bash

su - ubuntu
docker run -d --rm -p 8080:8080 $DOCKER_USER/sample_api:dev
```
* Click `Next`
* Choose `Security Group` as `sample-web-tire`
* Click `Launch`
* Use  log file to debug script behavir `/var/log/cloud-init-output.log`

#### Check Api in browser
* Find public DNS of created instance and point browser to port 8080
`http://{instance_public_dns}:8080`




### Reources
* http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/user-data.html
* http://docs.aws.amazon.com/AmazonECS/latest/developerguide/private-auth.html
* https://docs.docker.com/engine/reference/commandline/login/#login-to-a-self-hosted-registry