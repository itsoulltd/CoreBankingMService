####Run application:
    ~>$ mvn clean package -DskipTests
    ~>$ mvn spring-boot:run
####Setting up:
    Access swagger @ http://localhost/api/swagger-ui.html
    Access h2-console @ http://localhost/api/h2-console
    JDBC URL: jdbc:h2:~/testDB
    username: sa
    password: sa
    
####How to Test User-Stories:
    [Note: Usually I do Unit Testing at least for service layer,
     In this assignment most of the api's like LadgerBook or JSQLEditor alredy tested and running in production.]
    
    Story-1: Login As a User
    -> curl -X POST "http://localhost/api/auth/v1/login" 
            -H  "accept: */*" 
            -H  "Content-Type: application/json" 
            -d "{  \"password\": \"1234\",  \"role\": \"User\",  \"username\": \"giko\"}"
            
    Story-2: Create 2 User and Their Account
    -> curl -X POST "http://localhost/api/auth/v1/new/account" 
            -H  "accept: */*" 
            -H  "Authorization: Bearer eyJra......TbZrttBrIUwiw" 
            -H  "Content-Type: application/json" 
            -d "{  \"amount\": \"30000.00\",  \"currency\": \"BDT\",  \"email\": \"rajib@gmail.com\",  \"mobile\": \"01704161276\",  \"password\": \"1234\",  \"username\": \"rajib\"}"
            
    -> curl -X POST "http://localhost/api/auth/v1/new/account" 
                -H  "accept: */*" 
                -H  "Authorization: Bearer eyJra......TbZrttBrIUwiw" 
                -H  "Content-Type: application/json" 
                -d "{  \"amount\": \"10000.00\",  \"currency\": \"BDT\",  \"email\": \"giko@gmail.com\",  \"mobile\": \"01704161275\",  \"password\": \"1234\",  \"username\": \"giko\"}"
    
    Story-3: Check Balance            
    -> curl -X GET "http://localhost/api/account/v1/balance?prefix=CASH&username=giko" 
            -H  "accept: */*" 
            -H  "Authorization: Bearer eyJraWQiOiJmNWJiZXV........GM5MwA3gEWRVrTbZrttBrIUwiw"
            
    Story-4: Make some deposit
    -> curl -X POST "http://localhost/api/account/v1/make/deposit" 
            -H  "accept: */*" -H  "Authorization: Bearer eyJraWQiOiJmNWJiZXVXSXLHepGM5MwA3gEWRVrTbZrttBrIUwiw" 
            -H  "Content-Type: application/json" 
            -d "{  \"amount\": \"20000.00\",  \"currency\": \"BDT\",  \"username\": \"giko\"}"
            
    Story-5: Make a withdrawal
    -> curl -X POST "http://localhost/api/account/v1/make/withdrawal" 
            -H  "accept: */*" 
            -H  "Authorization: Bearer eyJraWQiOiJmNWJiZX......F5woGTvdCLHepGM5MwA3gEWRVrTbZrttBrIUwiw" 
            -H  "Content-Type: application/json" -d "{  \"amount\": \"3000.00\",  \"currency\": \"BDT\",  \"username\": \"giko\"}"
            
    Story-6: Make some transfer (Giko will send some money to rajib)
    -> curl -X POST "http://localhost/api/account/v1/make/transaction" 
            -H  "accept: */*" 
            -H  "Authorization: Bearer eyJraWQiOiJmNWJi..........EWRVrTbZrttBrIUwiw" 
            -H  "Content-Type: application/json" 
            -d "{  \"amount\": \"540.00\",  \"currency\": \"BDT\",  \"prefix\": \"CASH\",  \"to\": \"CASH@rajib\",  \"type\": \"transfer\",  \"username\": \"giko\"}"
            
    Story-7: Find last 10 Transaction History
    -> curl -X GET "http://localhost/api/account/v1/recent/transactions?prefix=CASH&username=giko" 
            -H  "accept: */*" 
            -H  "Authorization: Bearer eyJraWQiOiJ..........VrTbZrttBrIUwiw"
            
    Story-8: Find Paged Transaction By Date and Type
    -> curl -X POST "http://localhost/api/account/v1/search/transactions?prefix=CASH&username=giko" 
    -H  "accept: */*" 
    -H  "Authorization: Bearer eyJraWQiOiJmNWJiWRVrTbZrttBrIUwiw" 
    -H  "Content-Type: application/json" 
    -d "{  \"descriptors\": [{\"keys\": [],\"order\": \"DESC\"}]
         ,  \"page\": 0
         ,  \"properties\": [{\"key\": \"from\",\"logic\": \"AND\", \"nextKey\": null,\"operator\": \"EQUAL\",\"type\": \"STRING\",\"value\": \"2022-07-23\"}
                            ,{\"key\": \"type\",\"logic\": \"AND\", \"nextKey\": null,\"operator\": \"EQUAL\",\"type\": \"STRING\",\"value\": \"deposit\"}]
         ,  \"size\": 10}"
    
    
####Framework understanding:
    There are 4 opensource api has been user: all written and mantained by myself.
    1-> JSQLEditor: https://github.com/itsoulltd/JSQLEditor
    2-> LedgerBook: https://github.com/itsoulltd/WebComponentKit/tree/master/LedgerBook
    3-> WebComponentKit: https://github.com/itsoulltd/WebComponentKit/tree/master/HttpRestClient
    4-> JWTKit: https://github.com/itsoulltd/WebComponentKit/tree/master/JJWTWebToken

####Some points:
    Honestly speaking, I am always packed with quite convenssing number of production ready start-up code,
    ranging from back-end microservices to front-end applications.
    When ever I do some RnD on a specific technology, eventually I do wapped my work in a production ready startup code-base.
    Feel free to browse some of them:
    Spring-Boot Microservice Apps: (https://github.com/itsoulltd/WebAppStarterProjects)
    Web-Frontend Apps: (https://github.com/itsoulltd/WebUIAppStarterProjects)
    iOS-Startup App: (https://github.com/itsoulltd/StartUp-IOS)
    Android-Startup App: (https://github.com/itsoulltd/AndroidAppStarter)
    JUnit-SpringBoot Best Practice: (https://github.com/itsoulltd/SpringMServiceJpaUnitTest)
    
