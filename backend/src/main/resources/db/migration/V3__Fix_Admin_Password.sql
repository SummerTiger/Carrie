-- Fix admin user password
-- Password: admin123
-- BCrypt hash generated with Spring Security BCryptPasswordEncoder (strength 10)

UPDATE users
SET password = '$2a$10$dXJ3SW6G7P50lK85K7We.Q8OkuX4PW.9O.fjxMj7bCCmhpPJqfZ6'
WHERE username = 'admin';
