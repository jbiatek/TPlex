# TPlex

TPlex is a translation and test case generation tool suite for 
[PLEXIL](http://plexil.sourceforge.net/wiki/index.php/Main_Page), a 
planning and execution language in development by NASA. 

Originally TPlex focused on translation to Java for use in Java
PathFinder. This translation still exists, but currently development
focuses on translation to Lustre. The Lustre translation allows for
test case generation using [JKind](https://github.com/agacek/jkind/).

## Installation

A precompiled JAR file can be found on the release page:

[https://github.com/jbiatek/TPlex/releases](https://github.com/jbiatek/TPlex/releases)

It can be run like any other JAR file:

    java -jar path/to/TPlex.jar 

Otherwise, the source code is also available on GitHub. The Ant 
build script in the root directory can build a JAR file for you.

The `--help` option shows all the possible flags that can be passed
to TPlex. 

The release also includes a small shell script `tplex`. To run TPlex on the
command line, it is recommended to put `tplex` and `TPlex.jar` on your
`$PATH`. As long as they are in the same directory, `tplex` will run 
TPlex without having to specify the path to the JAR file. 

### Z3

Currently, test case generation is hard-coded to use the Z3 SMT
sovler as its back-end. Z3 is open source and can be found here:

   https://github.com/Z3Prover/z3/releases

It may also be available in your system's package manager. On the Mac,
it can be installed with Homebrew very easily:

    brew install z3

There also appear to be releases for Windows, Ubuntu, Debian, and FreeBSD.

As long as Z3 is on your `$PATH`, JKind should be able to find it. 
Make sure that 

    `which z3`

displays the path to the Z3 binary, and it should work. 


## Translation

TPlex supports translation of compiled PLEXIL plans and scripts into 
a few different langauges:

    tplex --lang lustre SomePlan.plx

This will translate `SomePlan.plx` into `SomePlan.lus`, a Lustre file. 
The `--lang` option also accepts `java`, `plexil`, and `none`.

### Lustre

The Lustre translation is being actively developed. As such, there
are a few PLEXIL language features that are not supported:

- Parameterized lookups
  - This means that `Lookup("sensor", 1)` and `Lookup("sensor", 2)` 
  will both be converted to `Lookup("sensor")`. A warning is printed
  when this occurs. 
- Assignments to array variables. These will throw an exception. 
- Commands which return values. These will throw an exception. 
- Update nodes. These will translate, but their "handles" will never
  get a response, so they will never finish.

If any of the above features are needed, file an issue in the GitHub
project. The solutions to all of these problems is known, they just
haven't been needed yet. (All but parameterized lookups should be
fairly simple to implement.)

If your plan uses PLEXIL's Library node feature, it can be translated
but the libraries have to be included statically. Pass in the
`--static-libs` option to enable static libraries, and use the
`--library-dir path/to/lib/dir` option to identify where the intended
PLEXIL library plans can be found. The translator will replace all
the Library nodes that it finds with the correct PLEXIL plan. If a
Library node cannot be replaced, an exception is thrown. 

#### Strings

Lustre does not have native strings, so an enumeration of possible
string values is used instead. 

Because of this, there are some other language features
that will probably never be supported:

- The string concatenation operator is not supported, because new 
  string values cannot be created.
- Dynamic command and lookup names are not supported.
  - For example, `Lookup(var)`, where `var` is a String variable. 
    It is also possible in PLEXIL to issue a command `var()`, 
    where `var` is a String variable. This is not possible in 
    Lustre. 

The enumeration of possible strings is extracted from the PLEXIL
plan. Any string literals in the plan will also be present in
the Lustre translation of the plan, and will be possible string
inputs when generating test cases.

If the plan does not contain any literals because the environment is 
the only one expected to generate or return strings, the 
`--lustre-generic-strings <num>` option may be of interest. It will add
generic strings like "1", "2", "3"... to the list of possible 
strings. If this is not used, the only possible strings may end
up being `UNKNOWN` and the empty string, which will severely limit
test case generation (for one, it will be impossible for strings
to not equal each other). 


### Java

The Java translation still exists and should still be usable, but is
no longer the focus of development. It generates a Java class that
performs the same actions as the original PLEXIL plan, using a set
of PLEXIL-specific classes and interfaces. The `TPlex.jar` file will
need to be on the classpath to compile or run a `JavaPlan`. 

### PLEXIL

The "PLEXIL" language option serves two purposes:

- You can reverse compile a `.plx` file into a `.ple` file. This 
  translation hasn't been extensively verified, but it does produce
  a much more readable version if the `.plx` file is all you have.
- When generating test cases with JKind, Lustre counterexamples
  are created and saved to `.csv` files or `.lus.xml` files. 
  These can be translated back to PLEXIL scripts using 
  `--lang plexil`. An example of this is shown in more detail below.

### None

This option suppresses output of any translated files. This is 
useful when you are just using TPlex to generate a report of
some sort, and not actually interested in translation. This is also
the default option, so 

    tplex --print-type-info SomePlan.plx

will just print a type report and won't create any new files.

## Test case generation

JKind is bundled with this JAR file, and can be used for test case 
generation. However, it is currently dependent on Z3 being installed.

First, make sure that your plan translates to Lustre with no errors.

    tplex --lang lustre YourPlan.plx
    
If your plan uses libraries, make sure to include both `--static-libs`
and `--library-dir path/to/your/libraries`.

Once the translation completes with no errors, add the option 
`--lustre-incremental`. TPlex will, after translating the plan, use 
JKind to search for test cases. 

When a test case is found, the Lustre inputs for the test case will
be written to `jkind-traces`. 

### Translation back to PLEXIL

When the search is complete, the Lustre test cases can be translated
into PLEXILScript files as follows:

    tplex --lang plexil --compliance-plx YourPlan.plx --sim-lustre-file YourPlan.lus --directory jkind-traces

The `--compliance-plx` option will ensure that the PLEXILScript file
that is generated produces the exact same behavior that is seen in the
Lustre translation. If it does not, translation will stop with an error.

In addition, if any library options were necessary to generate the
Lustre file they will need to be passed in again.

Since these commands are currently rather cumbersome, it might be
a good idea to write a small shell script that captures the options
that your plan needs instead of writing them every time. 

### Search goals

By default, it will attempt to generate tests that execute each PLEXIL
node at least once. To instead attempt to generate tests that cover
each possible PLEXIL transition, use `--lustre-search-goal transition`.
