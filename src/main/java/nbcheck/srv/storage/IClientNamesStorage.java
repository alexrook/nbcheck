package nbcheck.srv.storage;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author moroz
 */
public interface IClientNamesStorage {

    public class ClientNamesStorageException extends IOException {

        public ClientNamesStorageException(String message, Throwable cause) {
            super(message, cause);
        }

    }
    
    

    Map<Integer, String> getClientsIPAddresses() throws ClientNamesStorageException;

    Map<Integer, String> getAllClientsIPAddresses() throws ClientNamesStorageException;

    int updateClientNames(Map<Integer, Set<String>> clientAddressIdToNamesMap)
            throws ClientNamesStorageException;

    QueryRows getQueryRows(String selectSQL,String queryName) throws ClientNamesStorageException;

}
