#dataSets

For each architecture there are 4 CSV containing data collected during the execution of different microbenchmarks:

CPU: 

  Ibench suite: 3 benchmarks from this suite have been adapted in order to stress different types of instructions and perform different type of operations (floating-point multiplications and divisions, integer and square root operations).
  Stress-ng : starts a variable number of processes spinning on square roots of random numbers and cache memory stressing.
  Sysbench : calculation of prime numbers using 64-bit integers.
  Prime95: feeds the processor a consistent and verifiable workload to test the stability of the CPU and the L1/L2/L3 processor cache. 

Memory:

  Ibench memory capacity benchmark.
  Stress-ng memory: start a variable number of workers spinning on mapping files on memory (anonymous mmap command).
  Pmbw: set of assembler routines to measure the parallel memory
  
Disk:

  Ibench disk capacity benchmark.
  Stress-ng disk: starting a variable number of workers spinning on write operations followed by unlinking the file (this is, detaching the entry from the file system)
  Fio: to spawn a number of threads or processes doing a particular type of I/O actions.
  Sysbench disk: produces different types of I/O workloads.

Network:

  Iperf3: an increasing amount of data is sent or received by the server.

The validation directory contains data collected during the execution of real-workload from Cloudsuite (http://parsa.epfl.ch/cloudsuite/cloudsuite.html) and  NAS benchmarks (http://www.nas.nasa.gov/publications/npb.html).


Each row of the CSVs represents a sample captured during the execution of the benchmarks.
The 1st column contains the timestamp of the sample. 
The 2nd column contains the power consumed. 
The rest of the columns contains different performance indicators (that can vary depending on the server architecture) representing both system-level and low-level metrics for different resources (CPU, cache, main memory, disk, network).

