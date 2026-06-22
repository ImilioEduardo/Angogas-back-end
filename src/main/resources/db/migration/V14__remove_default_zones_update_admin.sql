-- Remove zonas padrão inseridas em V3 (admin define as suas próprias)
UPDATE delivery_agents SET zone_id = NULL
WHERE zone_id IN (
    SELECT id FROM zones
    WHERE nome IN ('Talatona Norte','Talatona Sul','Maianga Centro','Ingombota','Rangel','Samba')
);

UPDATE orders SET zone_id = NULL
WHERE zone_id IN (
    SELECT id FROM zones
    WHERE nome IN ('Talatona Norte','Talatona Sul','Maianga Centro','Ingombota','Rangel','Samba')
);

DELETE FROM zones
WHERE nome IN ('Talatona Norte','Talatona Sul','Maianga Centro','Ingombota','Rangel','Samba');

-- Contacto do administrador
UPDATE users
SET telefone = '944916156'
WHERE email = 'admin@angogas.ao';
