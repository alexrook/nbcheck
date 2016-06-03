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

    List<String> getClientsIPAddresses() throws ClientNamesStorageException;

    List<String> getAllClientsIPAddresses() throws ClientNamesStorageException;

    int updateClientNames(Map<String, List<String>> clientIPAddressToNamesMap) throws ClientNamesStorageException;

}
