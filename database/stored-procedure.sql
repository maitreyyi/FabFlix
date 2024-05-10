DROP PROCEDURE IF EXISTS add_movie;

DELIMITER //
CREATE PROCEDURE add_movie (IN movie_id varchar(10), IN title varchar(100), IN year int, IN director varchar(100), IN star_name varchar(100), IN genre varchar(100) )
BEGIN
	DECLARE star_id varchar(10);
	DECLARE genre_id int;
	DECLARE movie_title varchar(100);

    -- check if movie exists or not (if it doesn't exist, add into movies table)
SELECT title INTO movie_title FROM movies WHERE id = movie_id;
IF movie_title is NULL THEN
		INSERT INTO movies (id, title, year, director) VALUES (movie_id, title, year, director);
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
END //
DELIMITER ;