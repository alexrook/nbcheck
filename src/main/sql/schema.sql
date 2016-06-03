
/* 
    client host (from sqstats project)
*/

/*
drop table if exists clienthost cascade;
create table clienthost (
    id serial not null primary key,
    address inet unique,
    name varchar(255),
    description varchar(255)
);
*/


drop table if exists  netbiosnames cascade;

create table netbiosnames(
    id int,
    name varchar(255)
);

alter table netbiosnames add constraint fk_nb_clienthost
                foreign key (id) 
                references clienthost on delete cascade;
