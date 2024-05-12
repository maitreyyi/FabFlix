DROP PROCEDURE IF EXISTS add_movie;

DELIMITER //
CREATE PROCEDURE add_movie (IN m_title varchar(100), IN m_year int, IN m_director varchar(100), IN star_name varchar(100), IN genre varchar(100) )
BEGIN
	DECLARE movie_id varchar(10);
	DECLARE star_id varchar(10);
	DECLARE genre_id int;
	DECLARE movie_title varchar(100);
    
    -- check if movie exists or not (if it doesn't exist, add into movies table)
    SELECT id INTO movie_id FROM movies WHERE title = m_title AND year = m_year AND director = m_director;
    IF movie_id is NULL THEN
		SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1 INTO movie_id FROM movies; 
        IF movie_id IS NULL THEN
            SET movie_id = 'tt0000001'; 
        ELSE
            SET movie_id = CONCAT('tt', LPAD(movie_id, 7, '0')); 
        END IF;
		INSERT INTO movies (id, title, year, director) VALUES (movie_id, m_title, m_year, m_director);
	END IF;
    
    -- If the star doesn't exist, insert into the stars table
	SELECT id INTO star_id FROM stars WHERE name = star_name;
    IF star_id IS NULL THEN
		SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1 INTO star_id FROM stars; 
        IF star_id IS NULL THEN
            SET star_id = 'nm0000001'; 
        ELSE
            SET star_id = CONCAT('nm', LPAD(star_id, 7, '0')); 
        END IF;
        INSERT INTO stars (id, name, birthYear) VALUES (star_id, star_name, NULL);
    END IF;
    -- once star is created/found, insert into stars_in_movies
	INSERT INTO stars_in_movies(starId, movieId) VALUES(star_id, movie_id);
    
	-- check if genre exists
	SELECT id INTO genre_id FROM genres WHERE name = genre;
    IF genre_id is NULL THEN
		INSERT INTO genres (name) VALUES (genre);
		SET genre_id = LAST_INSERT_ID();
	END IF;
	INSERT INTO genres_in_movies(genreId, movieId) VALUES(genre_id, movie_id);
    
    -- ratings insert
    INSERT IGNORE INTO ratings(movieId, rating, numVotes) VALUES(movie_id, 0, 0);
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS add_star;

DELIMITER //
CREATE PROCEDURE add_star( IN star_name varchar(100), IN birth_year int)
BEGIN
	DECLARE star_id varchar(10);
SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) + 1 INTO star_id FROM stars;

IF star_id IS NULL THEN
		SET star_id = 'nm0000001';
ELSE
		SET star_id = CONCAT('nm', LPAD(star_id, 7, '0'));
END IF;
INSERT INTO stars (id, name, birthYear) VALUES (star_id, star_name, birth_year);
END //
DELIMITER ;

