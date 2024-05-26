# Environment Set Up

This section provides a guidence on how to set up the environment to run the code.

## 1 Overview

**System**

- Linux Ubuntu 22.04

**Dependencies**

- openjdk version "11.0.22" 2024-01-16
- mysql Ver 8.0.36-0ubuntu0.22.04.1 for Linux on x86_64 ((Ubuntu))
- mysql-connector-j_8.4.0-1ubuntu22.04_all
- glassfish-6.1.0

**Assumption**

- RMI, JMS use localhost.

## 2 Set Up MySQL

## 2.1 Install MySQL

Install MySQL server in Linux terminal.

```bash
sudo apt install mysql-server
sudo service mysql status
sudo apt install mysql-client
```

## 2.2 Set Up Database & Tables

Open MySQL Console.

```bash
sudo mysql -u root -p
```

Set Up Database and Database User.

```sql
CREATE DATABASE GameDB;
CREATE USER 'gameUser'@'localhost' IDENTIFIED BY 'gamePassword';
GRANT ALL PRIVILEGES ON GameDB.* TO 'gameUser'@'localhost';
FLUSH PRIVILEGES;
```

Switch to the Game Database.

```sql
USE GameDB;
```

Set Up Tables.

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

Quit MySQL Console.

```sql
\q
```

## 3 Set Up Glassfish 6.1.0 (JMS Service)

**Important**: Glassfish 6.1.0 is very different from Glassfish 5, since it migrate the `jms` package from `javax.jms` to `jakarta.jms`. Please **DO NOT** run this application under Glassfish 5.

## 3.1 Install Glassfish 6.1.0 on Linux Ubuntu

- Follow the tutorial at [https://www.howtoforge.com/how-to-install-glassfish-on-ubuntu-22-04/](https://www.howtoforge.com/how-to-install-glassfish-on-ubuntu-22-04/) to download and set up glassfish 6.1.0.

## 3.2 Set Up Glassfish JMS Service

- Enter you glassfish admin console at [http://localhost:4848](http://localhost:4848), and login to it.
- Under side bar, navigate to `Resources -> JMS Resources -> Connection Factories`.

  ![Glassfish Connection Factory](/assets/images/glassfish_1.png)

- Click `New` Button on the right Panel to create a `JPoker24GameConnectionFactory`, the field `JNDI Name` is `jms/JPoker24GameConnectionFactory` and the `Resource Type` is `jakarta.jms.ConnectionFactory`.

  ![Glassfish Connection Factory Set Up](/assets/images/glassfish_2.png)

- Under side bar, navigate to `Resources -> JMS Resources -> Destination Resources`.

  ![Glassfish Destination Resources](/assets/images/glassfish_3.png)

- Click `New` Button on the right Panel to create a `JPoker24GameQueue`, the field `JNDI Name` is `jms/JPoker24GameQueue`, the `Physical Destination Name` is `JPoker24GameQueue`, and the `Resource Type` is `jakarta.jms.Queue`.

  ![Glassfish Destination Resources](/assets/images/glassfish_4.png)

- Click `New` Button on the right Panel to create a `JPoker24GameTopic`, the field `JNDI Name` is `jms/JPoker24GameTopic`, the `Physical Destination Name` is `JPoker24GameTopic`, and the `Resource Type` is `jakarta.jms.Topic`.

  ![Glassfish Destination Resources](/assets/images/glassfish_5.png)
