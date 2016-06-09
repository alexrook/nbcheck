package nbcheck.srv.rs;

import java.io.IOException;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import nbcheck.srv.ErrorService;
import nbcheck.srv.IClientNamesStorage;
import nbcheck.std.Utils;

/**
 * REST Web Service
 *
 * @author moroz
 */
@Path("/")
public class NBCheckRS extends AbstractRS{

    public static String NETBIOS_KEY_NAMES_SQL = "netbios.sql.stmt.select.names";

    private static String dnsAndNetBIOSNames;

    @Context
    private UriInfo context;

    @Inject
    IClientNamesStorage clientNamesStorage;

    @EJB
    ErrorService errors;

    public NBCheckRS() throws IOException {
        dnsAndNetBIOSNames = Utils.tryPropertyNotEmpty(NETBIOS_KEY_NAMES_SQL);
    }

    @GET
    @Produces(value = {MediaType.APPLICATION_JSON/*, MediaType.APPLICATION_XML*/})
    public List<List<String>> getDNSandNetBIOSNamesList() throws IClientNamesStorage.ClientNamesStorageException {
        return clientNamesStorage.getQueryRows(dnsAndNetBIOSNames);
    }

}
