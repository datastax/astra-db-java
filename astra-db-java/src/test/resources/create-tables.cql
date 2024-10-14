CREATE TABLE test_types_tables (
    user_id uuid,
    order_id uuid,
    order_date timestamp,
    address frozen<tuple<text, text, int>>,
    age int,
    is_active boolean,
    login_attempts set<int>,
    name text,
    photo blob,
    preferences map<text, text>,
    salary decimal,
    tags list<text>,
    PRIMARY KEY (user_id, order_id, order_date)
) WITH CLUSTERING ORDER BY (order_id ASC, order_date DESC);


INSERT INTO test_types_tables (
    user_id,
    order_id,
    order_date,
    name,
    age,
    salary,
    is_active,
    tags,
    preferences,
    login_attempts,
    address,
    photo
) VALUES (
             uuid(),
             uuid(),
             toTimestamp(now()),
             'John Doe',
             30,
             75000.00,
             true,
             ['developer', 'admin'],
             {'theme': 'dark', 'language': 'en'},
             {1, 2, 3},
             ('123 Main St', 'Anytown', 12345),
             0xFFD8FFE000104A46494600010101004800480000FFDB0043
         );
INSERT INTO test_types_tables (
    user_id,
    order_id,
    order_date,
    name,
    age,
    salary,
    is_active,
    tags,
    preferences,
    login_attempts,
    address,
    photo
) VALUES (
             uuid(),
             uuid(),
             toTimestamp(now()),
             'Jane Smith',
             28,
             85000.50,
             false,
             ['manager', 'hr'],
             {'theme': 'light', 'language': 'fr'},
             {2, 4},
             ('456 Elm St', 'Othertown', 67890),
             0xFFD8FFE000104A46494600010101004800480000FFDB0044
         );
INSERT INTO test_types_tables (
    user_id,
    order_id,
    order_date,
    name,
    age,
    salary,
    is_active,
    tags,
    preferences,
    login_attempts,
    address,
    photo
) VALUES (
             uuid(),
             uuid(),
             toTimestamp(now()),
             'Bob Johnson',
             45,
             95000.75,
             true,
             ['guest'],
             {'theme': 'dark', 'language': 'es'},
             {5, 7, 9},
             ('789 Oak St', 'Sometown', 24680),
             0xFFD8FFE000104A46494600010101004800480000FFDB0045
         );
INSERT INTO test_types_tables (
    user_id,
    order_id,
    order_date,
    name,
    age,
    salary,
    is_active,
    tags,
    preferences,
    login_attempts,
    address,
    photo
) VALUES (
             123e4567-e89b-12d3-a456-426614174000, -- Fixed user_id
             223e4567-e89b-12d3-a456-426614174001, -- Fixed order_id
             '2023-01-01 12:00:00+0000',           -- Fixed order_date
             'Alice Johnson',
             29,
             88000.00,
             true,
             ['engineer', 'team lead'],
             {'theme': 'dark', 'language': 'en'},
             {1, 3, 5},
             ('10 Downing St', 'London', 123456),
             0xFFD8FFE000104A46494600010101004800480000FFDB0046
         );
SELECT * FROM test_types_tables
WHERE user_id = 123e4567-e89b-12d3-a456-426614174000
  AND order_id = 223e4567-e89b-12d3-a456-426614174001
  AND order_date = '2023-01-01 12:00:00+0000';

