## CS 122B Project 2 FabFlix

### Demo: 
1. Video URL: https://youtu.be/0bP6hQm2EJU
2. URL to access: http://54.153.65.32:8080/cs122b-team-cs/

### Instructions to deploy Fablix: 
1. Clone this repository using `git clone https://github.com/UCI-Chenli-teaching/cs122b-s24-team-cs.git'


### Group Member Contributions: 

#### Sharon:
1. Created login page that redirects to a main page
2. Updated ordering of genres and stars on single movie pages
3. Added browsing by genre and starting characters
4. Added code for sorting and pagination of results
5. Worked on page for processing payment and confirmation to add sales record to database
6. Made efforts to improve beautify pages

#### Maitreyi:

1. Implemented search function and redirected to movie-list with specified parameters
2. Worked on navigation bar and added into all pages
3. Created shopping cart page that allows user to modify quantity, see the total price and proceed to checkout
4. Added code for cart servlet that handled get/post requests
5. Worked on jump functionality among pages
6. Added add to cart options in movie-list and single movie pages with a notification upon success
7. Helped debug issues we ran into when integrating all features together

### Substring Matching:
1. Starting characters -- LIKE "?%"
   * Starts with a particular character
2. All search options -- LIKE "%?%"
   * Title, year, director, star name
   * Exists anywhere in the field
