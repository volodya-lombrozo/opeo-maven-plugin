<img alt="logo" src="https://www.objectionary.com/cactus.svg" height="100px" />

[![Maven Central](https://img.shields.io/maven-central/v/org.eolang/opeo-maven-plugin.svg)](https://maven-badges.herokuapp.com/maven-central/org.eolang/opeo-maven-plugin)
[![Javadoc](http://www.javadoc.io/badge/org.eolang/opeo-maven-plugin.svg)](http://www.javadoc.io/doc/org.eolang/opeo-maven-plugin)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE.txt)
[![Hits-of-Code](https://hitsofcode.com/github/objectionary/opeo-maven-plugin?branch=master&label=Hits-of-Code)](https://hitsofcode.com/github/objectionary/opeo-maven-plugin/view?branch=master&label=Hits-of-Code)
![Lines of code](https://sloc.xyz/github/objectionary/opeo-maven-plugin)
[![codecov](https://codecov.io/gh/objectionary/opeo-maven-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/objectionary/opeo-maven-plugin)

This Maven plugin takes XMIR generated
by [jeo-maven-plugin](https://github.com/objectionary/jeo-maven-plugin)
and tries to transform combinations of `opcode` objects into EO objects.

Input:

```
seq > @
  opcode > NEW-187-29
    "org/eolang/ineo/A"
  opcode > DUP-89-30
  opcode > BIPUSH-16-31
    42
  opcode > INVOKESPECIAL-183-32
    "org/eolang/ineo/A"
    "<init>"
    "(I)V"
```

Output:

```
+alias org.eolang.ineo.jA

jA 42 > x
x.init
```

Then, it can translate declarative EO code back to imperative EO code which
uses `opcode` atoms.

## How to Use

The opeo-maven-plugin comprises two goals: `decompile` and `compile`. The
decompile goal processes the output of
the `jeo-maven-plugin`, converting it into high-level EO code. On the other
hand, the compile goal processes the
high-level EO code, transforming it back into low-level EO code that is
compatible with the `jeo-maven-plugin`.
Below, you will find examples demonstrating how to execute the plugin.

### Direct Command

To execute the `opeo-maven-plugin`, you need a minimum of **Maven 3.1.+** and *
*Java 8+** installed on your system.
You can initiate the plugin from the command line with a single command. For
instance, to transform the output generated
by the `jeo-maven-plugin` into high-level EO code, you can utilize the following
command:

```shell
mvn opeo:decompile
```

Subsequently, you can convert the high-level EO code back to low-level EO code,
which is also compatible with the
`jeo-maven-plugin`. To accomplish this, use the following command:

```shell
mvn opeo:compile
```

### Maven Build

Another way to use the plugin is to add it directly to your `pom.xml` file:

```xml

<plugin>
  <groupId>org.eolang</groupId>
  <artifactId>opeo-maven-plugin</artifactId>
  <version></version>
  <executions>
    <execution>
      <id>opeo-decompile</id>
      <goals>
        <goal>decompile</goal>
      </goals>
    </execution>
    <execution>
      <id>opeo-compile</id>
      <goals>
        <goal>compile</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

The default phase for the both goals is `process-classes`.

More details about plugin usage you can find in our
[Maven site](https://objectionary.github.io/opeo-maven-plugin).

## How to Contribute

Fork repository, make changes, then send us
a [pull request](https://www.yegor256.com/2014/04/15/github-guidelines.html).
We will review your changes and apply them to the `master` branch shortly,
provided they don't violate our quality standards. To avoid frustration,
before sending us your pull request please run full Maven build:

```bash
$ mvn clean install -Pqulice
```

You will need [Maven 3.3+](https://maven.apache.org) and Java 8+ installed.

## Troubleshooting

If you have any questions or trouble with the plugin, please submit an issue.

If you are a developer, and you have found a bug in a decompilation/compilation
process, please try to
run [this integration test](src/test/java/it/DetectiveIT.java) to
specify the place where the bug is located.
Moreover, this test can
greatly help you to troubleshoot any problems with the plugin.