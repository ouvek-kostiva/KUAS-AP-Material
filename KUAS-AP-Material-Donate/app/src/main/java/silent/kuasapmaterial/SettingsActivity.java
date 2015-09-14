package silent.kuasapmaterial;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.kuas.ap.donate.R;

import silent.kuasapmaterial.base.SilentActivity;
import silent.kuasapmaterial.libs.Constant;
import silent.kuasapmaterial.libs.Memory;

public class SettingsActivity extends SilentActivity implements View.OnClickListener {

	private View mNotifyCourseView, mNotifyBusView, mHeadPhotoView, mAppVersionView, mFeedbackView;
	private SwitchCompat mNotifyCourseSwitch, mNotifyBusSwitch, mHeadPhotoSwitch;
	private TextView mAppVersionTextView;

	private long lastDebugPressTime = 0l;
	private int easterEggCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		setContentView(R.layout.activity_settings);
		init(R.string.settings, R.layout.activity_settings, R.id.nav_settings);

		initGA("Settings Screen");
		findViews();
		setUpViews();
		restorePreference();
	}

	@Override
	public void finish() {
		super.finish();

		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	private void findViews() {
		mNotifyCourseView = findViewById(R.id.view_course_notify);
		mNotifyBusView = findViewById(R.id.view_bus_notify);
		mFeedbackView = findViewById(R.id.view_feedback);
		mAppVersionView = findViewById(R.id.view_app_version);
		mHeadPhotoView = findViewById(R.id.view_head_photo);

		mNotifyCourseSwitch = (SwitchCompat) findViewById(R.id.switch_course_notify);
		mNotifyBusSwitch = (SwitchCompat) findViewById(R.id.switch_bus_notify);
		mHeadPhotoSwitch = (SwitchCompat) findViewById(R.id.switch_head_photo);

		mAppVersionTextView = (TextView) findViewById(R.id.textView_app_version);
	}

	private void setUpViews() {
		mNotifyCourseView.setOnClickListener(this);
		mNotifyBusView.setOnClickListener(this);
		mHeadPhotoView.setOnClickListener(this);
		mFeedbackView.setOnClickListener(this);
		mAppVersionView.setOnClickListener(this);

		try {
			PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			mAppVersionTextView.setText(pkgInfo.versionName);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			mAppVersionTextView.setText("1.0.0");
		}
	}

	private void restorePreference() {
		mHeadPhotoSwitch.setChecked(Memory.getBoolean(this, Constant.PREF_HEAD_PHOTO, true));
	}

	@Override
	public void onClick(View v) {
		if (v == mNotifyCourseView) {
			mTracker.send(
					new HitBuilders.EventBuilder().setCategory("notify course").setAction("click")
							.build());
			Toast.makeText(this, R.string.function_not_open, Toast.LENGTH_SHORT).show();
		} else if (v == mNotifyBusView) {
			mTracker.send(
					new HitBuilders.EventBuilder().setCategory("notify bus").setAction("click")
							.build());
			Toast.makeText(this, R.string.function_not_open, Toast.LENGTH_SHORT).show();
		} else if (v == mHeadPhotoView) {
			mHeadPhotoSwitch.setChecked(!mHeadPhotoSwitch.isChecked());
			mTracker.send(
					new HitBuilders.EventBuilder().setCategory("head photo").setAction("click")
							.setLabel(mHeadPhotoSwitch.isChecked() + "").build());
			Memory.setBoolean(this, Constant.PREF_HEAD_PHOTO, mHeadPhotoSwitch.isChecked());
			setUpUserPhoto();
		} else if (v == mFeedbackView) {
			mTracker.send(new HitBuilders.EventBuilder().setCategory("feedback").setAction("click")
					.build());
			try {
				Uri uri = Uri.parse("fb://messaging/735951703168873");
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
			} catch (ActivityNotFoundException e) {
				Uri uri = Uri.parse("https://www.facebook.com/messages/735951703168873");
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
			}
		} else if (v == mAppVersionView) {
			mTracker.send(
					new HitBuilders.EventBuilder().setCategory("easter egg").setAction("click")
							.build());
			if (System.currentTimeMillis() - lastDebugPressTime <= 500) {
				easterEggCount++;
				if (easterEggCount == 3) {
					mTracker.send(new HitBuilders.EventBuilder().setCategory("easter egg")
							.setAction("click").setLabel("success").build());
					lastDebugPressTime = 0l;
					easterEggCount = 0;
					Snackbar.make(findViewById(android.R.id.content), R.string.easter_egg_juke,
							Snackbar.LENGTH_SHORT)
							.setActionTextColor(getResources().getColor(R.color.accent)).show();
				}
			} else {
				easterEggCount = 1;
			}
			lastDebugPressTime = System.currentTimeMillis();
		}
	}
}
