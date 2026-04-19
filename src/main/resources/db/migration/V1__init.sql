create table if not exists tasks (
    id bigserial primary key,
    title varchar(255) not null,
    description text,
    status varchar(50) not null default 'NEW',
    created_at timestamp default now()
);

create table if not exists time_records (
    id bigserial primary key,
    employee_id bigint not null,
    task_id bigint not null references tasks(id),
    start_time timestamp not null,
    end_time timestamp not null,
    work_description text,
    created_at timestamp default now()
);