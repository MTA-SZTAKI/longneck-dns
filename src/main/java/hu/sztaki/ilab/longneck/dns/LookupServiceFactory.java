package hu.sztaki.ilab.longneck.dns;

import hu.sztaki.ilab.longneck.bootstrap.PropertyUtils;
import java.util.Properties;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 *
 * @author Peter Molnar <molnar.peter@sztaki.mta.hu>
 */
public class LookupServiceFactory implements InitializingBean {

    /** runtime properties */
    private Properties runtimeProperties;

    /** query timeout threshold */
    private int queryTimeout;
    /** reverse lookup answer validity in days - expiration threshold */
    private int reverseValidityDays;
    /** list of nameservers to be used for lookup */
    private String[] nameservers;

    /** Thread-local lookup service. */
    private ThreadLocal<LookupService> localLookupService = new ThreadLocal<LookupService>() {
        @Override
        protected LookupService initialValue() {
            LookupService service = new LookupService();
            service.setQueryTimeout(queryTimeout);
            service.setReverseValidityDays(reverseValidityDays);
            service.setNameservers(nameservers);

            service.afterPropertiesSet();

            return service;
        }
    };

    @Override
    public void afterPropertiesSet() {
        queryTimeout = PropertyUtils.getIntProperty(runtimeProperties, "dns.queryTimeout", 120);
        reverseValidityDays = PropertyUtils.getIntProperty(runtimeProperties, "dns.reverseValidityDays", 5);
        nameservers = PropertyUtils.getFilteredStringList(
                runtimeProperties, "dns.nameservers", null).toArray(new String[0]);
    }

    public LookupService getLookupService() {
        return localLookupService.get();
    }

    public Properties getRuntimeProperties() {
        return runtimeProperties;
    }

    public void setRuntimeProperties(Properties runtimeProperties) {
        this.runtimeProperties = runtimeProperties;
    }
}
