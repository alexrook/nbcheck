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
public class PlanSqlClientNamesStorage implements IClientNamesStorage {

    public static final String NETBIOS_KEY_SELECT_SQL = "netbios.sql.stmt.select",
            NETBIOS_KEY_SELECT_ALL_SQL = "netbios.sql.stmt.select.all",
            NETBIOS_KEY_DELETE_SQL = "netbios.sql.stmt.delete",
            NETBIOS_KEY_INSERT_SQL = "netbios.sql.stmt.insert";

    private String selectSQL, selectAllSQL, deleteSQL, insertSQL;

    @Resource(lookup = "java:jboss/datasources/nbcheckDS")
    DataSource dataSource;

    Exception initEx = null;

    @Override
    public List<String> getClientsIPAddresses() throws ClientNamesStorageException {
        return getClientsIPAddresses(selectSQL);
    }

    @Override
    public List<String> getAllClientsIPAddresses() throws ClientNamesStorageException {
        return getClientsIPAddresses(selectAllSQL);
    }

    public List<String> getClientsIPAddresses(String aSelectSQL) throws ClientNamesStorageException {

        if (initEx != null) {
            throw new ClientNamesStorageException(initEx.getMessage(), initEx);
        }

        List<String> ret = new ArrayList(150);
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(aSelectSQL);) {

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    ret.add(rs.getString(1));
                }

            } catch (SQLException ex) {
                throw new ClientNamesStorageException("ClientNamesStorageException while executing "
                        + aSelectSQL, ex);
            }
        } catch (SQLException connEx) {
            throw new ClientNamesStorageException("ClientNamesStorageException unable open jdbc connection "
                    + aSelectSQL, connEx);
        }

        return ret;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public int updateClientNames(Map<String, List<String>> clientAddressToNamesMap) throws ClientNamesStorageException {

        if (initEx != null) {
            throw new ClientNamesStorageException(initEx.getMessage(), initEx);
        }

        if ((clientAddressToNamesMap == null) || (clientAddressToNamesMap.isEmpty())) {
            return 0;
        }

        try (Connection conn = dataSource.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement delStmt = conn.prepareStatement(deleteSQL);
                    PreparedStatement insStmt = conn.prepareStatement(insertSQL);) {

                for (String address : clientAddressToNamesMap.keySet()) {

                    delStmt.setString(1, address);
                    delStmt.addBatch();

                    for (String nbName : clientAddressToNamesMap.get(address)) {
                        insStmt.setString(1, nbName);
                        insStmt.setString(2, address);
                        insStmt.addBatch();
                    }

                }

                delStmt.executeBatch();

                int[] i = insStmt.executeBatch();

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
                throw new ClientNamesStorageException("ClientNamesStorageException while executing updateClientNames: "
                        + ex.getMessage(), ex);
            }

        } catch (SQLException connEx) {
            throw new ClientNamesStorageException("ClientNamesStorageException unable open jdbc connection ",
                    connEx);
        }

    }

    public PlanSqlClientNamesStorage() {
    }

    @PostConstruct
    public void init() {
        try {
            selectSQL = Utils.tryPropertyNotEmpty(NETBIOS_KEY_SELECT_SQL);
            selectAllSQL = Utils.tryPropertyNotEmpty(NETBIOS_KEY_SELECT_ALL_SQL);
            deleteSQL = Utils.tryPropertyNotEmpty(NETBIOS_KEY_DELETE_SQL);
            insertSQL = Utils.tryPropertyNotEmpty(NETBIOS_KEY_INSERT_SQL);
        } catch (IOException ex) {
            initEx = ex;
            Logger.getLogger(PlanSqlClientNamesStorage.class
                    .getName()).log(Level.SEVERE,
                            "could not get PlanSqlClientAddressStorage property from cfg file: {0}",
                            ex.getMessage());
        }
    }

}
