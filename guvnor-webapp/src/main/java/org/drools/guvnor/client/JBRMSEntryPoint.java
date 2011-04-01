/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.guvnor.client;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import org.drools.guvnor.client.common.GenericCallback;
import org.drools.guvnor.client.configurations.ConfigurationsLoader;
import org.drools.guvnor.client.explorer.*;
import org.drools.guvnor.client.messages.Constants;
import org.drools.guvnor.client.resources.GuvnorResources;
import org.drools.guvnor.client.resources.RoundedCornersResource;
import org.drools.guvnor.client.rpc.ConfigurationService;
import org.drools.guvnor.client.rpc.ConfigurationServiceAsync;
import org.drools.guvnor.client.rpc.RepositoryServiceFactory;
import org.drools.guvnor.client.rpc.UserSecurityContext;
import org.drools.guvnor.client.ruleeditor.StandaloneEditorManager;

import java.util.Collection;

/**
 * This is the main launching/entry point for the JBRMS web console.
 * It essentially sets the initial layout.
 * <p/>
 * If you hadn't noticed, this is using GWT from google. Refer to GWT docs
 * if GWT is new to you (it is quite a different way of building web apps).
 */
public class JBRMSEntryPoint
        implements
        EntryPoint {

    private Constants constants = GWT.create(Constants.class);
    private PerspectivesPanel perspectivesPanel;

    public void onModuleLoad() {
        loadStyles();
        checkLogIn();
    }

    private void loadStyles() {
        GuvnorResources.INSTANCE.headerCss().ensureInjected();
        RoundedCornersResource.INSTANCE.roundCornersCss().ensureInjected();
    }

    /**
     * Check if user is logged in, if not, then show prompt.
     * If it is, then we show the app, in all its glory !
     */
    private void checkLogIn() {
        RepositoryServiceFactory.getSecurityService().getCurrentUser(new GenericCallback<UserSecurityContext>() {
            public void onSuccess(UserSecurityContext userSecurityContext) {
                String userName = userSecurityContext.getUserName();
                if (userName != null) {
                    showMain(userName);
                } else {
                    logIn();
                }
            }
        });
    }

    private void logIn() {
        final LoginWidget loginWidget = new LoginWidget();
        loginWidget.setLoggedInEvent(new Command() {
            public void execute() {
                showMain(loginWidget.getUserName());
            }
        });
        loginWidget.show();
    }

    private void showMain(final String userName) {

        Window.setStatus(constants.LoadingUserPermissions());

        loadConfigurations(userName);
    }

    private void loadConfigurations(final String userName) {
        ConfigurationsLoader.loadPreferences(new Command() {
            public void execute() {
                loadUserCapabilities(userName);
            }
        });
    }

    private void loadUserCapabilities(final String userName) {
        ConfigurationsLoader.loadUserCapabilities(new Command() {
            public void execute() {
                setUpMain(userName);
            }
        });
    }

    private void setUpMain(String userName) {
        Window.setStatus(" ");

        createMain();

        perspectivesPanel.setUserName(userName);
    }

    /**
     * Creates the main view of Guvnor.
     * The path used to invoke guvnor is used to identify the
     * view to show:
     * If the path contains "StandaloneEditor.html" then the StandaloneGuidedEditorManager is used
     * to render the view.
     * If not, the default view is shown.
     */
    private void createMain() {
        if (Window.Location.getPath().contains("StandaloneEditor.html")) {
            RootLayoutPanel.get().add(new StandaloneEditorManager().getBaseLayout());
        } else {

            ClientFactory clientFactory = GWT.create(ClientFactory.class);
            EventBus eventBus = clientFactory.getEventBus();
            PlaceController placeController = clientFactory.getPlaceController();
            Perspective defaultPlace = new AuthorPerspectivePlace();

            perspectivesPanel = new PerspectivesPanel(clientFactory.getPerspectivesPanelView(hideTitle()), placeController);

            loadPerspectives();

            // TODo: Hide the dropdown if the default one is the only one -Rikkola-

            ActivityMapper activityMapper = new GuvnorActivityMapper(clientFactory);
            ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
            activityManager.setDisplay(perspectivesPanel);

            GuvnorPlaceHistoryMapper historyMapper = GWT.create(GuvnorPlaceHistoryMapper.class);
            PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
            historyHandler.register(placeController, eventBus, defaultPlace);

            historyHandler.handleCurrentHistory();

            RootLayoutPanel.get().add(perspectivesPanel.getView());
        }
    }

    private void loadPerspectives() {
        ConfigurationServiceAsync configurationServiceAsync = GWT.create(ConfigurationService.class);

        PerspectiveLoader perspectiveLoader = new PerspectiveLoader(configurationServiceAsync);
        perspectiveLoader.loadPerspectives(new LoadPerspectives() {
            public void loadPerspectives(Collection<Perspective> perspectives) {
                for (Perspective perspective : perspectives) {
                    perspectivesPanel.addPerspective(perspective);
                }
            }
        });
    }

    private boolean hideTitle() {
        String parameter = Window.Location.getParameter("nochrome");

        if (parameter == null) {
            return true;
        } else {
            return parameter.equals("true");
        }
    }

}
