package cn.jason31416.planetlib;

public class InvalidConfigurationException extends RuntimeException {
    public InvalidConfigurationException(String file, String item){
        super("Configuration file: "+file+" contains an invalid item: "+item+"!");
    }
}
