CREATE TABLE agencies (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome            VARCHAR(150) NOT NULL,
    nif             VARCHAR(20)  NOT NULL UNIQUE,
    responsavel     VARCHAR(100) NOT NULL,
    latitude        NUMERIC(10, 8),
    longitude       NUMERIC(11, 8),
    morada          VARCHAR(255),
    activa          BOOLEAN NOT NULL DEFAULT true,
    criado_em       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_em  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_agencies_activa ON agencies (activa);
