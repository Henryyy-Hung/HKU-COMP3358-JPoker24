# Database Design

```sql
CREATE TABLE Users (
    name VARCHAR(32) NOT NULL,
    password VARCHAR(32) NOT NULL,
    is_online BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (name)
);

CREATE TABLE Games (
    id INT NOT NULL AUTO_INCREMENT,
    completion_time DECIMAL(10, 3),
    PRIMARY KEY (id)
);

CREATE TABLE Participations (
    game_id INT NOT NULL,
    user_name VARCHAR(32) NOT NULL,
    is_winner BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_name, game_id),
    FOREIGN KEY (user_name) REFERENCES Users(name),
    FOREIGN KEY (game_id) REFERENCES Games(id)
);
```

# Drop Table

```sql
DROP TABLE Participations;
DROP TABLE Games;
DROP TABLE Users;
```

# Sample Data

```sql
INSERT INTO Users (name, password) VALUES
('Alex', '1234'),
('Bella', '1234'),
('Casey', '1234'),
('Daisy', '1234'),
('Emma', '1234'),
('Fiona', '1234'),
('Grace', '1234'),
('Henry', '1234');
```

# Check Tabls

```
SELECT * FROM Users;
SELECT * FROM Games;
SELECT * FROM Participations;

SELECT * FROM Participations ORDER BY game_id ASC, is_winner DESC, user_name ASC;
```
