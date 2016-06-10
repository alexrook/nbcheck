package nbcheck.srv.rs;

import java.io.IOException;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import nbcheck.srv.ErrorService;
import nbcheck.srv.IClientNamesStorage;
import nbcheck.srv.QueryRows;
import nbcheck.std.Utils;

/**
 *
 * @author moroz
 */
@Path("nbn")
public class NBCheckRS extends AbstractRS {

    public static String NETBIOS_KEY_NAMES_SQL = "netbios.sql.stmt.select.names";

    private static String dnsAndNetBIOSNames;

//    @Context
//    private UriInfo context;
    @Inject
    IClientNamesStorage clientNamesStorage;

    @EJB
    ErrorService errors;

    public NBCheckRS() throws IOException {
        dnsAndNetBIOSNames = Utils.tryPropertyNotEmpty(NETBIOS_KEY_NAMES_SQL);
    }

    @GET
    @Produces(value = {MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML})
    public QueryRows getDNSandNetBIOSNamesList() throws IClientNamesStorage.ClientNamesStorageException {
        return clientNamesStorage.getQueryRows(dnsAndNetBIOSNames,
                "DNSandNetbiosNames");
    }

}
