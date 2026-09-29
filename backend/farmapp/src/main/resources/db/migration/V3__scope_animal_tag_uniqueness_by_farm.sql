DROP INDEX IF EXISTS uk_animals_tag;

CREATE UNIQUE INDEX IF NOT EXISTS uk_animals_farm_tag ON animals (farm_id, tag);
