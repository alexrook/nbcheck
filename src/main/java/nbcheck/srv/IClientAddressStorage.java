package nbcheck.srv;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author moroz
 */
public interface IClientAddressStorage {

    public class ClientAddressStorageException extends IOException {

        public ClientAddressStorageException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    List<String> getClientsAddresses() throws ClientAddressStorageException;

    List<String> getAllClientsAddresses() throws ClientAddressStorageException;

    int updateClientAddreses(Map<String, String> clientAddressToNameMap) throws ClientAddressStorageException;

}
