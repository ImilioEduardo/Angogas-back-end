-- ============================================================
-- AngoGÁS — Schema Fase 2: Entregadores, Tracking, Notificações
-- ============================================================

-- Zonas de entrega
CREATE TABLE zones (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nome        VARCHAR(100) NOT NULL,
    municipio   VARCHAR(100) NOT NULL,
    activa      BOOLEAN     NOT NULL DEFAULT true
);

-- Perfis de entregador
CREATE TABLE delivery_agents (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    veiculo         VARCHAR(50),
    matricula       VARCHAR(20),
    zone_id         UUID            REFERENCES zones(id),
    disponivel      BOOLEAN         NOT NULL DEFAULT false,
    avaliacao_media NUMERIC(3, 2)   NOT NULL DEFAULT 0,
    total_entregas  INTEGER         NOT NULL DEFAULT 0,
    criado_em       TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- Rastreio de localização em tempo real
CREATE TABLE order_tracking (
    id          BIGSERIAL   PRIMARY KEY,
    order_id    UUID        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    agent_id    UUID        NOT NULL REFERENCES users(id),
    latitude    NUMERIC(10, 8) NOT NULL,
    longitude   NUMERIC(11, 8) NOT NULL,
    registado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Notificações in-app
CREATE TABLE notifications (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    titulo      VARCHAR(100) NOT NULL,
    mensagem    TEXT        NOT NULL,
    tipo        VARCHAR(30),
    lida        BOOLEAN     NOT NULL DEFAULT false,
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Avaliações de entrega
CREATE TABLE reviews (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id    UUID        NOT NULL UNIQUE REFERENCES orders(id),
    cliente_id  UUID        NOT NULL REFERENCES users(id),
    agent_id    UUID        NOT NULL REFERENCES users(id),
    nota        SMALLINT    NOT NULL CHECK (nota BETWEEN 1 AND 5),
    comentario  TEXT,
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- Índices
-- ============================================================
CREATE INDEX idx_delivery_agents_user_id   ON delivery_agents(user_id);
CREATE INDEX idx_delivery_agents_zone_id   ON delivery_agents(zone_id);
CREATE INDEX idx_order_tracking_order_id   ON order_tracking(order_id);
CREATE INDEX idx_notifications_user_id     ON notifications(user_id);
CREATE INDEX idx_notifications_lida        ON notifications(user_id, lida);
CREATE INDEX idx_reviews_agent_id          ON reviews(agent_id);
