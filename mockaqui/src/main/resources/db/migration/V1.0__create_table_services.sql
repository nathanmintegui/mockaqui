create table if not exists services
(
    id int primary key generated always as identity,
    name varchar(255) not null check ( length(name) >= 3)
);

