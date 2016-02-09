/*
 * Copyright 2015. Alex Zhang aka. ztc1997
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zprogrammer.tool.util;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.util.Pair;
import android.view.View;
import android.view.Window;

import com.zprogrammer.tool.R;

import java.util.ArrayList;
import java.util.Arrays;

public class TransitionUtil {
    public static void startActivity(Activity activity, Intent intent, Pair<? extends View, String>... pairs) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ArrayList<Pair<? extends View, String>> pairList = new ArrayList<>(Arrays.asList(pairs));

            View statusbar = activity.findViewById(android.R.id.statusBarBackground);
            if (statusbar != null)
                pairList.add(Pair.create(statusbar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));

            View navbar = activity.findViewById(android.R.id.navigationBarBackground);
            if (navbar != null)
                pairList.add(Pair.create(navbar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));

            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, pairList.toArray(new Pair[pairList.size()]));
            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);
        }
        activity.overridePendingTransition(R.anim.slide_up, R.anim.scale_down);
    }
}
