-- ============================================================
-- AngoGÁS — Schema Fase 2: Entregador, Rastreio, Notificações, Avaliações
-- ============================================================

CREATE TABLE zones (
    id        UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nome      VARCHAR(100) NOT NULL,
    municipio VARCHAR(100) NOT NULL,
    activa    BOOLEAN     NOT NULL DEFAULT true
);

CREATE TABLE delivery_agents (
    id              UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID           NOT NULL UNIQUE REFERENCES users(id),
    veiculo         VARCHAR(50),
    matricula       VARCHAR(20),
    zone_id         UUID           REFERENCES zones(id),
    disponivel      BOOLEAN        NOT NULL DEFAULT false,
    avaliacao_media NUMERIC(3, 2)  NOT NULL DEFAULT 0,
    total_entregas  INTEGER        NOT NULL DEFAULT 0,
    criado_em       TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE TABLE order_tracking (
    id           BIGSERIAL      PRIMARY KEY,
    order_id     UUID           NOT NULL REFERENCES orders(id),
    agent_id     UUID           NOT NULL REFERENCES users(id),
    latitude     NUMERIC(10, 8) NOT NULL,
    longitude    NUMERIC(11, 8) NOT NULL,
    registado_em TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE TABLE notifications (
    id        UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id   UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    titulo    VARCHAR(100) NOT NULL,
    mensagem  TEXT        NOT NULL,
    tipo      VARCHAR(30),
    lida      BOOLEAN     NOT NULL DEFAULT false,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE reviews (
    id          UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id    UUID      NOT NULL UNIQUE REFERENCES orders(id),
    cliente_id  UUID      NOT NULL REFERENCES users(id),
    agent_id    UUID      NOT NULL REFERENCES users(id),
    nota        SMALLINT  NOT NULL CHECK (nota BETWEEN 1 AND 5),
    comentario  TEXT,
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Índices
CREATE INDEX idx_delivery_agents_user_id  ON delivery_agents(user_id);
CREATE INDEX idx_delivery_agents_zone_id  ON delivery_agents(zone_id);
CREATE INDEX idx_order_tracking_order_id  ON order_tracking(order_id);
CREATE INDEX idx_order_tracking_agent_id  ON order_tracking(agent_id);
CREATE INDEX idx_notifications_user_id    ON notifications(user_id);
CREATE INDEX idx_notifications_lida       ON notifications(lida);
CREATE INDEX idx_reviews_order_id         ON reviews(order_id);
CREATE INDEX idx_reviews_agent_id         ON reviews(agent_id);

-- Zonas iniciais de Luanda
INSERT INTO zones (nome, municipio) VALUES
    ('Talatona Norte', 'Talatona'),
    ('Talatona Sul',   'Talatona'),
    ('Maianga Centro', 'Maianga'),
    ('Ingombota',      'Ingombota'),
    ('Rangel',         'Rangel'),
    ('Samba',          'Samba');
