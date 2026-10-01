-- ============================================================================
-- MusicSocial · esquema inicial
-- Ejecutar en Supabase: SQL Editor → pegar → Run (o `supabase db push`).
-- Los límites coinciden con domain/.../validation/Limits.kt: si cambias uno,
-- cambia el otro. La app valida primero; la base valida de nuevo por seguridad.
-- ============================================================================

-- ---------------------------------------------------------------------------
-- Perfiles (sin email: el email vive en auth.users y no se expone a otros)
-- ---------------------------------------------------------------------------
create table public.profiles (
    id          uuid primary key references auth.users (id) on delete cascade,
    username    text not null unique check (username ~ '^[a-z0-9._]{3,20}$'),
    artist_name text not null check (char_length(artist_name) between 2 and 40),
    photo_url   text,
    bio         text not null default '' check (char_length(bio) <= 300),
    roles       text[] not null check (cardinality(roles) > 0),
    genres      text[] not null check (cardinality(genres) > 0),
    city        text check (char_length(city) <= 60),
    country     text,
    links       jsonb not null default '[]'::jsonb,
    plan        text not null default 'FREE' check (plan in ('FREE', 'PRO')),
    created_at  timestamptz not null default now()
);

-- ---------------------------------------------------------------------------
-- Tracks del perfil
-- ---------------------------------------------------------------------------
create table public.tracks (
    id               uuid primary key default gen_random_uuid(),
    owner_id         uuid not null references public.profiles (id) on delete cascade,
    title            text not null check (char_length(title) between 1 and 80),
    genre            text not null,
    bpm              int check (bpm between 40 and 250),
    audio_url        text not null,
    audio_format     text not null,
    size_bytes       bigint not null check (size_bytes between 1 and 20971520),
    duration_seconds int not null check (duration_seconds between 1 and 300),
    created_at       timestamptz not null default now()
);
create index tracks_owner_idx on public.tracks (owner_id);

-- ---------------------------------------------------------------------------
-- Convocatorias de colaboración
-- ---------------------------------------------------------------------------
create table public.collab_calls (
    id               uuid primary key default gen_random_uuid(),
    author_id        uuid not null references public.profiles (id) on delete cascade,
    title            text not null check (char_length(title) between 3 and 80),
    looking_for      text not null,
    genre            text not null,
    description      text not null check (char_length(description) between 10 and 1000),
    audio_url        text not null,
    audio_format     text not null,
    size_bytes       bigint not null check (size_bytes between 1 and 20971520),
    duration_seconds int not null check (duration_seconds between 1 and 300),
    bpm              int check (bpm between 40 and 250),
    musical_key      text,
    deadline         timestamptz not null,
    deal_type        text not null check (deal_type in ('FREE_COLLAB', 'PAID', 'EXCHANGE')),
    budget_min_cents bigint,
    budget_max_cents bigint,
    currency         text,
    status           text not null default 'OPEN' check (status in ('OPEN', 'CLOSED')),
    created_at       timestamptz not null default now(),
    constraint budget_only_when_paid check (
        (deal_type = 'PAID' and budget_min_cents > 0 and budget_max_cents >= budget_min_cents and currency is not null)
        or (deal_type <> 'PAID' and budget_min_cents is null and budget_max_cents is null)
    )
);
create index collab_calls_feed_idx on public.collab_calls (status, deadline, created_at desc);
create index collab_calls_author_idx on public.collab_calls (author_id);

-- ---------------------------------------------------------------------------
-- Postulaciones (una por persona y convocatoria)
-- ---------------------------------------------------------------------------
create table public.applications (
    id               uuid primary key default gen_random_uuid(),
    call_id          uuid not null references public.collab_calls (id) on delete cascade,
    applicant_id     uuid not null references public.profiles (id) on delete cascade,
    demo_url         text not null,
    demo_format      text not null,
    size_bytes       bigint not null check (size_bytes between 1 and 20971520),
    duration_seconds int not null check (duration_seconds between 1 and 60),
    message          text not null default '' check (char_length(message) <= 300),
    status           text not null default 'PENDING' check (status in ('PENDING', 'ACCEPTED', 'REJECTED')),
    created_at       timestamptz not null default now(),
    unique (call_id, applicant_id)
);
create index applications_call_idx on public.applications (call_id);

-- ---------------------------------------------------------------------------
-- Chat
-- ---------------------------------------------------------------------------
create table public.conversations (
    id              uuid primary key default gen_random_uuid(),
    application_id  uuid not null unique references public.applications (id) on delete cascade,
    participant_ids uuid[] not null check (cardinality(participant_ids) = 2),
    created_at      timestamptz not null default now()
);

create table public.messages (
    id               uuid primary key default gen_random_uuid(),
    conversation_id  uuid not null references public.conversations (id) on delete cascade,
    sender_id        uuid not null references public.profiles (id) on delete cascade,
    kind             text not null check (kind in ('TEXT', 'VOICE', 'FILE')),
    text             text check (char_length(text) <= 2000),
    audio_url        text,
    audio_format     text,
    size_bytes       bigint,
    duration_seconds int,
    file_name        text,
    sent_at          timestamptz not null default now(),
    constraint message_content check (
        (kind = 'TEXT' and text is not null and audio_url is null)
        or (kind in ('VOICE', 'FILE') and audio_url is not null)
    )
);
create index messages_conversation_idx on public.messages (conversation_id, sent_at);

-- ============================================================================
-- Reglas de negocio en la base (por si alguien llama la API sin la app)
-- ============================================================================

-- Plan gratis: máximo 5 tracks.
create function public.check_track_limit() returns trigger
language plpgsql security definer set search_path = public as $$
begin
    if (select plan from profiles where id = new.owner_id) = 'FREE'
       and (select count(*) from tracks where owner_id = new.owner_id) >= 5 then
        raise exception 'PLAN_LIMIT_REACHED' using errcode = 'P0001';
    end if;
    return new;
end $$;
create trigger tracks_limit before insert on public.tracks
    for each row execute function public.check_track_limit();

-- Plan gratis: máximo 2 convocatorias activas. La fecha límite debe ser futura (máx. 90 días).
create function public.check_call_rules() returns trigger
language plpgsql security definer set search_path = public as $$
begin
    if new.deadline <= now() or new.deadline > now() + interval '90 days' then
        raise exception 'INVALID_DEADLINE' using errcode = 'P0001';
    end if;
    if (select plan from profiles where id = new.author_id) = 'FREE'
       and (select count(*) from collab_calls
            where author_id = new.author_id and status = 'OPEN' and deadline > now()) >= 2 then
        raise exception 'PLAN_LIMIT_REACHED' using errcode = 'P0001';
    end if;
    return new;
end $$;
create trigger collab_calls_rules before insert on public.collab_calls
    for each row execute function public.check_call_rules();

-- No postularse a tu propia convocatoria ni a una cerrada o vencida.
create function public.check_application_rules() returns trigger
language plpgsql security definer set search_path = public as $$
declare
    c collab_calls;
begin
    select * into c from collab_calls where id = new.call_id;
    if c.author_id = new.applicant_id then
        raise exception 'CANNOT_APPLY_TO_OWN_CALL' using errcode = 'P0001';
    end if;
    if c.status <> 'OPEN' or c.deadline <= now() then
        raise exception 'CALL_CLOSED' using errcode = 'P0001';
    end if;
    return new;
end $$;
create trigger applications_rules before insert on public.applications
    for each row execute function public.check_application_rules();

-- Ayudante para las políticas: ¿el usuario actual es autor de la convocatoria?
create function public.is_call_author(p_call_id uuid) returns boolean
language sql stable security definer set search_path = public as $$
    select exists (select 1 from collab_calls where id = p_call_id and author_id = auth.uid());
$$;

-- ============================================================================
-- Seguridad por fila (RLS): quién puede leer y escribir qué
-- ============================================================================
alter table public.profiles      enable row level security;
alter table public.tracks        enable row level security;
alter table public.collab_calls  enable row level security;
alter table public.applications  enable row level security;
alter table public.conversations enable row level security;
alter table public.messages      enable row level security;

-- Perfiles: todos los usuarios con sesión los ven; cada uno edita el suyo.
create policy "profiles_select" on public.profiles for select to authenticated using (true);
create policy "profiles_insert_own" on public.profiles for insert to authenticated with check (id = auth.uid());
create policy "profiles_update_own" on public.profiles for update to authenticated
    using (id = auth.uid()) with check (id = auth.uid());
-- El plan no lo puede cambiar el usuario (lo cambiará el sistema de pagos).
-- En Postgres no basta con quitar una columna: hay que quitar el permiso de
-- toda la tabla y luego dar permiso solo a las columnas editables.
revoke insert, update on public.profiles from authenticated;
grant insert (id, username, artist_name, photo_url, bio, roles, genres, city, country, links)
    on public.profiles to authenticated;
grant update (username, artist_name, photo_url, bio, roles, genres, city, country, links)
    on public.profiles to authenticated;

-- Tracks: públicos para usuarios con sesión; solo el dueño crea o borra.
create policy "tracks_select" on public.tracks for select to authenticated using (true);
create policy "tracks_insert_own" on public.tracks for insert to authenticated with check (owner_id = auth.uid());
create policy "tracks_delete_own" on public.tracks for delete to authenticated using (owner_id = auth.uid());

-- Convocatorias: públicas; el autor crea y edita (por ejemplo, cerrarla).
create policy "calls_select" on public.collab_calls for select to authenticated using (true);
create policy "calls_insert_own" on public.collab_calls for insert to authenticated with check (author_id = auth.uid());
create policy "calls_update_own" on public.collab_calls for update to authenticated
    using (author_id = auth.uid()) with check (author_id = auth.uid());

-- Postulaciones: las ve quien se postuló y el autor de la convocatoria.
create policy "applications_select" on public.applications for select to authenticated
    using (applicant_id = auth.uid() or public.is_call_author(call_id));
create policy "applications_insert_own" on public.applications for insert to authenticated
    with check (applicant_id = auth.uid() and status = 'PENDING');
-- Solo el autor de la convocatoria cambia el estado, y solo esa columna.
create policy "applications_review" on public.applications for update to authenticated
    using (public.is_call_author(call_id));
revoke update on public.applications from authenticated;
grant update (status) on public.applications to authenticated;

-- Conversaciones: las ven sus participantes; las crea el autor al aceptar.
create policy "conversations_select" on public.conversations for select to authenticated
    using (auth.uid() = any (participant_ids));
create policy "conversations_insert" on public.conversations for insert to authenticated
    with check (
        auth.uid() = any (participant_ids)
        and exists (
            select 1 from public.applications a
            where a.id = application_id
              and a.status = 'ACCEPTED'
              and public.is_call_author(a.call_id)
              and a.applicant_id = any (participant_ids)
        )
    );

-- Mensajes: solo participantes leen y escriben, y solo como ellos mismos.
create policy "messages_select" on public.messages for select to authenticated
    using (exists (select 1 from public.conversations c
                   where c.id = conversation_id and auth.uid() = any (c.participant_ids)));
create policy "messages_insert" on public.messages for insert to authenticated
    with check (sender_id = auth.uid()
                and exists (select 1 from public.conversations c
                            where c.id = conversation_id and auth.uid() = any (c.participant_ids)));

-- Chat en tiempo real.
alter publication supabase_realtime add table public.messages;

-- ============================================================================
-- Almacenamiento de audio
-- Bucket público de lectura (los nombres son UUID, no se pueden adivinar).
-- Cada usuario solo sube y borra dentro de su carpeta: audio/<user_id>/...
-- ============================================================================
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('audio', 'audio', true, 20971520,
        array['audio/mpeg', 'audio/wav', 'audio/x-wav', 'audio/aac', 'audio/mp4', 'audio/ogg']);

create policy "audio_upload_own_folder" on storage.objects for insert to authenticated
    with check (bucket_id = 'audio' and (storage.foldername(name))[1] = auth.uid()::text);
create policy "audio_delete_own_folder" on storage.objects for delete to authenticated
    using (bucket_id = 'audio' and (storage.foldername(name))[1] = auth.uid()::text);
