package no.bekk.wro4j.compass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.config.metadata.MetaDataFactory;
import ro.isdc.wro.manager.factory.standalone.StandaloneContext;
import ro.isdc.wro.maven.plugin.manager.factory.ConfigurableWroManagerFactory;

import java.io.File;
import java.util.Map;
import java.util.Properties;

public class CompassConfigurableWroManagerFactory extends ConfigurableWroManagerFactory  {

    public static final Logger LOG = LoggerFactory.getLogger(CompassConfigurableWroManagerFactory.class);

    private StandaloneContext standaloneContext;

    @Override
    public void initialize(StandaloneContext standaloneContext) {
        this.standaloneContext = standaloneContext;
        super.initialize(standaloneContext);
    }

    @Override
    protected MetaDataFactory newMetaDataFactory() {

        final MetaDataFactory parent = super.newMetaDataFactory();
        final Map<String, Object> parentProps = parent.create();
        Properties props = createProperties();

        File projectBaseDir = computeProjectDir();
        return new MetaDataFactoryUtil().createMetaDataFactory(parentProps, props, projectBaseDir, standaloneContext);
    }

    private File computeProjectDir() {
        File candidate = standaloneContext.getContextFolder();
        candidate = CompassFileUtils.locateMavenBaseDir(candidate);
        LOG.info("Resolved project directory to " + candidate);
        return candidate;
    }
}
