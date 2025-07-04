-- Database setup for Fivetran Connector
-- Run this script to create the required tables

-- Create database (run separately if needed)
-- CREATE DATABASE connector_db;

-- Connect to the database
\c connector_db;

-- Drop existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS sync_log CASCADE;

-- Users table (parent table)
CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    website VARCHAR(100),
    last_synced TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Posts table (references users)
CREATE TABLE posts (
    id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    last_synced TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Comments table (references posts)
CREATE TABLE comments (
    id INTEGER PRIMARY KEY,
    post_id INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    body TEXT NOT NULL,
    last_synced TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

-- Simplified sync log table
CREATE TABLE sync_log (
    id SERIAL PRIMARY KEY,
    sync_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(10) NOT NULL, -- STARTED, SUCCESS, FAILED
    record_count INTEGER DEFAULT 0,
    error_message TEXT
);

-- Indexes for better performance
CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_comments_post_id ON comments(post_id);
CREATE INDEX idx_sync_log_time ON sync_log(sync_time);
CREATE INDEX idx_sync_log_status ON sync_log(status);

-- Sample queries to verify the schema
SELECT 'Tables created successfully' AS status;

-- Show table structure
\d+ users;
\d+ posts;
\d+ comments;
\d+ sync_log;
