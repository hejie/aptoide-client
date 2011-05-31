package cm.aptoide.pt;

import java.io.File;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ApkInfo extends Activity{

	private Intent apkinfo = null;
	private Context mctx = null;
	
	private Intent rtrn_intent = null;
	
	private boolean jback = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.apkinfo);
		
		mctx = this;
		
		rtrn_intent = new Intent();
		
		apkinfo = getIntent();
		
		final String apk_id = apkinfo.getStringExtra("apk_id");
		final int type = apkinfo.getIntExtra("type", 0);
		
		String icon_path = apkinfo.getStringExtra("icon");
		String apk_name_str = apkinfo.getStringExtra("name");
		String apk_descr = apkinfo.getStringExtra("about");
		String apk_repo_str = apkinfo.getStringExtra("server");
		String apk_ver_str = apkinfo.getStringExtra("version");
		String apk_dwon_str = apkinfo.getStringExtra("dwn");
		
		Button serch_mrkt = (Button)findViewById(R.id.btn_market);
		serch_mrkt.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setData(Uri.parse("market://details?id="+apk_id));
				try{
					startActivity(intent);
				}catch (ActivityNotFoundException e){
					Toast.makeText(mctx, getText(R.string.error_no_market), Toast.LENGTH_LONG).show();
				}
			}
		});
		
		Button action = (Button) findViewById(R.id.btn1);
		switch (type) {
		case 0:
			action.setText(getString(R.string.install));
			break;

		case 1:
			action.setText(getString(R.string.rem));
			break;
			
		case 2:
			action.setText(getString(R.string.update));
			break;
		}
		
		action.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				/*new Thread() {
					public void run() {*/
						switch (type) {
						case 0:
							/*String apk_pat = downloadFile(apk_id);
							Message msg_al = new Message();
							if(apk_pat == null){
								msg_al.arg1= 1;
								download_error_handler.sendMessage(msg_al);
							}else if(apk_pat.equals("*md5*")){
								msg_al.arg1 = 0;
								download_error_handler.sendMessage(msg_al);
							}else{
								Message msg = new Message();
								msg.arg1 = 1;
								download_handler.sendMessage(msg);
								installApk(apk_pat);
							}*/
							rtrn_intent.putExtra("apkid", apk_id);
							jback = true;
							break;

						case 1:
							//removeApk(apk_id);
							rtrn_intent.putExtra("apkid", apk_id);
							jback = true;
							break;
							
							
						case 2:
							rtrn_intent.putExtra("apkid", apk_id);
							jback = true;
							/*String apk_path = downloadFile(apk_id);
							Message msg_alt = new Message();
							if(apk_path == null){
								msg_alt.arg1 = 1;
								download_error_handler.sendMessage(msg_alt);
							}else if(apk_path.equals("*md5*")){
								msg_alt.arg1 = 0;
								download_error_handler.sendMessage(msg_alt);
							}else{
								Message msg = new Message();
								msg.arg1 = 1;
								download_handler.sendMessage(msg);
								updateApk(apk_path, apk_id);
							}*/
							break;
						}
						finish();
					//}
				//}.start();
			}
		});
		
		ImageView icon = (ImageView) findViewById(R.id.appicon);
		File test_icon = new File(icon_path);
		if(test_icon.exists() && test_icon.length() > 0){
			icon.setImageDrawable(new BitmapDrawable(icon_path));
		}else{
			icon.setImageResource(android.R.drawable.sym_def_app_icon);
		}
		
		TextView apk_name = (TextView)findViewById(R.id.app_name);
		apk_name.setText(apk_name_str);
		
		TextView apk_about = (TextView)findViewById(R.id.descript);
		apk_about.setText(apk_descr);
		
		TextView apk_repo = (TextView)findViewById(R.id.app_repo);
		apk_repo.setText(apk_repo_str);
		
		TextView apk_version = (TextView)findViewById(R.id.app_ver);
		apk_version.setText("Version: " + apk_ver_str.replaceAll("\\n", ""));
		
		TextView apk_down_n = (TextView)findViewById(R.id.dwn);
		apk_down_n.setText("Downloads: " + apk_dwon_str.replaceAll("\\n", "").replaceAll("\\t", "").trim());
		
	}

	@Override
	public void finish() {
		if(jback)
			this.setResult(RESULT_OK, rtrn_intent);
		super.finish();
	}

	 
	
	

}
