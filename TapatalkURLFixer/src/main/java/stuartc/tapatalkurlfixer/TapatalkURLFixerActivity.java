package stuartc.tapatalkurlfixer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;


public class TapatalkURLFixerActivity extends Activity {

    protected static final String strBefore = "&out=";
    protected static final String strAfter = "&loc=";
    protected static final String PACKAGE_NAME = "stuartc.tapatalkurlfixer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent startingIntent = getIntent();
        String startingIntentDataString = startingIntent.getDataString();


        if (startingIntentDataString != null) {
            try {
                Log.d(PACKAGE_NAME, "Original URL: " + startingIntentDataString);


                Intent newIntent = new Intent(Intent.ACTION_VIEW);
                String embeddedURL = getLinkFromTapatalkURL(startingIntentDataString);

                if (embeddedURL == null) {
                    Log.d(PACKAGE_NAME, "No embedded URL found");

                    //no embedded URL so it must be a real link to tapatalk.com. Query the package manager
                    //and present a list of activities which can view the original URL
                    newIntent.setData(Uri.parse(startingIntentDataString));

                    List<ResolveInfo> results = getPackageManager().queryIntentActivities(newIntent, 0);

                    View itemList = getLayoutInflater().inflate(R.layout.activitylist, null);
                    LinearLayout itemListLinearLayout = (LinearLayout) itemList.findViewById(R.id.activityListLinearLayout);
                    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    for (ResolveInfo result : results) {
                        ActivityInfo info = result.activityInfo;

                        if (info != null && ! PACKAGE_NAME.equalsIgnoreCase(info.packageName)) {

                            View activityListItem = buildActivityListItem(this, info, startingIntentDataString, itemListLinearLayout);

                            itemListLinearLayout.addView(activityListItem, lp);
                        }

                    }
                    setContentView(itemList);

                } else {

                    //found an embedded URL, send out a new Intent
                    Log.d(PACKAGE_NAME, "Embedded URL: " + embeddedURL);

                    newIntent.setData(Uri.parse(embeddedURL));
                    newIntent.setFlags(0);
                    startActivity(newIntent);
                    finish();
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(PACKAGE_NAME, "UnsupporedEncodingException");
            } catch (NullPointerException e) {
                Log.e(PACKAGE_NAME, "NullPointerException oops");
                e.printStackTrace();
            }
        } else { //startingIntentDataString = null, nothing to do
            finish();
        }
    }

    protected View buildActivityListItem(Context context, final ActivityInfo info, final String DataUrl, ViewGroup root) {

        View ret = getLayoutInflater().inflate(R.layout.activitylist_item, null);

        TextView textView = (TextView) ret.findViewById(R.id.activityListItemText);
        textView.setText(info.loadLabel(getPackageManager()));

        ImageView imageView = (ImageView) ret.findViewById(R.id.activityListItemIcon);
        imageView.setImageDrawable(info.loadIcon(getPackageManager()));
        imageView.setPadding(10, 10, 10, 10);

        ret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(PACKAGE_NAME, "Activity Name: " + info.name + "\nPackage Name: " + info.packageName + "\nData Url: " + DataUrl);

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(DataUrl));
                i.setComponent(new ComponentName(info.packageName, info.name));

                startActivity(i);
                finish();
            }
        });

        return ret;
    }

    protected String getLinkFromTapatalkURL(String tapatalkURL) throws UnsupportedEncodingException {

        int indexBefore = tapatalkURL.indexOf(strBefore) + strBefore.length();
        int indexAfter = tapatalkURL.indexOf(strAfter, indexBefore);

        if (indexAfter == -1 || indexBefore == -1 || indexBefore >= indexAfter) {
            return null;
        }

        return URLDecoder.decode(tapatalkURL.substring(indexBefore, indexAfter), "UTF-8");
    }

}
