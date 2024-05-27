## CS 122B Project 3 FabFlix

### Demo: 
1. Video URL: https://www.youtube.com/watch?v=HW7fYep3yXQ
2. Master instance: http://54.193.76.82:8080/cs122b-team-cs/login.html
3. Slave instance: http://54.219.204.86:8080/cs122b-team-cs/
4. Balancer (AWS): http://54.193.174.184:80/cs122b-team-cs
5. Balancer (google cloud):  http://34.102.65.206/cs122b-team-cs/

### Instructions to deploy Fablix: 
1. Clone this repository using `git clone https://github.com/UCI-Chenli-teaching/cs122b-s24-team-cs.git'


### Group Member Contributions: 

#### Sharon:
1. Adjusted title search to use full-text search
2. Implemented Autocomplete with full-text and caching
3. Adjusted navbar for searching on each page

#### Maitreyi:
1. Implemented JDBC Connection Pooling
2. Worked on mysql master-slave replication on AWS instances
3. Created two balancers: AWS and Google Cloud and enabled load-balancing, connection Pooling, and sticky sessions.

### Connection Pooling
####Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
1. [AddMovieServlet.java](url)
2. [AddStarServlet](url)
3. [AutocompleteServlet](url)
4. [CartServlet](url)
5. [ConfirmationServlet](url)
6. [DashboardServlet](url)
7. [EmployeeLoginServlet](url)
8. [GenreBrowseServlet](url)
9. [LoginServlet](url)
10. [MovieListServlet](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-cs/blob/main/src/MovieListServlet.java)
#### Explain how Connection Pooling is utilized in the Fabflix code and how it works with two backend servers
By using connection pooling, we are able to efficiently manage connections using two backend servers. This is done by splitting requests between the two backend servers and reusing connections rather than creating new ones for each query which optimizes resource usage. With our two backend servers: Master, Slave, we are able to reduce the load on the master server to improve performance and scale our performance. In our implementation, the master instance is the only instance that handles write requests, and it shares read requests with slave (which is assigned randomly to either server).
