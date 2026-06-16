-- ============================================================
-- AngoGÁS — Schema Fase 3: Pagamentos, Subscrições, Fidelidade, Audit
-- ============================================================

-- Pagamentos
CREATE TABLE payments (
    id                  UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id            UUID            NOT NULL REFERENCES orders(id),
    metodo              VARCHAR(30)     NOT NULL CHECK (metodo IN ('MULTICAIXA','DINHEIRO')),
    status              VARCHAR(30)     NOT NULL DEFAULT 'PENDENTE'
                        CHECK (status IN ('PENDENTE','PROCESSANDO','APROVADO','RECUSADO','REEMBOLSADO')),
    referencia_externa  VARCHAR(100),
    valor_kz            NUMERIC(12, 2)  NOT NULL,
    criado_em           TIMESTAMPTZ     NOT NULL DEFAULT now(),
    confirmado_em       TIMESTAMPTZ
);

-- Subscrições mensais
CREATE TABLE subscriptions (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL REFERENCES users(id),
    plano           VARCHAR(50)     NOT NULL,
    preco_kz        NUMERIC(12, 2)  NOT NULL,
    inicio          DATE            NOT NULL,
    fim             DATE            NOT NULL,
    activa          BOOLEAN         NOT NULL DEFAULT true,
    renovacao_auto  BOOLEAN         NOT NULL DEFAULT true,
    criado_em       TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- Pontos de fidelidade
CREATE TABLE loyalty_points (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id),
    pontos      INTEGER     NOT NULL,
    motivo      VARCHAR(100) NOT NULL,
    order_id    UUID        REFERENCES orders(id),
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Audit log de acções administrativas
CREATE TABLE audit_logs (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     UUID        REFERENCES users(id),
    user_email  VARCHAR(150),
    accao       VARCHAR(100) NOT NULL,
    entidade    VARCHAR(100),
    entidade_id VARCHAR(100),
    detalhes    TEXT,
    ip          VARCHAR(45),
    criado_em   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Índices
CREATE INDEX idx_payments_order_id         ON payments(order_id);
CREATE INDEX idx_payments_status           ON payments(status);
CREATE INDEX idx_subscriptions_user_id     ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_activa      ON subscriptions(user_id, activa);
CREATE INDEX idx_loyalty_points_user_id    ON loyalty_points(user_id);
CREATE INDEX idx_audit_logs_user_id        ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_criado_em      ON audit_logs(criado_em DESC);
