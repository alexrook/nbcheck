package nbcheck.srv;

import nbcheck.srv.storage.IClientAddressStorage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * @author moroz
 *
 * Обновляет информацию об dns-именах для ip-адресов по расписанию
 *
 */
@Stateless
public class ClientAddressLookupService {

    @Inject
    IClientAddressStorage clientAddressStorage;

    @EJB
    ErrorService errors;

    /**
     * По расписанию, из clientAddressStorage, берет список ip-адресов для
     * которых не указанны dns-имена, и пытается разрешить их через dns.
     * Сохраняет разрешенные имена в clientAddressStorage
     *
     */
    @Schedule(dayOfWeek = "*", //see http://docs.oracle.com/javaee/6/tutorial/doc/bnboy.html
            hour = "5",
            persistent = false)
    public void lookupAddresses() {

        try {
            List<String> clientAddresses = clientAddressStorage.getClientsAddresses();
            int rows = clientAddressStorage.updateClientAddreses(lookupDNS(clientAddresses));
            Logger.getLogger(ClientAddressLookupService.class
                    .getName()).log(Level.INFO, "updated {0} clients addresses", rows);
        } catch (IClientAddressStorage.ClientAddressStorageException ex) {
            String msg = "ClientAddressStorageException, check app configuration";
            errors.addError("ClientAddressStorageException", ex);
            Logger.getLogger(ClientAddressLookupService.class.getName()).log(Level.SEVERE, msg + ": {0}", ex.getMessage());
        }

    }

    /**
     *
     * Для синхронизации с DNS. По расписанию, из clientAddressStorage, берет
     * список всех ip-адресов и пытается разрешить их через dns. Сохраняет
     * разрешенные имена в clientAddressStorage.
     *
     */
    @Schedule(dayOfWeek = "5",
            hour = "21",
            persistent = false)
    public void updateAllAddresses() {

        try {
            List<String> clientAddresses = clientAddressStorage.getAllClientsAddresses();
            int rows = clientAddressStorage.updateClientAddreses(lookupDNS(clientAddresses));
            Logger.getLogger(ClientAddressLookupService.class
                    .getName()).log(Level.INFO, "updated {0} all clients addresses", rows);
        } catch (IClientAddressStorage.ClientAddressStorageException ex) {
            String msg = "ClientAddressStorageException, check app configuration";
            errors.addError("ClientAddressStorageException", ex);
            Logger.getLogger(ClientAddressLookupService.class.getName())
                    .log(Level.SEVERE, msg + ": {0}", ex.getMessage());
        }

    }

    private Map<String, String> lookupDNS(List<String> clientAddresses) {
        Map<String, String> updatedCliAddrs = new HashMap<>();
        for (String address : clientAddresses) {

            try {

                InetAddress ia = InetAddress.getByName(address);

                if (!address.equalsIgnoreCase(ia.getCanonicalHostName())) {
                    updatedCliAddrs.put(address, ia.getCanonicalHostName());
                }

            } catch (UnknownHostException ex) {
                String msg = "unknown host exception for address:" + address;
                errors.addError("ClientAddressStorageException", ex);
                Logger.getLogger(ClientAddressLookupService.class.getName())
                        .log(Level.FINE, msg);
            }

        }

        return updatedCliAddrs;

    }

}
