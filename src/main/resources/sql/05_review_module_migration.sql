-- ============================================================================
-- SCRIPT 05: REVIEW MODULE MIGRATION
-- Add/adjust minimal structures for customer review + admin moderation.
-- ============================================================================

USE cinema_chain_db;
GO

IF OBJECT_ID(N'reviews', N'U') IS NULL
BEGIN
    CREATE TABLE reviews (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        movie_id INT NOT NULL,
        user_id INT NOT NULL,
        rating_score INT NOT NULL,
        comment NVARCHAR(MAX) NOT NULL,
        status NVARCHAR(30) NOT NULL DEFAULT 'PENDING',
        is_deleted BIT NOT NULL DEFAULT 0,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT fk_reviews_movie FOREIGN KEY (movie_id) REFERENCES movies (id) ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT ck_reviews_rating_score CHECK (rating_score BETWEEN 1 AND 5),
        CONSTRAINT uk_reviews_user_movie UNIQUE (user_id, movie_id)
    );

    CREATE INDEX idx_reviews_movie_status ON reviews (movie_id, status);
END
GO

-- Normalize status values and ensure default status is PENDING.
UPDATE reviews
SET status = 'HIDDEN'
WHERE status IN ('REJECTED', 'FLAGGED');
GO

UPDATE reviews
SET status = 'PENDING'
WHERE status IS NULL;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.default_constraints dc
    JOIN sys.columns c ON c.default_object_id = dc.object_id
    WHERE dc.parent_object_id = OBJECT_ID(N'reviews')
      AND c.name = 'status'
)
BEGIN
    ALTER TABLE reviews ADD CONSTRAINT df_reviews_status DEFAULT 'PENDING' FOR status;
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.check_constraints
    WHERE name = 'ck_reviews_rating_score'
      AND parent_object_id = OBJECT_ID(N'reviews')
)
BEGIN
    ALTER TABLE reviews WITH NOCHECK
    ADD CONSTRAINT ck_reviews_rating_score CHECK (rating_score BETWEEN 1 AND 5);
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.key_constraints
    WHERE [type] = 'UQ'
      AND [name] = 'uk_reviews_user_movie'
      AND parent_object_id = OBJECT_ID(N'reviews')
)
BEGIN
    ALTER TABLE reviews
    ADD CONSTRAINT uk_reviews_user_movie UNIQUE (user_id, movie_id);
END
GO

