package silent.kuasapmaterial.base;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kuas.ap.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import silent.kuasapmaterial.AboutActivity;
import silent.kuasapmaterial.BusActivity;
import silent.kuasapmaterial.CourseActivity;
import silent.kuasapmaterial.LeaveActivity;
import silent.kuasapmaterial.LoginActivity;
import silent.kuasapmaterial.MessagesActivity;
import silent.kuasapmaterial.ScoreActivity;
import silent.kuasapmaterial.UserInfoActivity;
import silent.kuasapmaterial.callback.GeneralCallback;
import silent.kuasapmaterial.callback.UserInfoCallback;
import silent.kuasapmaterial.libs.Constant;
import silent.kuasapmaterial.libs.Helper;
import silent.kuasapmaterial.libs.Memory;
import silent.kuasapmaterial.libs.Utils;
import silent.kuasapmaterial.models.UserInfoModel;

public class SilentActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener {

	public Toolbar toolbar;
	public DrawerLayout drawer;
	public NavigationView navigationView;

	public int mLayoutID;

	public AnimationActionBarDrawerToggle mDrawerToggle;

	public ImageLoader mImageLoader;

	public boolean isDisplayHomeAsUp = false;
	public List<Integer> itemList = new ArrayList<>(
			Arrays.asList(R.id.nav_course, R.id.nav_score, R.id.nav_leave, R.id.nav_bus,
					R.id.nav_simcourse, R.id.nav_messages, R.id.nav_about, R.id.nav_settings));

	public void init(int title, int layout) {
		init(title, layout, -1);
	}

	public void init(int title, int layout, int selectItem) {
		init(getString(title), layout, itemList.indexOf(selectItem));
	}

	public void init(String title, int layout, int selectItem) {
		mLayoutID = layout;
		setUpToolBar(title);
		setUpMenuDrawer(selectItem);
		setDisplayHomeAsUp(false);

		setUpUserPhoto();
		setUpUserInfo();
	}

	public void setUpToolBar(String title) {
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setTitle(title);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true);
		}
	}

	public void setUpUserInfo() {
		if (!Memory.getBoolean(this, Constant.PREF_IS_LOGIN, false) || navigationView == null) {
			return;
		}
		String userName = Memory.getString(this, Constant.PREF_USER_NAME, "");
		String userID = Memory.getString(this, Constant.PREF_USER_ID, "");
		if (userName.length() > 0 && userID.length() > 0) {
			((TextView) navigationView.findViewById(R.id.textView_name)).setText(userName);
			((TextView) navigationView.findViewById(R.id.textView_schoolID)).setText(userID);
		} else {
			Helper.getUserInfo(this, new UserInfoCallback() {

				@Override
				public void onSuccess(UserInfoModel userInfoModel) {
					super.onSuccess(userInfoModel);

					Memory.setString(SilentActivity.this, Constant.PREF_USER_NAME,
							userInfoModel.student_name_cht);
					Memory.setString(SilentActivity.this, Constant.PREF_USER_ID,
							userInfoModel.student_id);
					((TextView) navigationView.findViewById(R.id.textView_name))
							.setText(userInfoModel.student_name_cht);
					((TextView) navigationView.findViewById(R.id.textView_schoolID))
							.setText(userInfoModel.student_id);
				}
			});
		}
	}

	public void setUpUserPhoto() {
		if (!Memory.getBoolean(this, Constant.PREF_IS_LOGIN, false) || navigationView == null) {
			return;
		}
		mImageLoader = Utils.getDefaultImageLoader(this);
		String photo = Memory.getString(this, Constant.PREF_USER_PIC, "");
		if (photo.length() > 0) {
			mImageLoader.displayImage(photo,
					(ImageView) navigationView.findViewById(R.id.imageView_user),
					Utils.getHeadDisplayImageOptions(
							getResources().getDimensionPixelSize(R.dimen.head_mycard) / 2));
		} else {
			Helper.getUserPicture(this, new GeneralCallback() {

				@Override
				public void onSuccess(String data) {
					super.onSuccess(data);
					Memory.setString(SilentActivity.this, Constant.PREF_USER_PIC, data);
					mImageLoader.displayImage(data,
							(ImageView) navigationView.findViewById(R.id.imageView_user),
							Utils.getHeadDisplayImageOptions(
									getResources().getDimensionPixelSize(R.dimen.head_mycard) / 2));
				}
			});
		}
	}

	public void showUserInfo() {
		startActivity(new Intent(this, UserInfoActivity.class));
	}

	public void setUpMenuDrawer(int selectItem) {
		drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		navigationView = (NavigationView) findViewById(R.id.nav_view);
		if (navigationView.findViewById(R.id.layout_user) != null) {
			final boolean isLogin = Memory.getBoolean(this, Constant.PREF_IS_LOGIN, false);
			navigationView.findViewById(R.id.layout_user)
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							drawer.closeDrawers();
							if (mLayoutID == R.layout.activity_messages ||
									mLayoutID == R.layout.activity_about) {
								if (isLogin) {
									showUserInfo();
								} else {
									startActivity(
											new Intent(SilentActivity.this, LoginActivity.class));
								}
							} else {
								showUserInfo();
							}
						}
					});
		}

		drawer.setDrawerShadow(R.drawable.shadow_right, GravityCompat.START);
		drawer.setStatusBarBackgroundColor(getResources().getColor(R.color.main_theme_dark));

		mDrawerToggle = new AnimationActionBarDrawerToggle(this, drawer, R.string.open_drawer,
				R.string.close_drawer) {

			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				if (drawerView == navigationView) {
					super.onDrawerSlide(drawerView, slideOffset);
				}
			}

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View drawerView) {
				if (drawerView == navigationView) {
					super.onDrawerClosed(drawerView);
				}
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				if (drawerView == navigationView) {
					super.onDrawerOpened(drawerView);
				}
			}
		};

		// Set the drawer toggle as the DrawerListener
		drawer.setDrawerListener(mDrawerToggle);
		navigationView.setNavigationItemSelectedListener(this);

		if (-1 < selectItem && selectItem < navigationView.getMenu().size()) {
			navigationView.getMenu().getItem(selectItem).setChecked(true);
		}
	}

	public void setDisplayHomeAsUp(boolean value) {
		if (value == isDisplayHomeAsUp) {
			return;
		} else {
			isDisplayHomeAsUp = value;
		}

		ValueAnimator anim;
		if (value) {
			anim = ValueAnimator.ofFloat(0f, 1f);
		} else {
			anim = ValueAnimator.ofFloat(1f, 0f);
		}
		anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				float slideOffset = (float) valueAnimator.getAnimatedValue();
				setDrawerIconState(slideOffset);
			}
		});
		anim.setInterpolator(new AccelerateDecelerateInterpolator());
		anim.setDuration(300);
		anim.start();
	}

	public void setDrawerIconState(float slideOffset) {
		mDrawerToggle.onAnimationDrawerSlide(navigationView, slideOffset);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (drawer.isDrawerOpen(navigationView)) {
					drawer.closeDrawer(navigationView);
					return true;
				} else if (!drawer.isDrawerOpen(navigationView)) {
					drawer.openDrawer(navigationView);
					return true;
				}
				break;
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkNetwork();
	}

	public void checkNetwork() {
		if (!Utils.isNetworkConnected(this)) {
			Snackbar.make(findViewById(android.R.id.content), R.string.no_internet,
					Snackbar.LENGTH_INDEFINITE)
					.setAction(R.string.setting_internet, new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
						}
					}).setActionTextColor(getResources().getColor(R.color.accent)).show();
		}
	}

	@Override
	public boolean onNavigationItemSelected(MenuItem menuItem) {
		drawer.closeDrawers();
		if (menuItem.isChecked()) {
			return true;
		}
		boolean isLogin = Memory.getBoolean(this, Constant.PREF_IS_LOGIN, false);
		if (isLogin) {
			if (menuItem.getItemId() == R.id.nav_messages) {
				startActivity(new Intent(this, MessagesActivity.class));
			} else if (menuItem.getItemId() == R.id.nav_bus) {
				startActivity(new Intent(this, BusActivity.class));
			} else if (menuItem.getItemId() == R.id.nav_course) {
				startActivity(new Intent(this, CourseActivity.class));
			} else if (menuItem.getItemId() == R.id.nav_about) {
				startActivity(new Intent(this, AboutActivity.class));
			} else if (menuItem.getItemId() == R.id.nav_score) {
				startActivity(new Intent(this, ScoreActivity.class));
			} else if (menuItem.getItemId() == R.id.nav_leave) {
				startActivity(new Intent(this, LeaveActivity.class));
			}
			if (mLayoutID != R.layout.activity_logout && mLayoutID != R.layout.activity_login) {
				finish();
			}
		} else {
			if (menuItem.getItemId() == R.id.nav_messages) {
				startActivity(new Intent(this, MessagesActivity.class));
				if (mLayoutID != R.layout.activity_login) {
					finish();
				}
			} else if (menuItem.getItemId() == R.id.nav_about) {
				startActivity(new Intent(this, AboutActivity.class));
				if (mLayoutID != R.layout.activity_login) {
					finish();
				}
			} else {
				Toast.makeText(this, R.string.login_first, Toast.LENGTH_SHORT).show();
			}
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		if (drawer != null && navigationView != null && drawer.isDrawerOpen(navigationView)) {
			drawer.closeDrawers();
		} else {
			if (mLayoutID == R.layout.activity_logout) {
				new AlertDialog.Builder(this).setTitle(R.string.app_name)
						.setMessage(R.string.logout_check).setPositiveButton(R.string.determine,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								clearUserData();
								finish();
							}
						}).setNegativeButton(R.string.cancel, null).show();
			} else {
				super.onBackPressed();
			}
		}
	}

	public void clearUserData() {
		Memory.setBoolean(this, Constant.PREF_IS_LOGIN, false);
		Memory.setString(this, Constant.PREF_USER_PIC, "");
		Memory.setString(this, Constant.PREF_USER_ID, "");
		Memory.setString(this, Constant.PREF_USER_NAME, "");
	}

	public class AnimationActionBarDrawerToggle extends ActionBarDrawerToggle {

		public AnimationActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout,
		                                      int openDrawerContentDescRes,
		                                      int closeDrawerContentDescRes) {
			super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
		}

		public void onAnimationDrawerSlide(View drawerView, float slideOffset) {
			super.onDrawerSlide(drawerView, slideOffset);
		}
	}
}