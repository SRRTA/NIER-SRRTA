## SRRTA: Regression Testing Acceleration via State Reuse

SRRTA is  a state-reuse based acceleration approach for regression testing, which consists of two components,  state storage and state loading. In state storage, SRRTA collects some program states at the selected storage points; in state loading, SRRTA loads the stored program states  unaffected by regression modifications. SRRTA is implemented with ASM, a JVM byte-code manipulation framework, to collect and load program states by instrumenting the old version and the new version respectively at the byte-code level.  We next introduce how to apply SRRTA on your project and the experiment results of our work.

### Apply SRRTA

The implementation of SRRTA is placed in the "codes" directory, including GeneratorStore, GeneratorLoad and AuxiliaryFunction. When executing SRRTA, you need two steps.

#### 1 State Storage

The first step is to store the  program states when testing the old version by executing the instrumentation program **GeneratorStore**. 

```sh
javac classUnderTest.java 
javac GeneratorStore.java
java GeneratorStore classUnderTest.class storagePoints method
```

When executing  **GeneratorStore**, you  first need to compile the class(old version) you want to test. And then compile and execute **GeneratorStore**. The parameter **classUnderTest.class** is the byte-code got by compiling the class you want to test, **storagePoints** is the line numbers of the storage points selected(in the format of "line1,line2,line3"), and the **method** is the name of the method you want to test.

#### 2 State Loading

The second step is to load the program states  when testing the new version by executing the instrumentation program **GeneratorLoad**. 

```shell 
javac classUnderTest.java 
javac GeneratorLoad.java
java GeneratorLoad classUnderTest.class storagePoints method load_false
java GeneratorLoad classUnderTest.class storagePoints method load_true
```

When executing  **GeneratorLoad**,  as GeneratorStore, you first need to compile the class(new version) you want to test. And then compile and execute **GeneratorLoad**. GeneratorLoad needs to be executed two times. The first three parameters are the same as GeneratorStore and the last parameter represents the the time of instrumentation, "load_false" represents the first execution and only stores the unread parameters between storage point and loading point, and doesn't instrument; "load_true" represents the second execution and instruments.

### Experiment Results

We evaluate SRRTA on a real open-source project [Apache Common Math](https://commons.apache.org/proper/commons-math/), based [one](https://github.com/apache/commons-math/tree/bbfe7e4ea526e39ba0a79f0258200bc0d898f0de) of whose snapshots we construct 15 pairs of test cases and source code under test. To eliminate randomness in time collection, we repeat all the experiments 10 times and adopt the average as the final results. In the paper, we only presents the average  of various storage strategies on 15 tests. Here, we will also present the complete result on each test.

#### 1 Mean Result

| Strategy | Original Time | Online Time |   Reduced Time | Collection Time | Instrumentation Time | Occupied Space | #Success |
| -------: | ------------: | ----------: | -------------: | --------------: | -------------------: | -------------: | -------: |
|      all |      13,713.0 |     2,379.1 | 11,333.8/80.3% |   3,461.6/29.1% |              1,469.2 |       97,593.8 |    15/15 |
|     loop |               |     2,755.6 | 12,765.1/78.0% |   3,923.7/31.1% |              1,538.3 |      114,100.9 |    10/10 |
|   branch |               |     6,800.9 |  5,107.9/48.8% |      549.5/5.9% |                933.6 |            5.3 |     5/10 |
|   method |               |    12,774.7 |   -865.9/-6.6% |   1,606.1/13.8% |                685.7 |       32,235.4 |     0/10 |
|   random |               |     8,563.8 |  5,149.2/29.0% |   2,540.1/20.3% |              1,056.8 |       74,156.0 |     5/15 |

#### 2 All Result

|   Test | Strategy | Original Time | Online Time |    Reduced Time | Collection Time | Instrumentation Time | Occupied Space |
| -----: | -------: | ------------: | ----------: | --------------: | --------------: | -------------------: | -------------: |
|  test1 |      for |       5,263.4 |     2,611.5 |   2,651.9/50.4% |   3,576.3/67.9% |                998.1 |        114,285 |
|        |       if |               |     5,366.8 |    -103.4/-2.0% |      168.1/3.2% |                629.9 |              5 |
|        | function |               |     6,760.3 | -1,496.9/-28.4% |   1,880.1/35.7% |                773.5 |         35,169 |
|        |      all |               |     3,521.2 |   1,742.2/33.1% |  5,446.2/103.5% |              1,239.3 |        149,441 |
|        |   random |               |     6,477.3 | -1,213.9/-23.1% |   3,269.5/62.1% |                601.2 |        175,819 |
|  test2 |      for |      11,740.3 |     3,971.2 |   7,769.1/66.2% |   5,512.6/47.0% |                939.0 |        203,158 |
|        |       if |               |    12,706.7 |    -966.4/-8.2% |      436.8/3.7% |                675.4 |              4 |
|        | function |               |    16,108.2 | -4,367.9/-37.2% |   2,723.4/23.2% |                951.0 |         62,513 |
|        |      all |               |     5,782.0 |   5,958.3/50.8% |   8,141.0/69.3% |              1,226.0 |        265,672 |
|        |   random |               |    13,650.9 | -1,910.6/-16.3% |   5,613.1/47.8% |                570.4 |        187,527 |
|  test3 |      for |      18,476.7 |     5,717.6 |  12,759.1/69.1% |   9,711.7/52.6% |              1,187.6 |        257,117 |
|        |       if |               |    16,973.2 |    1,503.5/8.1% |      490.9/2.7% |                653.4 |              4 |
|        | function |               |    20,363.5 | -1,886.8/-10.2% |   3,431.6/18.6% |                605.2 |         79,116 |
|        |      all |               |     6,874.9 |  11,601.8/62.8% |  11,025.8/59.7% |              1,242.8 |        336,235 |
|        |   random |               |    18,603.8 |    -127.1/-0.7% |   7,119.5/38.5% |                596.1 |        237,336 |
|  test4 |      for |       9,095.6 |     3,616.3 |   5,479.3/60.2% |   5,160.6/56.7% |              1,128.0 |        155,007 |
|        |       if |               |     8,520.4 |      575.2/6.3% |      768.3/8.4% |                678.4 |              4 |
|        | function |               |    10,220.0 | -1,124.4/-12.4% |   1,910.2/21.0% |                645.3 |         47,864 |
|        |      all |               |     4,247.1 |   4,848.5/53.3% |   6,254.1/68.8% |              1,259.5 |        203,401 |
|        |   random |               |     9,228.9 |    -133.3/-1.5% |   4,369.1/48.0% |                542.9 |        143,580 |
|  test5 |      for |      24,024.4 |     6,050.0 |  17,974.4/74.8% |  10,193.9/42.4% |              1,133.6 |        317,423 |
|        |       if |               |    22,502.8 |    1,521.6/6.3% |      852.5/3.5% |                622.6 |              4 |
|        | function |               |    26,850.2 | -2,825.8/-11.8% |   4,334.8/18.0% |                634.6 |         97,672 |
|        |      all |               |     7,789.3 |  16,235.1/67.6% |  13,029.0/54.2% |              1,148.0 |        415,097 |
|        |   random |               |    28,842.3 | -4,817.9/-20.1% |   8,358.9/34.8% |                607.3 |        293,004 |
|  test6 |      for |       6,018.3 |           - |               - |               - |                    - |              - |
|        |       if |               |       391.4 |   5,626.9/93.5% |     737.2/12.2% |              1,205.5 |              6 |
|        | function |               |     5,603.3 |      415.0/6.9% |      376.8/6.3% |                650.4 |              4 |
|        |      all |               |       425.4 |   5,592.9/92.9% |      422.2/7.0% |              1,236.5 |              8 |
|        |   random |               |     5,646.8 |      371.5/6.2% |      329.8/5.5% |                750.1 |              4 |
|  test7 |      for |       5,929.4 |           - |               - |               - |                    - |              - |
|        |       if |               |       365.3 |   5,564.1/93.8% |     836.1/14.1% |              1,214.5 |              6 |
|        | function |               |     5,356.0 |      573.4/9.7% |      465.9/7.9% |                639.3 |              4 |
|        |      all |               |       338.4 |   5,591.0/94.3% |     741.3/12.5% |              1,091.6 |              8 |
|        |   random |               |     5,784.2 |      145.2/2.4% |     670.7/11.3% |                674.8 |              4 |
|  test8 |      for |      12,599.2 |           - |               - |               - |                    - |              - |
|        |       if |               |       396.4 |  12,202.8/96.9% |      213.4/1.7% |              1,213.0 |              7 |
|        | function |               |    11,706.5 |      892.7/7.1% |      153.7/1.2% |                646.8 |              4 |
|        |      all |               |       371.3 |  12,227.9/97.1% |      541.4/4.3% |              1,176.7 |              9 |
|        |   random |               |    11,814.1 |      785.1/6.2% |       27.3/0.2% |                677.0 |              4 |
|  test9 |      for |       8,624.2 |           - |               - |               - |                    - |              - |
|        |       if |               |       392.4 |   8,231.8/95.5% |      656.0/7.6% |              1,308.0 |              6 |
|        | function |               |     8,062.3 |      561.9/6.5% |      347.3/4.0% |                663.2 |              4 |
|        |      all |               |       382.2 |   8,242.0/95.6% |      845.0/9.8% |              1,186.5 |              8 |
|        |   random |               |     7,963.7 |      660.5/7.7% |      383.0/4.4% |                716.7 |              4 |
| test10 |      for |      17,316.2 |           - |               - |               - |                    - |              - |
|        |       if |               |       393.4 |  16,922.8/97.7% |      336.0/1.9% |              1,134.8 |              7 |
|        | function |               |    16,716.3 |      599.9/3.5% |      437.4/2.5% |                647.6 |              4 |
|        |      all |               |       366.4 |  16,949.8/97.9% |      395.9/2.3% |              1,233.9 |              9 |
|        |   random |               |    15,628.7 |    1,687.5/9.7% |      835.7/4.8% |                646.7 |              4 |
| test11 |      for |       8,985.9 |       937.4 |   8,048.5/89.6% |      812.7/9.0% |              2,063.6 |         10,380 |
|        |       if |               |           - |               - |               - |                    - |              - |
|        | function |               |           - |               - |               - |                    - |              - |
|        |      all |               |       937.4 |   8,048.5/89.6% |      812.7/9.0% |              2,063.6 |         10,380 |
|        |   random |               |       823.7 |   8,162.2/90.8% |   1,114.5/12.4% |              1,985.4 |          8,279 |
| test12 |      for |      38,046.2 |     1,634.6 |  36,411.6/95.7% |      256.7/0.7% |              2,080.3 |         40,108 |
|        |       if |               |           - |               - |               - |                    - |              - |
|        | function |               |           - |               - |               - |                    - |              - |
|        |      all |               |     1,634.6 |  36,411.6/95.7% |      256.7/0.7% |              2,080.3 |         40,108 |
|        |   random |               |     1,303.9 |  36,742.3/96.6% |    2,529.8/6.6% |              1,864.9 |         32,030 |
| test13 |      for |       9,149.7 |       953.6 |   8,196.1/89.6% |   1,598.2/17.5% |              2,080.0 |         10,340 |
|        |       if |               |           - |               - |               - |                    - |              - |
|        | function |               |           - |               - |               - |                    - |              - |
|        |      all |               |       953.6 |   8,196.1/89.6% |   1,598.2/17.5% |              2,080.0 |         10,340 |
|        |   random |               |       811.0 |   8,338.7/91.1% |      881.3/9.6% |              1,836.6 |          8,271 |
| test14 |      for |       9,144.2 |       861.0 |   8,283.2/90.6% |     965.9/10.6% |              1,833.5 |         10,382 |
|        |       if |               |           - |               - |               - |                    - |              - |
|        | function |               |           - |               - |               - |                    - |              - |
|        |      all |               |       861.0 |   8,283.2/90.6% |     965.9/10.6% |              1,833.5 |         10,382 |
|        |   random |               |       811.3 |   8,332.9/91.1% |      907.4/9.9% |              1,866.6 |          8,271 |
| test15 |      for |      21,280.6 |     1,202.4 |  20,078.2/94.3% |    1,447.9/6.8% |              1,939.3 |         22,809 |
|        |       if |               |           - |               - |               - |                    - |              - |
|        | function |               |           - |               - |               - |                    - |              - |
|        |      all |               |     1,202.4 |  20,078.2/94.3% |    1,447.9/6.8% |              1,939.3 |         22,809 |
|        |   random |               |     1,065.9 |  20,214.7/95.0% |    1,692.4/8.0% |              1,915.7 |         18,203 |