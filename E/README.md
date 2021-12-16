# xtcp
Run `./xtcp` with a single number port argument between 2048 and 63555. The program will listen for connections at that port,
and reverse and send back any JSON data received over the connection.

# Project Structure
```
├── Other
│   ├── custer.jar -- compiled jar to run the project
│   ├── META-INF -- stores information for building project jar
│   │   └── MANIFEST.MF
│   └── src
│       ├── main -- all main source files
│       │   └── Xtcp.scala
│       └── test
│           └── XTCPTest.scala -- all test source files
├── Tests -- test files to be run by the automatic grader 
│   ├── 1-in.json
│   ├── 1-out.json
│   ├── 2-in.json
│   ├── 2-out.json
│   ├── 3-in.json
│   └── 3-out.json
└── xtcp -- shell script to run program with
```

# Testing
Revamped tests from `xjson` and verified that the program worked on the Khoury login server.
