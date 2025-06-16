create table if not exists api_client
(
    id            int auto_increment
        primary key,
    client_id     int          null,
    client_secret varchar(255) null,
    description   varchar(255) null,
    created_at    datetime     null
);

create table if not exists beatmap
(
    id       bigint       not null
        primary key,
    title    varchar(512) null,
    artist   varchar(512) null,
    version  varchar(512) null,
    bpm      double       null,
    checksum varchar(512) null
);

create table if not exists player_summary
(
    id           bigint auto_increment
        primary key,
    last_updated datetime null
);

create table if not exists scores
(
    id           bigint   not null
        primary key,
    player_id    bigint   null,
    beatmap_id   bigint   null,
    accuracy     double   null,
    pp_speed     double   null,
    pp_stamina   double   null,
    pp_precision double   null,
    pp_aim       double   null,
    pp_jump      double   null,
    pp_flow      double   null,
    created_at   datetime null,
    pp_accuracy  double   null,
    combo        int      null,
    pp           double   null,
    constraint scores_beatmap_id_fk
        foreign key (beatmap_id) references beatmap (id),
    constraint scores_player_summary_id_fk
        foreign key (player_id) references player_summary (id)
);

create table if not exists score_mods
(
    score_id bigint       null,
    `mod`    varchar(256) null,
    constraint score_mods_scores_id_fk
        foreign key (score_id) references scores (id)
);

create table if not exists score_statistics
(
    score_id   bigint null,
    count_300  int    null,
    count_100  int    null,
    count_50   int    null,
    count_0    int    null,
    count_tick int    null,
    count_end  int    null,
    constraint score_statistics_scores_id_fk
        foreign key (score_id) references scores (id)
);

create index scores_pp_accuracy_index
    on scores (pp_accuracy);

create index scores_pp_aim_index
    on scores (pp_aim);

create index scores_pp_flow_index
    on scores (pp_flow);

create index scores_pp_index
    on scores (pp);

create index scores_pp_jump_index
    on scores (pp_jump);

create index scores_pp_precision_index
    on scores (pp_precision);

create index scores_pp_speed_index
    on scores (pp_speed);

create index scores_pp_stamina_index
    on scores (pp_stamina);

