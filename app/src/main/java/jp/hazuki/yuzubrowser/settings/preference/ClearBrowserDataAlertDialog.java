package jp.hazuki.yuzubrowser.settings.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.browser.BrowserManager;
import jp.hazuki.yuzubrowser.history.BrowserHistoryManager;
import jp.hazuki.yuzubrowser.search.SuggestProvider;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.settings.preference.common.CustomDialogPreference;
import jp.hazuki.yuzubrowser.tab.TabList;

public class ClearBrowserDataAlertDialog extends CustomDialogPreference {
    private final TabList mTabList;
    private int mSelected = 0;
    private int mArrayMax;

    public ClearBrowserDataAlertDialog(Context context) {
        this(context, null, null);
    }

    public ClearBrowserDataAlertDialog(Context context, AttributeSet attrs) {
        this(context, attrs, null);
    }

    public ClearBrowserDataAlertDialog(Context context, TabList tablist) {
        this(context, null, tablist);
    }

    private ClearBrowserDataAlertDialog(Context context, AttributeSet attrs, TabList tablist) {
        super(context, attrs);
        mTabList = tablist;
    }

    protected void runAction(int i) {
        switch (i) {
            case 0:
                BrowserManager.clearAppCacheFile(getContext().getApplicationContext());

                if (mTabList != null)
                    mTabList.clearCache(true);
                else
                    (new WebView(getContext())).clearCache(true);
                break;
            case 1:
                CookieManager.getInstance().removeAllCookies(null);
                break;
            case 2:
                WebViewDatabase.getInstance(getContext().getApplicationContext()).clearHttpAuthUsernamePassword();
                break;
            case 3:
                WebViewDatabase.getInstance(getContext()).clearFormData();
                break;
            case 4:
                BrowserManager.clearWebDatabase();
                break;
            case 5:
                BrowserManager.clearGeolocation();
                break;
            case 6:
                new BrowserHistoryManager(getContext().getApplicationContext()).deleteAll();
                break;
            case 7:
                getContext().getApplicationContext().getContentResolver().delete(SuggestProvider.URI_LOCAL, null, null);
                break;
        }
    }

    @Override
    public void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        mSelected = AppData.clear_data_default.get();

        final Context context = getContext();

        String[] arrays = context.getResources().getStringArray(R.array.clear_browser_data);

        final ListView listView = new ListView(context);
        listView.setAdapter(new ArrayAdapter<>(context.getApplicationContext(), R.layout.select_dialog_multichoice, arrays));
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mArrayMax = arrays.length;
        for (int i = 0; i < mArrayMax; ++i) {
            int shifted = 1 << i;
            listView.setItemChecked(i, ((mSelected & shifted) == shifted));
        }
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listView.isItemChecked(position))
                    mSelected |= 1 << position;
                else
                    mSelected &= ~(1 << position);
            }
        });

        builder
                .setTitle(R.string.pref_clear_browser_data)
            /*.setMultiChoiceItems(arrays, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					
				}})*/
                .setView(listView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onClickPositiveButton();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
        ;
    }

    protected void onClickPositiveButton() {
        for (int i = 0; i < mArrayMax; ++i) {
            int shifted = 1 << i;
            if ((mSelected & shifted) == shifted)
                runAction(i);
        }

        AppData.clear_data_default.set(mSelected);
        AppData.commit(getContext().getApplicationContext(), AppData.clear_data_default);
    }
}
