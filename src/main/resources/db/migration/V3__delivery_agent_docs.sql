-- ============================================================
-- AngoGÁS — Dados de documentação do entregador
-- ============================================================

ALTER TABLE delivery_agents
    ADD COLUMN data_nascimento      DATE,
    ADD COLUMN bi_numero            VARCHAR(30),
    ADD COLUMN carta_conducao       VARCHAR(30),
    ADD COLUMN carta_conducao_desde DATE,
    ADD COLUMN registo_criminal     BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN livrete_veiculo      VARCHAR(30),
    ADD COLUMN seguro_apolice       VARCHAR(50),
    ADD COLUMN inspecao_validade    DATE,
    ADD COLUMN tem_smartphone       BOOLEAN NOT NULL DEFAULT true;
