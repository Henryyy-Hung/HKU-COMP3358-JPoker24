# JPoker24 Game

## 1. Overview
**JPoker24** is a multiplayer card game where players use arithmetic expressions to reach the target number 24 using the values of four dealt cards. It support **multi-player**, **multi-room**, and **real-time update** of leaderboard.

This project utilizes technologies such as **RMI**, **JDBC**, **JNDI**, **JMS**, **MySQL**, and **Glassfish** to handle game logic, database interactions, and client-server communications.

## 2. Environment Set Up

Please follow this [Tutorial](/doc/setup.md) to set up the necessary environment before you start.

## 3. Quick Start

Open termials under root directory of the project.

```bash
chmod 777 ./run.sh     # Enable the start script
./run.sh compile       # Compile java class for server and client
./run.sh java server   # Run the server
./run.sh java client   # Run the client in another terminal
```

## 4. Playing the Game
1. Log in or register a new account.
2. Join a game with 2-4 users, try to form the number 24 using the four given card values and arithmetic operations (+, -, *, /).
3. The first player to correctly form 24 wins the round.

## 5. Game UI
![GUI Overview](/assets/images/gui_overview.png)

## 6. Build Jar & Run Jar

Open termials under root directory of the project.

```bash
chmod 777 ./run.sh     # Enable the start script
./run.sh build         # Build `.jar` file for server and client
./run.sh jar server    # Run the server jar
./run.sh jar client    # Run the client jar in another terminal
```
Note: You could find the `.jar` file at `/lib` directory.


## 7. Notes
- Ensure all services and the database are running before starting the game server and clients.
- The application is configured to run on localhost. Adjust the configurations if deploying on a different server or in a distributed environment.
- For detailed report, please refer to [Here](/doc/report.md).
- 都看到这了，说一句谢谢学长不过分吧，我亲爱的好学弟/学妹。`(๑*◡*๑)`
