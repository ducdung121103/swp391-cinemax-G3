-- ================================================================
-- SQL Server Script: Tạo Database và Bảng cho Module Movie Browsing
-- Dự án: Standalone Movie Browsing (Now Showing & Coming Soon)
-- ================================================================

-- Tạo database nếu chưa tồn tại
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'MovieBrowsingDB')
BEGIN
    CREATE DATABASE MovieBrowsingDB;
END
GO

USE MovieBrowsingDB;
GO

-- 1. Bảng genres (Thể loại phim)
IF OBJECT_ID(N'movie_genres', N'U') IS NOT NULL DROP TABLE movie_genres;
IF OBJECT_ID(N'movies', N'U') IS NOT NULL DROP TABLE movies;
IF OBJECT_ID(N'genres', N'U') IS NOT NULL DROP TABLE genres;
GO

CREATE TABLE genres (
    genre_id INT IDENTITY(1,1) PRIMARY KEY,
    genre_name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(255),
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT SYSDATETIME()
);
GO

-- 2. Bảng movies (Thông tin phim)
CREATE TABLE movies (
    movie_id INT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(200) NOT NULL,
    description NVARCHAR(MAX),
    duration INT NOT NULL, -- Thời lượng tính bằng phút
    release_date DATE NOT NULL, -- Ngày khởi chiếu
    end_date DATE, -- Ngày kết thúc chiếu (nếu có)
    rating DECIMAL(2, 1) DEFAULT 0.0 CHECK (rating >= 0 AND rating <= 5), -- Đánh giá sao (0.0 - 5.0)
    age_rating VARCHAR(10) DEFAULT 'P', -- P (Mọi lứa tuổi), K (Dưới 13 tuổi có bảo hộ), T13/PG-13, T16, T18/R
    director NVARCHAR(150),
    cast NVARCHAR(500),
    poster_url VARCHAR(500),
    trailer_url VARCHAR(500), -- Link youtube trailer hoặc embed URL
    is_active BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    updated_at DATETIME2 DEFAULT SYSDATETIME()
);
GO

-- Index tăng tốc tìm kiếm và phân loại phim
CREATE INDEX idx_movies_release_date ON movies (release_date);
CREATE INDEX idx_movies_end_date ON movies (end_date);
CREATE INDEX idx_movies_active ON movies (is_active);
CREATE INDEX idx_movies_rating ON movies (rating DESC);
GO

-- 3. Bảng movie_genres (Liên kết n-n giữa Phim và Thể loại)
CREATE TABLE movie_genres (
    movie_id INT NOT NULL,
    genre_id INT NOT NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT PK_movie_genres PRIMARY KEY (movie_id, genre_id),
    CONSTRAINT FK_mg_movie FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE,
    CONSTRAINT FK_mg_genre FOREIGN KEY (genre_id) REFERENCES genres(genre_id) ON DELETE CASCADE
);
GO

CREATE INDEX idx_mg_movie ON movie_genres(movie_id);
CREATE INDEX idx_mg_genre ON movie_genres(genre_id);
GO
