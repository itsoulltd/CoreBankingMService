####Run application:
    ~>$ mvn clean package -DskipTests
    ~>$ mvn spring-boot:run
####Setting up:
    Access swagger @ http://localhost/api/swagger-ui.html
    Access h2-console @ http://localhost/api/h2-console
    JDBC URL: jdbc:h2:~/testDB
    username: sa
    password: sa
    
####Framework understanding:
    There are 4 opensource api has been user: all written and mantained by myself.
    1-> JSQLEditor: https://github.com/itsoulltd/JSQLEditor
    2-> LedgerBook: https://github.com/itsoulltd/WebComponentKit/tree/master/LedgerBook
    3-> WebComponentKit: https://github.com/itsoulltd/WebComponentKit/tree/master/HttpRestClient
    4-> JWTKit: https://github.com/itsoulltd/WebComponentKit/tree/master/JJWTWebToken

####As a developer I have so much ready set go startup project for quick spin up a project.