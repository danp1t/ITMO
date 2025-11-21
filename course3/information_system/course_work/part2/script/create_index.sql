-- Для быстрого поиска по email при регистрации и входе
CREATE UNIQUE INDEX idx_account_email_lower ON "Account" (LOWER(email));

-- Для быстрого получения постов с сортировкой по дате
CREATE INDEX idx_post_created_at_desc ON "Post" (created_at DESC);

-- Для сортировки соревнований по дате начала
CREATE INDEX idx_tournament_start_date ON "Tournament" (start_date);
CREATE INDEX idx_tournament_start_date_desc ON "Tournament" (start_date DESC);

-- Для поиска товаров по названию
CREATE INDEX idx_product_name ON "Product" (name);
