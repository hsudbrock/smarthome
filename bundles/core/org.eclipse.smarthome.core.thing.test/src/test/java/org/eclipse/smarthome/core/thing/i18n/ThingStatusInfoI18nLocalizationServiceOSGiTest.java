/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.thing.i18n;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;

import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ManagedThingProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.magic.binding.handler.MagicColorLightHandler;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;

/**
 * OSGi tests for the {@link ThingStatusInfoI18nLocalizationServiceOSGiTest}.
 *
 * @author Thomas Höfer - Initial contribution
 * @author Henning Sudbrock - Migration from Groovy to Java
 */
public class ThingStatusInfoI18nLocalizationServiceOSGiTest extends JavaOSGiTest {

    private Thing thing;
    private ThingStatusInfoI18nLocalizationService thingStatusInfoI18nLocalizationService;
    private ManagedThingProvider managedThingProvider;
    private Locale defaultLocale;

    @Test
    public void thingStatusInfoNotChangedIfNoDescription() {
        ThingStatusInfo orgThingStatusInfo = thing.getStatusInfo();
        assertThat(orgThingStatusInfo.getDescription(), nullValue());
        ThingStatusInfo thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing,
                null);
        assertThat(thingStatusInfo, is(orgThingStatusInfo));
    }

    @Test
    public void thingStatusInfoNotChangedForNonI18nConstantDescription() {
        ThingStatusInfo expected = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "The description.");
        setThingStatusInfo(thing, expected);

        ThingStatusInfo thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing,
                null);
        assertThat(thingStatusInfo, is(expected));
    }

    @Test
    public void thingStatusInfoLocalizedForOnlineStatusWithoutArgument() {
        setThingStatusInfo(thing, new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/online"));

        ThingStatusInfo thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing,
                null);
        assertThat(thingStatusInfo,
                is(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Thing is online.")));

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.GERMAN);
        assertThat(thingStatusInfo,
                is(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Thing ist online.")));

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.ENGLISH);
        assertThat(thingStatusInfo,
                is(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Thing is online.")));
    }

    @Test
    public void thingStatusInfoLocalizedForOfflineStatusWithoutArgument() {
        setThingStatusInfo(thing,
                new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.without-param"));

        ThingStatusInfo thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing,
                null);
        assertThat(thingStatusInfo,
                is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Thing is offline.")));

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.GERMAN);
        assertThat(thingStatusInfo,
                is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Thing ist offline.")));

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.ENGLISH);
        assertThat(thingStatusInfo,
                is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Thing is offline.")));
    }

    @Test
    public void thingStatusInfoLocalizedForOfflineStatusWithOneArgument() {
        setThingStatusInfo(thing, new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/offline.with-one-param [\"5\"]"));

        ThingStatusInfo thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing,
                null);
        assertThat(thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Thing is offline because of 5 failed logins.")));

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.GERMAN);
        assertThat(thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Thing ist nach 5 fehlgeschlagenen Anmeldeversuchen offline.")));

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.ENGLISH);
        assertThat(thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Thing is offline because of 5 failed logins.")));
    }

    @Test
    public void thingStatusInfoLocalizedForOfflineStatusWithTwoArguments() {
        setThingStatusInfo(thing, new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/offline.with-two-params [\"@text/communication-problems\", \"120\"]"));

        ThingStatusInfo thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing,
                null);
        assertThat(thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Thing is offline because of communication problems. Pls try again after 120 seconds.")));

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.GERMAN);
        assertThat(thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Thing ist wegen Kommunikationsprobleme offline. Bitte versuchen Sie es in 120 Sekunden erneut.")));

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.ENGLISH);
        assertThat(thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Thing is offline because of communication problems. Pls try again after 120 seconds.")));
    }

    @Test
    public void thingStatusInfoLocalizedForMalformedArguments() {
        setThingStatusInfo(thing, new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/offline.with-two-params   [   \"@text/communication-problems\", ,      \"120\"  ]"));

        ThingStatusInfo thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing,
                null);
        assertThat(thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Thing is offline because of communication problems. Pls try again after 120 seconds.")));

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.GERMAN);
        assertThat(thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Thing ist wegen Kommunikationsprobleme offline. Bitte versuchen Sie es in 120 Sekunden erneut.")));

        thingStatusInfo = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.ENGLISH);
        assertThat(thingStatusInfo, is(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Thing is offline because of communication problems. Pls try again after 120 seconds.")));
    }

    @Test
    public void unhandledThingCanBeHandled() {
        ThingStatusInfo info = thingStatusInfoI18nLocalizationService
                .getLocalizedThingStatusInfo(ThingBuilder.create(new ThingTypeUID("xyz:abc"), "123").build(), null);
        assertThat(info, is(ThingStatusInfoBuilder.create(ThingStatus.UNINITIALIZED, ThingStatusDetail.NONE).build()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullThingIsRejected() {
        thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(null, null);
    }

    @Test
    public void translationsFromThingHandlerSuperclassBundleAreUsed() {
        setThingStatusInfo(thing, new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE,
                "@text/channel-type.magic.alert.label"));
        ThingStatusInfo info = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.GERMAN);
        assertThat(info, is(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Alarm")));
    }

    @Test
    public void noTranslationIsUsedIfNotEvenFoundInThingHandlerSuperclassBundle() {
        setThingStatusInfo(thing,
                new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/some-fake-label-for-this-test"));
        ThingStatusInfo info = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.GERMAN);
        assertThat(info, is(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE,
                "@text/some-fake-label-for-this-test")));
    }

    @Test
    public void argumentsForDescriptionAreAlsoTranslatedUsingThingHandlerSuperclassBundle() {
        setThingStatusInfo(thing, new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE,
                "@text/offline.with-two-params [@text/channel-type.magic.alert.label, 60]"));
        ThingStatusInfo info = thingStatusInfoI18nLocalizationService.getLocalizedThingStatusInfo(thing, Locale.GERMAN);
        assertThat(info, is(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE,
                "Thing ist wegen Alarm offline. Bitte versuchen Sie es in 60 Sekunden erneut.")));
    }

    @Before
    public void setup() throws IOException {
        LocaleProvider localeProvider = getService(LocaleProvider.class);
        assertThat(localeProvider, is(notNullValue()));
        defaultLocale = localeProvider.getLocale();

        setDefaultLocale(Locale.ENGLISH);

        registerVolatileStorageService();

        SimpleThingHandlerFactory simpleThingHandlerFactory = new SimpleThingHandlerFactory();
        ComponentContext componentContext = mock(ComponentContext.class);
        when(componentContext.getBundleContext()).thenReturn(bundleContext);
        simpleThingHandlerFactory.activate(componentContext);
        registerService(simpleThingHandlerFactory, ThingHandlerFactory.class.getName());

        thing = ThingBuilder.create(new ThingTypeUID("aaa:bbb"), "ccc").build();

        managedThingProvider = getService(ManagedThingProvider.class);
        assertThat(managedThingProvider, is(notNullValue()));

        managedThingProvider.add(thing);

        waitForAssert(() -> assertThat(thing.getStatus(), is(ThingStatus.ONLINE)));

        thingStatusInfoI18nLocalizationService = getService(ThingStatusInfoI18nLocalizationService.class);
        assertThat(thingStatusInfoI18nLocalizationService, is(notNullValue()));
    }

    @After
    public void tearDown() throws IOException {
        managedThingProvider.remove(thing.getUID());
        setDefaultLocale(defaultLocale);
    }

    private void setDefaultLocale(Locale locale) throws IOException {
        assertThat(locale, is(notNullValue()));

        ConfigurationAdmin configAdmin = getService(ConfigurationAdmin.class);
        assertThat(configAdmin, is(notNullValue()));

        LocaleProvider localeProvider = getService(LocaleProvider.class);
        assertThat(localeProvider, is(notNullValue()));

        Configuration config = configAdmin.getConfiguration("org.eclipse.smarthome.core.i18nprovider", null);
        assertThat(config, is(notNullValue()));

        Dictionary<String, Object> properties = config.getProperties();
        if (properties == null) {
            properties = new Hashtable<>();
        }

        properties.put("language", locale.getLanguage());
        properties.put("script", locale.getScript());
        properties.put("region", locale.getCountry());
        properties.put("variant", locale.getVariant());

        config.update(properties);

        waitForAssert(() -> assertThat(localeProvider.getLocale(), is(locale)));
    }

    @SuppressWarnings("null")
    private void setThingStatusInfo(Thing thing, ThingStatusInfo thingStatusInfo) {
        ((SimpleThingHandler) thing.getHandler()).setThingStatusInfo(thingStatusInfo);
    }

    private class SimpleThingHandlerFactory extends BaseThingHandlerFactory {

        @Override
        public void activate(ComponentContext componentContext) {
            super.activate(componentContext);
        }

        @Override
        public boolean supportsThingType(ThingTypeUID thingTypeUID) {
            return true;
        }

        @Override
        protected ThingHandler createHandler(Thing thing) {
            return new SimpleThingHandler(thing);
        }
    }

    private class SimpleThingHandler extends MagicColorLightHandler {

        SimpleThingHandler(Thing thing) {
            super(thing);
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
            // do nothing
        }

        @Override
        public void initialize() {
            updateStatus(ThingStatus.ONLINE);
        }

        public void setThingStatusInfo(ThingStatusInfo thingStatusInfo) {
            updateStatus(thingStatusInfo.getStatus(), thingStatusInfo.getStatusDetail(),
                    thingStatusInfo.getDescription());
        }
    }
}