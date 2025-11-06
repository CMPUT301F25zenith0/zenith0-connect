package com.example.connect.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.example.connect.R;
import com.example.connect.activities.MapActivity;
import com.example.connect.activities.OrganizerActivity;
import com.example.connect.activities.MessagesActivity;
import com.example.connect.activities.ProfileActivity;

public final class BottomNavHelper {
    private BottomNavHelper(){}

    public static void attach(Activity a, View root) {
        View bDash = root.findViewById(R.id.btnNavDashboard);
        View bMsg  = root.findViewById(R.id.btnNavMessage);
        View bMap  = root.findViewById(R.id.btnNavMap);
        View bProf = root.findViewById(R.id.btnNavProfile);

        if (bDash != null) bDash.setOnClickListener(v ->
                navigate(a, OrganizerActivity.class));
        if (bMsg  != null) bMsg.setOnClickListener(v ->
                navigate(a, MessagesActivity.class));
        if (bMap  != null) bMap.setOnClickListener(v ->
                navigate(a, MapActivity.class));
        if (bProf != null) bProf.setOnClickListener(v ->
                navigate(a, ProfileActivity.class));
    }

    private static void navigate(Activity a, Class<?> cls) {
        if (a.getClass() == cls) return;
        Intent i = new Intent(a, cls);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        a.startActivity(i);
    }
}
