CREATE OR REPLACE FUNCTION check_tournament_dates()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.finish_date < NEW.start_date THEN
        RAISE EXCEPTION 'Дата завершения не может быть меньше даты начала соревнований';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_tournament_dates
    BEFORE INSERT OR UPDATE ON "Tournament"
    FOR EACH ROW EXECUTE FUNCTION check_tournament_dates();

CREATE OR REPLACE FUNCTION update_product_count()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE "ProductInfo"
    SET count_items = count_items - 1
    WHERE product_id = NEW.product_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_product_count
    AFTER INSERT ON "OrderProduct"
    FOR EACH ROW EXECUTE FUNCTION update_product_count();

CREATE OR REPLACE FUNCTION check_product_availability()
RETURNS TRIGGER AS $$
DECLARE
    available_count INTEGER;
BEGIN
    SELECT count_items INTO available_count
    FROM "ProductInfo"
    WHERE product_id = NEW.product_id;

    IF available_count <= 0 THEN
        RAISE EXCEPTION 'Товар не найден';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_product_availability
    BEFORE INSERT ON "OrderProduct"
    FOR EACH ROW EXECUTE FUNCTION check_product_availability();
