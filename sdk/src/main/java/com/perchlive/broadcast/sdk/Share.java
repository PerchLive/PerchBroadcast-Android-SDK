package com.perchlive.broadcast.sdk;

import android.content.Context;
import android.content.Intent;

/**
 * Created by dbro on 12/16/15.
 */
public class Share {

    public static Intent createShareChooserIntentWithTitleAndUrl(Context c, String title, String url){
        return Intent.createChooser(createShareIntentWithUrl(c, url), title);
    }

    public static Intent createShareIntentWithUrl(Context c, String url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        if (AndroidUtil.isLollipop()) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }

        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, c.getString(R.string.share_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        return shareIntent;
    }
}