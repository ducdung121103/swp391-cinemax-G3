create database MBCMS
use MBCMS
create table genres
(
    genre_id    int identity,
    genre_name  nvarchar(100) collate SQL_Latin1_General_CP1_CI_AS not null,
    description nvarchar(255) collate SQL_Latin1_General_CP1_CI_AS,
    is_active   bit       default 1,
    created_at  datetime2 default sysdatetime(),
    updated_at  datetime2 default sysdatetime(),
    primary key (genre_id),
    unique (genre_name), , ,
)
go

create index idx_genres_active
    on genres (is_active)
go

-- Trigger: Auto update genres.updated_at
CREATE TRIGGER trg_genres_updated ON genres
AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE g SET updated_at = SYSDATETIME()
    FROM genres g
    INNER JOIN inserted i ON g.genre_id = i.genre_id;
END;
go

create table membership_tiers
(
    tier_id             int identity,
    tier_name           nvarchar(50) collate SQL_Latin1_General_CP1_CI_AS not null,
    min_points_required int           default 0                           not null,
    point_multiplier    decimal(3, 2) default 1.0,
    created_at          datetime2     default sysdatetime(),
    primary key (tier_id),
    unique (tier_name), , ,
)
go

create table movies
(
    movie_id     int identity,
    title        nvarchar(150) collate SQL_Latin1_General_CP1_CI_AS not null,
    description  nvarchar(max) collate SQL_Latin1_General_CP1_CI_AS,
    duration     int                                                not null,
    release_date date,
    end_date     date,
    rating       decimal(2, 1) default 0,
    age_rating   varchar(10) collate SQL_Latin1_General_CP1_CI_AS,
    director     nvarchar(100) collate SQL_Latin1_General_CP1_CI_AS,
    cast         nvarchar(500) collate SQL_Latin1_General_CP1_CI_AS,
    poster_url   varchar(255) collate SQL_Latin1_General_CP1_CI_AS,
    is_active    bit           default 1,
    created_at   datetime2     default sysdatetime(),
    updated_at   datetime2     default sysdatetime(),
    primary key (movie_id),
    constraint CK_movie_rating
        check ([rating] >= 0 AND [rating] <= 5), , , ,
)
go

create table movie_genres
(
    movie_id   int not null,
    genre_id   int not null,
    created_at datetime2 default sysdatetime(),
    constraint PK_movie_genres
        primary key (movie_id, genre_id),
    constraint FK_movie_genres_genre
        foreign key (genre_id) references genres
            on delete cascade,
    constraint FK_movie_genres_movie
        foreign key (movie_id) references movies
            on delete cascade,
)
go

create index idx_movie_genres_movie
    on movie_genres (movie_id)
go

create index idx_movie_genres_genre
    on movie_genres (genre_id)
go

create index idx_movies_release
    on movies (release_date)
go

create index idx_movies_active
    on movies (is_active)
go

create index idx_movies_rating
    on movies (rating)
go


-- Trigger: Auto update movies.updated_at
CREATE TRIGGER trg_movies_updated ON movies
AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE m SET updated_at = SYSDATETIME()
    FROM movies m
    INNER JOIN inserted i ON m.movie_id = i.movie_id;
END;
go

create table roles
(
    role_id    int identity,
    role_name  varchar(50) not null,
    created_at datetime2 default sysdatetime(),
    updated_at datetime2 default sysdatetime(),
    primary key (role_id),
    unique (role_name), ,
)
go


-- Trigger: Auto update roles.updated_at
CREATE TRIGGER trg_roles_updated ON roles
AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE r SET updated_at = SYSDATETIME()
    FROM roles r
    INNER JOIN inserted i ON r.role_id = i.role_id;
END;
go

create table users
(
    user_id                  int identity,
    role_id                  int                                               not null,
    username                 varchar(50) collate SQL_Latin1_General_CP1_CI_AS  not null,
    email                    varchar(100) collate SQL_Latin1_General_CP1_CI_AS not null,
    password                 varchar(255) collate SQL_Latin1_General_CP1_CI_AS not null,
    fullName                 nvarchar(255) collate SQL_Latin1_General_CP1_CI_AS,
    birthday                 datetime2,
    phone                    varchar(20) collate SQL_Latin1_General_CP1_CI_AS,
    avatarURL                varchar(255) collate SQL_Latin1_General_CP1_CI_AS,
    status                   varchar(10) default 'ACTIVE' collate SQL_Latin1_General_CP1_CI_AS,
    points                   int         default 0,
    created_at               datetime2   default sysdatetime(),
    updated_at               datetime2   default sysdatetime(),
    last_login               datetime2,
    total_accumulated_points int         default 0,
    tier_id                  int         default 1,
    primary key (user_id),
    unique (email),
    unique (username),
    constraint FK_users_role
        foreign key (role_id) references roles,
    constraint FK_users_tier
        foreign key (tier_id) references membership_tiers,
    constraint CK_users_status
        check ([status] = 'INACTIVE' OR [status] = 'LOCKED' OR [status] = 'ACTIVE'), , , , , ,
)
go

create table cinema_branches
(
    branch_id   int identity,
    branch_name nvarchar(100) not null,
    address     nvarchar(255),
    phone       varchar(20),
    email       varchar(100),
    manager_id  int,
    is_active   bit       default 1,
    created_at  datetime2 default sysdatetime(),
    updated_at  datetime2 default sysdatetime(),
    primary key (branch_id),
    constraint FK_branch_manager
        foreign key (manager_id) references users, , ,
)
go

create index idx_branches_active
    on cinema_branches (is_active)
go

create index idx_branches_manager
    on cinema_branches (manager_id)
go


-- Trigger: Auto update cinema_branches.updated_at
CREATE TRIGGER trg_branches_updated ON cinema_branches
AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE b SET updated_at = SYSDATETIME()
    FROM cinema_branches b
    INNER JOIN inserted i ON b.branch_id = i.branch_id;
END;
go

create table concessions
(
    concession_id   int identity,
    concession_type varchar(20) collate SQL_Latin1_General_CP1_CI_AS not null,
    quantity        int       default 0,
    price_base      decimal(10, 1)                                    not null,
    added_by        int,
    created_at      datetime2 default sysdatetime(),
    concession_name nvarchar(255) collate SQL_Latin1_General_CP1_CI_AS,
    primary key (concession_id),
    constraint FK_concessions_users
        foreign key (added_by) references users,
    constraint CK_concession_type
        check ([concession_type] = 'FOOD' OR [concession_type] = 'BEVERAGE'), ,
)
go

create table loyalty_configs
(
    config_id         int                          not null,
    earn_rate_amount  decimal(10, 2) default 10000 not null,
    earn_points       int            default 1     not null,
    min_redeem_points int            default 100   not null,
    updated_at        datetime2      default sysdatetime(),
    updated_by        int,
    primary key (config_id),
    constraint FK_loyalty_config_admin
        foreign key (updated_by) references users,
    check ([config_id] = 1), , , ,
)
go

create table point_history
(
    history_id       int identity,
    user_id          int                                              not null,
    points_changed   int                                              not null,
    transaction_type varchar(20) collate SQL_Latin1_General_CP1_CI_AS not null,
    description      nvarchar(255) collate SQL_Latin1_General_CP1_CI_AS,
    reference_id     int,
    created_at       datetime2 default sysdatetime(),
    primary key (history_id),
    constraint FK_point_history_user
        foreign key (user_id) references users,
    constraint CK_point_transaction_type
        check ([transaction_type] = 'ADJUSTMENT' OR [transaction_type] = 'EXPIRE' OR [transaction_type] = 'REFUND' OR
               [transaction_type] = 'REDEEM' OR [transaction_type] = 'EARN'),
)
go

create table revenue_reports
(
    report_id             int identity,
    branch_id             int,
    report_date           date        not null,
    sale_channel          varchar(10) not null,
    online_tickets_count  int            default 0,
    online_revenue        decimal(12, 2) default 0,
    counter_tickets_count int            default 0,
    counter_revenue       decimal(12, 2) default 0,
    total_tickets_count   int            default 0,
    total_revenue         decimal(12, 2) default 0,
    adult_tickets         int            default 0,
    child_tickets         int            default 0,
    normal_seats          int            default 0,
    vip_seats             int            default 0,
    couple_seats          int            default 0,
    generated_at          datetime2      default sysdatetime(),
    generated_by          int,
    primary key (report_id),
    constraint FK_revenue_branch
        foreign key (branch_id) references cinema_branches,
    constraint FK_revenue_generated_by
        foreign key (generated_by) references users,
    constraint CK_sale_channel
        check ([sale_channel] = 'COMBINED' OR [sale_channel] = 'COUNTER' OR
               [sale_channel] = 'ONLINE'), , , , , , , , , , , ,
)
go

create index idx_revenue_branch
    on revenue_reports (branch_id)
go

create index idx_revenue_date
    on revenue_reports (report_date)
go

create index idx_revenue_channel
    on revenue_reports (sale_channel)
go

create index idx_revenue_generated
    on revenue_reports (generated_at)
go

create table reviews
(
    review_id  int identity,
    user_id    int           not null,
    movie_id   int           not null,
    rating     decimal(2, 1) not null,
    comment    nvarchar(max) collate SQL_Latin1_General_CP1_CI_AS,
    created_at datetime2 default sysdatetime(),
    updated_at datetime2 default sysdatetime(),
    primary key (review_id),
    constraint FK_review_movie
        foreign key (movie_id) references movies,
    constraint FK_review_user
        foreign key (user_id) references users,
    constraint CK_review_rating
        check ([rating] >= 1 AND [rating] <= 5), , , ,
)
go

create index idx_review_movie
    on reviews (movie_id)
go

create index idx_review_user
    on reviews (user_id)
go

create index idx_review_rating
    on reviews (rating)
go

create index idx_review_created
    on reviews (created_at)
go


-- Trigger: Auto update reviews.updated_at
CREATE TRIGGER trg_reviews_updated ON reviews
AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE r SET updated_at = SYSDATETIME()
    FROM reviews r
    INNER JOIN inserted i ON r.review_id = i.review_id;
END;
go


-- Trigger: Update movie rating when review is added/updated
CREATE TRIGGER trg_update_movie_rating ON reviews
AFTER INSERT, UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE m
    SET rating = (
        SELECT ROUND(AVG(CAST(rating AS DECIMAL(3,2))), 1)
        FROM reviews
        WHERE movie_id = m.movie_id
    )
    FROM movies m
    WHERE m.movie_id IN (SELECT DISTINCT movie_id FROM inserted);
END;
go

create table screening_rooms
(
    room_id     int identity,
    branch_id   int                                                not null,
    room_name   nvarchar(100) collate SQL_Latin1_General_CP1_CI_AS not null,
    total_seats int         default 0                              not null,
    status      varchar(15) default 'ACTIVE' collate SQL_Latin1_General_CP1_CI_AS,
    created_at  datetime2   default sysdatetime(),
    updated_at  datetime2   default sysdatetime(),
    primary key (room_id),
    constraint FK_rooms_branch
        foreign key (branch_id) references cinema_branches,
    constraint CK_room_status
        check ([status] = 'CLOSED' OR [status] = 'MAINTENANCE' OR [status] = 'ACTIVE'), , , ,
)
go

create index idx_rooms_branch
    on screening_rooms (branch_id)
go

create index idx_rooms_status
    on screening_rooms (status)
go

create table seat_type_surcharges
(
    surcharge_id   int identity,
    branch_id      int                                              not null,
    seat_type      varchar(10) collate SQL_Latin1_General_CP1_CI_AS not null,
    surcharge_rate decimal(5, 2) default 0                          not null,
    updated_at     datetime2     default sysdatetime(),
    primary key (surcharge_id),
    constraint UQ_surcharge_branch_type
        unique (branch_id, seat_type),
    constraint FK_surcharge_branch
        foreign key (branch_id) references cinema_branches,
    constraint CK_surcharge_rate
        check ([surcharge_rate] >= 0),
    constraint CK_surcharge_seat_type
        check ([seat_type] = 'COUPLE' OR [seat_type] = 'VIP' OR [seat_type] = 'NORMAL'), ,
)
go

create table seats
(
    seat_id     int identity,
    room_id     int         not null,
    seat_code   varchar(10) not null,
    seat_type   varchar(10) default 'NORMAL',
    row_number  varchar(5),
    seat_number int,
    status      varchar(15) default 'AVAILABLE',
    created_at  datetime2   default sysdatetime(),
    primary key (seat_id),
    constraint FK_seats_room
        foreign key (room_id) references screening_rooms,
    constraint CK_seat_status
        check ([status] = 'MAINTENANCE' OR [status] = 'BROKEN' OR [status] = 'AVAILABLE'),
    constraint CK_seat_type
        check ([seat_type] = 'COUPLE' OR [seat_type] = 'VIP' OR [seat_type] = 'NORMAL'), , ,
)
go

create index idx_seats_room
    on seats (room_id)
go

create index idx_seats_status
    on seats (status)
go

create index idx_seats_type
    on seats (seat_type)
go

create unique index ux_seat_room_code
    on seats (room_id, seat_code)
go


CREATE TRIGGER trg_update_total_seats ON seats
AFTER INSERT, DELETE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE r
    SET total_seats = (
        SELECT COUNT(*)
        FROM seats s
        WHERE s.room_id = r.room_id AND s.status != 'BROKEN'
    )
    FROM screening_rooms r
    WHERE r.room_id IN (
        SELECT room_id FROM inserted
        UNION
        SELECT room_id FROM deleted
    );
END;
go

create table showtimes
(
    showtime_id         int identity,
    movie_id            int            not null,
    room_id             int            not null,
    show_date           date           not null,
    start_time          time           not null,
    end_time            time,
    base_price          decimal(10, 2) not null,
    status              varchar(15) default 'SCHEDULED' collate SQL_Latin1_General_CP1_CI_AS,
    created_at          datetime2   default sysdatetime(),
    cancellation_reason nvarchar(500) collate SQL_Latin1_General_CP1_CI_AS,
    cancelled_at        datetime2,
    primary key (showtime_id),
    constraint FK_showtime_movie
        foreign key (movie_id) references movies,
    constraint FK_showtime_room
        foreign key (room_id) references screening_rooms,
    constraint CK_showtime_status
        check ([status] = 'CANCELLED' OR [status] = 'COMPLETED' OR [status] = 'ONGOING' OR [status] = 'SCHEDULED'), ,
)
go

create table counter_tickets
(
    ticket_id      int identity,
    showtime_id    int                                              not null,
    seat_id        int                                              not null,
    ticket_type    varchar(10) collate SQL_Latin1_General_CP1_CI_AS not null,
    seat_type      varchar(10) collate SQL_Latin1_General_CP1_CI_AS not null,
    price          decimal(10, 2)                                   not null,
    sold_by        int                                              not null,
    payment_method varchar(20) collate SQL_Latin1_General_CP1_CI_AS not null,
    customer_name  nvarchar(100) collate SQL_Latin1_General_CP1_CI_AS,
    customer_phone varchar(20) collate SQL_Latin1_General_CP1_CI_AS,
    customer_email varchar(100) collate SQL_Latin1_General_CP1_CI_AS,
    notes          nvarchar(500) collate SQL_Latin1_General_CP1_CI_AS,
    sold_at        datetime2 default sysdatetime(),
    primary key (ticket_id),
    constraint FK_counter_ticket_seat
        foreign key (seat_id) references seats,
    constraint FK_counter_ticket_showtime
        foreign key (showtime_id) references showtimes,
    constraint FK_counter_ticket_sold_by
        foreign key (sold_by) references users,
    constraint CK_counter_payment_method
        check ([payment_method] = 'BANKING' OR [payment_method] = 'CASH'),
    constraint CK_counter_seat_type
        check ([seat_type] = 'COUPLE' OR [seat_type] = 'VIP' OR [seat_type] = 'NORMAL'),
    constraint CK_counter_ticket_type
        check ([ticket_type] = 'CHILD' OR [ticket_type] = 'ADULT'),
)
go

create index idx_counter_ticket_showtime
    on counter_tickets (showtime_id)
go

create index idx_counter_ticket_seat
    on counter_tickets (seat_id)
go

create index idx_counter_ticket_sold_by
    on counter_tickets (sold_by)
go

create index idx_counter_ticket_sold_at
    on counter_tickets (sold_at)
go

create unique index ux_counter_ticket_showtime_seat
    on counter_tickets (showtime_id, seat_id)
go

CREATE TRIGGER trg_prevent_double_booking_counter
ON counter_tickets
INSTEAD OF INSERT
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        INNER JOIN online_tickets ot
            ON i.showtime_id = ot.showtime_id
           AND i.seat_id = ot.seat_id
    )
    BEGIN
        RAISERROR(N'Seat already sold online', 16, 1);
        RETURN;
    END

    INSERT INTO counter_tickets (
        showtime_id, seat_id,
        ticket_type, seat_type,
        price, sold_by, payment_method,
        customer_name, customer_phone,
        customer_email, notes, sold_at
    )
    SELECT
        showtime_id, seat_id,
        ticket_type, seat_type,
        price, sold_by, payment_method,
        customer_name, customer_phone,
        customer_email, notes, SYSDATETIME()
    FROM inserted;
END;
go

create index idx_showtime_movie
    on showtimes (movie_id)
go

create index idx_showtime_room
    on showtimes (room_id)
go

create index idx_showtime_date
    on showtimes (show_date)
go

create index idx_showtime_status
    on showtimes (status)
go

create unique index ux_showtime_unique
    on showtimes (room_id, show_date, start_time)
go

create table ticket_prices
(
    price_id       int identity,
    ticket_type    varchar(10) collate SQL_Latin1_General_CP1_CI_AS not null,
    day_type       varchar(10) collate SQL_Latin1_General_CP1_CI_AS not null,
    time_slot      varchar(10) collate SQL_Latin1_General_CP1_CI_AS not null,
    price          decimal(10, 2)                                   not null,
    effective_from date                                             not null,
    effective_to   date,
    is_active      bit       default 1,
    created_at     datetime2 default sysdatetime(),
    branch_id      int,
    primary key (price_id),
    constraint FK_ticket_prices_branch
        foreign key (branch_id) references cinema_branches,
    constraint CK_effective_dates
        check ([effective_to] IS NULL OR [effective_to] >= [effective_from]),
    constraint CK_price_day_type
        check ([day_type] = 'HOLIDAY' OR [day_type] = 'WEEKEND' OR [day_type] = 'WEEKDAY'),
    constraint CK_price_positive
        check ([price] > 0),
    constraint CK_price_ticket_type
        check ([ticket_type] = 'CHILD' OR [ticket_type] = 'ADULT'),
    constraint CK_price_time_slot
        check ([time_slot] = 'NIGHT' OR [time_slot] = 'EVENING' OR [time_slot] = 'AFTERNOON' OR
               [time_slot] = 'MORNING'), ,
)
go

create index idx_ticket_prices_dates
    on ticket_prices (effective_from, effective_to, is_active)
go

create index idx_ticket_prices_branch
    on ticket_prices (branch_id, is_active)
go

create index idx_users_role
    on users (role_id)
go

create index idx_users_status
    on users (status)
go

create index idx_users_email
    on users (email)
go

create index idx_users_created
    on users (created_at)
go


CREATE TRIGGER trg_users_updated ON users
AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE u SET updated_at = SYSDATETIME()
    FROM users u
    INNER JOIN inserted i ON u.user_id = i.user_id;
END;
go

create table vouchers
(
    voucher_id      int identity,
    voucher_name    nvarchar(100) collate SQL_Latin1_General_CP1_CI_AS not null,
    voucher_type    varchar(20) default 'LOYALTY' collate SQL_Latin1_General_CP1_CI_AS,
    voucher_code    varchar(50) collate SQL_Latin1_General_CP1_CI_AS,
    points_cost     int         default 0                              not null,
    discount_amount decimal(10, 2)                                     not null,
    max_usage_limit int         default 0,
    valid_days      int         default 30,
    is_active       bit         default 1,
    created_at      datetime2   default sysdatetime(),
    current_usage   int         default 0,
    primary key (voucher_id),
    constraint CK_voucher_type
        check ([voucher_type] = 'PUBLIC' OR [voucher_type] = 'LOYALTY'), , , , , , ,
)
go

create table user_vouchers
(
    id           int identity,
    user_id      int                                              not null,
    voucher_id   int                                              not null,
    voucher_code varchar(50) collate SQL_Latin1_General_CP1_CI_AS not null,
    status       varchar(15) default 'AVAILABLE' collate SQL_Latin1_General_CP1_CI_AS,
    redeemed_at  datetime2   default sysdatetime(),
    expires_at   datetime2                                        not null,
    used_at      datetime2,
    primary key (id),
    unique (voucher_code),
    constraint FK_uv_user
        foreign key (user_id) references users,
    constraint FK_uv_voucher
        foreign key (voucher_id) references vouchers,
    constraint CK_uv_status
        check ([status] = 'EXPIRED' OR [status] = 'USED' OR [status] = 'AVAILABLE'), ,
)
go

create table bookings
(
    booking_id           int identity,
    user_id              int                                              not null,
    showtime_id          int                                              not null,
    booking_code         varchar(20) collate SQL_Latin1_General_CP1_CI_AS not null,
    total_amount         decimal(10, 2) default 0                         not null,
    discount_amount      decimal(10, 2) default 0,
    final_amount         decimal(10, 2) default 0                         not null,
    payment_method       varchar(20) collate SQL_Latin1_General_CP1_CI_AS,
    payment_status       varchar(15)    default 'PENDING' collate SQL_Latin1_General_CP1_CI_AS,
    booking_time         datetime2      default sysdatetime(),
    payment_time         datetime2,
    status               varchar(15)    default 'PENDING' collate SQL_Latin1_General_CP1_CI_AS,
    cancellation_reason  nvarchar(500) collate SQL_Latin1_General_CP1_CI_AS,
    cancelled_at         datetime2,
    applied_voucher_code varchar(50) collate SQL_Latin1_General_CP1_CI_AS,
    primary key (booking_id),
    unique (booking_code),
    constraint FK_booking_showtime
        foreign key (showtime_id) references showtimes,
    constraint FK_booking_user
        foreign key (user_id) references users,
    constraint FK_booking_voucher
        foreign key (applied_voucher_id) references user_vouchers,
    constraint CK_booking_amounts
        check ([total_amount] >= 0 AND [discount_amount] >= 0 AND [final_amount] >= 0 AND
               [final_amount] = ([total_amount] - [discount_amount])),
    constraint CK_booking_status
        check ([status] = 'EXPIRED' OR [status] = 'CANCELLED' OR [status] = 'CONFIRMED' OR [status] = 'PENDING'),
    constraint CK_payment_method
        check ([payment_method] = 'BANKING' OR [payment_method] = 'CREDIT_CARD' OR [payment_method] = 'ZALOPAY'),
    constraint CK_payment_status
        check ([payment_status] = 'FAILED' OR [payment_status] = 'REFUNDED' OR [payment_status] = 'PAID' OR
               [payment_status] = 'PENDING'),
    constraint CK_payment_time
        check ([payment_time] IS NULL OR [payment_time] >= [booking_time]), , , , , ,
)
go

create index idx_booking_user
    on bookings (user_id)
go

create index idx_booking_showtime
    on bookings (showtime_id)
go

create index idx_booking_status
    on bookings (status)
go

create index idx_booking_payment_status
    on bookings (payment_status)
go

create index idx_booking_time
    on bookings (booking_time)
go

create unique index ux_booking_code
    on bookings (booking_code)
go

create table invoices
(
    invoice_id      int identity,
    invoice_code    varchar(30)    not null,
    invoice_date    datetime2      default sysdatetime(),
    booking_id      int,
    sale_channel    varchar(10)    not null,
    customer_name   nvarchar(255)  not null,
    customer_phone  varchar(20),
    customer_email  varchar(100),
    branch_id       int            not null,
    total_amount    decimal(10, 2) not null,
    discount_amount decimal(10, 2) default 0,
    final_amount    decimal(10, 2) not null,
    payment_method  varchar(20)    not null,
    payment_status  varchar(15)    default 'PAID',
    status          varchar(15)    default 'ACTIVE',
    created_by      int            not null,
    notes           nvarchar(500),
    created_at      datetime2      default sysdatetime(),
    updated_at      datetime2      default sysdatetime(),
    primary key (invoice_id),
    unique (invoice_code),
    constraint FK_invoice_booking
        foreign key (booking_id) references bookings,
    constraint FK_invoice_branch
        foreign key (branch_id) references cinema_branches,
    constraint FK_invoice_created_by
        foreign key (created_by) references users,
    constraint CK_invoice_amounts
        check ([total_amount] >= 0 AND [discount_amount] >= 0 AND [final_amount] >= 0 AND
               [final_amount] = ([total_amount] - [discount_amount])),
    constraint CK_invoice_payment_method
        check ([payment_method] = 'CASH' OR [payment_method] = 'BANKING' OR [payment_method] = 'ZALOPAY'),
    constraint CK_invoice_payment_status
        check ([payment_status] = 'REFUNDED' OR [payment_status] = 'UNPAID' OR [payment_status] = 'PAID'),
    constraint CK_invoice_sale_channel
        check ([sale_channel] = 'COUNTER' OR [sale_channel] = 'ONLINE'),
    constraint CK_invoice_status
        check ([status] = 'CANCELLED' OR [status] = 'ACTIVE'), , , , , ,
)
go

create index idx_invoice_code
    on invoices (invoice_code)
go

create index idx_invoice_booking
    on invoices (booking_id)
go

create index idx_invoice_date
    on invoices (invoice_date)
go

create index idx_invoice_status
    on invoices (status)
go

create index idx_invoice_branch
    on invoices (branch_id)
go

create index idx_invoice_channel
    on invoices (sale_channel)
go

create index idx_invoice_customer
    on invoices (customer_phone, customer_email)
go


-- Auto update timestamp
CREATE TRIGGER trg_invoice_updated ON invoices
AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    UPDATE i SET updated_at = SYSDATETIME()
    FROM invoices i
    INNER JOIN inserted ins ON i.invoice_id = ins.invoice_id;
END;
go

create table online_tickets
(
    ticket_id   int identity,
    booking_id  int                                              not null,
    showtime_id int                                              not null,
    seat_id     int                                              not null,
    ticket_type varchar(10) collate SQL_Latin1_General_CP1_CI_AS not null,
    seat_type   varchar(10) collate SQL_Latin1_General_CP1_CI_AS not null,
    price       decimal(10, 2)                                   not null,
    created_at  datetime2 default sysdatetime(),
    primary key (ticket_id),
    constraint FK_online_ticket_booking
        foreign key (booking_id) references bookings,
    constraint FK_online_ticket_seat
        foreign key (seat_id) references seats,
    constraint FK_online_ticket_showtime
        foreign key (showtime_id) references showtimes,
    constraint CK_online_seat_type
        check ([seat_type] = 'COUPLE' OR [seat_type] = 'VIP' OR [seat_type] = 'NORMAL'),
    constraint CK_online_ticket_type
        check ([ticket_type] = 'CHILD' OR [ticket_type] = 'ADULT'),
)
go

create table invoice_items
(
    item_id           int identity,
    invoice_id        int            not null,
    item_type         varchar(20)    not null,
    online_ticket_id  int,
    counter_ticket_id int,
    item_description  nvarchar(500)  not null,
    movie_title       nvarchar(150),
    showtime_date     date,
    showtime_time     time,
    room_name         varchar(50),
    seat_code         varchar(10),
    ticket_type       varchar(10),
    seat_type         varchar(10),
    quantity          int       default 1,
    unit_price        decimal(10, 2) not null,
    amount            decimal(10, 2) not null,
    created_at        datetime2 default sysdatetime(),
    primary key (item_id),
    constraint FK_invoice_item_counter
        foreign key (counter_ticket_id) references counter_tickets,
    constraint FK_invoice_item_invoice
        foreign key (invoice_id) references invoices
            on delete cascade,
    constraint FK_invoice_item_online
        foreign key (online_ticket_id) references online_tickets,
    constraint CK_invoice_item_amounts
        check ([quantity] > 0 AND [unit_price] >= 0 AND [amount] = [quantity] * [unit_price]),
    constraint CK_invoice_item_seat_type
        check ([seat_type] = 'COUPLE' OR [seat_type] = 'VIP' OR [seat_type] = 'NORMAL'),
    constraint CK_invoice_item_ticket_type
        check ([ticket_type] = 'CHILD' OR [ticket_type] = 'ADULT'),
    constraint CK_invoice_item_type
        check ([item_type] = 'COUNTER_TICKET' OR [item_type] = 'ONLINE_TICKET'), ,
)
go

create index idx_invoice_item_invoice
    on invoice_items (invoice_id)
go

create index idx_invoice_item_online
    on invoice_items (online_ticket_id)
go

create index idx_invoice_item_counter
    on invoice_items (counter_ticket_id)
go

create index idx_online_ticket_booking
    on online_tickets (booking_id)
go

create index idx_online_ticket_showtime
    on online_tickets (showtime_id)
go

create index idx_online_ticket_seat
    on online_tickets (seat_id)
go

create unique index ux_online_ticket_showtime_seat
    on online_tickets (showtime_id, seat_id)
go


CREATE TRIGGER trg_calculate_booking_amount ON online_tickets
AFTER INSERT AS
BEGIN
    SET NOCOUNT ON;
    UPDATE b
    SET total_amount = (
        SELECT ISNULL(SUM(price), 0)
        FROM online_tickets
        WHERE booking_id = b.booking_id
    ),
    final_amount = (
        SELECT ISNULL(SUM(price), 0)
        FROM online_tickets
        WHERE booking_id = b.booking_id
    ) - ISNULL(b.discount_amount, 0)
    FROM bookings b
    WHERE b.booking_id IN (SELECT DISTINCT booking_id FROM inserted);
END;
go

CREATE TRIGGER trg_prevent_double_booking_online
ON online_tickets
INSTEAD OF INSERT
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        INNER JOIN counter_tickets ct
            ON i.showtime_id = ct.showtime_id
           AND i.seat_id = ct.seat_id
    )
    BEGIN
        RAISERROR(N'Seat already sold at counter', 16, 1);
        RETURN;
    END

    INSERT INTO online_tickets (
        booking_id, showtime_id, seat_id,
        ticket_type, seat_type, price
    )
    SELECT
        booking_id, showtime_id, seat_id,
        ticket_type, seat_type, price
    FROM inserted;
END;
go

IF COL_LENGTH('users', 'branch_id') IS NULL
BEGIN
    ALTER TABLE users
        ADD branch_id INT NULL;

    ALTER TABLE users
        ADD CONSTRAINT FK_users_branch
            FOREIGN KEY (branch_id) REFERENCES cinema_branches(branch_id);

    CREATE INDEX idx_users_branch
        ON users(branch_id);
END;
GO




