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