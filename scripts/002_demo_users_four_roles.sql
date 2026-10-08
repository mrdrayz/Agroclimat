-- =====================================================================
-- Демо-пользователи с одной ролью каждый (ADMIN / AGRONOMIST / OPERATOR / VIEWER).
-- Пароль у всех такой же, как у admin@agro.local (копируется password_hash).
-- Запускать в той же БД, где уже есть seed (роли в таблице role и админ).
--
-- Если admin@agro.local ещё нет — сначала один раз запустите приложение с DataInitializer.
-- =====================================================================

-- Список ролей и пользователей (для проверки)
-- SELECT id, code FROM role ORDER BY id;
-- SELECT id, email FROM app_user ORDER BY id;

INSERT INTO app_user (email, password_hash, full_name, status)
SELECT 'admin_demo@agro.local', u.password_hash, 'Тест администратор', 'ACTIVE'
FROM app_user u WHERE u.email = 'admin@agro.local'
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_user (email, password_hash, full_name, status)
SELECT 'agronomist_demo@agro.local', u.password_hash, 'Тест агроном', 'ACTIVE'
FROM app_user u WHERE u.email = 'admin@agro.local'
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_user (email, password_hash, full_name, status)
SELECT 'operator_demo@agro.local', u.password_hash, 'Тест оператор', 'ACTIVE'
FROM app_user u WHERE u.email = 'admin@agro.local'
ON CONFLICT (email) DO NOTHING;

INSERT INTO app_user (email, password_hash, full_name, status)
SELECT 'viewer_demo@agro.local', u.password_hash, 'Тест наблюдатель', 'ACTIVE'
FROM app_user u WHERE u.email = 'admin@agro.local'
ON CONFLICT (email) DO NOTHING;

-- Сброс связей user_role для этих email и назначение одной роли
DELETE FROM user_role ur
USING app_user au
WHERE ur.user_id = au.id
  AND au.email IN (
    'admin_demo@agro.local',
    'agronomist_demo@agro.local',
    'operator_demo@agro.local',
    'viewer_demo@agro.local'
  );

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM app_user u JOIN role r ON r.code = 'ADMIN'
WHERE u.email = 'admin_demo@agro.local';

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM app_user u JOIN role r ON r.code = 'AGRONOMIST'
WHERE u.email = 'agronomist_demo@agro.local';

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM app_user u JOIN role r ON r.code = 'OPERATOR'
WHERE u.email = 'operator_demo@agro.local';

INSERT INTO user_role (user_id, role_id)
SELECT u.id, r.id FROM app_user u JOIN role r ON r.code = 'VIEWER'
WHERE u.email = 'viewer_demo@agro.local';

-- Проверка
-- SELECT u.email, r.code
-- FROM app_user u
-- JOIN user_role ur ON ur.user_id = u.id
-- JOIN role r ON r.id = ur.role_id
-- WHERE u.email LIKE '%_demo@agro.local'
-- ORDER BY u.email;
