package hu.sztaki.ilab.longneck.dns;

import hu.sztaki.ilab.longneck.dns.db.LookupResult;
import hu.sztaki.ilab.longneck.dns.db.ReverseData;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Calendar;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.xbill.DNS.DClass;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

/**
 * DNS lookup service.
 * 
 * @author Bendig Loránd <lbendig@ilab.sztaki.hu>
 * @author Molnár Péter <molnar.peter@sztaki.mta.hu>
 *
 */
public class LookupService implements InitializingBean {
    /** The log. */
    private final Logger LOG = Logger.getLogger(LookupService.class);
    /** Validity of lookup results for reverse dns in days. */
    private int reverseValidityDays;
    /** Timeout for dns queries in seconds. */
    private int queryTimeout;
    /** Configured nameservers used for lookup. */    
    private String[] nameservers;
    /** Resolver for dns queries. */
    private ExtendedResolver resolver;

    @Override
    public void afterPropertiesSet() {
        try {
            resolver = ( nameservers == null || nameservers.length == 0) ? 
                    new ExtendedResolver() : new ExtendedResolver(nameservers);
            resolver.setTimeout(queryTimeout);
        } catch (UnknownHostException ex) {
            throw new RuntimeException("Invalid nameservers configuration.", ex);
        }
    }

    /**
     * Looks up reverse dns for the specified ip address.
     * 
     * @param ipAddr The ip address that is looked up.
     * @return The reverse dns data.
     */
    public ReverseData getReverseDns(String ipAddr) {
        try {

            Name name = ReverseMap.fromAddress(ipAddr.trim());
            Record rec = Record.newRecord(name, Type.PTR, DClass.IN);

            // Send query and receive response
            Message response = resolver.send(Message.newQuery(rec));
            Record[] answers = response.getSectionArray(Section.ANSWER); // fetch authoritative answer?

            // Prepare reverse data
            ReverseData reverseData = new ReverseData();
            reverseData.setResult(LookupResult.OK);
            reverseData.setIpAddress(ipAddr);
            reverseData.setExpirationDate(Calendar.getInstance().getTimeInMillis() + 
                    reverseValidityDays * 24 * 60 * 60);

            // Parse and return answer
            if (answers.length > 0) {
                for (Record answer : answers) {
                    if (answer.getType() == Type.PTR && 
                            !answer.rdataToString().matches("^.*\\.in-addr\\.arpa\\.$")) {

                        reverseData.setDomain(
                                answer.rdataToString().replaceAll("^(.+)(?<!\\.)\\.*$", "$1"));
                        return reverseData;
                    }
                }
            } else {
                reverseData.setDomain("-");
                reverseData.setResult(LookupResult.FAIL);
                
                return reverseData;
            }
        } catch (UnknownHostException e) {
            LOG.warn("Reverse lookup failed, unknown host: " + ipAddr, e);
        } catch (SocketTimeoutException e) {
            LOG.warn("Reverse dns lookup timed out: " + ipAddr, e);
        } catch (IOException e) {
            LOG.warn("Error during dns query: " + ipAddr, e);
        }

        // Return null on failure
        return null;
    }

    public int getReverseValidityDays() {
        return reverseValidityDays;
    }

    public void setReverseValidityDays(int reverseValidityDays) {
        this.reverseValidityDays = reverseValidityDays;
    }

    public int getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public String[] getNameservers() {
        return nameservers;
    }

    public void setNameservers(String[] nameservers) {
        this.nameservers = nameservers;
    }

}