CREATE TABLE "Account" (
    account_id SERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    salt          TEXT NOT NULL
);

CREATE TABLE "Post" (
    post_id    SERIAL PRIMARY KEY,
    owner_id   INTEGER REFERENCES "Account"(account_id) ON DELETE SET NULL,
    title      VARCHAR(255) NOT NULL,
    text       TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    count_like INTEGER DEFAULT 0 CHECK(count_like >= 0)
);

CREATE TABLE "Comment" (
    comment_id SERIAL PRIMARY KEY,
    post_id    INTEGER NOT NULL REFERENCES "Post"(post_id) ON DELETE CASCADE,
    account_id INTEGER REFERENCES "Account"(account_id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    user_comment TEXT
);

CREATE TABLE "TypeAttachment" (
    type_id     SERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT
);


CREATE TABLE "Attachment" (
    attachment_id SERIAL PRIMARY KEY,
    post_id INTEGER NOT NULL REFERENCES "Post"(post_id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    type_id INTEGER REFERENCES "TypeAttachment"(type_id) ON DELETE SET NULL,
    path TEXT NOT NULL
);

CREATE TABLE "Tag" (
    tag_id  SERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE "TagPost" (
    tag_post_id SERIAL PRIMARY KEY,
    tag_id      INTEGER NOT NULL REFERENCES "Tag"(tag_id) ON DELETE CASCADE,
    post_id     INTEGER NOT NULL REFERENCES "Post"(post_id) ON DELETE CASCADE,
    UNIQUE(tag_id, post_id)
);

CREATE TABLE "Rang" (
    rang_id     SERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE "Tournament" (
    tournament_id  SERIAL PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    rang_id        INTEGER REFERENCES "Rang"(rang_id) ON DELETE SET NULL,
    start_date     TIMESTAMP,
    finish_date    TIMESTAMP,
    address        VARCHAR(255) NOT NULL,
    link           VARCHAR(255)
);

CREATE TABLE "Role" (
    role_id     SERIAL  PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE "AccountRole" (
    account_role_id  SERIAL PRIMARY KEY,
    account_id       INTEGER NOT NULL REFERENCES "Account"(account_id) ON DELETE CASCADE,
    role_id          INTEGER NOT NULL REFERENCES "Role"(role_id) ON DELETE CASCADE,
    UNIQUE(account_id, role_id)
);


CREATE TABLE "OrderStatus" (
    status_id   SERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE "Order" (
    order_id        SERIAL PRIMARY KEY,
    account_id      INTEGER NOT NULL REFERENCES "Account"(account_id) ON DELETE CASCADE,
    address         VARCHAR(255) NOT NULL,
    status_id       INTEGER REFERENCES "OrderStatus"(status_id) ON DELETE SET NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    total_amount    INTEGER NOT NULL CHECK(total_amount > 0)
);

CREATE TABLE "Product" (
    product_id  SERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE "ProductInfo" (
    product_info_id   SERIAL PRIMARY KEY,
    product_id        INTEGER NOT NULL REFERENCES "Product"(product_id) ON DELETE CASCADE,
    size_name         VARCHAR(255) NOT NULL,
    count_items       INTEGER NOT NULL CHECK(count_items >= 0),
    UNIQUE(product_id, size_name)
);

CREATE TABLE "OrderProduct" (
    order_product_id    SERIAL PRIMARY KEY,
    order_id            INTEGER NOT NULL REFERENCES "Order"(order_id) ON DELETE CASCADE,
    product_id          INTEGER NOT NULL REFERENCES "Product"(product_id) ON DELETE CASCADE,
    UNIQUE(order_id, product_id)
);
