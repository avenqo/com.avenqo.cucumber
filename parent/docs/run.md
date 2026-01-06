# Run Commands

## Examples
### Manual BDD Test Example
#### Run
`mvn clean test -Dskip.this.module.tests=false`

To avoid the test interruption by necessary GUI interactions during source code build, the tests for the example project are skipped.


#### Debug
This stops before the tests of each module are executed.
`mvn -pl com.avenqo.cucumber:manual_bdd_test_example \
-am \
-Dmaven.surefire.debug \
test`
If you want to debug the example project only then first:  build all modules, and second: execute the example project

cd com.avenqo.cucumber/parent
mvn clean install

cd ../examples/manual-bdd-test-example
mvn -Dmaven.surefire.debug -Dskip.this.module.tests=false test