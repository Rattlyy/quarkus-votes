create sequence liste_id_seq
    as integer;

alter sequence liste_id_seq owner to myuser;

create sequence voti_id_seq
    as integer;

alter sequence voti_id_seq owner to myuser;

create sequence "voti_listaVotata_seq"
    as integer;

alter sequence "voti_listaVotata_seq" owner to myuser;

create table lists
(
    id          integer        default nextval('liste_id_seq'::regclass)                                   not null
        constraint lists_pk
            primary key
        constraint lists_pk2
            unique,
    name        varchar(255)                                                                               not null,
    list_master varchar(255)                                                                               not null,
    members     varchar(255)[] default ARRAY []::character varying[]                                       not null,
    color       varchar(10)    default to_hex((floor((random() * (16777215)::double precision)))::integer) not null
);

alter sequence liste_id_seq owned by lists.id;

create table votes
(
    id          integer default nextval('voti_id_seq'::regclass)            not null
        constraint votes_pk
            primary key
        constraint votes_pk2
            unique,
    "votedList" integer default nextval('"voti_listaVotata_seq"'::regclass) not null
        constraint voti_liste_id_fk
            references lists
);

alter sequence voti_id_seq owned by votes.id;

alter sequence "voti_listaVotata_seq" owned by votes."votedList";