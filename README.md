# actsAPICall
This is a related survey of the acts API.

## Intension
In order to easily and specifically process the required tasks (generation/scratch, extension/extend, coverage), and hope to try to output unnecessary information prompts and intermediate files in the task process, only use the Java runtime to call the ACTS Jar package Is not feasible.

The solution is to add the ACTS Jar package as a library to the project, call the required APIs through classes and objects, and copy only part of the code in the corresponding class to achieve the purpose when appropriate. In this way, we need to have a clear understanding of the tasks and specific details that ACTS can handle. This is one of the tasks of this research. In addition, how to call the API simply and without repetition is also a discovery task.

## Tasks
1. Test suite generation
---
Test case generation is one of the main functions of ACTS. According to the users of ACTS, we know that there are many optional generation configurations for ACTS, such as strength, algorithm, output format, etc. Simple and complete call and display are very important.

2. Test suite extention
---
This part is actually a test case generation task for a given "seed", that is, a tester gives a set of test cases based on domain knowledge before generation. The final test case set includes not only this part of the test cases but also the coverage Claim.

3. Coverage computation
---
CCM (Combinatorial Coverage Measurement): Calculate combined coverage for a given set of test cases.
The combined coverage is only related to the abstract model and has nothing to do with the actual system. Subsequent project arrangements may involve coverage standards commonly used in white-box testing such as statement coverage and conditional coverage. CCM may need to be evaluated in conjunction with source code coverage calculation tools such as gcov, codeCover, EclEmma, etc.
