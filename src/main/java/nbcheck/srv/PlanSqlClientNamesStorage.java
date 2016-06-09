package nbcheck.srv;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            NETBIOS_KEY_SELECT_IGNORES_SQL = "netbios.sql.stmt.select.ignores",
            NETBIOS_KEY_DELETE_SQL = "netbios.sql.stmt.delete",
            NETBIOS_KEY_INSERT_SQL = "netbios.sql.stmt.insert";

    private String selectSQL, selectAllSQL, selectIgnoresSQL, deleteSQL, insertSQL;

    @Resource(lookup = "java:jboss/datasources/nbcheckDS")
    DataSource dataSource;

    Exception initEx = null;

    @Override
    public Map<Integer, String> getClientsIPAddresses() throws ClientNamesStorageException {
        return getQueryMap(selectSQL);
    }

    @Override
    public Map<Integer, String> getAllClientsIPAddresses() throws ClientNamesStorageException {
        return getQueryMap(selectAllSQL);
    }

    private Map<Integer, String> getQueryMap(String selectSQL)
            throws ClientNamesStorageException {

        if (initEx != null) {
            throw new ClientNamesStorageException(initEx.getMessage(), initEx);
        }

        Map<Integer, String> ret = new HashMap<>(150);
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(selectSQL);) {

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    ret.put(rs.getInt(1), rs.getString(2));
                }

            } catch (SQLException ex) {
                throw new ClientNamesStorageException("ClientNamesStorageException while executing "
                        + selectSQL, ex);
            }
        } catch (SQLException connEx) {
            throw new ClientNamesStorageException("ClientNamesStorageException unable open jdbc connection "
                    + selectSQL, connEx);
        }

        return ret;
    }

    public List<List<String>> getQueryRows(String selectSQL)
            throws ClientNamesStorageException {
        if (initEx != null) {
            throw new ClientNamesStorageException(initEx.getMessage(), initEx);
        }

        List<List<String>> ret = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(selectSQL);) {

                ResultSet rs = stmt.executeQuery();

                int columns = rs.getMetaData().getColumnCount();

                while (rs.next()) {

                    List<String> row = new ArrayList<>(columns+1);
                    for (int i = 1; i < columns+1; i++) {
                        String val = rs.getObject(i)!=null?rs.getObject(i).toString():"";
                        row.add(val);//columns starts from 1
                    }

                    ret.add(row);
                }

            } catch (SQLException ex) {
                throw new ClientNamesStorageException("ClientNamesStorageException while executing "
                        + selectSQL, ex);
            }
        } catch (SQLException connEx) {
            throw new ClientNamesStorageException("ClientNamesStorageException unable open jdbc connection "
                    + selectSQL, connEx);
        }

        return ret;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public int updateClientNames(Map<Integer, Set<String>> clientAddressIdToNamesMap)
            throws ClientNamesStorageException {

        if (initEx != null) {
            throw new ClientNamesStorageException(initEx.getMessage(), initEx);
        }

        if ((clientAddressIdToNamesMap == null) || (clientAddressIdToNamesMap.isEmpty())) {
            return 0;
        }

        Collection<String> ignoredNames = getIgnoredNames();

        try (Connection conn = dataSource.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement delStmt = conn.prepareStatement(deleteSQL);
                    PreparedStatement insStmt = conn.prepareStatement(insertSQL);) {

                for (int id : clientAddressIdToNamesMap.keySet()) {

                    delStmt.setInt(1, id);
                    delStmt.addBatch();

                    for (String nbName : clientAddressIdToNamesMap.get(id)) {
                        if (!isIgnoredName(nbName, ignoredNames)) {
                            insStmt.setInt(1, id);
                            insStmt.setString(2, nbName);
                            insStmt.addBatch();
                        }
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

    public Collection<String> getIgnoredNames() throws ClientNamesStorageException {

        if ((selectIgnoresSQL != null) && (!selectIgnoresSQL.isEmpty())) {
            return getQueryMap(selectIgnoresSQL).values();
        }

        return null;
    }

    private boolean isIgnoredName(String name, Collection<String> ignoredNames) {

        if ((ignoredNames != null) && (!ignoredNames.isEmpty())) {
            for (String ignoredName : ignoredNames) {
                if (name.matches(ignoredName)) {
                    return true;
                }
            }
        }

        return false;
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
            selectIgnoresSQL = Utils.getProperty(NETBIOS_KEY_SELECT_IGNORES_SQL);
        } catch (IOException ex) {
            initEx = ex;
            Logger.getLogger(PlanSqlClientNamesStorage.class
                    .getName()).log(Level.SEVERE,
                            "could not get PlanSqlClientAddressStorage property from cfg file: {0}",
                            ex.getMessage());
        }
    }

}
