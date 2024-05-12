## CS 122B Project 3 FabFlix

### Demo: 
1. Video URL: 
2. URL to access: 

### Instructions to deploy Fablix: 
1. Clone this repository using `git clone https://github.com/UCI-Chenli-teaching/cs122b-s24-team-cs.git'


### Group Member Contributions: 

#### Sharon:
1. Set up HTTPS for AWS machine
2. Set up reCAPTCHA for local and AWS machines
3. Added employees login page and adjusted redirection based on type of user
4. Added code for parsing and inserting XML file data

#### Maitreyi:

1. Created index page for employee dashboard and displayed meta data of all the tables in moviedb database
2. Added add-star page and add-movie page with message of success/duplicate after communication with servlet
3. Worked on stored-procedures for add-movie and add-star as well as the servlets
4. Helped debug issues we ran into when integrating all features together

### PreparedStatement Files:
1. AddMovieServlet
2. AddStarServlet
3. CartServlet
4. ConfirmationServlet
5. DashboardServlet
6. DomParser
7. EmployeeLoginServlet
8. GenreBrowseServlet
9. LoginServlet
10. MovieListServlet
11. PurchaseServlet
12. SingleMovieServlet
13. SingleStarServlet

### XML Optimizations
Details recorded based on local machine
1. Naive Approach
   - This approach took more than 45 minutes due to the tremendous amount of transactions and duplication checking only upon inserting
2. Batch insertion
   - This approach used code from BatchInsert.java example in order to reduce the number of transactions. This reduced the time by more than half, for a total of about 21 minutes
3. HashMaps
   - By creating a map of the already existing entries, the inserts were able to assume no duplicate inserts, thus reducing the amount of search queries required to locate duplicates. This, along with batch insert, reduce the time to about 1 minute.

### XML Inconsistency Report
13 duplicate movies 
- Movies with the same information

77 inconsistent movies 
- Movies with invalid ids or titles

17 invalid movie years 
- Movies with invalid years

240 movies not found 
- Movies referenced by cast that did not exist

340 duplicate stars 
- Stars with the same information

15294 stars not found in casts
- Stars referenced by casts that did not exist

Total parsed 12025 movies

Total parsed 6523 stars

Total parsed 8757 casts

Total parsed 9 genres

#### All inconsistencies can be found in inconsistency_report.txt
