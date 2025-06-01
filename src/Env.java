import java.util.HashMap;
import java.util.ArrayList;

public class Env
{
    public Env prev;
    private HashMap<String,Object> valTable;

    public Env(Env prev)
    {
        // A new scope should start with its own empty table
        // and a link to its parent environment (prev).
        this.valTable = new HashMap<String,Object>();
        this.prev = prev;
    }

    // Puts a variable/function in the current scope.
    public void Put(String name, Object value)
    {
        valTable.put(name, value);
    }

    // Gets a variable/function by searching up the scope chain.
    public Object Get(String name)
    {
        for (Env e = this; e != null; e = e.prev) {
            Object found = e.valTable.get(name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    // Checks if an identifier exists ONLY in the current scope.
    // This is crucial for detecting re-declarations.
    public boolean IsInCurrentScope(String name) {
        return valTable.containsKey(name);
    }
}

// EnvHelper can remain as it is.
class EnvHelper{
    public  String name;
    public String type;
    public String value;
    public ArrayList<String> params;

    public EnvHelper(String name, String type, String value, ArrayList<String> params){
        this.name=name;
        this.type=type;
        this.value = value;
        this.params=params;
    }
}