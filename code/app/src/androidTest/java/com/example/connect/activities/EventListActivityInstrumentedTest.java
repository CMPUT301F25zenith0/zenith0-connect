package com.example.connect.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.widget.DatePicker;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.connect.R;
import com.example.connect.models.Event;
import com.example.connect.network.EventRepository;
import com.example.connect.network.EventRepositoryProvider;
import com.example.connect.testing.TestHooks;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * End-to-end UI tests that drive {@link EventListActivity} on an emulator.
 * We inject a deterministic {@link EventRepository} so that search, date, interest,
 * location, and "clear all" filters can be verified through real clicks and dialogs.
 */
@RunWith(AndroidJUnit4.class)
public class EventListActivityInstrumentedTest {

    private DateBundle techDate;
    private DateBundle musicDate;
    private DateBundle artDate;

    @Before
    public void setUp() {
        techDate = new DateBundle(1);
        musicDate = new DateBundle(2);
        artDate = new DateBundle(6);

        TestHooks.setUiTestMode(true);

        EventRepositoryProvider.setRepositoryForTesting(
                new ImmediateEventRepository(Arrays.asList(
                        createEvent("tech", "Tech Mixer", techDate.isoDate, "Campus Center",
                                "Technology", Arrays.asList("tech", "hardware")),
                        createEvent("music", "Music Gala", musicDate.isoDate, "Downtown Hall",
                                "Entertainment", Arrays.asList("music", "festival")),
                        createEvent("art", "Art Walk", artDate.isoDate, "Riverside Park",
                                "Art", Arrays.asList("art", "outdoors"))
                ))
        );
    }

    @After
    public void tearDown() {
        EventRepositoryProvider.reset();
        TestHooks.setUiTestMode(false);
    }

    @Test
    public void searchTextFiltersBothListsImmediately() {
        try (ActivityScenario<EventListActivity> scenario = ActivityScenario.launch(EventListActivity.class)) {
            waitForEventsLoaded(scenario);
            onView(withId(R.id.etSearchHeader))
                    .perform(replaceText("Tech"), closeSoftKeyboard());

            assertVisibleEvents(scenario, "Tech Mixer");
            assertPopularEvents(scenario, "Tech Mixer");
        }
    }

    @Test
    public void dateChipFiltersAndLongPressClears() {
        try (ActivityScenario<EventListActivity> scenario = ActivityScenario.launch(EventListActivity.class)) {
            waitForEventsLoaded(scenario);
            selectDate(musicDate);

            assertVisibleEvents(scenario, "Music Gala");
            assertPopularEvents(scenario, "Music Gala");

            onView(withId(R.id.chip_date)).perform(longClick());
            assertVisibleEvents(scenario, "Tech Mixer", "Music Gala", "Art Walk");
            assertPopularEvents(scenario, "Tech Mixer", "Music Gala");
        }
    }



    @Test
    public void locationChipFiltersByFullMatch() {
        try (ActivityScenario<EventListActivity> scenario = ActivityScenario.launch(EventListActivity.class)) {
            waitForEventsLoaded(scenario);
            applyLocationFilter("Downtown Hall");

            assertVisibleEvents(scenario, "Music Gala");
            assertPopularEvents(scenario, "Music Gala");
        }
    }

    @Test
    public void clearFiltersChipResetsSearchAndChips() {
        try (ActivityScenario<EventListActivity> scenario = ActivityScenario.launch(EventListActivity.class)) {
            waitForEventsLoaded(scenario);
            onView(withId(R.id.etSearchHeader))
                    .perform(replaceText("Tech"), closeSoftKeyboard());
            applyLocationFilter("Campus");

            assertVisibleEvents(scenario, "Tech Mixer");

            onView(withId(R.id.chip_clear_filters)).perform(click());

            assertVisibleEvents(scenario, "Tech Mixer", "Music Gala", "Art Walk");
            onView(withId(R.id.etSearchHeader)).check(matches(withText("")));
            assertPopularEvents(scenario, "Tech Mixer", "Music Gala");
        }
    }

    private void assertVisibleEvents(ActivityScenario<EventListActivity> scenario, String... expectedNames) {
        scenario.onActivity(activity -> {
            ListView listView = activity.findViewById(R.id.events_ListView);
            ListAdapter adapter = listView.getAdapter();
            if (adapter instanceof HeaderViewListAdapter) {
                adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
            }

            List<String> actualNames = new ArrayList<>();
            for (int i = 0; i < adapter.getCount(); i++) {
                Event event = (Event) adapter.getItem(i);
                actualNames.add(event.getName());
            }

            MatcherAssert.assertThat(
                    "Visible events mismatch",
                    actualNames,
                    Matchers.containsInAnyOrder(expectedNames)
            );
        });
    }

    private void assertPopularEvents(ActivityScenario<EventListActivity> scenario, String... expectedNames) {
        scenario.onActivity(activity -> {
            try {
                java.lang.reflect.Field field = EventListActivity.class.getDeclaredField("popularEventsList");
                field.setAccessible(true);
                @SuppressWarnings("unchecked")
                List<Event> popular = (List<Event>) field.get(activity);

                List<String> names = new ArrayList<>();
                for (Event event : popular) {
                    names.add(event.getName());
                }

                MatcherAssert.assertThat(
                        "Popular events mismatch",
                        names,
                        Matchers.containsInAnyOrder(expectedNames)
                );
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new AssertionError("Unable to inspect popular events", e);
            }
        });
    }

    private void waitForEventsLoaded(ActivityScenario<EventListActivity> scenario) {
        long timeout = System.currentTimeMillis() + 5_000;
        while (System.currentTimeMillis() < timeout) {
            final boolean[] ready = {false};
            scenario.onActivity(activity -> {
                try {
                    java.lang.reflect.Field field = EventListActivity.class.getDeclaredField("allEventsList");
                    field.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    List<Event> all = (List<Event>) field.get(activity);
                    ready[0] = all != null && !all.isEmpty();
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new AssertionError("Unable to check all events list", e);
                }
            });
            if (ready[0]) {
                return;
            }
            android.os.SystemClock.sleep(50);
        }
        throw new AssertionError("Timed out waiting for events to load");
    }

    private void selectDate(DateBundle bundle) {
        onView(withId(R.id.chip_date)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName())))
                .perform(PickerActions.setDate(bundle.year, bundle.month, bundle.day));
        onView(withText(android.R.string.ok)).perform(click());
    }

    private void applyInterestFilter(String interest) {
        onView(withId(R.id.chip_interest)).perform(click());
        onView(withHint("Enter an interest (e.g., Music, Tech, Sports)"))
                .perform(replaceText(interest), closeSoftKeyboard());
        onView(withText("Apply")).perform(click());
    }

    private void applyLocationFilter(String location) {
        onView(withId(R.id.chip_location)).perform(click());
        onView(withHint("Enter a location"))
                .perform(replaceText(location), closeSoftKeyboard());
        onView(withText("Apply")).perform(click());
    }

    private Event createEvent(String id, String name, String date, String location,
                              String category, List<String> labels) {
        Event event = new Event();
        event.setEventId(id);
        event.setName(name);
        event.setDateTime(date);
        event.setLocation(location);
        event.setCategory(category);
        event.setDescription(name + " description");
        event.setLabels(new ArrayList<>(labels));
        return event;
    }

    private static class DateBundle {
        final int year;
        final int month;
        final int day;
        final String isoDate;

        DateBundle(int daysAhead) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, daysAhead);
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH) + 1;
            day = calendar.get(Calendar.DAY_OF_MONTH);
            isoDate = String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
        }
    }

    private static class ImmediateEventRepository extends EventRepository {
        private final List<Event> seed;

        ImmediateEventRepository(List<Event> seed) {
            super(null);
            this.seed = new ArrayList<>(seed);
        }

        @Override
        public void getAllEvents(EventCallback callback) {
            callback.onSuccess(new ArrayList<>(seed));
        }
    }
}

