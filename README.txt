REASON FORKED:

1)  There was a race condition in the CompassCssPreProcessor that occurred when the plugin is used as
part of a runtime solution. The issue in the original Compass plugin can be reproduced by creating a sample page that loads 2 different
wro groups on the first load. The result is that you will see the compiling message twice. While the files are
not compiled twice two compilers are created. In rare cases this causes competition for gems resulting in a null pointer
and all future compile requests fail until the server is restarted.

2)  Lock down the version # of gems installed for use in compiling to prevent inconsistencies between production and dev.

3)  We wanted to add support for susy see http://susy.oddbird.net/

HOW TO USE

- put compass config.rb in the project base directory

- Example configuration:

<plugin>
  <groupId>ro.isdc.wro4j</groupId>
  <artifactId>wro4j-maven-plugin</artifactId>
  <version>${wro4j.version}</version>
  <executions>
    <execution>
      <id>combine-all-js-and-css-code</id>
      <phase>compile</phase>
      <goals>
        <goal>run</goal>
      </goals>
      <configuration>
        <wroManagerFactory>no.bekk.wro4j.compass.CompassConfigurableWroManagerFactory</wroManagerFactory>
        <extraConfigFile>${basedir}/src/main/webapp/WEB-INF/wro.properties</extraConfigFile>
      </configuration>
    </execution>
  </executions>
  <dependencies>
    <dependency>
      <groupId>no.bekk.wro4j</groupId>
      <artifactId>wro4j-compass</artifactId>
      <version>1.6.3-SNAPSHOT</version>
    </dependency>
  </dependencies>
</plugin>

And in your wro.properties, add

preProcessors=compassCss.scss

- Specifying where gems are installed:
 -- in wro.properties, add
        gemHome=<path relative to project base directory>
- Gem versions:
    By Default this now installs only:
        Sass            3.2.12
        Compass         0.12.2
        Compass-rails   1.1.2
        Susy            1.0.9
    To customize gem versions add:
        vSass=<version>
        vCompass=<version>
        vCompassRails=<version>
        vSusy=<version>
    in wro.properties

TROUBLESHOOTING:
    NoClassDefFoundError: org/w3c/dom/ElementTraversal:
        Add xml-apis-2.10.0.jar to the class path.


