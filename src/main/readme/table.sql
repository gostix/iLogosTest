CREATE TABLE public.table_ilogos (
  id INTEGER PRIMARY KEY NOT NULL DEFAULT nextval('table_ilogos_id_seq'::regclass),
  timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
  content TEXT,
  creation_date TEXT
);
CREATE UNIQUE INDEX table_ilogos_id_uindex ON table_ilogos USING BTREE (id);