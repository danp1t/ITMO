CREATE OR REPLACE FUNCTION register_user(
    p_email VARCHAR(255),
    p_password_hash TEXT,
    p_salt TEXT,
    p_name VARCHAR(255)
) RETURNS INTEGER AS $$
DECLARE
    v_account_id INTEGER;
BEGIN
    INSERT INTO "Account" (name, email, password_hash, salt)
    VALUES (p_name, p_email, p_password_hash, p_salt)
    RETURNING account_id INTO v_account_id;

    RETURN v_account_id;
EXCEPTION
    WHEN unique_violation THEN
        RAISE EXCEPTION 'Email уже занят';
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Ошибка регистрации: %', SQLERRM;
END;
$$ LANGUAGE plpgsql;




CREATE OR REPLACE FUNCTION authenticate_user(
    p_email VARCHAR(255),
    p_password_hash TEXT
) RETURNS TABLE(account_id INTEGER, name VARCHAR) AS $$
BEGIN
    RETURN QUERY
    SELECT a.account_id, a.name
    FROM "Account" a
    WHERE a.email = p_email AND a.password_hash = p_password_hash;
END;
$$ LANGUAGE plpgsql;




CREATE OR REPLACE FUNCTION change_password(
    p_account_id INTEGER,
    p_old_password_hash TEXT,
    p_new_password_hash TEXT,
    p_new_salt TEXT
) RETURNS BOOLEAN AS $$
DECLARE
    v_current_password_hash TEXT;
    v_account_name VARCHAR(255);
BEGIN
    SELECT password_hash, name INTO v_current_password_hash, v_account_name
    FROM "Account"
    WHERE account_id = p_account_id;

    IF v_current_password_hash IS NULL THEN
        RAISE EXCEPTION 'Пользователь с ID % не найден', p_account_id;
    END IF;

    IF v_current_password_hash != p_old_password_hash THEN
        RAISE EXCEPTION 'Неверный старый пароль';
    END IF;

    UPDATE "Account"
    SET password_hash = p_new_password_hash, salt = p_new_salt
    WHERE account_id = p_account_id;

    RAISE NOTICE 'Пароль для пользователя "%" успешно изменен', v_account_name;
    RETURN TRUE;

EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Ошибка при смене пароля: %', SQLERRM;
        RETURN FALSE;
END;
$$ LANGUAGE plpgsql;






CREATE OR REPLACE FUNCTION create_post(
    p_owner_id INTEGER,
    p_title VARCHAR(255),
    p_text TEXT,
    p_tags INTEGER[] DEFAULT NULL
) RETURNS INTEGER AS $$
DECLARE
    v_post_id INTEGER;
    v_tag_id INTEGER;
BEGIN
    INSERT INTO "Post" (owner_id, title, text)
    VALUES (p_owner_id, p_title, p_text)
    RETURNING post_id INTO v_post_id;

    IF p_tags IS NOT NULL THEN
        FOREACH v_tag_id IN ARRAY p_tags
        LOOP
            INSERT INTO "TagPost" (tag_id, post_id) VALUES (v_tag_id, v_post_id);
        END LOOP;
    END IF;

    RETURN v_post_id;
END;
$$ LANGUAGE plpgsql;






CREATE OR REPLACE FUNCTION add_comment(
    p_post_id INTEGER,
    p_account_id INTEGER,
    p_comment_text TEXT
) RETURNS INTEGER AS $$
DECLARE
    v_comment_id INTEGER;
BEGIN
    INSERT INTO "Comment" (post_id, account_id, user_comment)
    VALUES (p_post_id, p_account_id, p_comment_text)
    RETURNING comment_id INTO v_comment_id;

    RETURN v_comment_id;
EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Ошибка добавления комментария';
END;
$$ LANGUAGE plpgsql;
