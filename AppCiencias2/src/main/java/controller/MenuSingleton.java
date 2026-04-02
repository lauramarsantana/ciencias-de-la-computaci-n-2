package controller;

public class MenuSingleton {
    private static MenuController instance;

    public static void setInstance(MenuController controller) {
        instance = controller;
    }

    public static MenuController getInstance() {
        return instance;
    }
}