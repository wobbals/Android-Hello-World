package com.opentok.helloworld;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.opentok.Publisher;
import com.opentok.Session;
import com.opentok.Stream;
import com.opentok.Subscriber;

public class MainActivity extends Activity implements Publisher.Listener, Session.Listener, Callback {
	ExecutorService executor;
	SurfaceView publisherView;
	SurfaceView subscriberView;
	Camera camera;
	Publisher publisher;
	Subscriber subscriber;
	private Session session;
	private WakeLock wakeLock;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		publisherView = (SurfaceView)findViewById(R.id.publisherview);
		subscriberView = (SurfaceView)findViewById(R.id.subscriberview);
		publisherView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		executor = Executors.newCachedThreadPool();
		publisherView.getHolder().addCallback(this);

		PowerManager powerManager =
				(PowerManager)getSystemService(Context.POWER_SERVICE);
		wakeLock =
				powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
						"Full Wake Lock");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onStop() {
		super.onStop();

		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
		//what else needs to get in here?
		if (null != camera) camera.release();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!wakeLock.isHeld()) {
			wakeLock.acquire();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		if (publisher == null) {
			executor.submit(new Runnable() {

				@Override
				public void run() {
					try {
						camera = Camera.open(Camera.getNumberOfCameras() - 1);
						camera.setPreviewDisplay(publisherView.getHolder());
						camera.startPreview();

						session = Session.newInstance(getApplicationContext(), 
								"2_MX4xMzExMjU3MX43Mi41LjE2Ny4xNTh-VGh1IE9jdCAxOCAxNToxMzoyOCBQRFQgMjAxMn4wLjMzMjY4NDF-",
								"T1==cGFydG5lcl9pZD0xMzExMjU3MSZzaWc9YWEzYTg1NDRkZWRjZTA1ODRmOWNkZjFmOWYwNWExZjQ0NWQ2NDZiYjpyb2xlPW1vZGVyYXRvciZzZXNzaW9uX2lkPTJfTVg0eE16RXhNalUzTVg0M01pNDFMakUyTnk0eE5UaC1WR2gxSUU5amRDQXhPQ0F4TlRveE16b3lPQ0JRUkZRZ01qQXhNbjR3TGpNek1qWTROREYtJmNyZWF0ZV90aW1lPTEzNTQ1MjUyMjUmbm9uY2U9MC42MTE3NzA1MDA0NDU2MDk4",
								"13112571",
								MainActivity.this);
						session.connect();

					} catch (Throwable t) {
						t.printStackTrace();
					}

				}});
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSessionConnected() {
		executor.submit(new Runnable() {

			@Override
			public void run() {
				publisher = session.createPublisher(camera, publisherView.getHolder());
				publisher.connect();
			}});
	}

	@Override
	public void onSessionDidReceiveStream(final Stream stream) {
		executor.submit(new Runnable() {

			@Override
			public void run() {
				if (publisher.getStreamId().equals(stream.getStreamId())) {
					subscriber = session.createSubscriber(subscriberView, stream);
					subscriber.connect();
				}
			}});
	}

	@Override
	public void onPublisherStreamingStarted() {
		Log.i("hello-world", "publisher is streaming!");
	}

	@Override
	public void onPublisherFailed() {
		Log.e("hello-world", "publisher failed!");
	}

	@Override
	public void onSessionDidDropStream(Stream stream) {
		Log.i("hello-world", String.format("stream %d dropped", stream.toString()));
	}

	@Override
	public void onSessionError() {
		Log.e("hello-world", "session failed!");	
	}

	@Override
	public void onSessionDisconnected() {
		Log.i("hello-world", "session disconnected");	
	}

	@Override
	public void onPublisherDisconnected() {
		// TODO Auto-generated method stub
		
	}

}
