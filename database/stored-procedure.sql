DELIMITER //
CREATE PROCEDURE add_movie (IN @movie_id varchar(10), IN @title varchar(100), IN @year int, IN @director varchar(100), IN @star_name varchar(100), IN @genre varchar(100) )

BEGIN
	DECLARE star_id;
	DECLARE genre_id;
	DECLARE movie_title;

    -- check if movie exists or not (if it doesn't exist, add into movies table)
SELECT title INTO movie_title FROM movies WHERE id = movie_id;
IF movie_title is NULL THEN
		INSERT INTO movies (id, title, year, director) VALUES (@movie_id, @title, @year, @director)
END IF;

    -- If the star doesn't exist, insert into the stars table
SELECT id INTO star_id FROM stars WHERE name = star_name;
IF star_id IS NULL THEN
        INSERT INTO stars (name, birthYear) VALUES (star_name, NULL);
        SET star_id = LAST_INSERT_ID();
INSERT INTO stars_in_movies(starId, movieId) VALUES(@star_id, @movie_d)
END IF;

	-- check if genre exists
SELECT id INTO genre_id FROM genres WHERE name = genre
    IF genre_id is NULL THEN
INSERT INTO genres_in_movies(genreId, movieId)
VALUES(@genre_id, @movie_id)
END IF;

END
DELIMITER //;