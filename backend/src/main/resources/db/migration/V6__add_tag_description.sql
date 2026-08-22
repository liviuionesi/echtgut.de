-- V6__add_tag_description.sql
-- Adds description to taxonomy tags for frontend rendering

ALTER TABLE tags ADD COLUMN description VARCHAR(255);

-- Seed descriptions for V4 tags based on frontend requirements
UPDATE tags SET description = 'Saunen, Spas und stille Orte zum Runterkommen.' WHERE slug = 'auszeit';
UPDATE tags SET description = 'Besondere Orte für unvergessliche Abende.' WHERE slug = 'romantik';
UPDATE tags SET description = 'Regionale Spezialitäten und kulinarische Geheimtipps.' WHERE slug = 'feinschmecker';
UPDATE tags SET description = 'Wanderungen, Parks und Erlebnisse an der frischen Luft.' WHERE slug = 'natur';
UPDATE tags SET description = 'Museen, Galerien und historische Schätze.' WHERE slug = 'kultur';
UPDATE tags SET description = 'Bequeme Sessel, gute Haltung, echte Erholung.' WHERE slug = 'wellness';
