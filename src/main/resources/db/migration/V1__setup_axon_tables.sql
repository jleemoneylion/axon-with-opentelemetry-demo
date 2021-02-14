CREATE TABLE IF NOT EXISTS public.domainevententry (
	globalindex bigserial NOT NULL,
	aggregateidentifier varchar(255) NOT NULL,
	sequencenumber int8 NOT NULL,
	"type" varchar(255) NULL,
	eventidentifier varchar(255) NOT NULL,
	metadata bytea NULL,
	payload bytea NOT NULL,
	payloadrevision varchar(255) NULL,
	payloadtype varchar(255) NOT NULL,
	"timestamp" varchar(255) NOT NULL,
	CONSTRAINT domainevententry_aggregateidentifier_sequencenumber_key UNIQUE (aggregateidentifier, sequencenumber),
	CONSTRAINT domainevententry_eventidentifier_key UNIQUE (eventidentifier),
	CONSTRAINT domainevententry_pkey PRIMARY KEY (globalindex)
);

CREATE TABLE IF NOT EXISTS public.snapshotevententry (
	aggregateidentifier varchar(255) NOT NULL,
	sequencenumber int8 NOT NULL,
	"type" varchar(255) NOT NULL,
	eventidentifier varchar(255) NOT NULL,
	metadata bytea NULL,
	payload bytea NOT NULL,
	payloadrevision varchar(255) NULL,
	payloadtype varchar(255) NOT NULL,
	"timestamp" varchar(255) NOT NULL,
	CONSTRAINT snapshotevententry_eventidentifier_key UNIQUE (eventidentifier),
	CONSTRAINT snapshotevententry_pkey PRIMARY KEY (aggregateidentifier, sequencenumber)
);

CREATE TABLE IF NOT EXISTS public.tokenentry (
	processorname varchar(255) NOT NULL,
	segment int4 NOT NULL,
	"token" bytea NULL,
	tokentype varchar(255) NULL,
	"timestamp" varchar(255) NULL,
	"owner" varchar(255) NULL,
	CONSTRAINT tokenentry_pkey PRIMARY KEY (processorname, segment)
);

CREATE TABLE IF NOT EXISTS public.associationvalueentry (
	id bigserial NOT NULL,
	associationkey varchar(255) NULL,
	associationvalue varchar(255) NULL,
	sagaid varchar(255) NULL,
	sagatype varchar(255) NULL,
	CONSTRAINT associationvalueentry_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS public.sagaentry (
	sagaid varchar(255) NOT NULL,
	revision varchar(255) NULL,
	sagatype varchar(255) NULL,
	serializedsaga bytea NULL,
	CONSTRAINT sagaentry_pkey PRIMARY KEY (sagaid)
);