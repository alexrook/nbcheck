package nbcheck.srv;

import java.util.Map;
import javax.ejb.Singleton;

/**
 * @author moroz
 */
@Singleton
public class ErrorService {

    public void addError(String name, Exception ex) {
    }

    public Map<String, Exception> getErrorMap() {
        return null;
    }
    
    public void cleanErrors(){
    }
}
