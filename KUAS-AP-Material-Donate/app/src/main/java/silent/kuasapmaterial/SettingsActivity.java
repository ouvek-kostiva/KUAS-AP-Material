package silent.kuasapmaterial;

import android.app.Dialog;
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
import silent.kuasapmaterial.callback.GeneralCallback;
import silent.kuasapmaterial.libs.Constant;
import silent.kuasapmaterial.libs.Memory;
import silent.kuasapmaterial.libs.Utils;

public class SettingsActivity extends SilentActivity implements View.OnClickListener {

	private View mNotifyCourseView, mNotifyBusView, mHeadPhotoView, mAppVersionView, mFeedbackView,
			mVibrateCourseView;
	private SwitchCompat mNotifyCourseSwitch, mNotifyBusSwitch, mHeadPhotoSwitch,
			mVibrateCourseSwitch;
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
		mVibrateCourseView = findViewById(R.id.view_course_vibrate);

		mNotifyCourseSwitch = (SwitchCompat) findViewById(R.id.switch_course_notify);
		mVibrateCourseSwitch = (SwitchCompat) findViewById(R.id.switch_course_vibrate);
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
		mVibrateCourseView.setOnClickListener(this);

		try {
			PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			mAppVersionTextView.setText(pkgInfo.versionName + " Donate");
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			mAppVersionTextView.setText("1.0.0 Donate");
		}
	}

	private void restorePreference() {
		mHeadPhotoSwitch.setChecked(Memory.getBoolean(this, Constant.PREF_HEAD_PHOTO, true));
		mNotifyCourseSwitch.setChecked(Memory.getBoolean(this, Constant.PREF_COURSE_NOTIFY, false));
		mNotifyBusSwitch.setChecked(Memory.getBoolean(this, Constant.PREF_BUS_NOTIFY, false));
		mVibrateCourseSwitch
				.setChecked(Memory.getBoolean(this, Constant.PREF_COURSE_VIBRATE, false));
	}

	@Override
	public void onClick(View v) {
		if (v == mNotifyCourseView) {
			setUpCourseNotify();
		} else if (v == mNotifyBusView) {
			setUpBusNotify();
		} else if (v == mVibrateCourseView) {
			setUpCourseVibrate();
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
				Uri uri = Uri.parse("fb://messaging/954175941266264");
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
			} catch (ActivityNotFoundException e) {
				Uri uri = Uri.parse("https://www.facebook.com/messages/954175941266264");
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

	private void setUpBusNotify() {
		mTracker.send(new HitBuilders.EventBuilder().setCategory("notify bus").setAction("create")
				.build());
		mNotifyBusSwitch.setChecked(!mNotifyBusSwitch.isChecked());
		mTracker.send(new HitBuilders.EventBuilder().setCategory("notify bus").setAction("click")
				.setLabel(mNotifyBusSwitch.isChecked() + "").build());
		if (!mNotifyBusSwitch.isChecked()) {
			Memory.setBoolean(SettingsActivity.this, Constant.PREF_BUS_NOTIFY, false);
			return;
		}

		final Dialog progressDialog = Utils.createLoadingDialog(this, R.string.loading);
		progressDialog.show();
		Utils.setUpCourseNotify(this, new GeneralCallback() {
			@Override
			public void onSuccess() {
				super.onSuccess();
				mTracker.send(
						new HitBuilders.EventBuilder().setCategory("notify bus").setAction("status")
								.setLabel("success").build());
				Memory.setBoolean(SettingsActivity.this, Constant.PREF_BUS_NOTIFY, true);
				progressDialog.dismiss();
				Toast.makeText(SettingsActivity.this, R.string.bus_notify_hint, Toast.LENGTH_SHORT)
						.show();
				Toast.makeText(SettingsActivity.this, R.string.beta_function, Toast.LENGTH_SHORT)
						.show();
			}

			@Override
			public void onFail(String errorMessage) {
				super.onFail(errorMessage);
				mTracker.send(
						new HitBuilders.EventBuilder().setCategory("notify bus").setAction("status")
								.setLabel("fail " + errorMessage).build());
				progressDialog.dismiss();
				mNotifyBusSwitch.setChecked(false);
				Memory.setBoolean(SettingsActivity.this, Constant.PREF_BUS_NOTIFY, false);
				Toast.makeText(SettingsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onTokenExpired() {
				super.onTokenExpired();
				mTracker.send(
						new HitBuilders.EventBuilder().setCategory("notify bus").setAction("status")
								.setLabel("token expired").build());
				progressDialog.dismiss();
				Utils.createTokenExpired(SettingsActivity.this).show();
			}
		});
	}

	private void setUpCourseNotify() {
		mTracker.send(
				new HitBuilders.EventBuilder().setCategory("notify course").setAction("create")
						.build());
		mNotifyCourseSwitch.setChecked(!mNotifyCourseSwitch.isChecked());
		mTracker.send(new HitBuilders.EventBuilder().setCategory("notify course").setAction("click")
				.setLabel(mNotifyCourseSwitch.isChecked() + "").build());
		if (!mNotifyCourseSwitch.isChecked()) {
			Memory.setBoolean(SettingsActivity.this, Constant.PREF_COURSE_NOTIFY, false);
			return;
		}

		final Dialog progressDialog = Utils.createLoadingDialog(this, R.string.loading);
		progressDialog.show();
		Utils.setUpCourseNotify(this, new GeneralCallback() {
			@Override
			public void onSuccess() {
				super.onSuccess();
				mTracker.send(new HitBuilders.EventBuilder().setCategory("notify course")
						.setAction("status").setLabel("success").build());
				Memory.setBoolean(SettingsActivity.this, Constant.PREF_COURSE_NOTIFY, true);
				progressDialog.dismiss();
				Toast.makeText(SettingsActivity.this, R.string.course_notify_hint,
						Toast.LENGTH_SHORT).show();
				Toast.makeText(SettingsActivity.this, R.string.beta_function, Toast.LENGTH_SHORT)
						.show();
			}

			@Override
			public void onFail(String errorMessage) {
				super.onFail(errorMessage);
				mTracker.send(new HitBuilders.EventBuilder().setCategory("notify course")
						.setAction("status").setLabel("fail " + errorMessage).build());
				progressDialog.dismiss();
				mNotifyCourseSwitch.setChecked(false);
				Memory.setBoolean(SettingsActivity.this, Constant.PREF_COURSE_NOTIFY, false);
				Toast.makeText(SettingsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onTokenExpired() {
				super.onTokenExpired();
				mTracker.send(new HitBuilders.EventBuilder().setCategory("notify course")
						.setAction("status").setLabel("token expired").build());
				progressDialog.dismiss();
				Utils.createTokenExpired(SettingsActivity.this).show();
			}
		});
	}

	private void setUpCourseVibrate() {
		mTracker.send(
				new HitBuilders.EventBuilder().setCategory("vibrate course").setAction("create")
						.build());
		mVibrateCourseSwitch.setChecked(!mVibrateCourseSwitch.isChecked());
		mTracker.send(
				new HitBuilders.EventBuilder().setCategory("vibrate course").setAction("click")
						.setLabel(mVibrateCourseSwitch.isChecked() + "").build());
		if (!mVibrateCourseSwitch.isChecked()) {
			Memory.setBoolean(SettingsActivity.this, Constant.PREF_COURSE_VIBRATE, false);
			return;
		}

		final Dialog progressDialog = Utils.createLoadingDialog(this, R.string.loading);
		progressDialog.show();
		Utils.setUpCourseNotify(this, new GeneralCallback() {
			@Override
			public void onSuccess() {
				super.onSuccess();
				mTracker.send(new HitBuilders.EventBuilder().setCategory("vibrate course")
						.setAction("status").setLabel("success").build());
				Memory.setBoolean(SettingsActivity.this, Constant.PREF_COURSE_VIBRATE, true);
				progressDialog.dismiss();
				Toast.makeText(SettingsActivity.this, R.string.course_vibrate_hint,
						Toast.LENGTH_SHORT).show();
				Toast.makeText(SettingsActivity.this, R.string.beta_function, Toast.LENGTH_SHORT)
						.show();
			}

			@Override
			public void onFail(String errorMessage) {
				super.onFail(errorMessage);
				mTracker.send(new HitBuilders.EventBuilder().setCategory("vibrate course")
						.setAction("status").setLabel("fail " + errorMessage).build());
				progressDialog.dismiss();
				mVibrateCourseSwitch.setChecked(false);
				Memory.setBoolean(SettingsActivity.this, Constant.PREF_COURSE_VIBRATE, false);
				Toast.makeText(SettingsActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onTokenExpired() {
				super.onTokenExpired();
				mTracker.send(new HitBuilders.EventBuilder().setCategory("vibrate course")
						.setAction("status").setLabel("token expired").build());
				progressDialog.dismiss();
				Utils.createTokenExpired(SettingsActivity.this).show();
			}
		});
	}
}
