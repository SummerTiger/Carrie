-- Update admin password with verified BCrypt hash
-- Password: admin123
-- Generated using: new BCryptPasswordEncoder().encode("admin123")

UPDATE users
SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi'
WHERE username = 'admin';
