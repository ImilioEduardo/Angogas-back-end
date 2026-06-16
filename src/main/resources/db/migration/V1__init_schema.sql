-- ============================================================
-- AngoGÁS — Schema Inicial (Fase 1)
-- ============================================================

-- Utilizadores e autenticação
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome            VARCHAR(100)  NOT NULL,
    email           VARCHAR(150)  UNIQUE,
    telefone        VARCHAR(20)   UNIQUE,
    password_hash   VARCHAR(255)  NOT NULL,
    role            VARCHAR(20)   NOT NULL CHECK (role IN ('CLIENTE', 'ENTREGADOR', 'ADMIN')),
    activo          BOOLEAN       NOT NULL DEFAULT true,
    criado_em       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    actualizado_em  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT users_email_or_telefone CHECK (email IS NOT NULL OR telefone IS NOT NULL)
);

CREATE TABLE refresh_tokens (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    expira_em   TIMESTAMPTZ NOT NULL,
    revogado    BOOLEAN     NOT NULL DEFAULT false,
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Catálogo de produtos
CREATE TABLE products (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    nome        VARCHAR(100)    NOT NULL,
    descricao   TEXT,
    preco_kz    NUMERIC(12, 2)  NOT NULL CHECK (preco_kz > 0),
    peso_kg     NUMERIC(5, 2)   NOT NULL CHECK (peso_kg > 0),
    stock       INTEGER         NOT NULL DEFAULT 0 CHECK (stock >= 0),
    imagem_url  VARCHAR(500),
    activo      BOOLEAN         NOT NULL DEFAULT true,
    criado_em   TIMESTAMPTZ     NOT NULL DEFAULT now(),
    actualizado_em TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Endereços de entrega
CREATE TABLE addresses (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rua         VARCHAR(200),
    bairro      VARCHAR(100) NOT NULL,
    municipio   VARCHAR(100) NOT NULL,
    referencia  TEXT,
    latitude    NUMERIC(10, 8),
    longitude   NUMERIC(11, 8),
    predefinido BOOLEAN     NOT NULL DEFAULT false,
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Pedidos
CREATE TABLE orders (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id          UUID            NOT NULL REFERENCES users(id),
    entregador_id       UUID            REFERENCES users(id),
    address_id          UUID            NOT NULL REFERENCES addresses(id),
    status              VARCHAR(30)     NOT NULL DEFAULT 'PENDENTE'
                        CHECK (status IN ('PENDENTE','CONFIRMADO','A_PREPARAR','A_CAMINHO','ENTREGUE','CANCELADO')),
    metodo_pagamento    VARCHAR(30)     CHECK (metodo_pagamento IN ('MULTICAIXA','DINHEIRO')),
    total_kz            NUMERIC(12, 2)  NOT NULL CHECK (total_kz >= 0),
    notas               TEXT,
    criado_em           TIMESTAMPTZ     NOT NULL DEFAULT now(),
    actualizado_em      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- Itens do pedido
CREATE TABLE order_items (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID            NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id      UUID            NOT NULL REFERENCES products(id),
    quantidade      INTEGER         NOT NULL CHECK (quantidade > 0),
    preco_unitario  NUMERIC(12, 2)  NOT NULL CHECK (preco_unitario > 0)
);

-- ============================================================
-- Índices
-- ============================================================
CREATE INDEX idx_refresh_tokens_user_id  ON refresh_tokens(user_id);
CREATE INDEX idx_addresses_user_id       ON addresses(user_id);
CREATE INDEX idx_orders_cliente_id       ON orders(cliente_id);
CREATE INDEX idx_orders_entregador_id    ON orders(entregador_id);
CREATE INDEX idx_orders_status           ON orders(status);
CREATE INDEX idx_order_items_order_id    ON order_items(order_id);

-- ============================================================
-- Dados iniciais — admin padrão (password: Admin@123)
-- ============================================================
INSERT INTO users (nome, email, password_hash, role)
VALUES (
    'Administrador',
    'admin@angogas.ao',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN'
);
