-- Adicionar zona ao pedido e novo status AGUARDANDO_ACEITACAO
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS zone_id UUID REFERENCES zones(id);

ALTER TABLE orders
    DROP CONSTRAINT IF EXISTS orders_status_check;

ALTER TABLE orders
    ADD CONSTRAINT orders_status_check
    CHECK (status IN (
        'PENDENTE',
        'AGUARDANDO_ACEITACAO',
        'CONFIRMADO',
        'A_PREPARAR',
        'A_CAMINHO',
        'ENTREGUE',
        'CANCELADO'
    ));

CREATE INDEX IF NOT EXISTS idx_orders_zone_id ON orders(zone_id);
