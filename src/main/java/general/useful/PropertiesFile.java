package general.useful;

import java.util.HashMap;

public class PropertiesFile {

   private  HashMap<PropertyTypeEnum, String> properties;

    public PropertiesFile() {
        this.properties = new HashMap<>();
    }

    public HashMap<PropertyTypeEnum, String> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<PropertyTypeEnum, String> properties) {
        this.properties = properties;
    }
}
