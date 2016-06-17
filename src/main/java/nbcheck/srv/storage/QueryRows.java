package nbcheck.srv.storage;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author moroz
 */
@XmlRootElement(name = "queryRows")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class QueryRows {

    @XmlRootElement(name = "row")
    public static class QueryRow {

        private ArrayList<String> data;

        public QueryRow(int rowSize) {
            data = new ArrayList<>(rowSize);
        }

        public QueryRow() {
            data = new ArrayList(150);
        }

        public ArrayList<String> getData() {
            return data;
        }

        public void setData(ArrayList<String> rowData) {
            this.data = rowData;
        }

        public void add(String v) {
            data.add(v);
        }

    }
    private String name, sql;
    private Date generationDate;
    private final int rowSize;
    private final List<QueryRow> rows;

    public QueryRows(int cap, int rowSize) {
        rows = new ArrayList<>(cap);
        this.rowSize = rowSize;
    }

    public QueryRows() {
        rows = new ArrayList<>(150);
        this.rowSize = 5;
    }

    public QueryRow createRow() {
        QueryRow ret = new QueryRow(rowSize);
        rows.add(ret);
        return ret;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Date getGenerationDate() {
        return generationDate;
    }

    public void setGenerationDate(Date generationDate) {
        this.generationDate = generationDate;
    }

    @XmlElementWrapper(name = "rows")
    @XmlElement(name="row")
    public List<QueryRow> getRows() {
        return rows;
    }

}
