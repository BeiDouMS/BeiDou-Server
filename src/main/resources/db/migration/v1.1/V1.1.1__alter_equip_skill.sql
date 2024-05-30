ALTER TABLE inventoryequipment
    ADD skill BOOLEAN DEFAULT FALSE AFTER ringid;

ALTER TABLE mts_items
    ADD skill BOOLEAN DEFAULT FALSE AFTER ringid;


