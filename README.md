Things to send in
-----
- Your code (including the dockerfile), either as a link to a repository or as a zip-file
- A short description of your solution and why you decided to build it like this
- A couple of sentences about how you would handle additional non-functional requirements such as:
    - High load/traffic
    - Authentication & Security


# Simple blog API

Run standalone: `./mvnw spring-boot:run`

Build docker image: `./mvnw spring-boot:build-image`

Run w. docker: `docker run -d -p 8080:8080 blog:0.0.1-SNAPSHOT`

# Solution description
This solution is the most effective way I could think of to get a working simple PoC blog CRUD API in working condition within the time alloted (~ 4 hours), taking into account that I had zero knowledge of the java/kotlin ecosystem at the start.

* Spring boot initializr: Get a working foundation w.o any knowledge of the Java ecosystem
* Read up on tutorials describing how to use spring boot to set up simple APIs
* Build iteratively from the simplest possible app (that did nothing), to having a RestController that replied w. static content, to a hibernate solution with a H2 DB
* Used spring boot and hibernate as much as possible to ease the burden of having to learn everything from scratch. With the simple use case at hand, I could mostly just use these libraries as lego bricks to build my application.
* Skip making my own Dockerfile, and use the spring boot built-in `build-image` goal. Being short on time, I'm pretty confident this will do a better job setting up a good docker image than me doing it by hand and figuring out all the details around how to build java docker applications.

Most of the time went into getting a dev env working with VS Code, and troubleshooting infinite recursion problems when serializing the Entities w. reference cycles

# Future improvements
* Really understand what makes the application tick. Right now I've put together big chunks of library black boxes that do "magic" things, and it "just works". In order to improve on this I need to read up a lot more on for example Spring Boot and Hibernate. Also get this reviewed by someone with experience building java/kotlin application.
* Toss the in-memory DB. Very quick to get going with, and great for running tests against, but our data needs to be durable in production.
* Better control over the serialized output. Right now we just return the Entities with defaults for serialization behaviour except for the reference cycle break.
* As we start to actually get some business logic in the app, refactor so we start isolating that logic from the controller/framework code. Both for ease of unit testing, and ease of re-use for other purposes (maybe we want to build some admin component, or a CLI app?)
* Tests. I haven't had time to write any tests, so I'm not sure this application actually works as intended right now. Since there is basically zero business logic, unit tests are not very useful at the moment. Would go for some end-to-end tests with a test client as long as it just says a pure CRUD app, and start unit testing when actual logic starts showing up. Also should do some basic performance tests to make sure I haven't made any grave mistakes when setting up the ORM stuff.
* Set up CI/CD for building, testing and deploying the app.

# Non-functional requirements
## Scaling traffic
For scaling, a simple solution would be to put a load balancer in front (on-prem maybe HAproxy?, AWS an ALB for example), and scale horizontally by setting up a cluster in ECS for AWS or a kubernetes cluster in AWS or GCP. Also, looking into using a CDN like Cloudfront or Cloud CDN could be a good way to reduce load on the API itself. This would be easiest for content that never changes, like if we did not allow updating our blog posts, otherwise we need to think about cache invalidation strategies
This assumes that the API service is stateless, so first we need to move the DB out of the application itself.

## Authentication & Security
For authentication we could set up an Oauth middleware in our app, together with setting up a few scopes so we can have greater granularity in what users can do.
To keep this API service "microservice-y", I would build a separate authentication application that handles the token creation and has the user DB with permissions etc, and gives out JWTs. These JWTs contain everything our API app needs to authenticate a user and check whether they're authorized to perform a specific action without having to make network calls to ask the auth server on every request, or having to keep a cache of user info.