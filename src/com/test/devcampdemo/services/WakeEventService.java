package com.test.devcampdemo.services;

import android.app.Service;
import android.content.Context;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

abstract public class WakeEventService extends Service {

	private static final String LOCK_NAME_STATIC = "com.barefoot.crosstalk.services.WakeEventService";
	private static PowerManager.WakeLock lockStatic;
	
	public static void acquireStaticLock(Context context) {
		getLock(context).acquire();
	}
	
	/** 
	 * Android provides a mechanism to request for wake locks while doing background processing. Please note that this significantly affects the battery life
	 * and unless needed, should not be acquired. Even if we do acquire it, it should be partial wake lock, so that the screen is off, though CPU keeps running.
	 * In code below, what we have done is, obtained Power Manager service from current "context"; and requested for a partial wake lock. However, this is to
	 * hold a reference to the type of lock that we want to acquire. It doesn't actually acquire the lock here. 
	 * 
	 * When a service is invoked, we first acquire the wake lock and then start the service. It is important to acquire a wake lock, before a service instance
	 * is invoked, so as to ensure that between the time invocation is called and service actually invokes, if phone suspends/goes to sleep; then the http 
	 * connections and other activities are suspended. 
	 * 
	 * To ensure, that we allow the service invocation to happen, we acquire lock; invoke service and upon finishing the task in service; call release on lock
	 * in it's onDestroy method. 
	 * 
	 * Note: setReferenceCount ensure that if a lock is requested by 3 processes; then a counter is maintained and only when all three have been released, the phone
	 * is allowed to go back to sleep. Sometimes it may not be necessary; however it's usually safer to maintain a reference count.
	 * 
	 */
	synchronized private static PowerManager.WakeLock getLock(Context context) {
		if (lockStatic == null) {
			PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,LOCK_NAME_STATIC);
			lockStatic.setReferenceCounted(true);
		}

		return(lockStatic);
	}

	//This method is the one that should be implemented by concrete service class.
	public abstract void doServiceTask();
	
	//This is the first method in service lifecycle and is being overriden here. There is no onPause or onResume in services, as they are not forground based
	//and never have that state.
	@Override
	public void onCreate() {
		super.onCreate();
		
		//Run the service code in separate thread
		Thread serviceThread = new Thread() {
			public void run() {
				// We need to prepare looper on current thread as we would need this to ensure, that spawned thread can communicate back to the main thread.
				// Most of the time, this won't be needed, but in current case we need location through location provider, and removing this would result in
				// an error that it cannot declare an handler where looper has been been prepared. 
				Looper.prepare();
				
				doServiceTask();
				
				//No actually start looping on the queue for incoming posts or messages.
				Looper.loop();
			}
		};
		serviceThread.start();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("WakeEventService", "Releasing Lock now!");
		getLock(this).release();
	}
}
