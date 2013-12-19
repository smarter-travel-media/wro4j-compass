package no.bekk.wro4j.compass;

import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.jruby.CompatVersion;
import org.jruby.RubyInstanceConfig;
import org.jruby.embed.ScriptingContainer;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.metadata.MetaDataFactory;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A utilities class that creates a the common {@link MetaDataFactory}
 */
public class MetaDataFactoryUtil {

    /**
     * The factory method used by a runtime wro solution. Given the parent and current properties creates a populated
     * {@link MetaDataFactory} for injection into {@link CompassCssPreProcessor}
     * @param parentProps a map of defined properties originating from {@link ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory}
     * @param props properties specific to the calling class
     * @param projectBaseDir {@link File} that specifies the root of all defined properties.
     * @return Populated {@link MetaDataFactory}
     */
    public MetaDataFactory createMetaDataFactory(final Map<String, Object> parentProps, Properties props, File projectBaseDir) {

        final CompassSettings compassSettings = getCompassSettings(props, projectBaseDir, null);
        return createMetaDataFactory(parentProps, compassSettings);

    }

    /**
     * The factory method used by a maven wro solution. Given the parent and current properties creates a populated
     * {@link MetaDataFactory} for injection into {@link CompassCssPreProcessor}
     * @param parentProps a map of defined properties originating from {@link ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory}
     * @param props properties specific to the calling class
     * @param projectBaseDir {@link File} that specifies the root of all defined properties.
     * @param standaloneContext {@link StandaloneContext} the context passed in from the maven pulgin.
     * @return Populated {@link MetaDataFactory}
     */
    public MetaDataFactory createMetaDataFactory(Map<String, Object> parentProps, Properties props,
                                                        File projectBaseDir, StandaloneContext standaloneContext) {

        final CompassSettings compassSettings = getCompassSettings(props, projectBaseDir, standaloneContext);
        return createMetaDataFactory(parentProps, compassSettings);

    }

    /**
     * Helper that returns a {@link MetaDataFactory}.
     * @param parentProps existing wro4j properties.
     * @param compassSettings populated settings for compass
     * @return
     */
    private MetaDataFactory createMetaDataFactory(final Map<String, Object> parentProps, final CompassSettings compassSettings) {

        return new MetaDataFactory() {

            private Map<String, Object> map = new HashMap<String, Object>();

            {
                map.putAll(parentProps);
                map.put("compassSettings", compassSettings);
            }

            @Override
            public Map<String, Object> create() {
                return Collections.unmodifiableMap(map);
            }
        };
    }

    /**
     * Given a properties file looks for Compass specific properties and returns a fully populated instance of
     * {@link CompassSettings}
     * @param props Properties file that hold compass specific configurations
     * @param projectBaseDir The file that represents the root of any other configured paths
     * @param context Standalone context (can be null)
     * @return
     */
    private CompassSettings getCompassSettings(Properties props, File projectBaseDir, @Nullable StandaloneContext context) {

        String compassBaseDir = props.getProperty("compassBaseDir");
        String gemHome = props.getProperty("gemHome", (compassBaseDir != null ? compassBaseDir + "./gems" : null));
        Map<String, String> gemVersions = getGemVersions(props);
        CompassSettings settings = getCompassSettings(projectBaseDir, compassBaseDir, gemHome);
        if (context != null) {
            settings.setStandaloneContext(context);
        }
        settings.setCompiler(createCompiler(settings.getGemHome(), gemVersions));
        return settings;
    }

    private Map<String, String> getGemVersions(Properties props) {
        Map<String, String> gemVersions = Maps.newHashMap();
        gemVersions.put("$vSass", props.getProperty("vSass", "3.2.12"));
        gemVersions.put("$vCompass" , props.getProperty("vCompass", "0.12.2"));
        gemVersions.put("$vCompassRails", props.getProperty("vCompassRails", "1.1.2"));
        gemVersions.put("$vSusy", props.getProperty("vSusy", "1.0.9"));
        return gemVersions;
    }

    /**
     * Helper to {@link #getCompassSettings(java.util.Properties, java.io.File, ro.isdc.wro.manager.factory.standalone.StandaloneContext)}
     * @param projectBaseDir
     * @param compassBaseDir
     * @param gemHome
     * @return
     */
    private CompassSettings getCompassSettings(File projectBaseDir, String compassBaseDir, String gemHome) {

        CompassSettings compassSettings = new CompassSettings();
        compassSettings.setProjectBaseDir(projectBaseDir);
        compassSettings.setGemHome(CompassFileUtils.computePath(projectBaseDir, gemHome));
        compassSettings.setCompassBaseDir(CompassFileUtils.computePath(projectBaseDir, compassBaseDir));
        return compassSettings;
    }

    /**
     * Creates the jruby compiler instance.
     * @param gemHome where jruby will look for or install its required gems.
     * @return
     */
    private CompassCompiler createCompiler(String gemHome, Map<String, String> gemVersions) {

        ScriptingContainer container = new ScriptingContainer();
        container.setCompileMode(RubyInstanceConfig.CompileMode.JIT);
        container.setCompatVersion(CompatVersion.RUBY1_9);
        container.put("$gem_home", gemHome);

        for (String key : gemVersions.keySet()) {
            container.put(key, gemVersions.get(key));
        }

        Object reciver;
        try {
            reciver = container.runScriptlet(IOUtils.toString(CompassCssPreProcessor.class.getResource("/wro4j_compass.rb")));
        } catch (IOException e) {
            throw new WroRuntimeException(e.getMessage(), e);
        }
        return container.getInstance(reciver, CompassCompiler.class);
    }


}
