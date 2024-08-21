## Setup
Before using the terminal, you should set the environment variable:
`export GCCPREFIX=/.../the/path/to/loongarch-gnu/prefix-`
## Usage
```bash
python3 term.py -t IP:PORT
```

Then you can use the following commands:
* R: display values in the user registers
* D: display data in the specified memory region
* A: load user input instructions/data to specified address
* G: run program at the specified address
* Q: quit the terminal

