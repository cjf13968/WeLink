package com.fived.welink.welcome;

import java.util.ArrayList;

import com.fived.welink.R;
import com.fived.welink.dataproc.DataStoreManager;
import com.fived.welink.wifidirect.WiFiDirectActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

/**
 * ��������
 */
public class HelpActivity extends Activity {

	// ��ҳ�ؼ�
	private ViewPager mViewPager;

	// ��5���ǵײ���ʾ��ǰ״̬��imageView
	private ImageView mPage0;
	private ImageView mPage1;
	private ImageView mPage2;
	private ImageView mPage3;
	private ImageView mPage4;
	private ImageView mPage5;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// ȥ��������ȫ����ʾ
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.whats_activity_main);
		//

		mViewPager = (ViewPager) findViewById(R.id.whatsnew_viewpager);

		mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		mPage0 = (ImageView) findViewById(R.id.page0);
		mPage1 = (ImageView) findViewById(R.id.page1);
		mPage2 = (ImageView) findViewById(R.id.page2);
		mPage3 = (ImageView) findViewById(R.id.page3);
		mPage4 = (ImageView) findViewById(R.id.page4);
		mPage5 = (ImageView) findViewById(R.id.page5);

		/*
		 * ������ÿһҳҪ��ʾ�Ĳ��֣�����Ӧ����Ҫ���ص����������ʾ������ �Լ���Ҫ��ʾ����ҳ��
		 */
		LayoutInflater mLi = LayoutInflater.from(this);
		View view1 = mLi.inflate(R.layout.whats_news_gallery_one, null);
		View view2 = mLi.inflate(R.layout.whats_news_gallery_two, null);
		View view3 = mLi.inflate(R.layout.whats_news_gallery_three, null);
		View view4 = mLi.inflate(R.layout.whats_news_gallery_four, null);
		View view5 = mLi.inflate(R.layout.whats_news_gallery_five, null);
		View view6 = mLi.inflate(R.layout.whats_news_gallery_six, null);

		System.out.println("dd");
		Button btnHelp = (Button) view6.findViewById(R.id.whats_new_start_btn);
		btnHelp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				boolean isFirstStart = DataStoreManager.getDefaultInstance(
						HelpActivity.this).getStartStauts();
				if (isFirstStart) {
					DataStoreManager.getDefaultInstance(HelpActivity.this)
							.setStartStatus();
					Intent it = new Intent(HelpActivity.this,
							WiFiDirectActivity.class);
					startActivity(it);
					overridePendingTransition(R.anim.fade, R.anim.hold);
				}
				HelpActivity.this.finish();
			}

		});
		/*
		 * ���ｫÿһҳ��ʾ��view��ŵ�ArrayList������ ������ViewPager��������˳�����չʾ
		 */
		final ArrayList<View> views = new ArrayList<View>();
		views.add(view1);
		views.add(view2);
		views.add(view3);
		views.add(view4);
		views.add(view5);
		views.add(view6);

		/*
		 * ÿ��ҳ���Title���ݴ�ŵ�ArrayList������ ������ViewPager�������е���չʾ
		 */
		final ArrayList<String> titles = new ArrayList<String>();
		titles.add("tab1");
		titles.add("tab2");
		titles.add("tab3");
		titles.add("tab4");
		titles.add("tab5");
		titles.add("tab6");

		// ���ViewPager������������
		HelpPagerAdapter mPagerAdapter = new HelpPagerAdapter(views, titles);
		mViewPager.setAdapter(mPagerAdapter);
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {

		public void onPageSelected(int page) {

			// ��ҳʱ��ǰpage,�ı䵱ǰ״̬԰��ͼƬ
			switch (page) {
			case 0:
				mPage0.setImageDrawable(getResources().getDrawable(
						R.drawable.whats_page_now));
				mPage1.setImageDrawable(getResources().getDrawable(
						R.drawable.whats_page));
				break;
			case 1:
				mPage1.setImageDrawable(getResources().getDrawable(
						R.drawable.whats_page_now));
				mPage0.setImageDrawable(getResources().getDrawable(
						R.drawable.whats_page));
				mPage2.setImageDrawable(getResources().getDrawable(
						R.drawable.whats_page));
				break;
			case 2:
				mPage2.setImageDrawable(getResources().getDrawable(
						R.drawable.whats_page_now));
				mPage1.setImageDrawable(getResources().getDrawable(
						R.drawable.whats_page));
				mPage3.setImageDrawable(getResources().getDrawable(
						R.drawable.whats_page));
				break;
			case 3:
				mPage3.setImageDrawable(getResources().getDrawable(
						R.drawable.whats_page_now));
				mPage4.setImageDrawable(getResources().getDrawable(
						R.drawable.whats_page));
				mPage2.setImageDrawable(getResources().getDrawable(
						R.drawable.whats_page));
				break;
			case 4:
				mPage4.setImageDrawable(getResources().getDrawable(
						R.drawable.whats_page_now));
				mPage3.setImageDrawable(getResources().getDrawable(
						R.drawable.whats_page));
				mPage5.setImageDrawable(getResources().getDrawable(
						R.drawable.whats_page));
				break;
			case 5:
				mPage5.setImageDrawable(getResources().getDrawable(
						R.drawable.whats_page_now));
				mPage4.setImageDrawable(getResources().getDrawable(
						R.drawable.whats_page));
				break;
			}
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		public void onPageScrollStateChanged(int arg0) {
		}
	}

}
