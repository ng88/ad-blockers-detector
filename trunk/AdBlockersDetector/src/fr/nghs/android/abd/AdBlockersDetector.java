/*
 * AdBlockersDetector.java
 * 
 * Copyright (c) 2012, Nicolas GUILLAUME. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package fr.nghs.android.abd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

/**
 * This class provides a way to detect ad blockers.
 * @author ng
 *
 */
public class AdBlockersDetector
{

	public static interface Callback
	{
		/**
		 * Called in the GUI thread when result is available.
		 * @param adBlockFound true if an ad blocker is installed.
		 */
		public void onResult(boolean adBlockerFound);
	}


	private Context context;
	
	/**
	 * @param c c can be null, in this case the method using package name is not used.
	 */
	public AdBlockersDetector(Context c)
	{
		context = c;
	}
	
	/**
	 * Asynchronous ad-blockers detection.
	 * Callback is called in GUI thread.
	 * @param callback
	 */
	public void detectAdBlockers(Callback callback)
	{
		new DetectTask(callback).execute();
	}
	
	/**
	 * Synchronous ad-blockers detection
	 * This is blocking and should be called in a separated thread.
	 * In Android activies, prefer the asynchronous version.
	 * @return
	 */
	public boolean detectAdBlockers()
	{
		return detectInHostFile() ||
				detectAppNames() ||
				detectHostName();
	}
	
	private boolean detectInHostFile()
	{
		final File host = new File(HOST_FILE);
		if(host.canRead())
		{
			BufferedReader in = null;
			try
			{
				in = new BufferedReader(new FileReader(host));
				String ln;
				while( (ln = in.readLine()) != null )
				{
					if(ln.contains(HOST_AD_PATTERN))
						return true;
				}
			}
			catch(Exception e)
			{
				return false;
			}	
			finally
			{
				try
				{
					if(in != null)
						in.close();
				}
				catch (IOException e) { }
			}
		}
		return false;
	}

	private boolean detectHostName()
	{
		for(final String h : HOSTS)
			if(isLocalHost(h))
				return true;
		return false;
	}
	
	private boolean isLocalHost(String hostName)
	{
		try
		{
			final InetAddress a = InetAddress.getByName(hostName);
			return a.isAnyLocalAddress() ||
					a.isLinkLocalAddress() ||
					a.isLoopbackAddress();
		}
		catch(Exception ex)
		{
			return false;
		}
	}
	
	private boolean detectAppNames()
	{
		if(context != null)
			for(final String app : BLOCKERS_APP_NAMES)
				if(isAppInstalled(app))
					return true;
		return false;
	}
	
	private boolean isAppInstalled(String packageName)
	{
		try
		{
			final PackageManager pm = context.getPackageManager();
			return pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES) != null;
		}
		catch(Exception e) // PackageManager.NameNotFoundException
		{
			return false;
		}
	}
	
	private static final String HOST_FILE = "/etc/hosts";

	private static final String HOST_AD_PATTERN = "admob";
	
	private static final String[] BLOCKERS_APP_NAMES = 
		{
		 "de.ub0r.android.adBlock",
		 "org.adblockplus.android",
		 "com.bigtincan.android.adfree",
		 "org.adaway"
		};
	
	private static final String[] HOSTS = 
		{
		 "a.admob.com",
		 "mm.admob.com",
		 "p.admob.com",
		 "r.admob.com",
		 "mmv.admob.com"
		};
	
	private class DetectTask extends AsyncTask<Void, Void, Boolean>
	{
		
		private Callback callback;
		
		public DetectTask(Callback c)
		{
			callback = c;
		}

		@Override
		protected Boolean doInBackground(Void... params)
		{
			return detectAdBlockers();
		}
		
		@Override
		protected void onPostExecute(Boolean r)
		{
			if(callback != null && r != null)
				callback.onResult(r);
		}
		
	}
}
