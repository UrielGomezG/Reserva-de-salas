package interfaz.reservadesalas.util;

import java.net.URL;

public class ResourceManager {
    private static final String BASE_PATH = "/interfaz/reservadesalas/";
    
    public static String getViewPath(String viewName) {
        return BASE_PATH + "Vista/" + viewName;
    }
    
    public static String getStylePath(String styleName) {
        return BASE_PATH + "CSS/" + styleName;
    }
    
    public static URL getViewResource(String viewName) {
        String path = getViewPath(viewName);
        URL url = ResourceManager.class.getResource(path);
        if (url == null) {
            url = Thread.currentThread().getContextClassLoader().getResource(path.substring(1));
        }
        if (url == null) {
            url = ResourceManager.class.getClassLoader().getResource(path.substring(1));
        }
        return url;
    }
    
    public static URL getStyleResource(String styleName) {
        String path = getStylePath(styleName);
        URL url = ResourceManager.class.getResource(path);
        if (url == null) {
            url = Thread.currentThread().getContextClassLoader().getResource(path.substring(1));
        }
        if (url == null) {
            url = ResourceManager.class.getClassLoader().getResource(path.substring(1));
        }
        return url;
    }
    
    public static String getStyleExternalForm(String styleName) {
        URL url = getStyleResource(styleName);
        if (url == null) {
            System.err.println("Warning: CSS resource not found: " + getStylePath(styleName));
            System.err.println("Tried paths: " + getStylePath(styleName) + " and " + getStylePath(styleName).substring(1));
            return null;
        }
        return url.toExternalForm();
    }
}

