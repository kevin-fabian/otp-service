--- execute using hpa account
DROP DATABASE IF EXISTS otp_db;
DROP SCHEMA IF EXISTS otp_user;
DROP USER IF EXISTS otp_user;
DROP USER IF EXISTS otp_apps;
CREATE USER otp_user WITH PASSWORD 'otp__user' CREATEDB;
CREATE USER otp_apps WITH PASSWORD 'otp_apps';
CREATE DATABASE otp_db WITH OWNER = otp_user;

--- execute using otp_user
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA otp_user AUTHORIZATION otp_user;
ALTER USER otp_user SET search_path to 'otp_user';
GRANT ALL ON SCHEMA "otp_user" TO otp_user;
GRANT ALL ON SCHEMA "otp_user" TO otp_apps;
