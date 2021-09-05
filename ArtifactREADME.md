# LooPy: Interactive Program Synthesis with Control Structures (Artifact)
Quick setup guide for evaluating the Loopy submission and understanding its components.

This document also appears as README.md in the home directory of the VM.

## Getting started:

### Required Software:
- VirtualBox client version 6.1.12, available here:
  - [Windows download link](https://download.virtualbox.org/virtualbox/6.1.12/VirtualBox-6.1.12-139181-Win.exe)
  - [OSX download link](https://download.virtualbox.org/virtualbox/6.1.12/VirtualBox-6.1.12-139181-OSX.dmg)
  - [Ubuntu download link](https://download.virtualbox.org/virtualbox/6.1.12/virtualbox-6.1_6.1.12-139181~Ubuntu~eoan_amd64.deb)
- Loopy VM, [available here](https://drive.google.com/file/d/1gWEp5Lmq2BUfagaXZ5Rvu_rPLq6Y9Tt_/view).
- We recommend at least 8 CPU cores and 16GB RAM.

### VM Login:
    username: osboxes
    password: osboxes.org

### Kicking the tires
1. Import and run VM.
2. Quick-test the empirical benchmarks:
   1. Open the "Terminal" app and, if needed, navigate to the home directory: `cd ~/`.
   2. Run `./scripts/kick_the_tires.sh`.
      This will run a limited set of benchmarks from each of the empirical benchmark sets with shorter timeouts, and will take approximately 4 minutes.
   3. If all runs succeed, you should see an output similar to:
      ```
      Compiling:      OK (18s)
      BenchmarksCSV:  OK (27s)
      NoCFBenchmarks: OK (108s)
      IterSelectionBenchmarks:        OK (50s)
      SimBenchmarksCSV:       OK (16s)
      ```
3. Quick-test the Loopy tool:
   1. Open the "Terminal" app and, if needed, navigate to the home directory: `cd ~/`.
   2. Run `./scripts/vscode.sh`.
   4. To try a simple synthesis example:
      1. Open file `0_tutorial.py` in directory `~/programming_tasks`.
      2. Type `words = ??` on line 13.
      3. Enter example into the projection box:
        ```
        ['Alan', 'Mathison', 'Turing']
        ``` 
        (Note the brackets and quotes around each word)
		
      4. Hit `Enter`.
      5. Wait for the code to update to `words = inp.split()`

## Reproducing Experiments (Step-by-step guide)

### Paper claims vs. artifact claims

List of claims in the paper _supported_ by the artifact:

1. Loopy's handling of a wide range of synthesis tasks at interactive speeds (Section 6.1)
2. The amount of specifications needed for Loopy (Section 6.2)
3. The overhead of Loopy (Section 6.3)
4. The need for simultaneous assignment (Section 6.4)

List of claims _not supported_ by the artifact:

1. The results of the user study (Section 7). Reviewers may still wish to use Loopy to try the tasks given to user study participants, as detailed below.
 
Note: Running the Loopy synthesizer in a VM will reduce the number of programs that Loopy can explore within its 7 second timeout. This may impact some of the results presented in section 6, and may cause some synthesis tasks run from VSCode to time out and return "synthesis failed".

### Section 6.1, Figure 8

From directory `~/` run `./scripts/figure8.sh`. Note that after printing the headers, the benchmark performs a dry-run to warm up the JVM. During this time, nothing will be printed, but the synthesizer _is_ running. After the warm-up, the synthesizer will print the benchmark results as it goes. 

The script will output the running times and correctness of solution of the benchmarks in a CSV format. For each benchmark we print the name of the benchmark and which benchmark set it belongs to (`suite`,`group`, and `name`), the number of variables in the task context (`variables`), execution time in msec (`time`), the number of programs seen until the result (`count`), and correctness of the result (`correct`) as `+` (success), `-` (failure), or `?` (timeout).

Figure 8 plots the `time` column for benchmarks where the `correct` column is either `+` (subfigure a) or `+`/`-` (subfigure b).

Save this data aside, it is reused in Figure 11.

### Section 6.2, Figure 9

For subfigures a and b, from directory `~/` run `./scripts/figure9.sh`.

The script will output the correctness data for each of the benchmarks in a CSV format. The column `name` is the name of the Frangel benchmark, the column `examples` is the number of examples selected, `iters` is the nubmer of iterations selected from each example, and `correct` will be `+` for a correct solution, `-` for an incorrect solution, and `?` for a timeout.

Figure 9 plots the percentage of benchmarks where the `correct` column is either `+` (subfigure a) or `+`/`-` per `examples` and `iters`.

For subfigure c, from the FrAngel tool directory `~/FrAngel` run `bash main.sh -fragments=true -angelic=true`. 

The script outputs the solutions for each of the original and modified FrAngel benchmarks along with the number of examples. The benchmark name without any number suffix is the original benchmark which most likely outputs the correct solution. Reviewers will have to manually check if the modified benchmarks (after reducing a few examples) output solutions that are close to the gold standard. Note that due to randomness in FrAngel, we ran the benchmarks 3 times and considered the best result out of the 3.

To recompile the project, run `ant frangel-jar`.

### Section 6.3, Figure 10

From directory `~/` run `./scripts/figure10.sh`. 

This will print out two CSV sets of data, one named `Single variable` and one named `Multivar`. `Single variable` is the data for subfigure a and c, and `Multivar` for subfigure b.

In the returned CSV, for each benchmark we print the name of the benchmark (`name`), the correctness, time to solution, and number of programs in the modified Loopy synthesizer (`se_correct`,`se_time`,`se_count`, resp.), and the correctness, time, and number of programs in the regular Loopy synthesizer (`loopy_correct`,`loopy_time`, and `loopy_count`, resp.). Figures 10(a) and 10(b) plot `se_time` vs. `loopy_time`, and Figure 10(c) plots `se_correct` and `loopy_correct` in the `Single variable` CSV.

### Section 6.4, Figure 11

From directory `~/` run `./scripts/figure11.sh`. 

This will print a CSV with the same columns as for Figure 8. Figure 11(a) plots the `time` column from this CSV and the CSV for Figure 8, per benchmark. Figure 11(b) plots the tally of changes in the `correct` column between this CSV and the CSV for Figure 8.

## Evaluating Loopy

### Using Loopy in VSCode

You can use Loopy inside VSCode (Figure 3 in the paper). For example, you can try the tasks from the user study (section 7)

To run Loopy, open an interactive shell and use the following commands:
```
cd ~/
./scripts/vscode.sh
```

You can try to solve the programming tasks users in our study were given. You can open their python files at
- `~/programming_tasks/1_string_compression.py`
- `~/programming_tasks/2_kth_digital_root.py`
- `~/programming_tasks/3_extract_numbers.py`

### Benchmark scripts

To run benchmarks (total ~11 hours):

`cd ~/synthesizer/`

Figure 8:
`./scripts/figure8.sh` (~3.5 hours)

Figure 9: 
`./scripts/figure9.sh` (~2 hours)

Figure 10:
`./scripts/figure10.sh` (~2 hours)

Figure 11:
`./scripts/figure11.sh` (~3.5 hours)

### Modifying the code:
The code for the synthesis engine is in the `~/synthesizer` directory. It is built with maven. 

The code for the modified version of Visual Studio Code is in `~/vscode`. 

To recompile the project's sources:
```
cd ~/
./build.sh
```

## Loopy Components
Loopy consists of 2 parts:

**synthesizer**: Synthesis logic, including benchmark calculation.
The code resides in `~/synthesizer/src/main/scala`.
Synthesis server's main function is in `~/synthesizer/src/main/scala/edu/ucsd/snippy/Snippy.scala`.
Figure 8's main function is in `~/synthesizer/src/main/scala/edu/ucsd/snippy/BenchmarksCSV.scala`.
Figure 9's main function is in `~/synthesizer/src/main/scala/edu/ucsd/snippy/IterSelectionBenchmarks.scala`.
Figure 10's main function is in `~/synthesizer/src/main/scala/edu/ucsd/snippy/NoCFBenchmarks.scala`.
Figure 11's main function is in `~/synthesizer/src/main/scala/edu/ucsd/snippy/SimBenchmarksCSV.scala`.

**vscode**: A build of VSCode with Projection Boxes and the ability to call Loopy.
The code resides in `~/vscode/`.
