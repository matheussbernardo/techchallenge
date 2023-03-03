# TECH CHALLENGE

## How to run project?
Publish app image locally
$ sbt Docker / publishLocal

Start App and Postgres
$ docker compose up 

## Architecture Decisions
I decided to simplify a lot given the time constraint.
I created a User and Post package to separate the two concerns of the project.

### Saving Images
The current implementation is not very scalable regarding images.
I decided to save the images in plain text encoded as base64.
But the code is reusable enough where in case it is needed it is possible to change
the implementation to save images somewhere else (AWS S3, etc...).
I assumed that the image is going to be gzipped by the client and then I decompress the image
and convert to a base64 String.

### HTTP API
Regarding, the HTTP API it has the capability to 
 - Create Users
 - Create Posts
 - Add Image to Posts
 - Get all Posts

I think the design decision of separating the Image Upload from the Post Creation makes a lot of
sense, I expect the client to first create a Post and this endpoint returns an UUID that later it
can be used to add an Image to the Post. 


### Notifying Users
I did not had the time to implement the notifying users.
I have experience in asynchronous systems and would approach this problem by 
adding some mechanism of asynchronicity, it can be through Kafka, Message Queues or even 
Postgres. It is possible to use webhooks to notify users for example.

## Database
I did a very simple 1 : N relation from User to Posts, using a FK on the Post table.
Regarding the code I decided not to add libraries for handling migrations jus to make things simpler.
