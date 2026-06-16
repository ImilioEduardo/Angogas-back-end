-- Activa pgcrypto para geração de hashes BCrypt
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Corrige a password do admin padrão para: Admin@123
UPDATE users
SET password_hash = crypt('Admin@123', gen_salt('bf', 10))
WHERE email = 'admin@angogas.ao';
