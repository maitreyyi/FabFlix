CREATE PROCEDURE add_movie @id varchar(10), @title varchar(100), @year int, @director varchar(100), @star_name varchar(100), @birthYear int, @genre varchar(100)
AS
BEGIN
INSERT INTO movies (id, title, year, director)
VALUES (@id, @title, @year, @director)

    INSERT INTO stars_in_movies(starId, movieId)
VALUES(@star_id, @movieId)

INSERT INTO stars(id, name, birthYear)
VALUES(@star_id, @star_name, @birthYear)
