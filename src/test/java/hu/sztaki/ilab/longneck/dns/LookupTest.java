package hu.sztaki.ilab.longneck.dns;

import java.util.Properties;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Peter Molnar <molnar.peter@sztaki.mta.hu>
 */
public class LookupTest {

    @Test
    public void testReverseLookup() {
        Properties runtimeProperties = new Properties();
        runtimeProperties.setProperty("dns.nameservers", "209.244.0.3,209.244.0.4");

        LookupServiceFactory factory = new LookupServiceFactory();
        factory.setRuntimeProperties(runtimeProperties);
        factory.afterPropertiesSet();

        LookupService service = factory.getLookupService();

        Assert.assertEquals("localhost", service.getReverseDns("127.0.0.1").getDomain());
        Assert.assertEquals("web.sztaki.hu", service.getReverseDns("193.6.200.73").getDomain());
    }

}
