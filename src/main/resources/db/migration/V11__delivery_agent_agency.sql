ALTER TABLE delivery_agents
    ADD COLUMN agency_id UUID REFERENCES agencies(id) ON DELETE SET NULL;

CREATE INDEX idx_delivery_agents_agency ON delivery_agents(agency_id);
