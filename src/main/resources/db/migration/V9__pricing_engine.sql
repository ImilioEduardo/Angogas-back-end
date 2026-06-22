-- ── Motor de precificação ─────────────────────────────────────────────
-- Parâmetros globais da plataforma (linha singleton id = 1)
CREATE TABLE IF NOT EXISTS platform_settings (
    id                      BIGINT PRIMARY KEY DEFAULT 1,
    preco_por_km            NUMERIC(10,2) NOT NULL DEFAULT 300.00,
    preco_por_minuto        NUMERIC(10,2) NOT NULL DEFAULT 50.00,
    comissao_app            NUMERIC(5,4)  NOT NULL DEFAULT 0.1000, -- 10%
    distancia_min_km        NUMERIC(5,2)  NOT NULL DEFAULT 2.00,
    distancia_max_km        NUMERIC(5,2)  NOT NULL DEFAULT 30.00,
    margem_entrega          NUMERIC(5,4)  NOT NULL DEFAULT 0.2500, -- 25%
    quantidade_max_botijas  INTEGER       NOT NULL DEFAULT 15,
    custofixo_mensal        NUMERIC(14,2) NOT NULL DEFAULT 168000.00,
    entregas_estimadas_mes  INTEGER       NOT NULL DEFAULT 200,
    armazem_latitude        NUMERIC(10,8) NOT NULL DEFAULT -8.90680000, -- Luanda
    armazem_longitude       NUMERIC(11,8) NOT NULL DEFAULT 13.17730000,
    velocidade_media_kmh    NUMERIC(5,2)  NOT NULL DEFAULT 30.00,
    CONSTRAINT platform_settings_singleton CHECK (id = 1)
);

-- Garante que existe sempre exactamente uma linha
INSERT INTO platform_settings (id) VALUES (1) ON CONFLICT (id) DO NOTHING;

-- ── Configuração de precificação por produto ───────────────────────────
-- Armazena custo SonaGas, margem e depósito de garrafa por produto.
-- Desligada da tabela products por design: o admin pode ajustar margens
-- sem alterar o preço de venda directamente.
CREATE TABLE IF NOT EXISTS product_pricing (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id           UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    custo_sonagaz_kz     NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    margem_produto       NUMERIC(5,4)  NOT NULL DEFAULT 0.2000, -- 20%
    deposito_garrafa_kz  NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    activo               BOOLEAN NOT NULL DEFAULT true,
    criado_em            TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_em       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT product_pricing_product_unique UNIQUE (product_id)
);

CREATE INDEX IF NOT EXISTS idx_product_pricing_product ON product_pricing(product_id);

-- ── Registo financeiro por entrega concluída ───────────────────────────
-- Guarda os dados financeiros de cada entrega para o dashboard de gestão.
-- Preenchido automaticamente quando um pedido passa a ENTREGUE.
CREATE TABLE IF NOT EXISTS delivery_financials (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id              UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    distancia_real_km     NUMERIC(8,3)  NOT NULL DEFAULT 0.000,
    tempo_real_min        INTEGER       NOT NULL DEFAULT 0,
    preco_cobrado_kz      NUMERIC(14,2) NOT NULL,
    custo_produto_kz      NUMERIC(14,2) NOT NULL DEFAULT 0.00,
    taxa_entrega_kz       NUMERIC(14,2) NOT NULL DEFAULT 0.00,
    comissao_plataforma_kz NUMERIC(14,2) NOT NULL DEFAULT 0.00,
    lucro_liquido_kz      NUMERIC(14,2) NOT NULL DEFAULT 0.00,
    criado_em             TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT delivery_financials_order_unique UNIQUE (order_id)
);

CREATE INDEX IF NOT EXISTS idx_delivery_financials_order ON delivery_financials(order_id);
CREATE INDEX IF NOT EXISTS idx_delivery_financials_criado ON delivery_financials(criado_em);
