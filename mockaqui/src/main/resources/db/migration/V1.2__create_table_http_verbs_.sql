create table if not exists http_verbs(
    id smallint primary key generated always as identity,
    name varchar(7) not null unique check ( length(name) >=3 )
);

insert into http_verbs (name)
values ('GET')
on conflict do nothing;
insert into http_verbs (name)
values ('HEAD')
    on conflict do nothing;
insert into http_verbs (name)
values ('OPTIONS')
    on conflict do nothing;
insert into http_verbs (name)
values ('TRACE')
    on conflict do nothing;
insert into http_verbs (name)
values ('PUT')
    on conflict do nothing;
insert into http_verbs (name)
values ('DELETE')
    on conflict do nothing;
insert into http_verbs (name)
values ('POST')
    on conflict do nothing;
insert into http_verbs (name)
values ('PATCH')
    on conflict do nothing;
insert into http_verbs (name)
values ('CONNECT')
    on conflict do nothing;
