package com.android.monkeyrunner.recorder;

import com.android.SdkConstants;
import com.android.hierarchyviewerlib.HierarchyViewerDirector;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HierachyViewerMonkeyDirector extends HierarchyViewerDirector{
	
	  private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

	  public static HierarchyViewerDirector createDirector() {
	    return HierachyViewerMonkeyDirector.sDirector = new HierachyViewerMonkeyDirector();
	  }

	  public void terminate()
	  {
	    super.terminate();
	    this.mExecutor.shutdown();
	  }

	  public void executeInBackground(final String paramString, final Runnable paramRunnable)
	  {
	    this.mExecutor.execute(new Runnable()
	    {
	      public void run() {
	        paramRunnable.run();
	      }
	    });
	  }

	@Override
	public String getAdbLocation() {
		// TODO Auto-generated method stub
		return null;
	}
}
