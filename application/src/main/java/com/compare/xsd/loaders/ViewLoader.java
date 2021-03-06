package com.compare.xsd.loaders;

import com.compare.xsd.managers.PrimaryWindowNotAvailableException;
import com.compare.xsd.managers.WindowNotFoundException;
import com.compare.xsd.managers.ViewManager;
import com.compare.xsd.ui.UIText;
import com.compare.xsd.views.ViewProperties;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Log4j2
@Component
public class ViewLoader {
    private static final String VIEW_DIRECTORY = "/views/";
    private static final String FONT_DIRECTORY = "/fonts/";

    private final ApplicationContext applicationContext;
    private final ViewManager viewManager;
    private final UIText uiText;

    /**
     * Intialize a new instance of {@link ViewLoader}.
     *
     * @param applicationContext Set the current application context.
     * @param viewManager        Set the view manager to store the views in.
     * @param uiText             Set the UI text manager.
     */
    public ViewLoader(ApplicationContext applicationContext, ViewManager viewManager, UIText uiText) {
        this.applicationContext = applicationContext;
        this.viewManager = viewManager;
        this.uiText = uiText;
    }

    @PostConstruct
    public void init() {
        loadFonts();
    }

    /**
     * Load the given view.
     *
     * @param view Set the view name to load.
     * @return Returns the loaded view.
     * @throws ViewNotFoundException Is thrown when the given view file couldn't be found.
     */
    public Parent load(String view) throws ViewNotFoundException {
        Assert.hasText(view, "view cannot be empty");
        FXMLLoader loader = new FXMLLoader(getClass().getResource(VIEW_DIRECTORY + view));

        loader.setControllerFactory(applicationContext::getBean);
        loader.setResources(uiText.getResourceBundle());

        try {
            return loader.load();
        } catch (IllegalStateException ex) {
            throw new ViewNotFoundException(view, ex);
        } catch (IOException ex) {
            log.error("View '" + view + "' is invalid", ex);
        }

        return null;
    }

    /**
     * Load and show the given view.
     *
     * @param view Set the view to load and show.
     */
    public void show(String view) {
        Assert.hasText(view, "view cannot be empty");
        Scene windowView = loadScene(view);
        Stage window;

        try {
            window = viewManager.getPrimaryWindow();
            window.setScene(windowView);
        } catch (WindowNotFoundException | PrimaryWindowNotAvailableException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * Show the given view in a new window.
     *
     * @param view       Set the view to load and show.
     * @param properties Set the properties of the window.
     */
    public void showWindow(String view, ViewProperties properties) {
        Assert.hasText(view, "view cannot be empty");
        Assert.notNull(properties, "properties cannot be null");
        Scene windowView = loadScene(view);
        Stage window = new Stage();

        if (properties.isMaximizeDisabled()) {
            window.setResizable(false);
        }

        window.setScene(windowView);
        window.setTitle(properties.getTitle());
        viewManager.addWindowView(window, windowView);

        if (properties.isDialog()) {
            window.showAndWait();
        } else {
            window.show();
        }
    }

    private Scene loadScene(String view) {
        Parent loadedView = load(view);
        return new Scene(loadedView);
    }

    private void loadFonts() {
        Font.loadFont(getClass().getResource(FONT_DIRECTORY + "fontawesome-webfont.ttf").toExternalForm(), 10);
    }
}
