package hu.sztaki.ilab.longneck.dns.db;

import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

/**
 *
 * @author Peter Molnar <molnar.peter@sztaki.mta.hu>
 */
public class DnsCacheAccessor {

    /** The reverse DNS cache table. */
    public final EntityStore reverseStore;
    /** The reverse DNS primary index. */
    public final PrimaryIndex<String,ReverseData> reversePrimaryIndex;

    public DnsCacheAccessor(Environment environment, StoreConfig storeConfig) {
        reverseStore = new EntityStore(environment, "reverseStore", storeConfig);        
        reversePrimaryIndex = reverseStore.getPrimaryIndex(String.class, ReverseData.class);
    }

    public void close() {
        reverseStore.close();
    }
}
