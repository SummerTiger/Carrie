-- Update admin password with correctly generated BCrypt hash
-- Password: admin123
-- This hash was generated and verified using Spring Security BCryptPasswordEncoder

UPDATE users
SET password = '$2a$10$hWUnEmk5ihW90hTD4/mgxuV5mg3N/7VVXlXZnGdYDqA7qrdnhzwg2'
WHERE username = 'admin';
