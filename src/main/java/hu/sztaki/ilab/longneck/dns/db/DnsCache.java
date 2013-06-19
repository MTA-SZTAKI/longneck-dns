package hu.sztaki.ilab.longneck.dns.db;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.StoreConfig;
import hu.sztaki.ilab.longneck.util.database.BerkeleyDBConfiguration;
import hu.sztaki.ilab.longneck.util.database.Configuration;
import hu.sztaki.ilab.longneck.util.database.DatabaseConfigurationException;
import hu.sztaki.ilab.longneck.util.database.DatabaseConnectionManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

/**
 * Cache for reverse DNS query results.
 *
 * This class is thread-safe.
 *
 * @author Bendig Loránd <lbendig@ilab.sztaki.hu>
 * @author Molnár Péter <molnar.peter@sztaki.mta.hu>
 */

public class DnsCache implements DisposableBean {

    private final Logger LOG = Logger.getLogger(DnsCache.class);

    /** The database environment. */
    private Environment environment;

    /** The cache accessor. */
    private DnsCacheAccessor dnsCacheAccessor;

    public DnsCache(DatabaseConnectionManager databaseConnectionManager, String connectionName) {
        // Get configuration and check correct type
        Configuration dbConf = databaseConnectionManager.getConfiguration(connectionName);
        if (! (dbConf instanceof BerkeleyDBConfiguration)) {
            throw new DatabaseConfigurationException(
                    String.format("Connection %1$s should be instance of %2$s.",
                    connectionName, BerkeleyDBConfiguration.class.getName()));
        }
        BerkeleyDBConfiguration bdbConf = (BerkeleyDBConfiguration) dbConf;

        // Get database environment
        environment = bdbConf.getEnvironment();

        // Configure store
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setAllowCreate(true);

        dnsCacheAccessor = new DnsCacheAccessor(environment, storeConfig);
    }

    @Override
    public void destroy() throws Exception {
        dnsCacheAccessor.close();
        dnsCacheAccessor = null;
    }

    public ReverseData add(ReverseData reverseData) {
        return dnsCacheAccessor.reversePrimaryIndex.put(reverseData);
    }

    public ReverseData getReverse(String ipAddress) {
        try {
            return dnsCacheAccessor.reversePrimaryIndex.get(ipAddress);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized List<ReverseData> getReverseList() {
        List<ReverseData> result = new ArrayList<ReverseData>();
        EntityCursor<ReverseData> cursor = null;

        try {
            cursor = dnsCacheAccessor.reversePrimaryIndex.entities();
            for (ReverseData ent : cursor) {
                result.add(ent);
            }

            return result;
        } finally {
            cursor.close();
        }
    }

    public void removeReverse(String ipAddress) {
        dnsCacheAccessor.reversePrimaryIndex.delete(ipAddress);
    }

    /**
     * Removes expired entries from the database.
    */
    public void removeExpiredReverse() {

        EntityCursor<ReverseData> cursor = null;
        Transaction txn = null;
        int removedEntries = 0;
        try {
            long currentDate = Calendar.getInstance().getTimeInMillis();
            txn = environment.beginTransaction(null, null);
            cursor = dnsCacheAccessor.reversePrimaryIndex.entities(txn, null);

            for (ReverseData entity = cursor.first(); entity != null;
                entity = cursor.next()) {

                if (currentDate > entity.getExpirationDate()) {
                    cursor.delete();
                    removedEntries++;
                }
            }

            txn.commit();
        } catch (DatabaseException ex) {
            LOG.error("Expired reverse dns data cleanup has failed.", ex);
            if (txn != null) txn.abort();
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
