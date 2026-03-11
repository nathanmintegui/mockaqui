create table if not exists collections
(
    id int primary key generated always as identity,
    id_service int not null references services(id),
    name varchar(255) not null check ( length(name) >= 3)
);
