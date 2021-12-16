# xhead
Run `./xhead` with a single natural number argument preceded by a `-`. 
This number is the maximum number of lines from `stdin` the program will print to `stdout`.

Example: `./xhead -5`

# Project Structure
```
├── Other
│   ├── custer.jar -- compiled program to be run through the shell script
│   └── src
│       ├── main -- all main source files
│       │   └── XHead.scala
│       └── test -- all test source files
│           └── XHeadTest.scala
├── README.md
└── xhead.sh
```

# Testing

Currently tests are run through IntelliJ with the run button.
