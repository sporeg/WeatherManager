package com.sporeg.weathermanager;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.SearchView;
import android.widget.TextView;

public class MainActivity extends SwipeBackActivity {
	private static final int VIBRATE_DURATION = 20;

	private int[] mBgColors;

	private static int mBgIndex = 0;

	private SwipeBackLayout mSwipeBackLayout;
	String cityid = "101200101";
	String city_s;
	String time_s;
	String temp_s;
	String wind_s;
	String weather_s;
	String suggest_s;
	TextView city, time, temp, wind, weather, suggest;
	HttpGet httpRequest;
	String[] cityname;
	String[] citycode;
	SearchManager searchManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weather);
		changeActionBarColor();
		findViews();
		mSwipeBackLayout = getSwipeBackLayout();
		mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_ALL);

		mSwipeBackLayout.setSwipeListener(new SwipeBackLayout.SwipeListener() {
			@Override
			public void onScrollStateChange(int state, float scrollPercent) {

			}

			@Override
			public void onEdgeTouch(int edgeFlag) {
				vibrate(VIBRATE_DURATION);
			}

			@Override
			public void onScrollOverThreshold() {
				vibrate(VIBRATE_DURATION);
			}
		});
		restore();
		cityname = CityCode.getcityname();
		citycode = CityCode.getcitycode();

	}

	Handler handler = new Handler();

	Runnable mUpdateResults = new Runnable() {
		public void run() {
			city.postInvalidate();
			time.postInvalidate();
			temp.postInvalidate();
			wind.postInvalidate();
			weather.postInvalidate();
			suggest.postInvalidate();
			PreferenceUtils.setPrefString(getApplicationContext(), "cityid",
					cityid);
			if (city_s != null) {
				city.setText(city_s);
				PreferenceUtils.setPrefString(getApplicationContext(), "city",
						city_s);
			}
			if (time_s != null) {
				time.setText(time_s);
				PreferenceUtils.setPrefString(getApplicationContext(), "time",
						time_s);
			}
			if (temp_s != null) {
				temp.setText(temp_s);
				PreferenceUtils.setPrefString(getApplicationContext(), "temp",
						temp_s);
			}
			if (wind_s != null) {
				wind.setText(wind_s);
				PreferenceUtils.setPrefString(getApplicationContext(), "wind",
						wind_s);
			}
			if (weather_s != null) {
				weather.setText(weather_s);
				PreferenceUtils.setPrefString(getApplicationContext(),
						"weather", weather_s);
			}
			if (suggest_s != null) {
				suggest.setText(suggest_s);
				PreferenceUtils.setPrefString(getApplicationContext(),
						"suggest", suggest_s);
			}
		}
	};

	protected void NetworkOperation() {

		Thread t = new Thread() {
			public void run() {
				httpRequest = new HttpGet("http://m.weather.com.cn/data/"
						+ cityid + ".html");
				// httpRequest = new
				// HttpGet("http://www.weather.com.cn/data/sk/"
				// + cityid + ".html");

				String strResult = "";
				try {
					// HttpClient对象
					HttpClient httpClient = new DefaultHttpClient();
					// 获得HttpResponse对象
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						// 取得返回的数据
						strResult = EntityUtils.toString(httpResponse
								.getEntity());
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Log.i("Infor", strResult);
				parseJson(strResult);

				// 处理得到结果了，这里一些内容保存在主类的成员变量中
				handler.post(mUpdateResults); // 高速UI线程可以更新结果了
			}
		};
		t.start();
	}

	// 对于返回的结果我们通过Json解析工具进行解析。下面是解析函数的代码，其参数就是要解析的Json格式数据字符串。
	private void parseJson(String strResult) {
		try {
			JSONObject jsonObj = new JSONObject(strResult)
					.getJSONObject("weatherinfo");
			city_s = jsonObj.getString("city");
			time_s = jsonObj.getString("date_y"); // 当前日期
			temp_s = jsonObj.getString("temp1");
			wind_s = jsonObj.getString("wind1");
			weather_s = jsonObj.getString("weather1");
			suggest_s = jsonObj.getString("index_d");
			// String dayofweek = jsonObj.getString("week"); // 当前星期
			// String city = jsonObj.getString("city"); // 城市名称
			// int ftime = jsonObj.getInt("fchh"); // 更新时间（整点）【更新时间确定temp1属于哪天】
			// 由于数据较多此处省略了部分代码，其他数据解析方法相同，大家可以照葫芦画瓢。
		} catch (JSONException e) {
			Log.i("Erorr", "Json parse error");
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		NetworkOperation();
	}

	private void changeActionBarColor() {
		getActionBar().setBackgroundDrawable(
				new ColorDrawable(getColors()[mBgIndex]));
		mBgIndex++;
		if (mBgIndex >= getColors().length) {
			mBgIndex = 0;
		}
	}

	private void findViews() {
		city = (TextView) findViewById(R.id.cityname);
		time = (TextView) findViewById(R.id.date);
		temp = (TextView) findViewById(R.id.temperature);
		wind = (TextView) findViewById(R.id.winddirection);
		weather = (TextView) findViewById(R.id.weathernow);
		suggest = (TextView) findViewById(R.id.suggest);
	}

	private int[] getColors() {
		if (mBgColors == null) {
			Resources resource = getResources();
			mBgColors = new int[] { resource.getColor(R.color.androidColorA),
					resource.getColor(R.color.androidColorB),
					resource.getColor(R.color.androidColorC),
					resource.getColor(R.color.androidColorD),
					resource.getColor(R.color.androidColorE), };
		}
		return mBgColors;
	}

	private void vibrate(long duration) {
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		long[] pattern = { 0, duration };
		vibrator.vibrate(pattern, -1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
				.getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		// searchView.setIconifiedByDefault(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			scrollToFinishActivity();
		case R.id.action_search:

			onSearchRequested();
			return true;
		case R.id.action_share:
			Intent intent = new Intent(Intent.ACTION_SEND);

			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
			intent.putExtra(Intent.EXTRA_TEXT, city_s + "今日气温" + temp_s + " "
					+ weather_s);
			startActivity(Intent.createChooser(intent, "share"));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// Because this activity has set launchMode="singleTop", the system
		// calls this method
		// to deliver the intent if this activity is currently the foreground
		// activity when
		// invoked again (when the user executes a search from this activity, we
		// don't create
		// a new instance of this activity, so the system delivers the search
		// intent here)

		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			// handles a search query
			String query = intent.getStringExtra(SearchManager.QUERY);
			initsearch(query);
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {

			initsearch(intent.getData().toString());

		}
	}

	public void initsearch(String selection) {
		for (int i = 0; i < cityname.length; i++) {
			if (selection.equals(cityname[i])) {
				cityid = citycode[i];
				NetworkOperation();
				break;
			}
		}

	}

	// mUri = getContentResolver().insert(intent.getData(), null);

	private void restore() {
		cityid = PreferenceUtils.getPrefString(getApplicationContext(),
				"cityid", cityid);
		city.setText(PreferenceUtils.getPrefString(getApplicationContext(),
				"city", city_s));
		time.setText(PreferenceUtils.getPrefString(getApplicationContext(),
				"time", time_s));
		temp.setText(PreferenceUtils.getPrefString(getApplicationContext(),
				"temp", temp_s));
		wind.setText(PreferenceUtils.getPrefString(getApplicationContext(),
				"wind", wind_s));
		weather.setText(PreferenceUtils.getPrefString(getApplicationContext(),
				"weather", weather_s));
		suggest.setText(PreferenceUtils.getPrefString(getApplicationContext(),
				"suggest", suggest_s));
	}

	@Override
	public void onBackPressed() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle(R.string.exit)
				.setMessage("你真的要退出了么")
				.setPositiveButton(R.string.exit,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								scrollToFinishActivity();
								android.os.Process
										.killProcess(android.os.Process.myPid());

							}
						}).setNegativeButton(R.string.menu_cancel, null)
				.create().show();
	}

}