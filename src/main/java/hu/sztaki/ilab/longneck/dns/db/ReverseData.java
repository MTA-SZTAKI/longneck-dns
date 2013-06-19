package hu.sztaki.ilab.longneck.dns.db;


import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * Reverse DNS query answer.
 *
 * @author Bendig Loránd <lbendig@sztaki.mta.hu>
 * @author Molnár Péter <molnar.peter@sztaki.mta.hu>
 */
@Entity
public class ReverseData {
    
    /** The ip address that was resolved. */
    @PrimaryKey
    private String ipAddress;

    /** The domain name resolved. */
    private String domain;
    /** The expiration date of the resolved record. */
    private long expirationDate;

    public ReverseData() {}

    public ReverseData(String ipAddress, String domain, long expirationDate) {
        this.ipAddress = ipAddress;
        this.domain = domain;
        this.expirationDate = expirationDate;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
    }
}
