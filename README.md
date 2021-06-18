# Parking Prices #

## How To Build/Run ##

The app uses Gradle as dependency management, which also facilitates the build, test, run and even deployment of the system. 
There is no need to install Gradle as we are using the Gradle Wrapper. The only requirement to run this app is JDK 11.
For more information on the Gradle Wrapper (gradlew) see: https://docs.gradle.org/current/userguide/gradle_wrapper.html

To build the app, while also running all the unit tests, use `./gradlew clean build`. 
To run, use `./gradlew bootRun` after building. 

Spring runs in an embedded Tomcat server. 
The system will be available on `locahost:5000`.

If you wish to run in a docker container, you can create an image using `./gradlew docker`. 
The `Dockerfile` provided is just a basic example which can be improved for production environments.

Be mindful to update the timezone in the `Dockerfile` and `application.properties`/`parking.timezone` to your current one.


## Documentation And Metrics ##

The system is annotated to generate swagger docs automatically. The resulting json is saved to the folder `/docs/swagger.json`, but you can also access a webpage version on `localhost:5000/api/swagger`.

Likewise, the system includes the following metrics endpoints: `localhost:5000/health`, `localhost:5000/info` and `localhost:5000/metrics`. 

The last one gives a list of acessible performance metrics. Each can be accessed by calling an endpoit with the same name, like so: `localhost:5000/metrics/http.server.requests`.

The docs folder also includes a postman collection with a simple example for each of the available endpoints, to make testing easier.


## Database And Cache ##

The system was coded using an in-memory DB (H2) to make it more portable for the examiners, since they won't need to initialize an outside DB.
It is possible to access a console for the H2 DB using `http://localhost:5000/h2-console` with the following credentials:

```
jdbc.url = jdbc:h2:mem:parking
username = sa
password = password
```

The cache also uses an embedded Redis that is started along with the app. 
If this is to be a real app to be run on a server, we can easily switch the DB and cache to any other external one just using the `application.properties` file.

WARNING: Unfortunately, the embedded Redis is sometimes not closed after the app is stopped, which may cause an error `Could not start redis server, port is in use or server already started.` the next time the app is run, as the redis server is already up. 
This is a problem specifically with the embedded Redis server library and would not affect the app if it was a real external Redis server. 
It also does not affect the functionality of the app.


## Available Endpoints ##

The best way to document the app is using swagger, which can be access as seem above. Regardless, below is a brief description of the available endpoints:

 - GET(localhost:5000/api/rates) -> This endpoint has not inputs and returns the json of the current rates available on the DB in the same format as they were inputted. 
 
 - PUT(localhost:5000/api/rates) -> This endpoint takes as input body the json of the rates that will overwrite the ones currently on the DB (and cache). It returns the same as output for auditing.
 
 - GET(localhost:5000/api/prices) -> This endpoint requires two dates, start and end, on the format `2011-12-03T10:15:30+01:00`. Even though query parameters don't normally accept the `+` sign (which means a space in the URL), this endpoint will accept either a `+` (which will be converted to a space), an actual space (one character space) in the same place as the `+` sign would be, or the correct escaped sign of `%2b`. It will return as output the price with the format `"{"price": 1000}"` or a literal string `"unavailable"` if no rates were found.


## Design And Assumptions ##

Per the requirements, when the system starts a json file located in `src\main\resources\data\rates.json` is loaded to both the DB and cache as the default rates.

The requirements also mention that both the rates and the requests for prices can be in any timezone, which will require that we test the requested dates against each rate available.
Since we can't be sure that the requested dates, when changed to another timezone, will continue to be the same day of the week we are not even able to filter the rates by the day of the week first, before comparing the times.

For example, we have a rate such that:
 - Timezone = UTC+9
 - Time = 1am-6am
 - Days = Wednesday
 - Price = 1000
 
And we get a request asking for prices for next Wednesday at 2pm-6pm on UTC-4. 

If we simply search for any rates that are available for a Wednesday, then the above rate would be returned. However, even if the available rate is for a Wednesday, it is for another timezone. Therefore we must first change the timezone of the requested date to match the timezone of the rate, which would become: `Wednesday 2pm UTC-4 -> Thursday 3am UTC+9`, making the Wednesday rate invalid for the requested date. It can even be that when changing the timezone of a request, the start date is in one day, but the end is on the next, which automatically means the price is unavailable.

Because of this complex calculation I have decided to keep the rates in a redis cache, since every request for prices would basically need to grab everything from the DB. This will improve the performance by eliminating a trip to the DB. 

Every 30 minutes, a Spring Quartz scheduled task will run to reset the cache using data from the DB, which should prevent any possible caching inconsistencies. Quartz was used in this case to make sure the cache reset is only run once per cluster for every cron trigger, considering that this system will probably run in a multi-server environment.

Overall, the system should be able to handle multiple requests at once, but if the performance is insuficient it can be easily packaged into a docker and run on a cloud server like AWS EC2 or Fargate.

