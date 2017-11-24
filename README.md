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
Here we will configure AWS to scale out server automaticaly when load is increassed and scale in when load in descresed.
In order to achive it we will create instance image which will be used and base image to setup new instances. 
Then  setup loadbalancer to balance traffic between all instances.

### Create security groups `services>EC2>Security Group`
Create two groups one to controll **load balancer** traffic and another to controll web tire traffic.
* Create group for **Load Balancer**
  * Click `Create Security Group` and set name for load balancer group
  * `Add Rule` for inbound traffic
  * `Add Rule` for **outbound** traffic
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
* Select creates earlier `key pair` and `Launch` instance
* Open instance and give name
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
```
After that docker file with credentials will be stored in `~/.docker/config.json`.


#### Add application start commands into  startup script
```
sudo vim /etc/rc.local
```
* Add  run commands  just befor `exit 0`
```
su - ubuntu
export DOCKER_CONFIG=/home/ubuntu/.docker/
export SAMPLE_APP_ENV=dev
export DOCKER_CONFIG=/home/ubuntu/.docker/
docker pull maksymenko/sample_api:$SAMPLE_APP_ENV
docker run -d --rm -p 8080:8080 $DOCKER_USER/sample_api:$SAMPLE_APP_ENV
```


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
Here we are going to specify custom bash commands tspecific for instance.
```
#!/bin/bash

```
* Click `Next`
* Choose `Security Group` as `sample-web-tire`
* Click `Launch`
* Use  log file to debug script behavir 
  * `/var/log/cloud-init-output.log`
  * `/var/log/syslog`

### Check Api in browser
* Find public DNS of created instance and point browser to port 8080
`http://{instance_public_dns}:8080`


### Setup Load balancer
* Navigate to `Services > EC2 > Load Balancers` and click `Create Load Balancer`
* Choose `Application Load Balancer` and click `Create`
* Give name (e.g. sample-app-loadbalancer)
* Set port 8080 for http as our application uses port 8080
* Add `https` is recommended for production usage (here we can skip it)
* Select availability zones, lets choose at least to zones `a` and zone `b` for system reliability.
* Define tags to mark balancer.
* Click `Next` and agail `Next` to choose `Security Group` created above.
* Selected existing security group created to controll loadbalancer traffic.
* Click `Next` to configure routing and set port 8080
* Create `new target group` to which load balancer will send traffic.
* Give name to group and click `Next` to `register targets` instances
* Select instances and `add to registered` on port 8080.
* Click `Next` to review and `Create`


### Setup Autoscaling
In order to setup autoscaling we have to define two components "Launch Configuration" and "Autoscaling Group".

#### Launch Configurations 

* Open  section `Services > EC2 > AUTO SCALING > Launch Configurations` and click `Create Auto Scaling group` then `Create launch configuration`.
* Choose base image. As we created earlier our preconfigured instance, click "My AMIs" and select image.
* Select instance type and click "Next".
* Give name to launch configuration e.g. "Sample web-app launch configuration".
* Click "Next" then again "Next"
* Select existing security group (created above for web application) and click `Review`.
* Click `Create launch configuration` and choose key for ssh access.

#### Create Auto Scaling Group
* Give name for group
* Select group size (put 2 to have one instance in two different availability zones)
* Choose Subnet (availability zones) let choos `a` and `b` zones which were selected for load balancer.
* Go to `Advanced Details` and select that group will select traffic from one or more load balancer.
* Then select name of target group created for load balancer, and click `Next`
* Define scaling policy (when and how to scale application), and click `Next`
* Configure notification as needed and click `Next`
* Add tegs to ad some metadate to created resource, and click `Review`.
* Click `Create Autoscaling Group` for complete process and `Close` to come back to autoscaling home page.

#### Removed unused instances
As now we have aouto scaling group we can remove instances created earlier.

TODO: complete aoute scaling configuration step




### Reources
* Load balancer Security group http://docs.aws.amazon.com/elasticloadbalancing/latest/classic/elb-security-groups.html#elb-vpc-security-groups
* health check troubleshoot http://docs.aws.amazon.com/elasticloadbalancing/latest/classic/ts-elb-healthcheck.html#ts-elb-healthcheck-failed-vpc
* http://docs.aws.amazon.com/AmazonECS/latest/developerguide/create-application-load-balancer.html#alb-configure-routing 
* https://aws.amazon.com/premiumsupport/knowledge-center/troubleshoot-unhealthy-checks-ecs/
* https://docs.oseems.com/general/operatingsystem/linux/automatically-run-program-on-startup
* http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/user-data.html
* http://docs.aws.amazon.com/AmazonECS/latest/developerguide/private-auth.html
* https://docs.docker.com/engine/reference/commandline/login/#login-to-a-self-hosted-registry