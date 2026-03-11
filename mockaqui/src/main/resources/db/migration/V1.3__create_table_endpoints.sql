create table if not exists endpoints
(
    id int primary key generated always as identity,
    id_collection int not null references collections (id) on delete cascade,
    uri varchar(256) not null check ( length( uri ) > 0 ),
    verb smallint not null references http_verbs (id),
    status_code smallint not null,
    payload jsonb,
    headers jsonb,
    query_params jsonb,
    response_latency int not null
);
