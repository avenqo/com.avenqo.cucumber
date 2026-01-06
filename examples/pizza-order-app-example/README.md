# Manual BDD Test Example Project

**Feature Viewer Plugin** and **Step Viewer Plugin** are used for BDD Test execution in a manual way.

This can be very useful during the phase of test automation.
Usually, you start with a feature file created by team's collaboration processes.

## How to run

* Run the feature example by
  ```shell
  cd examples/manual-bdd-test-example
  mvn clean test -Dskip.this.module.tests=false
  ```
