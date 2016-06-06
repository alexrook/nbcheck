package nbcheck.srv;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author moroz
 */
public interface IClientNamesStorage {

    public class ClientNamesStorageException extends IOException {

        public ClientNamesStorageException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    Map<Integer,String> getClientsIPAddresses() throws ClientNamesStorageException;

    Map<Integer,String> getAllClientsIPAddresses() throws ClientNamesStorageException;

    int updateClientNames(Map<Integer, List<String>> clientAddressIdToNamesMap) throws ClientNamesStorageException;

}
