# WebLogic Monitor

 WebLogic Monitor allows to monitor WebLogic internal counters like thread count, hogging threads, etc
 Work in progress

## Compilation

 We assume you have [java](http://openjdk.java.net/) and [maven](https://maven.apache.org/) installed

```console
netto@morpheus:~ $ mvn package
[INFO] Scanning for projects...
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] Building WebLogicMon 0.0.1-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ WebLogicMon ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /home/netto/Dropbox/devel/weblogic-mon/src/main/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ WebLogicMon ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 1 source file to /home/netto/Dropbox/devel/weblogic-mon/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ WebLogicMon ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /home/netto/Dropbox/devel/weblogic-mon/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ WebLogicMon ---
[INFO] Changes detected - recompiling the module!
[INFO] 
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ WebLogicMon ---
[INFO] 
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ WebLogicMon ---
[INFO] Building jar: /home/netto/Dropbox/devel/weblogic-mon/target/WebLogicMon-0.0.1-SNAPSHOT.jar
[INFO] META-INF/maven/net.sf.exdev/WebLogicMon/pom.xml already added, skipping
[INFO] META-INF/maven/net.sf.exdev/WebLogicMon/pom.properties already added, skipping
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1.337 s
[INFO] Finished at: 2018-08-18T02:30:15-03:00
[INFO] Final Memory: 16M/350M
[INFO] ------------------------------------------------------------------------
```

## Execution
```console
netto@morpheus:~ $ # export web logic classpath
netto@morpheus:~ $ export CLASSPATH=${CLASSPATH}:${WEBLOGIC_PATH}/bin/wlserver_10.3/server/lib/weblogic.jar:${WEBLOGIC_PATH}:.
netto@morpheus:~ $ java WebLogicMon.jar ${HOST} ${PORT} ${USER} ${PASSWORD}
```

## TODO
 cleanup code
 replace hardcoded mbeans by one dynamic plugin-like architecture

## License
 [MIT](https://opensource.org/licenses/MIT)

## References
 [Getting WebLogic DataSource Properties Using JMX](http://middlewaremagic.com/weblogic/?p=50)
 [Oracle Fusion Middleware Oracle WebLogic Server MBean Reference 11g Release 1 (10.3.5)](https://docs.oracle.com/cd/E24001_01/apirefs.1111/e13951/core/index.html)
 [WebLogic Server MBean Reference](http://docs.oracle.com/cd/E12840_01/wls/docs103/wlsmbeanref/core/index.html)
 [WebLogic Stuck Threads: Creating, Understanding and Dealing with them](http://www.munzandmore.com/2012/ora/weblogic-stuck-threads-howto)
