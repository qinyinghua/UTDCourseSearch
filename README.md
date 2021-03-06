# UTD Course Search
[2020] Sunny Guan

This project is to provide a consolidated view for UTD course search. It combines UTD course catalog (Coursebook), UTDGrades data, and RateMyProfessor ratings on the fly and return the search result. 

The production deployment is on Heroku platform.  Production URL: https://utdrmp.herokuapp.com/



## Project Setup

If you want to contribute on this project,  you might fork this repo and send in pull requests.  I will review the pull requests and merge them. 

The following project setup instruction is using Eclipse IDE as example.  

Step #1 - Import the maven project. 

Git clone the repo. Git pull the latest changes. In the Eclipse IDE, choose to import existing maven project to import the project.

Step #2 - Run and test the project.

In Eclipse Project Explorer, right click the project name -> select "Run As" -> "Maven Build...". 

In the goals, enter `spring-boot:run` then click Run button.



## Technology Stack

This project is a built on top of several open source libraries. That includes spring-boot, 

###### Spring Boot

https://spring.io/projects/spring-boot

> Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run". We take an opinionated view of the Spring platform and third-party libraries so you can get started with minimum fuss. Most Spring Boot applications need minimal Spring configuration.

###### Server-Sent Event - One Way Messaging

https://www.w3schools.com/html/html5_serversentevents.asp

> A server-sent event (SSE) is when a web page automatically gets updates from a server. This was also possible before, but the web page would have to ask if any updates were available. With server-sent events, the updates come automatically. 



## License

Free for personal or research use; for commercial use please contact me.
