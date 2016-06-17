package nbcheck.srv;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;
import nbcheck.std.Utils;

/**
 * @author moroz
 */
@Singleton
public class PlanSqlClientAddressStorage implements IClientAddressStorage {

    public static final String DNS_KEY_SELECT_SQL = "dns.sql.stmt.select",
            DNS_KEY_SELECT_ALL_SQL = "dns.sql.stmt.select.all",
            DNS_KEY_UPDATE_SQL = "dns.sql.stmt.update";

    private String selectSQL, selectAllSQL, updateSQL;

    @Resource(lookup = "java:jboss/datasources/nbcheckDS")
    DataSource dataSource;

    Exception initEx = null;

    @Override
    public List<String> getClientsAddresses() throws ClientAddressStorageException {
        return getClientsAddresses(selectSQL);
    }

    public List<String> getClientsAddresses(String aSelectSQL) throws ClientAddressStorageException {

        if (initEx != null) {
            throw new ClientAddressStorageException(initEx.getMessage(), initEx);
        }

        List<String> ret = new ArrayList(150);
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(aSelectSQL);) {

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    ret.add(rs.getString(1));
                }

            } catch (SQLException ex) {
                throw new ClientAddressStorageException("ClientAddressStorageException while executing "
                        + aSelectSQL, ex);
            }
        } catch (SQLException connEx) {
            throw new ClientAddressStorageException("ClientAddressStorageException unable open jdbc connection "
                    + aSelectSQL, connEx);
        }

        return ret;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public int updateClientAddreses(Map<String, String> clientAddressToNameMap) throws ClientAddressStorageException {

        if (initEx != null) {
            throw new ClientAddressStorageException(initEx.getMessage(), initEx);
        }

        if ((clientAddressToNameMap == null) || (clientAddressToNameMap.isEmpty())) {
            return 0;
        }

        try (Connection conn = dataSource.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(updateSQL);) {

                for (String address : clientAddressToNameMap.keySet()) {
                    stmt.setString(1, clientAddressToNameMap.get(address)); //set host name
                    stmt.setString(2, address); //where clause
                    stmt.addBatch();
                }

                int[] i = stmt.executeBatch();
                conn.commit();

                int rows = 0;

                for (int k : i) {
                    rows = rows + k;
                }

                return rows;

            } catch (SQLException ex) {
                if (!conn.isClosed()) {
                    conn.rollback();
                }
                throw new ClientAddressStorageException("ClientAddressStorageException while executing "
                        + updateSQL + " :" + ex.getMessage(), ex);
            }

        } catch (SQLException connEx) {
            throw new ClientAddressStorageException("ClientAddressStorageException unable open jdbc connection "
                    + updateSQL, connEx);
        }

    }

    public PlanSqlClientAddressStorage() {
    }

    @PostConstruct
    public void init() {
        try {
            selectSQL = Utils.tryPropertyNotEmpty(DNS_KEY_SELECT_SQL);
            selectAllSQL = Utils.tryPropertyNotEmpty(DNS_KEY_SELECT_ALL_SQL);
            updateSQL = Utils.tryPropertyNotEmpty(DNS_KEY_UPDATE_SQL);
        } catch (IOException ex) {
            initEx = ex;
            Logger.getLogger(PlanSqlClientAddressStorage.class
                    .getName()).log(Level.SEVERE,
                            "could not get PlanSqlClientAddressStorage property from cfg file: {0}",
                            ex.getMessage());
        }
    }

    @Override
    public List<String> getAllClientsAddresses() throws ClientAddressStorageException {
        return getClientsAddresses(selectAllSQL);
    }

}
