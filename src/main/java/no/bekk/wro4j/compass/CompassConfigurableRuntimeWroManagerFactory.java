package no.bekk.wro4j.compass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory;
import java.io.File;
import java.util.Map;
import java.util.Properties;

public class CompassConfigurableRuntimeWroManagerFactory extends ConfigurableWroManagerFactory {
    public static final Logger LOG = LoggerFactory.getLogger(CompassConfigurableRuntimeWroManagerFactory.class);

    @Override
    protected ro.isdc.wro.config.metadata.MetaDataFactory newMetaDataFactory() {
        final ro.isdc.wro.config.metadata.MetaDataFactory parent = super.newMetaDataFactory();
        final Map<String, Object> parentProps = parent.create();
        Properties props = newConfigProperties();
        File projectBaseDir = computeProjectDir();

        return new MetaDataFactoryUtil().createMetaDataFactory(parentProps, props, projectBaseDir);
    }

    protected File computeProjectDir() {
        File candidate = new File(Context.get().getServletContext().getRealPath("./"));
        candidate = CompassFileUtils.locateMavenBaseDir(candidate);
        LOG.info("Resolved project directory to " + candidate);
        return candidate;

    }
}
