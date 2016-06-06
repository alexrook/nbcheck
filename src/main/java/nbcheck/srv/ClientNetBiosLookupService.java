package nbcheck.srv;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import jcifs.netbios.NbtAddress;

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
     * По расписанию, из clientNamesStorage, берет список ip-адресов для которых
     * не указаны NetBIOS-имена, и пытается разрешить их. Сохраняет разрешенные
     * имена в clientNamesStorage
     *
     */
    @Schedule(dayOfWeek = "*", //see http://docs.oracle.com/javaee/6/tutorial/doc/bnboy.html
            hour = "*",
            minute = "*/3",
            second = "1",
            persistent = false)
    public void lookupNBNames() {

        Logger.getLogger(ClientNetBiosLookupService.class
                .getName()).log(Level.INFO, "updating NetBIOS addresses ...");

        try {
            Map<Integer, String> clientIPAddresses = clientNamesStorage.getClientsIPAddresses();

            int rows = clientNamesStorage.updateClientNames(lookupNetBIOS(clientIPAddresses));

            Logger.getLogger(ClientNetBiosLookupService.class
                    .getName()).log(Level.INFO, "updated {0} clients addresses", rows);

        } catch (IClientNamesStorage.ClientNamesStorageException ex) {
            String msg = "ClientNamesStorageException, check app configuration";
            errors.addError("ClientNamesStorageException",
                    ex);
            Logger.getLogger(ClientNetBiosLookupService.class.getName())
                    .log(Level.SEVERE, msg + ": {0}", ex.getMessage());
        }

    }

    /**
     *
     * Для синхронизации с NetBIOS. По расписанию, из clientNamesStorage, берет
     * список всех ip-адресов и пытается разрешить их через NetBIOS BCAST (see
     * init). Сохраняет разрешенные имена в clientNamesStorage.
     *
     */
    @Schedule(dayOfWeek = "*",
            hour = "*",
            minute = "*/5",
            second = "1",
            persistent = false)
    public void updateAllNames() {

        Logger.getLogger(ClientNetBiosLookupService.class
                .getName()).log(Level.INFO, "updating all NetBIOS names ...");

        try {
            Map<Integer, String> clientAddresses = clientNamesStorage.getAllClientsIPAddresses();

            int rows = clientNamesStorage.updateClientNames(lookupNetBIOS(clientAddresses));

            Logger.getLogger(ClientNetBiosLookupService.class
                    .getName()).log(Level.INFO, "updated {0} all clients names", rows);

        } catch (IClientNamesStorage.ClientNamesStorageException ex) {
            String msg = "ClientNamesStorageException, check app configuration";
            errors.addError("ClientNamesStorageException",
                    ex);
            Logger.getLogger(ClientNetBiosLookupService.class
                    .getName()).log(Level.SEVERE, msg + ": {0}", ex.getMessage());
        }

    }

    private Map<Integer, List<String>> lookupNetBIOS(Map<Integer, String> clientAddresses) {

        Map<Integer, List<String>> updatedCliAddrs = new HashMap<>();

        for (int id : clientAddresses.keySet()) {

            String address = clientAddresses.get(id);

            try {

                NbtAddress[] aa = NbtAddress.getAllByAddress(address);

                List<String> names = new ArrayList<>();

                for (NbtAddress na : aa) {
                    if (!address.equalsIgnoreCase(na.getHostName())) {
                        names.add(na.getHostName());//TODO: add only unique names 
                    }
                }

                updatedCliAddrs.put(id, names);

            } catch (UnknownHostException ex) {
                String msg = "unknown host exception for address:" + address;
                errors.addError(address,
                        ex);
                Logger.getLogger(ClientNetBiosLookupService.class.getName()).log(Level.SEVERE, msg);
            }

        }

        return updatedCliAddrs;

    }

    @PostConstruct
    public void init() {
        jcifs.Config.setProperty("jcifs.resolveOrder", "BCAST");
    }
}
