alter table public.flyway_test drop constraint flyway_test_pkey;
ALTER TABLE public.flyway_test
ADD COLUMN ID SERIAL PRIMARY KEY;