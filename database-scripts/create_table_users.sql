create table users (
                       id uuid unique,
                       username varchar(100) not null,
                       email varchar not null unique,
                       password varchar not null,
                       balance numeric not null,
                       card_number varchar
);