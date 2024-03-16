package de.keksuccino.linguji.linguji.frontend.util.os.windows;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNull;
import java.lang.reflect.Method;

public class FXWinUtil {

    public static WinDef.HWND getNativeHandleForStage(@NotNull Stage stage) throws Exception {
        Method getPeer = Window.class.getDeclaredMethod("getPeer");
        getPeer.setAccessible(true);
        Object tkStage = getPeer.invoke(stage);
        Method getRawHandle = tkStage.getClass().getMethod("getRawHandle");
        getRawHandle.setAccessible(true);
        Pointer pointer = new Pointer((Long) getRawHandle.invoke(tkStage));
        return new WinDef.HWND(pointer);
    }

    public static void setDarkMode(@NotNull Stage stage, boolean darkMode) throws Exception {
        WinDef.HWND hwnd = FXWinUtil.getNativeHandleForStage(stage);
        Dwmapi dwmapi = Dwmapi.INSTANCE;
        WinDef.BOOLByReference darkModeRef = new WinDef.BOOLByReference(new WinDef.BOOL(darkMode));
        dwmapi.DwmSetWindowAttribute(hwnd, 20, darkModeRef, Native.getNativeSize(WinDef.BOOLByReference.class));
        forceUpdateTitleBar(stage);
    }

    private static void forceUpdateTitleBar(@NotNull Stage stage) {
        double opacity = stage.getOpacity();
        stage.setOpacity(0.5);
        stage.setOpacity(opacity);
    }

}
