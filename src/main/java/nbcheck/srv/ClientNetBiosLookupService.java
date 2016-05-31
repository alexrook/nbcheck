package nbcheck.srv;

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
 * Обновляет информацию об NetBIOS-именах для ip-адресов по расписанию
 *
 */
@Stateless
public class ClientNetBiosLookupService {

    @Inject
    IClientNamesStorage clientNamesStorage;

    @EJB
    ErrorService errors;

    /**
     * По расписанию, из clientNamesStorage, берет список ip-адресов для
     * которых не указаны NetBIOS-имена, и пытается разрешить их.
     * Сохраняет разрешенные имена в clientNamesStorage
     *
     */
    @Schedule(dayOfWeek = "*", //see http://docs.oracle.com/javaee/6/tutorial/doc/bnboy.html
            hour = "3",
            persistent = false)
    public void lookupNBNames() {

        try {
            List<String> clientIPAddresses = clientNamesStorage.getClientsIPAddresses();
            int rows = clientNamesStorage.updateClientNames(lookupNetBIOS(clientIPAddresses));
            Logger.getLogger(ClientNetBiosLookupService.class
                    .getName()).log(Level.INFO, "updated {0} clients addresses", rows);
        } catch (IClientNamesStorage.ClientNamesStorageException ex) {
            String msg = "ClientNamesStorageException, check app configuration";
            errors.addError("ClientNamesStorageException",
                    ex);
            Logger.getLogger(ClientNetBiosLookupService.class.getName()).log(Level.SEVERE, msg + ": {0}", ex.getMessage());
        }

    }

    /**
     *
     * Для синхронизации с NetBIOS. По расписанию, из clientNamesStorage, берет
     * список всех ip-адресов и пытается разрешить их. Сохраняет
     * разрешенные имена в clientNamesStorage.
     *
     */
    @Schedule(dayOfWeek = "5",
            hour = "23",
            persistent = false)
    public void updateAllAddresses() {

        try {
            List<String> clientAddresses = clientNamesStorage.getAllClientsIPAddresses();
            int rows = clientNamesStorage.updateClientNames(lookupNetBIOS(clientAddresses));
            Logger.getLogger(ClientNetBiosLookupService.class
                    .getName()).log(Level.INFO, "updated {0} all clients addresses", rows);
        } catch (IClientNamesStorage.ClientNamesStorageException ex) {
            String msg = "ClientNamesStorageException, check app configuration";
            errors.addError("ClientNamesStorageException",
                    ex);
            Logger.getLogger(ClientNetBiosLookupService.class
                    .getName()).log(Level.SEVERE, msg + ": {0}", ex.getMessage());
        }

    }

    private Map<String, String> lookupNetBIOS(List<String> clientAddresses) {
        Map<String, String> updatedCliAddrs = new HashMap<>();
        for (String address : clientAddresses) {

            try {

                InetAddress ia = InetAddress.getByName(address);

                if (!address.equalsIgnoreCase(ia.getCanonicalHostName())) {
                    updatedCliAddrs.put(address, ia.getCanonicalHostName());
                }

            } catch (UnknownHostException ex) {
                String msg = "unknown host exception for address:" + address;
                errors.addError(address,
                        ex);
                Logger.getLogger(ClientNetBiosLookupService.class.getName()).log(Level.SEVERE, msg);
            }

        }

        return updatedCliAddrs;

    }

}